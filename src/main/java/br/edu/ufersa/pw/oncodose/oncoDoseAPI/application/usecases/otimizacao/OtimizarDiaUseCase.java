package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.otimizacao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.EstoqueInsuficienteException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.FrascosRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.AlocacaoFrasco;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.Otimizacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.OtimizacaoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.StatusPrescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.SolverFracionamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.SolverFracionamento.Alocacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.SolverFracionamento.Solucao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.SolverFracionamento.TipoFrasco;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: otimizar o fracionamento de um medicamento num dia (instância do artigo).
 * Pacientes = prescrições APTO do dia; tipos = apresentações do medicamento.
 */
@Service
public class OtimizarDiaUseCase {

    private final MedicamentoRepository medicamentoRepository;
    private final PrescricaoRepository prescricaoRepository;
    private final FrascosRepository frascosRepository;
    private final OtimizacaoRepository otimizacaoRepository;

    public OtimizarDiaUseCase(
            MedicamentoRepository medicamentoRepository,
            PrescricaoRepository prescricaoRepository,
            FrascosRepository frascosRepository,
            OtimizacaoRepository otimizacaoRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.prescricaoRepository = prescricaoRepository;
        this.frascosRepository = frascosRepository;
        this.otimizacaoRepository = otimizacaoRepository;
    }

    // ponytail: agrupa por dia inteiro (como o artigo); janela de estabilidade (~2h) ou lote escolhido entram aqui
    @Transactional
    public Otimizacao executar(UUID medicamentoId, LocalDate data) {
        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", medicamentoId));

        List<Prescricao> prescricoes = prescricaoRepository
                .findByMedicamentoIdAndDataPrescricaoAndStatus(medicamentoId, data, StatusPrescricao.APTO);
        if (prescricoes.isEmpty()) {
            throw new RecursoNaoEncontradoException("Prescrição APTO", "medicamento=" + medicamentoId + " data=" + data);
        }
        List<Frascos> frascos = frascosRepository.findByMedicamentoId(medicamentoId);

        List<Double> doses = prescricoes.stream().map(Prescricao::getDoseMg).toList();
        List<TipoFrasco> tipos = frascos.stream()
                .map(f -> new TipoFrasco(f.getVolumeMg(), f.getCusto(), f.getQuantidadeEstoque()))
                .toList();

        // Pré-condição da patente BR102024012896 [0029]: estoque total cobre a demanda do dia.
        double demanda = doses.stream().mapToDouble(Double::doubleValue).sum();
        double capacidade = tipos.stream().mapToDouble(t -> t.volumeMg() * t.estoque()).sum();
        if (capacidade + 1e-6 < demanda) {
            throw new EstoqueInsuficienteException(medicamentoId, demanda, capacidade);
        }

        Solucao solucao = SolverFracionamento.resolver(doses, tipos);
        BigDecimal custoOtimizado = dinheiro(solucao.custoTotal());
        BigDecimal custoSemCompartilhar = dinheiro(SolverFracionamento.custoSemCompartilhamento(doses, tipos));

        Otimizacao otimizacao = new Otimizacao();
        otimizacao.setMedicamento(medicamento);
        otimizacao.setDataReferencia(data);
        otimizacao.setCustoTotal(custoOtimizado);
        otimizacao.setDesperdicioMg(solucao.desperdicioMg());
        otimizacao.setEconomiaVsEmpirico(custoSemCompartilhar.subtract(custoOtimizado));
        for (Alocacao a : solucao.alocacoes()) {
            AlocacaoFrasco alocacao = new AlocacaoFrasco();
            alocacao.setOtimizacao(otimizacao);
            alocacao.setPrescricao(prescricoes.get(a.paciente()));
            alocacao.setApresentacao(frascos.get(a.tipo()));
            alocacao.setNumeroFrasco(a.numero() + 1);
            alocacao.setFracao(a.fracao());
            otimizacao.getAlocacoes().add(alocacao);
        }
        return otimizacaoRepository.save(otimizacao);
    }

    private static BigDecimal dinheiro(double valor) {
        return BigDecimal.valueOf(valor).setScale(4, RoundingMode.HALF_UP);
    }
}
