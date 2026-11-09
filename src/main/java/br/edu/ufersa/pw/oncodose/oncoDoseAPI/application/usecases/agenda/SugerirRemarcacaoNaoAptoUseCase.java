package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.agenda;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.agenda.PreverSugestoesUseCase.Cenario;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.agenda.SugestaoRemarcacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.agenda.SugestaoRemarcacaoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.EstadoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.PeriodoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.Aplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.AplicacaoRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloMedicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.StatusAplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.PlanejadorAgenda;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.PlanejadorAgenda.AplicacaoPlanejada;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.PlanejadorAgenda.Sugestao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: paciente não apto precisa voltar a partir de uma data mínima (definida pelo médico). Entre a
 * data mínima e alguns dias depois, sugere o dia em que a aplicação inteira custa menos somada aos outros
 * pacientes dos mesmos medicamentos, e grava como sugestão NAO_APTO para a enfermagem aceitar ou recusar.
 * Respeita o intervalo mínimo com as vizinhas; as aplicações seguintes não se movem (a definir com a LMECC).
 */
@Service
public class SugerirRemarcacaoNaoAptoUseCase {

    private final AplicacaoRepository aplicacaoRepository;
    private final SugestaoRemarcacaoRepository sugestaoRepository;
    private final PreverSugestoesUseCase preverSugestoes;
    private final int janelaDias;

    public SugerirRemarcacaoNaoAptoUseCase(
            AplicacaoRepository aplicacaoRepository,
            SugestaoRemarcacaoRepository sugestaoRepository,
            PreverSugestoesUseCase preverSugestoes,
            // ponytail: prazo de busca fixo (14 dias) até a LMECC informar o prazo real por causa
            @Value("${oncodose.agenda.janela-nao-apto-dias:14}") int janelaDias) {
        this.aplicacaoRepository = aplicacaoRepository;
        this.sugestaoRepository = sugestaoRepository;
        this.preverSugestoes = preverSugestoes;
        this.janelaDias = janelaDias;
    }

    @Transactional
    public SugestaoRemarcacao executar(UUID aplicacaoId, LocalDate dataMinima, String motivo) {
        Aplicacao aplicacao = aplicacaoRepository.findById(aplicacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aplicação", aplicacaoId));
        if (aplicacao.getStatus() != StatusAplicacao.AGENDADA) {
            throw new EstadoInvalidoException("Aplicação", aplicacaoId, "já está " + aplicacao.getStatus());
        }
        if (!dataMinima.isAfter(aplicacao.getDataAgendada())) {
            throw new PeriodoInvalidoException(dataMinima, dataMinima,
                    "a data mínima deve ser depois da data agendada (" + aplicacao.getDataAgendada() + ")");
        }
        LocalDate ate = dataMinima.plusDays(janelaDias);

        // A aplicação vai inteira: dose de cada quimioterápico IV do dia, como no protocolo.
        Map<UUID, Double> doses = new HashMap<>();
        for (ProtocoloMedicamento item : aplicacao.medicamentosDoDia()) {
            if (PreverSugestoesUseCase.intravenoso(item)) {
                doses.put(item.getMedicamento().getId(), item.getDoseMg());
            }
        }
        if (doses.isEmpty()) {
            throw new EstadoInvalidoException("Aplicação", aplicacaoId, "não tem quimioterápico intravenoso no dia");
        }

        Cenario cenario = preverSugestoes.montarCenario(Set.copyOf(doses.keySet()), dataMinima, ate);
        List<AplicacaoPlanejada> outras = cenario.aplicacoes().stream()
                .filter(a -> !a.id().equals(aplicacaoId))
                .collect(Collectors.toList());
        // Vizinhas do mesmo protocolo fora do período também contam para o intervalo mínimo.
        aplicacao.getProtocolo().getAplicacoes().stream()
                .filter(v -> !v.getId().equals(aplicacaoId) && outras.stream().noneMatch(o -> o.id().equals(v.getId())))
                .forEach(v -> outras.add(new AplicacaoPlanejada(v.getId(), v.getProtocolo().getId(),
                        v.getProtocolo().getCodigoProtocolo(), v.getNumero(), Map.of(), v.getDataAgendada(),
                        v.getDataAgendada(), v.getDataAgendada(), v.getProtocolo().getIntervaloMinimoDias(), false)));
        AplicacaoPlanejada alvo = new AplicacaoPlanejada(aplicacaoId, aplicacao.getProtocolo().getId(),
                aplicacao.getProtocolo().getCodigoProtocolo(), aplicacao.getNumero(), doses, aplicacao.getDataAgendada(),
                dataMinima, ate, aplicacao.getProtocolo().getIntervaloMinimoDias(), true);

        Sugestao melhor = PlanejadorAgenda.melhorDia(alvo, outras, cenario.dosesFixas(), cenario.tipos(),
                        dataMinima, ate, LocalDate.now(), cenario.diasFechados())
                .orElseThrow(() -> new EstadoInvalidoException("Aplicação", aplicacaoId,
                        "nenhum dia possível entre " + dataMinima + " e " + ate
                                + " (intervalo mínimo com as vizinhas ou estoque)"));

        sugestaoRepository.expirarPendentes(aplicacaoId, null);
        return sugestaoRepository.save(SugestaoRemarcacao.deNaoApto(aplicacao, melhor.destino(),
                BigDecimal.valueOf(melhor.ganho()).setScale(4, RoundingMode.HALF_UP), motivo));
    }
}
