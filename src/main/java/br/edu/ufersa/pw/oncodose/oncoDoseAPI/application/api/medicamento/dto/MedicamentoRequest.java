package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento.dto;

import lombok.Data;

@Data
public class MedicamentoRequest {
    private String nome;
    private String codigoInterno;
}
