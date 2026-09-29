package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.otimizacao.dto;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.AlocacaoFrasco;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.Otimizacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao.StatusOtimizacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Resultado de uma instância: custo do modelo vs. cada paciente com seus próprios frascos,
 * os frascos a abrir e quanto de cada frasco vai para cada prescrição.
 */
public record OtimizacaoResponse(UUID id, UUID medicamentoId, LocalDate data, StatusOtimizacao status,
                                 LocalDateTime confirmadaEm,
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
                o.getConfirmadaEm(),
                o.getCustoTotal(),
                o.getCustoTotal().add(o.getEconomiaVsEmpirico()),
                o.getEconomiaVsEmpirico(),
                arredondar(o.getDesperdicioMg(), 2),
                alocacoes.stream()
                        .map(a -> new FrascoAberto(a.getApresentacao().getId(), a.getApresentacao().getVolumeMg(),
                                a.getApresentacao().getCusto(), a.getNumeroFrasco()))
                        .distinct()
                        .sorted(Comparator.comparing(FrascoAberto::volumeMg).reversed()
                                .thenComparing(FrascoAberto::numeroFrasco))
                        .toList(),
                alocacoes.stream()
                        .map(a -> new Alocacao(a.getPrescricao().getCodigoPrescricao(), a.getApresentacao().getId(),
                                a.getNumeroFrasco(), arredondar(a.getFracao(), 4), arredondar(a.getVolumeUtilizadoMg(), 2)))
                        .sorted(Comparator.comparing(Alocacao::codigoPrescricao)
                                .thenComparing(Alocacao::volumeMg, Comparator.reverseOrder()))
                        .toList()
        );
    }

    /** Só apresentação: tira ruído de ponto flutuante (0.9999999999999999). O banco guarda o valor exato. */
    private static Double arredondar(Double valor, int casas) {
        return BigDecimal.valueOf(valor).setScale(casas, RoundingMode.HALF_UP).doubleValue();
    }
}
