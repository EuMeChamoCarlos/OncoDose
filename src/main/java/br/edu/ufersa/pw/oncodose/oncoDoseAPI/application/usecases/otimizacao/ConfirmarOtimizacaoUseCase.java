package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.otimizacao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.Aplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.EstadoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.FrascoIndisponivelException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.FrascosRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.AlocacaoFrascoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.Otimizacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.OtimizacaoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.StatusOtimizacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: confirmar que o preparo do plano foi feito e baixar o estoque.
 * Idempotente: confirmar de novo a mesma otimização devolve o mesmo estado, sem baixar outra vez
 * (cliente que perdeu a resposta pode simplesmente repetir).
 */
@Service
public class ConfirmarOtimizacaoUseCase {

    private final OtimizacaoRepository otimizacaoRepository;
    private final FrascosRepository frascosRepository;
    private final PrescricaoRepository prescricaoRepository;
    private final AlocacaoFrascoRepository alocacaoFrascoRepository;

    public ConfirmarOtimizacaoUseCase(
            OtimizacaoRepository otimizacaoRepository,
            FrascosRepository frascosRepository,
            PrescricaoRepository prescricaoRepository,
            AlocacaoFrascoRepository alocacaoFrascoRepository) {
        this.otimizacaoRepository = otimizacaoRepository;
        this.frascosRepository = frascosRepository;
        this.prescricaoRepository = prescricaoRepository;
        this.alocacaoFrascoRepository = alocacaoFrascoRepository;
    }

    @Transactional
    public Otimizacao executar(UUID otimizacaoId) {
        // SELECT ... FOR UPDATE: duplo clique espera a primeira terminar e cai no caminho idempotente.
        Otimizacao otimizacao = otimizacaoRepository.findWithLockById(otimizacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Otimização", otimizacaoId));
        if (otimizacao.isConfirmada()) {
            return otimizacao;
        }
        if (otimizacao.isRecusada()) {
            throw new EstadoInvalidoException("Otimização", otimizacaoId, "plano recusado; otimize de novo");
        }
        if (otimizacaoRepository.existsByMedicamentoIdAndDataReferenciaAndStatus(
                otimizacao.getMedicamento().getId(), otimizacao.getDataReferencia(), StatusOtimizacao.CONFIRMADA)) {
            throw new RecursoDuplicadoException("Confirmação", "medicamento e data",
                    otimizacao.getDataReferencia().toString());
        }

        // Baixa o frasco inteiro aberto (a sobra é descarte): quantidade por apresentação.
        Map<UUID, Long> abertosPorFrasco = otimizacao.getAlocacoes().stream()
                .map(a -> Map.entry(a.getApresentacao().getId(), a.getNumeroFrasco()))
                .distinct()
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.counting()));
        abertosPorFrasco.forEach((frascoId, quantidade) -> {
            if (frascosRepository.baixarEstoque(frascoId, quantidade.intValue()) == 0) {
                // Exceção desfaz a transação inteira: nenhum frasco fica baixado pela metade.
                throw new FrascoIndisponivelException(frascoId,
                        "estoque atual menor que os " + quantidade + " frasco(s) do plano; otimize de novo");
            }
        });

        otimizacao.confirmar();

        // Aplicação realizada só quando todos os quimioterápicos do dia tiverem o preparo confirmado
        // (no AC, doxorrubicina e ciclofosfamida). Aí ela sai da agenda remarcável e vira histórico.
        otimizacao.getAlocacoes().stream()
                .map(a -> a.getPrescricao().getAplicacao())
                .filter(Objects::nonNull)
                .distinct()
                .filter(this::todosMedicamentosConfirmados)
                .forEach(Aplicacao::marcarRealizada);
        return otimizacao;
    }

    private boolean todosMedicamentosConfirmados(Aplicacao aplicacao) {
        List<Prescricao> linhas = prescricaoRepository.findByAplicacaoId(aplicacao.getId());
        return aplicacao.medicamentosDoDia().stream().allMatch(item -> linhas.stream()
                .filter(p -> Objects.equals(p.getMedicamento().getId(), item.getMedicamento().getId()))
                .anyMatch(p -> alocacaoFrascoRepository.existsByPrescricaoIdAndOtimizacaoStatus(
                        p.getId(), StatusOtimizacao.CONFIRMADA)));
    }
}
