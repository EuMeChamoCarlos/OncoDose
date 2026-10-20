package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.ciclo.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Ciclo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Só dados anonimizados (LGPD). diasAntes/diasDepois são opcionais (padrão 0) e viram a janela
 * inicial de todas as aplicações; cada uma pode ser ajustada depois em PATCH /api/aplicacoes/{id}/janela.
 */
public record CicloRequest(
        @NotNull(message = "O código é obrigatório")
        @Pattern(regexp = Ciclo.PADRAO_CODIGO, message = "O código deve seguir o padrão CIC0000-000000")
        String codigoCiclo,
        @NotNull(message = "O medicamento é obrigatório") UUID medicamentoId,
        @NotNull(message = "A dose é obrigatória") @Positive(message = "A dose deve ser maior que zero") Double doseMg,
        @NotNull(message = "A data de início é obrigatória") LocalDate dataInicio,
        @NotNull(message = "O total de aplicações é obrigatório")
        @Min(value = 1, message = "Deve haver ao menos 1 aplicação")
        @Max(value = Ciclo.MAX_APLICACOES, message = "No máximo 100 aplicações por ciclo")
        Integer totalAplicacoes,
        @NotNull(message = "O intervalo é obrigatório") @Min(value = 1, message = "O intervalo deve ser de pelo menos 1 dia")
        Integer intervaloDias,
        @NotNull(message = "O intervalo mínimo é obrigatório") @Min(value = 0, message = "O intervalo mínimo não pode ser negativo")
        Integer intervaloMinimoDias,
        @Min(value = 0, message = "A janela não pode ser negativa") Integer diasAntes,
        @Min(value = 0, message = "A janela não pode ser negativa") Integer diasDepois) {
}
