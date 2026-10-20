package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.ciclo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Autorização médica: quantos dias a aplicação pode antecipar (diasAntes) ou adiar (diasDepois). */
public record JanelaRequest(
        @NotNull(message = "diasAntes é obrigatório") @Min(value = 0, message = "A janela não pode ser negativa") Integer diasAntes,
        @NotNull(message = "diasDepois é obrigatório") @Min(value = 0, message = "A janela não pode ser negativa") Integer diasDepois) {
}
