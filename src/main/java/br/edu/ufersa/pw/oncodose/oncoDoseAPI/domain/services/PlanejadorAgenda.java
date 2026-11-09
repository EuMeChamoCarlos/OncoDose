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
 * Agenda: heurística gulosa que sugere remarcar aplicações para dias em que dividam frascos com outras.
 * A aplicação se move inteira: no AC, doxorrubicina e ciclofosfamida mudam de data juntas, e o ganho é a
 * soma do ganho de cada medicamento, cada um com seus frascos e com os outros pacientes do dia. Mede o
 * custo com o {@link SolverFracionamento} e só simula: nenhuma data real muda aqui. Lógica pura.
 *
 * <p>Limitações conhecidas (documentar no artigo): estoque avaliado por dia, sem consumo entre dias do
 * período; busca local sem garantia de ótimo global; cada aplicação move no máximo uma vez.
 */
public final class PlanejadorAgenda {

    /**
     * Aplicação no cenário. doses: mg por medicamento no dia (vazio = não entra no custo, ex: NAO_APTO).
     * movivel = false: só serve de vizinha no intervalo mínimo ou como demanda fixa do dia.
     * Canceladas não entram no cenário.
     */
    public record AplicacaoPlanejada(UUID id, UUID protocoloId, String codigoProtocolo, int numero,
                                     Map<UUID, Double> doses, LocalDate data, LocalDate inicioJanela,
                                     LocalDate fimJanela, int intervaloMinimoDias, boolean movivel) {}

    public record Sugestao(UUID aplicacaoId, String codigoProtocolo, int numero,
                           LocalDate origem, LocalDate destino, double ganho) {}

    private record Chave(UUID medicamentoId, List<Double> doses) {}

    private static final double EPS = 1e-2;  // ganho mínimo: um centavo

    private PlanejadorAgenda() {}

    /**
     * @param dosesFixas   demanda que não se move, por medicamento e dia (ex: prescrições do legado)
     * @param tipos        frascos de cada medicamento
     * @param hoje         nada se move de um dia passado nem para um dia passado
     * @param diasFechados dias com preparo já confirmado, por medicamento: nunca recebem paciente
     */
    public static List<Sugestao> planejar(List<AplicacaoPlanejada> aplicacoes,
                                          Map<UUID, Map<LocalDate, List<Double>>> dosesFixas,
                                          Map<UUID, List<TipoFrasco>> tipos, LocalDate inicio, LocalDate fim,
                                          LocalDate hoje, Map<UUID, Set<LocalDate>> diasFechados) {
        Map<UUID, LocalDate> data = new HashMap<>();
        aplicacoes.forEach(a -> data.put(a.id(), a.data()));
        Map<Chave, Double> cache = new HashMap<>();
        Set<UUID> movidas = new HashSet<>();
        List<Sugestao> sugestoes = new ArrayList<>();
        LocalDate primeiroDia = max(inicio, hoje);

        // Ordem fixa: em empate de ganho vence a primeira (origem, protocolo, número, destino) → resultado reprodutível.
        List<AplicacaoPlanejada> candidatas = aplicacoes.stream()
                .filter(a -> a.movivel() && !a.doses().isEmpty())
                .sorted(Comparator.comparing(AplicacaoPlanejada::data)
                        .thenComparing(AplicacaoPlanejada::codigoProtocolo)
                        .thenComparingInt(AplicacaoPlanejada::numero))
                .toList();

        // ponytail: reavalia todos os movimentos a cada rodada (O(n² · janela)); incremental se o período crescer muito
        while (true) {
            Sugestao melhor = null;
            for (AplicacaoPlanejada a : candidatas) {
                LocalDate origem = data.get(a.id());
                if (movidas.contains(a.id()) || origem.isBefore(primeiroDia) || origem.isAfter(fim)) {
                    continue;
                }
                LocalDate ate = min(a.fimJanela(), fim);
                for (LocalDate destino = max(a.inicioJanela(), primeiroDia); !destino.isAfter(ate); destino = destino.plusDays(1)) {
                    if (destino.equals(origem) || fechado(a, destino, diasFechados)
                            || !respeitaIntervalo(a, destino, aplicacoes, data)) {
                        continue;
                    }
                    double ganho = 0;
                    for (Map.Entry<UUID, Double> dose : a.doses().entrySet()) {
                        UUID med = dose.getKey();
                        List<TipoFrasco> frascos = tipos.getOrDefault(med, List.of());
                        List<Double> naOrigem = doses(med, origem, aplicacoes, data, dosesFixas);
                        List<Double> origemSem = new ArrayList<>(naOrigem);
                        origemSem.remove(dose.getValue());
                        List<Double> noDestino = doses(med, destino, aplicacoes, data, dosesFixas);
                        List<Double> destinoCom = new ArrayList<>(noDestino);
                        destinoCom.add(dose.getValue());
                        ganho += custo(med, naOrigem, frascos, cache) + custo(med, noDestino, frascos, cache)
                                - custo(med, origemSem, frascos, cache) - custo(med, destinoCom, frascos, cache);
                    }
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

    /** O destino está fechado se algum medicamento da aplicação já teve o preparo confirmado nesse dia. */
    private static boolean fechado(AplicacaoPlanejada a, LocalDate destino, Map<UUID, Set<LocalDate>> diasFechados) {
        return a.doses().keySet().stream().anyMatch(med -> diasFechados.getOrDefault(med, Set.of()).contains(destino));
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

    private static List<Double> doses(UUID med, LocalDate dia, List<AplicacaoPlanejada> aplicacoes, Map<UUID, LocalDate> data,
                                      Map<UUID, Map<LocalDate, List<Double>>> dosesFixas) {
        List<Double> doses = new ArrayList<>(dosesFixas.getOrDefault(med, Map.of()).getOrDefault(dia, List.of()));
        for (AplicacaoPlanejada a : aplicacoes) {
            Double dose = a.doses().get(med);
            if (dose != null && dia.equals(data.get(a.id()))) {
                doses.add(dose);
            }
        }
        return doses;
    }

    /** Custo ótimo do medicamento no dia; dia sem estoque para a demanda vale infinito. Cache por conjunto de doses. */
    private static double custo(UUID med, List<Double> doses, List<TipoFrasco> tipos, Map<Chave, Double> cache) {
        if (doses.isEmpty()) {
            return 0;
        }
        Chave chave = new Chave(med, doses.stream().sorted().toList());
        Double conhecido = cache.get(chave);
        if (conhecido != null) {
            return conhecido;
        }
        double valor;
        try {
            valor = SolverFracionamento.resolver(chave.doses(), tipos).custoTotal();
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
