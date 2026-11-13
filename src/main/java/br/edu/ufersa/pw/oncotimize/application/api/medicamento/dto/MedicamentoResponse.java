package br.edu.ufersa.pw.oncotimize.application.api.medicamento.dto;

import br.edu.ufersa.pw.oncotimize.domain.medicamento.Medicamento;

import java.util.UUID;

public record MedicamentoResponse(UUID id, String nome, String codigoInterno) {
    public static MedicamentoResponse fromMedicamento(Medicamento medicamento) {
        return new MedicamentoResponse(medicamento.getId(), medicamento.getNome(), medicamento.getCodigoInterno());
    }
}
