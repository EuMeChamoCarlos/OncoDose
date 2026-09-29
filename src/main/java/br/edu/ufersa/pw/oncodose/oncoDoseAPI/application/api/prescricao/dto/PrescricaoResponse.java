package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.prescricao.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.StatusPrescricao;

import java.time.LocalDate;
import java.util.UUID;

public record PrescricaoResponse(UUID id, String codigoPrescricao, UUID medicamentoId,
                                 Double doseMg, StatusPrescricao status, LocalDate dataPrescricao) {
    public static PrescricaoResponse fromPrescricao(Prescricao prescricao) {
        return new PrescricaoResponse(
                prescricao.getId(),
                prescricao.getCodigoPrescricao(),
                prescricao.getMedicamento().getId(),
                prescricao.getDoseMg(),
                prescricao.getStatus(),
                prescricao.getDataPrescricao()
        );
    }
}
