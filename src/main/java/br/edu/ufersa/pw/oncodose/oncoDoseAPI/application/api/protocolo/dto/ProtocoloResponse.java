package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.protocolo.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloMedicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloPaciente;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProtocoloResponse(UUID id, String codigoProtocolo, String medicacaoSubtipo, String grupo, LocalDate dataInicio,
                                Integer ciclos, Integer diasIntervalo, Integer intervaloMinimoDias, int totalAplicacoes,
                                List<MedicamentoResponse> medicamentos, List<AplicacaoResponse> aplicacoes) {

    public record MedicamentoResponse(UUID medicamentoId, Double doseMg, String via, String diasAplicacao) {
        static MedicamentoResponse from(ProtocoloMedicamento item) {
            return new MedicamentoResponse(item.getMedicamento().getId(), item.getDoseMg(), item.getVia(), item.getDiasAplicacao());
        }
    }

    public static ProtocoloResponse fromProtocolo(ProtocoloPaciente protocolo) {
        return new ProtocoloResponse(
                protocolo.getId(),
                protocolo.getCodigoProtocolo(),
                protocolo.getMedicacaoSubtipo(),
                protocolo.getGrupo(),
                protocolo.getDataInicio(),
                protocolo.getCiclos(),
                protocolo.getDiasIntervalo(),
                protocolo.getIntervaloMinimoDias(),
                protocolo.totalAplicacoes(),
                protocolo.getMedicamentos().stream().map(MedicamentoResponse::from).toList(),
                protocolo.getAplicacoes().stream().map(AplicacaoResponse::fromAplicacao).toList()
        );
    }
}
