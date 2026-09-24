package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicamentoRequest {
    @NotBlank(message = "O nome é obrigatório")
    private String nome;
    private String codigoInterno;
}
