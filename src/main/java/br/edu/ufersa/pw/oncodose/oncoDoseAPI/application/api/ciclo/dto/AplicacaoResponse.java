package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.ciclo.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Aplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.StatusAplicacao;

import java.time.LocalDate;
import java.util.UUID;

/** inicioJanela/fimJanela: datas extremas para onde a aplicação pode ser remarcada. */
public record AplicacaoResponse(UUID id, UUID cicloId, Integer numero, LocalDate dataPrevista, LocalDate dataAgendada,
                                Integer diasAntes, Integer diasDepois, LocalDate inicioJanela, LocalDate fimJanela,
                                StatusAplicacao status) {
    public static AplicacaoResponse fromAplicacao(Aplicacao aplicacao) {
        return new AplicacaoResponse(
                aplicacao.getId(),
                aplicacao.getCiclo().getId(),
                aplicacao.getNumero(),
                aplicacao.getDataPrevista(),
                aplicacao.getDataAgendada(),
                aplicacao.getDiasAntes(),
                aplicacao.getDiasDepois(),
                aplicacao.inicioJanela(),
                aplicacao.fimJanela(),
                aplicacao.getStatus()
        );
    }
}
