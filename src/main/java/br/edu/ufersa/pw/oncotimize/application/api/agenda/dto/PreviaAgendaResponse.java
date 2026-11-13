package br.edu.ufersa.pw.oncotimize.application.api.agenda.dto;

import br.edu.ufersa.pw.oncotimize.application.usecases.agenda.PreverSugestoesUseCase.Previa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Prévia do planejador: sugestões na ordem em que o guloso as escolheu. ganhoTotal = custo do período
 * hoje menos o custo com todas as sugestões aplicadas. tempoMs: métrica do artigo (tempo × período).
 */
public record PreviaAgendaResponse(UUID medicamentoId, LocalDate inicio, LocalDate fim,
                                   BigDecimal ganhoTotal, long tempoMs, List<SugestaoResponse> sugestoes) {

    public record SugestaoResponse(UUID aplicacaoId, String codigoProtocolo, int numero,
                                   LocalDate origem, LocalDate destino, BigDecimal ganho) {}

    public static PreviaAgendaResponse fromPrevia(Previa previa) {
        List<SugestaoResponse> sugestoes = previa.sugestoes().stream()
                .map(s -> new SugestaoResponse(s.aplicacaoId(), s.codigoProtocolo(), s.numero(),
                        s.origem(), s.destino(), dinheiro(s.ganho())))
                .toList();
        return new PreviaAgendaResponse(previa.medicamentoId(), previa.inicio(), previa.fim(),
                dinheiro(previa.sugestoes().stream().mapToDouble(s -> s.ganho()).sum()),
                previa.tempoMs(), sugestoes);
    }

    private static BigDecimal dinheiro(double valor) {
        return BigDecimal.valueOf(valor).setScale(4, RoundingMode.HALF_UP);
    }
}
