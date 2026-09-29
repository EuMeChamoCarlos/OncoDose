package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.otimizacao.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.AlocacaoFrasco;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.Otimizacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.StatusOtimizacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Resultado de uma instância: custo do modelo vs. cada paciente com seus próprios frascos,
 * os frascos a abrir e quanto de cada frasco vai para cada prescrição.
 */
public record OtimizacaoResponse(UUID id, UUID medicamentoId, LocalDate data, StatusOtimizacao status,
                                 BigDecimal custoOtimizado, BigDecimal custoSemCompartilhamento,
                                 BigDecimal economia, Double desperdicioMg,
                                 List<FrascoAberto> frascosAbertos, List<Alocacao> alocacoes) {

    public record FrascoAberto(UUID apresentacaoId, Double volumeMg, Double custo, Integer numeroFrasco) {}

    public record Alocacao(String codigoPrescricao, UUID apresentacaoId, Integer numeroFrasco,
                           Double fracao, Double volumeMg) {}

    public static OtimizacaoResponse fromOtimizacao(Otimizacao o) {
        List<AlocacaoFrasco> alocacoes = o.getAlocacoes();
        return new OtimizacaoResponse(
                o.getId(),
                o.getMedicamento().getId(),
                o.getDataReferencia(),
                o.getStatus(),
                o.getCustoTotal(),
                o.getCustoTotal().add(o.getEconomiaVsEmpirico()),
                o.getEconomiaVsEmpirico(),
                o.getDesperdicioMg(),
                alocacoes.stream()
                        .map(a -> new FrascoAberto(a.getApresentacao().getId(), a.getApresentacao().getVolumeMg(),
                                a.getApresentacao().getCusto(), a.getNumeroFrasco()))
                        .distinct()
                        .toList(),
                alocacoes.stream()
                        .map(a -> new Alocacao(a.getPrescricao().getCodigoPrescricao(), a.getApresentacao().getId(),
                                a.getNumeroFrasco(), a.getFracao(), a.getVolumeUtilizadoMg()))
                        .toList()
        );
    }
}
