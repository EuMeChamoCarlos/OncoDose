package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.medicamento.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;

import java.util.UUID;

public record MedicamentoResponse(UUID id, String nome, String codigoInterno) {
    public static MedicamentoResponse fromMedicamento(Medicamento medicamento) {
        return new MedicamentoResponse(medicamento.getId(), medicamento.getNome(), medicamento.getCodigoInterno());
    }
}
