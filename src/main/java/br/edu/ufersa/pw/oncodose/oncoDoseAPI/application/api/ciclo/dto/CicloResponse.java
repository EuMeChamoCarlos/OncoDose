package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.ciclo.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Ciclo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CicloResponse(UUID id, String codigoCiclo, UUID medicamentoId, Double doseMg, LocalDate dataInicio,
                            Integer totalAplicacoes, Integer intervaloDias, Integer intervaloMinimoDias,
                            List<AplicacaoResponse> aplicacoes) {
    public static CicloResponse fromCiclo(Ciclo ciclo) {
        return new CicloResponse(
                ciclo.getId(),
                ciclo.getCodigoCiclo(),
                ciclo.getMedicamento().getId(),
                ciclo.getDoseMg(),
                ciclo.getDataInicio(),
                ciclo.getTotalAplicacoes(),
                ciclo.getIntervaloDias(),
                ciclo.getIntervaloMinimoDias(),
                ciclo.getAplicacoes().stream().map(AplicacaoResponse::fromAplicacao).toList()
        );
    }
}
