package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.frascos.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;

import java.util.UUID;

public record FrascoResponse(UUID id, UUID medicamentoId, Double volumeMg, Double custo, Integer quantidadeEstoque) {
    public static FrascoResponse fromFrasco(Frascos frasco) {
        return new FrascoResponse(
                frasco.getId(),
                frasco.getMedicamento().getId(),
                frasco.getVolumeMg(),
                frasco.getCusto(),
                frasco.getQuantidadeEstoque()
        );
    }
}
