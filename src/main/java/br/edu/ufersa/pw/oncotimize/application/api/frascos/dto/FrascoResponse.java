package br.edu.ufersa.pw.oncotimize.application.api.frascos.dto;

import br.edu.ufersa.pw.oncotimize.domain.frascos.Frascos;

import java.util.UUID;

public record FrascoResponse(UUID id, UUID medicamentoId, Double volumeMg, Double custo, Integer quantidadeEstoque,
                             String descricaoTasy) {
    public static FrascoResponse fromFrasco(Frascos frasco) {
        return new FrascoResponse(
                frasco.getId(),
                frasco.getMedicamento().getId(),
                frasco.getVolumeMg(),
                frasco.getCusto(),
                frasco.getQuantidadeEstoque(),
                frasco.getDescricaoTasy()
        );
    }
}
