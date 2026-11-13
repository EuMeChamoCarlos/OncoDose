package br.edu.ufersa.pw.oncotimize.application.api.prescricao.dto;

import br.edu.ufersa.pw.oncotimize.domain.prescricao.Prescricao;
import br.edu.ufersa.pw.oncotimize.domain.prescricao.StatusPrescricao;

import java.time.LocalDate;
import java.util.UUID;

public record PrescricaoResponse(UUID id, String codigoPrescricao, UUID medicamentoId,
                                 Double doseMg, StatusPrescricao status, LocalDate dataPrescricao,
                                 UUID aplicacaoId) {
    public static PrescricaoResponse fromPrescricao(Prescricao prescricao) {
        return new PrescricaoResponse(
                prescricao.getId(),
                prescricao.getCodigoPrescricao(),
                prescricao.getMedicamento().getId(),
                prescricao.getDoseMg(),
                prescricao.getStatus(),
                prescricao.getDataPrescricao(),
                prescricao.getAplicacao() == null ? null : prescricao.getAplicacao().getId()
        );
    }
}
