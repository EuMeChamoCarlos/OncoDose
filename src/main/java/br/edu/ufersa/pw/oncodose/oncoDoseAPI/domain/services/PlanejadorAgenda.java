package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.SolverFracionamento.TipoFrasco;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Agenda v1 (plano-agenda §5): heurística gulosa que sugere remarcar aplicações para dias em que
 * dividam frascos com outras do mesmo medicamento. Mede o custo de cada dia com o
 * {@link SolverFracionamento} e só simula: nenhuma data real muda aqui. Lógica pura.
 *
 * <p>Limitações conhecidas (documentar no artigo): estoque avaliado por dia, sem consumo entre
 * dias do período; busca local sem garantia de ótimo global; cada aplicação move no máximo uma vez.
 */
public final class PlanejadorAgenda {

    /**
     * Aplicação no cenário. doseMg null = não entra no custo do dia (ex: prescrição NAO_APTO).
     * movivel = false: só serve de vizinha no intervalo mínimo (realizada, fora do período...).
     * Canceladas não entram no cenário.
     */
    public record AplicacaoPlanejada(UUID id, UUID protocoloId, String codigoProtocolo, int numero, Double doseMg,
                                     LocalDate data, LocalDate inicioJanela, LocalDate fimJanela,
                                     int intervaloMinimoDias, boolean movivel) {}

    public record Sugestao(UUID aplicacaoId, String codigoProtocolo, int numero,
                           LocalDate origem, LocalDate destino, double ganho) {}

    private static final double EPS = 1e-2;  // ganho mínimo: um centavo

    private PlanejadorAgenda() {}

    /**
     * @param dosesFixas   demanda que não se move, por dia (ex: prescrições do legado, sem aplicação)
     * @param diasFechados dias com preparo já confirmado: nunca recebem paciente
     */
    public static List<Sugestao> planejar(List<AplicacaoPlanejada> aplicacoes, Map<LocalDate, List<Double>> dosesFixas,
                                          List<TipoFrasco> tipos, LocalDate inicio, LocalDate fim,
                                          Set<LocalDate> diasFechados) {
        Map<UUID, LocalDate> data = new HashMap<>();
        aplicacoes.forEach(a -> data.put(a.id(), a.data()));
        Map<List<Double>, Double> cache = new HashMap<>();
        Set<UUID> movidas = new HashSet<>();
        List<Sugestao> sugestoes = new ArrayList<>();

        // Ordem fixa: em empate de ganho vence a primeira (origem, protocolo, número, destino) → resultado reprodutível.
        List<AplicacaoPlanejada> candidatas = aplicacoes.stream()
                .filter(a -> a.movivel() && a.doseMg() != null)
                .sorted(Comparator.comparing(AplicacaoPlanejada::data)
                        .thenComparing(AplicacaoPlanejada::codigoProtocolo)
                        .thenComparingInt(AplicacaoPlanejada::numero))
                .toList();

        // ponytail: reavalia todos os movimentos a cada rodada (O(n² · janela)); incremental se o período crescer muito
        while (true) {
            Sugestao melhor = null;
            for (AplicacaoPlanejada a : candidatas) {
                LocalDate origem = data.get(a.id());
                if (movidas.contains(a.id()) || origem.isBefore(inicio) || origem.isAfter(fim)) {
                    continue;
                }
                List<Double> naOrigem = doses(origem, aplicacoes, data, dosesFixas);
                List<Double> origemSem = new ArrayList<>(naOrigem);
                origemSem.remove(a.doseMg());
                double custoOrigem = custo(naOrigem, tipos, cache);
                double custoOrigemSem = custo(origemSem, tipos, cache);

                LocalDate ate = min(a.fimJanela(), fim);
                for (LocalDate destino = max(a.inicioJanela(), inicio); !destino.isAfter(ate); destino = destino.plusDays(1)) {
                    if (destino.equals(origem) || diasFechados.contains(destino)
                            || !respeitaIntervalo(a, destino, aplicacoes, data)) {
                        continue;
                    }
                    List<Double> noDestino = doses(destino, aplicacoes, data, dosesFixas);
                    List<Double> destinoCom = new ArrayList<>(noDestino);
                    destinoCom.add(a.doseMg());
                    double ganho = custoOrigem + custo(noDestino, tipos, cache)
                            - custoOrigemSem - custo(destinoCom, tipos, cache);
                    // Infinito/NaN = algum dia sem estoque para a demanda: movimento descartado.
                    if (Double.isFinite(ganho) && ganho > EPS && (melhor == null || ganho > melhor.ganho() + EPS)) {
                        melhor = new Sugestao(a.id(), a.codigoProtocolo(), a.numero(), origem, destino, ganho);
                    }
                }
            }
            if (melhor == null) {
                return sugestoes;
            }
            data.put(melhor.aplicacaoId(), melhor.destino());
            movidas.add(melhor.aplicacaoId());
            sugestoes.add(melhor);
        }
    }

    /** Mesma regra de ProtocoloPaciente.reagendar, mas contra as datas já simuladas das vizinhas. */
    private static boolean respeitaIntervalo(AplicacaoPlanejada a, LocalDate destino,
                                             List<AplicacaoPlanejada> aplicacoes, Map<UUID, LocalDate> data) {
        List<AplicacaoPlanejada> doProtocolo = aplicacoes.stream().filter(v -> v.protocoloId().equals(a.protocoloId())).toList();
        Optional<AplicacaoPlanejada> anterior = doProtocolo.stream().filter(v -> v.numero() < a.numero())
                .max(Comparator.comparingInt(AplicacaoPlanejada::numero));
        Optional<AplicacaoPlanejada> seguinte = doProtocolo.stream().filter(v -> v.numero() > a.numero())
                .min(Comparator.comparingInt(AplicacaoPlanejada::numero));
        return anterior.map(v -> ChronoUnit.DAYS.between(data.get(v.id()), destino) >= a.intervaloMinimoDias()).orElse(true)
                && seguinte.map(v -> ChronoUnit.DAYS.between(destino, data.get(v.id())) >= a.intervaloMinimoDias()).orElse(true);
    }

    private static List<Double> doses(LocalDate dia, List<AplicacaoPlanejada> aplicacoes, Map<UUID, LocalDate> data,
                                      Map<LocalDate, List<Double>> dosesFixas) {
        List<Double> doses = new ArrayList<>(dosesFixas.getOrDefault(dia, List.of()));
        for (AplicacaoPlanejada a : aplicacoes) {
            if (a.doseMg() != null && dia.equals(data.get(a.id()))) {
                doses.add(a.doseMg());
            }
        }
        return doses;
    }

    /** Custo ótimo do dia; dia sem estoque para a demanda vale infinito. Cache por conjunto de doses. */
    private static double custo(List<Double> doses, List<TipoFrasco> tipos, Map<List<Double>, Double> cache) {
        if (doses.isEmpty()) {
            return 0;
        }
        List<Double> chave = doses.stream().sorted().toList();
        Double conhecido = cache.get(chave);
        if (conhecido != null) {
            return conhecido;
        }
        double valor;
        try {
            valor = SolverFracionamento.resolver(chave, tipos).custoTotal();
        } catch (IllegalArgumentException | IllegalStateException semSolucao) {
            valor = Double.POSITIVE_INFINITY;
        }
        cache.put(chave, valor);
        return valor;
    }

    private static LocalDate max(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDate min(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }
}
