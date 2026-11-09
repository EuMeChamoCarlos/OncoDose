package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.agenda;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.PeriodoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.FrascosRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.MedicamentoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.OtimizacaoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.StatusOtimizacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.PrescricaoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.StatusPrescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.Aplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloMedicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloPaciente;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloPacienteRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.StatusAplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.PlanejadorAgenda;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.PlanejadorAgenda.AplicacaoPlanejada;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.PlanejadorAgenda.Sugestao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.SolverFracionamento.TipoFrasco;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: prévia da agenda (só leitura). Monta o cenário do banco e roda o planejador sem gravar
 * nada; gravar, aceitar e recusar as sugestões vem depois.
 *
 * <p>O medicamento escolhe quais aplicações podem se mover; a aplicação se move inteira, e o ganho conta
 * todos os quimioterápicos intravenosos dela. Para isso entram também os outros pacientes desses
 * medicamentos no período. Dose de cada medicamento: a da linha da prescrição, se houver; senão, a do
 * protocolo. Prescrição NAO_APTO tira a aplicação do custo e da remarcação. Legado (sem aplicação) é
 * demanda fixa. Nada se move de ou para um dia anterior a hoje.
 */
@Service
public class PreverSugestoesUseCase {

    /** Limita o tempo do solver: cada dia candidato é uma instância do modelo. */
    public static final int MAX_DIAS = 62;

    public record Previa(UUID medicamentoId, LocalDate inicio, LocalDate fim, List<Sugestao> sugestoes, long tempoMs) {}

    private final MedicamentoRepository medicamentoRepository;
    private final ProtocoloPacienteRepository protocoloRepository;
    private final PrescricaoRepository prescricaoRepository;
    private final FrascosRepository frascosRepository;
    private final OtimizacaoRepository otimizacaoRepository;

    public PreverSugestoesUseCase(
            MedicamentoRepository medicamentoRepository,
            ProtocoloPacienteRepository protocoloRepository,
            PrescricaoRepository prescricaoRepository,
            FrascosRepository frascosRepository,
            OtimizacaoRepository otimizacaoRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.protocoloRepository = protocoloRepository;
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

        // Medicamentos envolvidos: o pedido e os outros intravenosos dos mesmos protocolos (no AC, os dois).
        List<ProtocoloPaciente> principais = protocoloRepository.buscarComAplicacaoNoPeriodo(medicamentoId, inicio, fim);
        Set<UUID> medicamentos = new LinkedHashSet<>();
        medicamentos.add(medicamentoId);
        principais.forEach(p -> p.getMedicamentos().stream().filter(PreverSugestoesUseCase::intravenoso)
                .forEach(m -> medicamentos.add(m.getMedicamento().getId())));

        Map<UUID, ProtocoloPaciente> protocolos = new LinkedHashMap<>();
        for (UUID med : medicamentos) {
            protocoloRepository.buscarComAplicacaoNoPeriodo(med, inicio, fim).forEach(p -> protocolos.putIfAbsent(p.getId(), p));
        }

        List<UUID> aplicacaoIds = protocolos.values().stream()
                .flatMap(p -> p.getAplicacoes().stream()).map(Aplicacao::getId).toList();
        Map<UUID, List<Prescricao>> linhasPorAplicacao = aplicacaoIds.isEmpty() ? Map.of()
                : prescricaoRepository.findByAplicacaoIdIn(aplicacaoIds).stream()
                        .collect(Collectors.groupingBy(p -> p.getAplicacao().getId()));

        List<AplicacaoPlanejada> cenario = new ArrayList<>();
        for (ProtocoloPaciente protocolo : protocolos.values()) {
            for (Aplicacao a : protocolo.getAplicacoes()) {
                if (a.getStatus() == StatusAplicacao.CANCELADA) {
                    continue;
                }
                List<Prescricao> linhas = linhasPorAplicacao.getOrDefault(a.getId(), List.of());
                boolean naoApto = linhas.stream().anyMatch(p -> p.getStatus() == StatusPrescricao.NAO_APTO);
                Map<UUID, Double> doses = naoApto ? Map.of() : doses(a, linhas, medicamentos);
                cenario.add(new AplicacaoPlanejada(a.getId(), protocolo.getId(), protocolo.getCodigoProtocolo(), a.getNumero(),
                        doses, a.getDataAgendada(), a.inicioJanela(), a.fimJanela(), protocolo.getIntervaloMinimoDias(),
                        a.getStatus() == StatusAplicacao.AGENDADA && doses.containsKey(medicamentoId)));
            }
        }

        Map<UUID, Map<LocalDate, List<Double>>> legado = new HashMap<>();
        Map<UUID, List<TipoFrasco>> tipos = new HashMap<>();
        Map<UUID, Set<LocalDate>> fechados = new HashMap<>();
        for (UUID med : medicamentos) {
            legado.put(med, prescricaoRepository.buscarLegadoNoPeriodo(med, inicio, fim, StatusPrescricao.APTO).stream()
                    .collect(Collectors.groupingBy(Prescricao::getDataPrescricao,
                            Collectors.mapping(Prescricao::getDoseMg, Collectors.toList()))));
            tipos.put(med, frascosRepository.findByMedicamentoId(med).stream()
                    .map(f -> new TipoFrasco(f.getVolumeMg(), f.getCusto(), f.getQuantidadeEstoque()))
                    .toList());
            fechados.put(med, new HashSet<>(
                    otimizacaoRepository.buscarDiasNoPeriodo(med, inicio, fim, StatusOtimizacao.CONFIRMADA)));
        }

        long t0 = System.nanoTime();
        List<Sugestao> sugestoes = PlanejadorAgenda.planejar(cenario, legado, tipos, inicio, fim, LocalDate.now(), fechados);
        long tempoMs = (System.nanoTime() - t0) / 1_000_000;
        return new Previa(medicamentoId, inicio, fim, sugestoes, tempoMs);
    }

    /** mg de cada quimioterápico intravenoso do dia: a da linha da prescrição, se houver; senão, a do protocolo. */
    private static Map<UUID, Double> doses(Aplicacao a, List<Prescricao> linhas, Set<UUID> medicamentos) {
        Map<UUID, Double> doses = new HashMap<>();
        for (ProtocoloMedicamento item : a.medicamentosDoDia()) {
            UUID med = item.getMedicamento().getId();
            if (!intravenoso(item) || !medicamentos.contains(med)) {
                continue;
            }
            double dose = linhas.stream().filter(p -> p.getMedicamento().getId().equals(med))
                    .map(Prescricao::getDoseMg).findFirst().orElse(item.getDoseMg());
            doses.put(med, dose);
        }
        return doses;
    }

    /** O modelo de Gê trata só medicamentos intravenosos (no Tasy, via "IV - Intravenosa"). */
    private static boolean intravenoso(ProtocoloMedicamento item) {
        return item.getVia().trim().toUpperCase().startsWith("IV");
    }
}
