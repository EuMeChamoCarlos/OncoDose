package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.prescricao.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.StatusPrescricao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/** Código e medicamento não mudam: prescrição errada é excluída e recriada. */
public record AtualizarPrescricaoRequest(
        @NotNull(message = "A dose é obrigatória") @Positive(message = "A dose deve ser maior que zero") Double doseMg,
        @NotNull(message = "O status é obrigatório") StatusPrescricao status,
        @NotNull(message = "A data é obrigatória") LocalDate dataPrescricao) {
}
