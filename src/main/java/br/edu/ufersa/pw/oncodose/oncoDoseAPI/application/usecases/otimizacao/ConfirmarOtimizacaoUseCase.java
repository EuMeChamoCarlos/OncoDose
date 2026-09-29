package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.otimizacao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.FrascoIndisponivelException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.FrascosRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.Otimizacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.OtimizacaoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.StatusOtimizacao;
import java.util.Map;
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

    public ConfirmarOtimizacaoUseCase(
            OtimizacaoRepository otimizacaoRepository,
            FrascosRepository frascosRepository) {
        this.otimizacaoRepository = otimizacaoRepository;
        this.frascosRepository = frascosRepository;
    }

    @Transactional
    public Otimizacao executar(UUID otimizacaoId) {
        // SELECT ... FOR UPDATE: duplo clique espera a primeira terminar e cai no caminho idempotente.
        Otimizacao otimizacao = otimizacaoRepository.findWithLockById(otimizacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Otimização", otimizacaoId));
        if (otimizacao.isConfirmada()) {
            return otimizacao;
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
        return otimizacao;
    }
}
