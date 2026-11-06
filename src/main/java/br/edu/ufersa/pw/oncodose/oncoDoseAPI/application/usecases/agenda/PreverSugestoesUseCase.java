package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.agenda;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Aplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Ciclo;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.CicloRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.StatusAplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.PeriodoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.FrascosRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.OtimizacaoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.StatusOtimizacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.StatusPrescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.PlanejadorAgenda;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.PlanejadorAgenda.AplicacaoPlanejada;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.PlanejadorAgenda.Sugestao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.SolverFracionamento.TipoFrasco;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: prévia da agenda (só leitura). Monta o cenário do banco e roda o planejador sem gravar
 * nada; a fase 4 grava as sugestões e permite aceitar/recusar.
 *
 * <p>Dose de cada aplicação: a da prescrição APTO, se houver; NAO_APTO não entra no custo e não se
 * move; sem prescrição, a do ciclo. Prescrições do legado (sem aplicação) entram como demanda fixa.
 */
@Service
public class PreverSugestoesUseCase {

    /** Limita o tempo do solver: cada dia candidato é uma instância do modelo. */
    public static final int MAX_DIAS = 62;

    public record Previa(UUID medicamentoId, LocalDate inicio, LocalDate fim, List<Sugestao> sugestoes, long tempoMs) {}

    private final MedicamentoRepository medicamentoRepository;
    private final CicloRepository cicloRepository;
    private final PrescricaoRepository prescricaoRepository;
    private final FrascosRepository frascosRepository;
    private final OtimizacaoRepository otimizacaoRepository;

    public PreverSugestoesUseCase(
            MedicamentoRepository medicamentoRepository,
            CicloRepository cicloRepository,
            PrescricaoRepository prescricaoRepository,
            FrascosRepository frascosRepository,
            OtimizacaoRepository otimizacaoRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.cicloRepository = cicloRepository;
        this.prescricaoRepository = prescricaoRepository;
        this.frascosRepository = frascosRepository;
        this.otimizacaoRepository = otimizacaoRepository;
    }

    @Transactional(readOnly = true)
    public Previa executar(UUID medicamentoId, LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio) || ChronoUnit.DAYS.between(inicio, fim) >= MAX_DIAS) {
            throw new PeriodoInvalidoException(inicio, fim, "fim deve ser >= início e o período ter até " + MAX_DIAS + " dias");
        }
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new RecursoNaoEncontradoException("Medicamento", medicamentoId);
        }

        List<Ciclo> ciclos = cicloRepository.buscarComAplicacaoNoPeriodo(medicamentoId, inicio, fim);
        List<UUID> aplicacaoIds = ciclos.stream().flatMap(c -> c.getAplicacoes().stream()).map(Aplicacao::getId).toList();
        Map<UUID, Prescricao> prescricaoPorAplicacao = aplicacaoIds.isEmpty() ? Map.of()
                : prescricaoRepository.findByAplicacaoIdIn(aplicacaoIds).stream()
                        .collect(Collectors.toMap(p -> p.getAplicacao().getId(), Function.identity()));

        List<AplicacaoPlanejada> cenario = new ArrayList<>();
        for (Ciclo ciclo : ciclos) {
            for (Aplicacao a : ciclo.getAplicacoes()) {
                if (a.getStatus() == StatusAplicacao.CANCELADA) {
                    continue;
                }
                Prescricao p = prescricaoPorAplicacao.get(a.getId());
                boolean naoApto = p != null && p.getStatus() == StatusPrescricao.NAO_APTO;
                Double dose = naoApto ? null : p != null ? p.getDoseMg() : ciclo.getDoseMg();
                cenario.add(new AplicacaoPlanejada(a.getId(), ciclo.getId(), ciclo.getCodigoCiclo(), a.getNumero(), dose,
                        a.getDataAgendada(), a.inicioJanela(), a.fimJanela(), ciclo.getIntervaloMinimoDias(),
                        a.getStatus() == StatusAplicacao.AGENDADA && !naoApto));
            }
        }

        Map<LocalDate, List<Double>> legado = prescricaoRepository
                .buscarLegadoNoPeriodo(medicamentoId, inicio, fim, StatusPrescricao.APTO).stream()
                .collect(Collectors.groupingBy(Prescricao::getDataPrescricao,
                        Collectors.mapping(Prescricao::getDoseMg, Collectors.toList())));
        List<TipoFrasco> tipos = frascosRepository.findByMedicamentoId(medicamentoId).stream()
                .map(f -> new TipoFrasco(f.getVolumeMg(), f.getCusto(), f.getQuantidadeEstoque()))
                .toList();
        Set<LocalDate> fechados = new HashSet<>(
                otimizacaoRepository.buscarDiasNoPeriodo(medicamentoId, inicio, fim, StatusOtimizacao.CONFIRMADA));

        long t0 = System.nanoTime();
        List<Sugestao> sugestoes = PlanejadorAgenda.planejar(cenario, legado, tipos, inicio, fim, fechados);
        long tempoMs = (System.nanoTime() - t0) / 1_000_000;
        return new Previa(medicamentoId, inicio, fim, sugestoes, tempoMs);
    }
}
