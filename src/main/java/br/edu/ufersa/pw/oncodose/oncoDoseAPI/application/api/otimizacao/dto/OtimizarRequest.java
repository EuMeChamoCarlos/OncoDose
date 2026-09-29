package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.otimizacao.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record OtimizarRequest(
        @NotNull(message = "O medicamento é obrigatório") UUID medicamentoId,
        @NotNull(message = "A data é obrigatória") LocalDate data) {
}
