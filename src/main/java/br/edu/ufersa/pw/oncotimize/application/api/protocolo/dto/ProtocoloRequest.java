package br.edu.ufersa.pw.oncotimize.application.api.protocolo.dto;

import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloMedicamento;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.ProtocoloPaciente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Só dados anonimizados (LGPD): nenhum campo cabe nome, CPF ou prontuário. diasAntes/diasDepois são
 * opcionais (padrão 0) e viram a janela inicial de todas as aplicações; cada uma pode ser ajustada
 * depois em PATCH /api/aplicacoes/{id}/janela.
 */
public record ProtocoloRequest(
        @NotNull(message = "O código é obrigatório")
        @Pattern(regexp = ProtocoloPaciente.PADRAO_CODIGO, message = "O código deve seguir o padrão PTC0000-000000")
        String codigoProtocolo,
        @NotBlank(message = "O subtipo é obrigatório") @Size(max = 100, message = "O subtipo tem no máximo 100 caracteres")
        String medicacaoSubtipo,
        @Size(max = 100, message = "O grupo tem no máximo 100 caracteres") String grupo,
        @NotNull(message = "A data de início é obrigatória") LocalDate dataInicio,
        @NotNull(message = "O número de ciclos é obrigatório")
        @Min(value = 1, message = "Deve haver ao menos 1 ciclo")
        @Max(value = ProtocoloPaciente.MAX_APLICACOES, message = "No máximo 100 ciclos por protocolo")
        Integer ciclos,
        @NotNull(message = "O intervalo é obrigatório") @Min(value = 1, message = "O intervalo deve ser de pelo menos 1 dia")
        Integer diasIntervalo,
        @NotNull(message = "O intervalo mínimo é obrigatório") @Min(value = 0, message = "O intervalo mínimo não pode ser negativo")
        Integer intervaloMinimoDias,
        @Min(value = 0, message = "A janela não pode ser negativa") Integer diasAntes,
        @Min(value = 0, message = "A janela não pode ser negativa") Integer diasDepois,
        @NotEmpty(message = "Informe ao menos um medicamento") List<@Valid @NotNull MedicamentoRequest> medicamentos) {

    /** Quimioterápico do protocolo; pré-medicação e diluente não entram. */
    public record MedicamentoRequest(
            @NotNull(message = "O medicamento é obrigatório") UUID medicamentoId,
            @NotNull(message = "A dose é obrigatória") @Positive(message = "A dose deve ser maior que zero") Double doseMg,
            @NotBlank(message = "A via é obrigatória") @Size(max = 10, message = "A via tem no máximo 10 caracteres") String via,
            @NotNull(message = "Os dias de aplicação são obrigatórios")
            @Pattern(regexp = ProtocoloMedicamento.PADRAO_DIAS, message = "Os dias devem seguir o padrão D1,D8")
            String diasAplicacao) {
    }
}
