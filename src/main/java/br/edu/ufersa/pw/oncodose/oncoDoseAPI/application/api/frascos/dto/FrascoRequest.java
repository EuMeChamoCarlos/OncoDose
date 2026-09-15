package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.frascos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;
import lombok.Data;

@Data
public class FrascoRequest {
    @NotNull(message = "O medicamento é obrigatório")
    private UUID medicamentoId;

    @NotNull(message = "O volume é obrigatório")
    @Positive(message = "O volume deve ser maior que zero")
    private Double volumeMg;

    @NotNull(message = "O custo é obrigatório")
    @Positive(message = "O custo deve ser maior que zero")
    private Double custo;

    @NotNull(message = "O estoque é obrigatório")
    @PositiveOrZero(message = "O estoque não pode ser negativo")
    private Integer quantidadeEstoque;
}
