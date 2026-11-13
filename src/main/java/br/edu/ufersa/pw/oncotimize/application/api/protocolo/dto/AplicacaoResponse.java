package br.edu.ufersa.pw.oncotimize.application.api.protocolo.dto;

import br.edu.ufersa.pw.oncotimize.domain.protocolo.Aplicacao;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.StatusAplicacao;

import java.time.LocalDate;
import java.util.UUID;

/** inicioJanela/fimJanela: datas extremas para onde a aplicação pode ser remarcada. */
public record AplicacaoResponse(UUID id, UUID protocoloId, Integer numero, Integer cicloNumero, Integer diaCiclo,
                                LocalDate dataPrevista, LocalDate dataAgendada, LocalDate dataReal,
                                Integer diasAntes, Integer diasDepois, LocalDate inicioJanela, LocalDate fimJanela,
                                StatusAplicacao status, String motivoAlteracao) {
    public static AplicacaoResponse fromAplicacao(Aplicacao aplicacao) {
        return new AplicacaoResponse(
                aplicacao.getId(),
                aplicacao.getProtocolo().getId(),
                aplicacao.getNumero(),
                aplicacao.getCicloNumero(),
                aplicacao.getDiaCiclo(),
                aplicacao.getDataPrevista(),
                aplicacao.getDataAgendada(),
                aplicacao.getDataReal(),
                aplicacao.getDiasAntes(),
                aplicacao.getDiasDepois(),
                aplicacao.inicioJanela(),
                aplicacao.fimJanela(),
                aplicacao.getStatus(),
                aplicacao.getMotivoAlteracao()
        );
    }
}
