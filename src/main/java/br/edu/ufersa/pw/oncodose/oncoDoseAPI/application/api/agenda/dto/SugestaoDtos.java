package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.agenda.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.agenda.SugestaoRemarcacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** DTOs das sugestões de remarcação e das decisões (aceite/recusa). */
public final class SugestaoDtos {

    private SugestaoDtos() {}

    public record GerarSugestoesRequest(
            @NotNull(message = "O medicamento é obrigatório") UUID medicamentoId,
            @NotNull(message = "O início é obrigatório") LocalDate inicio,
            @NotNull(message = "O fim é obrigatório") LocalDate fim) {}

    /** Paciente não apto: a partir de quando pode voltar e por quê (definido pelo médico). */
    public record RemarcacaoRequest(
            @NotNull(message = "A data mínima é obrigatória") LocalDate dataMinima,
            @NotBlank(message = "O motivo é obrigatório") @Size(max = 255, message = "O motivo tem no máximo 255 caracteres")
            String motivo) {}

    /** Motivo opcional do aceite ou da recusa. */
    public record DecisaoRequest(@Size(max = 500, message = "O motivo tem no máximo 500 caracteres") String motivo) {}

    public record SugestaoResponse(UUID id, UUID aplicacaoId, String codigoProtocolo, int numero,
                                   SugestaoRemarcacao.Tipo tipo, LocalDate origem, LocalDate destino,
                                   BigDecimal economiaEstimada, String motivoRemarcacao, SugestaoRemarcacao.Status status,
                                   LocalDateTime geradaEm, LocalDateTime decididaEm, String decididaPor, String motivoDecisao) {
        public static SugestaoResponse fromSugestao(SugestaoRemarcacao s) {
            return new SugestaoResponse(s.getId(), s.getAplicacao().getId(),
                    s.getAplicacao().getProtocolo().getCodigoProtocolo(), s.getAplicacao().getNumero(), s.getTipo(),
                    s.getDataOrigem(), s.getDataDestino(), s.getEconomiaEstimada(), s.getMotivoRemarcacao(), s.getStatus(),
                    s.getGeradaEm(), s.getDecididaEm(), s.getDecididaPor(), s.getMotivoDecisao());
        }
    }
}
