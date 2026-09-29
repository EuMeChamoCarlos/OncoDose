package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.prescricao.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.StatusPrescricao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

/** Só dados anonimizados (LGPD): nenhum campo cabe nome, CPF ou prontuário. */
public record PrescricaoRequest(
        @NotNull(message = "O código é obrigatório")
        @Pattern(regexp = Prescricao.PADRAO_CODIGO, message = "O código deve seguir o padrão PRE0000-000000")
        String codigoPrescricao,
        @NotNull(message = "O medicamento é obrigatório") UUID medicamentoId,
        @NotNull(message = "A dose é obrigatória") @Positive(message = "A dose deve ser maior que zero") Double doseMg,
        @NotNull(message = "O status é obrigatório") StatusPrescricao status,
        @NotNull(message = "A data é obrigatória") LocalDate dataPrescricao) {
}
