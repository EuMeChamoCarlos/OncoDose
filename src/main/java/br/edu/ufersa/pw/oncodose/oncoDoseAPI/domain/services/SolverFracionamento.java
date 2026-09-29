package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Modelo de Gê et al. (2023), eq. 7–10, com custo por frasco aberto. Uma instância é
 * um medicamento num grupo de pacientes que podem dividir frascos (ex: o mesmo dia).
 *
 * <pre>
 *   min  Σ_j Σ_k c_j · y_jk
 *   s.a. Σ_j Σ_k v_j · x_ijk = d_i   ∀ i        (dose exata do paciente i)
 *        Σ_i x_ijk ≤ y_jk            ∀ j,k      (só usa frasco aberto; pode dividir entre pacientes)
 *        y_jk ∈ {0,1},  0 ≤ x_ijk ≤ 1
 * </pre>
 *
 * A sobra de um frasco aberto é descartada (estabilidade curta), por isso o custo é o do
 * frasco inteiro. Lógica pura: sem Spring e sem banco.
 */
public final class SolverFracionamento {

    /** Apresentação j: volume v_j (mg), custo c_j e estoque E_j. */
    public record TipoFrasco(double volumeMg, double custo, int estoque) {}

    /** Frasco físico k do tipo j que foi aberto. */
    public record FrascoAberto(int tipo, int numero) {}

    /** x_ijk > 0: fração do frasco (tipo, numero) que vai para o paciente. */
    public record Alocacao(int paciente, int tipo, int numero, double fracao) {}

    /** otima = false quando o limite de tempo parou o solver numa solução só viável. */
    public record Solucao(double custoTotal, double desperdicioMg, boolean otima,
                          List<FrascoAberto> abertos, List<Alocacao> alocacoes) {}

    private static final double EPS = 1e-6;
    private static final long LIMITE_MS = 60_000;

    static {
        Loader.loadNativeLibraries();
    }

    private SolverFracionamento() {
    }

    /**
     * @param doses d_i de cada paciente (índice = paciente)
     * @param tipos apresentações disponíveis (índice = tipo)
     * @throws IllegalArgumentException se o estoque total não cobre a demanda; o chamador
     *                                  checa isso antes (patente BR102024012896, [0029])
     */
    public static Solucao resolver(List<Double> doses, List<TipoFrasco> tipos) {
        double demanda = doses.stream().mapToDouble(Double::doubleValue).sum();
        double capacidade = tipos.stream().mapToDouble(t -> t.volumeMg() * t.estoque()).sum();
        if (capacidade + EPS < demanda) {
            throw new IllegalArgumentException("Estoque total (" + capacidade + " mg) menor que a demanda (" + demanda + " mg)");
        }

        MPSolver solver = MPSolver.createSolver("SCIP");
        try {
            solver.setTimeLimit(LIMITE_MS);
            int n = doses.size();
            MPVariable[][] y = new MPVariable[tipos.size()][];
            MPVariable[][][] x = new MPVariable[n][tipos.size()][];
            MPObjective custo = solver.objective();

            for (int j = 0; j < tipos.size(); j++) {
                TipoFrasco t = tipos.get(j);
                // Nunca é útil abrir mais que ceil(D / v_j) frascos de um tipo: corta variáveis sem mudar o ótimo.
                int kMax = (int) Math.min(t.estoque(), Math.ceil(demanda / t.volumeMg()));
                y[j] = new MPVariable[kMax];
                for (int k = 0; k < kMax; k++) {
                    y[j][k] = solver.makeBoolVar("y_" + j + "_" + k);
                    custo.setCoefficient(y[j][k], t.custo());
                    if (k > 0) {
                        // Quebra de simetria: frascos do mesmo tipo são idênticos, abre em ordem.
                        MPConstraint ordem = solver.makeConstraint(0, MPSolver.infinity());
                        ordem.setCoefficient(y[j][k - 1], 1);
                        ordem.setCoefficient(y[j][k], -1);
                    }
                }
                for (int i = 0; i < n; i++) {
                    x[i][j] = new MPVariable[kMax];
                    for (int k = 0; k < kMax; k++) {
                        x[i][j][k] = solver.makeNumVar(0, 1, "x_" + i + "_" + j + "_" + k);
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                MPConstraint dose = solver.makeConstraint(doses.get(i), doses.get(i));
                for (int j = 0; j < tipos.size(); j++) {
                    for (MPVariable xijk : x[i][j]) {
                        dose.setCoefficient(xijk, tipos.get(j).volumeMg());
                    }
                }
            }
            for (int j = 0; j < tipos.size(); j++) {
                for (int k = 0; k < y[j].length; k++) {
                    MPConstraint aberto = solver.makeConstraint(-MPSolver.infinity(), 0);
                    for (int i = 0; i < n; i++) {
                        aberto.setCoefficient(x[i][j][k], 1);
                    }
                    aberto.setCoefficient(y[j][k], -1);
                }
            }
            custo.setMinimization();

            MPSolver.ResultStatus status = solver.solve();
            if (status != MPSolver.ResultStatus.OPTIMAL && status != MPSolver.ResultStatus.FEASIBLE) {
                throw new IllegalStateException("Solver não encontrou solução: " + status);
            }

            List<FrascoAberto> abertos = new ArrayList<>();
            double volumeAberto = 0;
            for (int j = 0; j < tipos.size(); j++) {
                for (int k = 0; k < y[j].length; k++) {
                    if (y[j][k].solutionValue() > 0.5) {
                        abertos.add(new FrascoAberto(j, k));
                        volumeAberto += tipos.get(j).volumeMg();
                    }
                }
            }
            return new Solucao(custo.value(), volumeAberto - demanda,
                    status == MPSolver.ResultStatus.OPTIMAL, abertos, redistribuir(doses, tipos, abertos));
        } finally {
            solver.delete();
        }
    }

    /**
     * Pós-processamento: o custo depende só de quais frascos abrir (y); qualquer divisão das
     * doses entre esses frascos é igualmente ótima. O x do solver sai arbitrário (ex: 8 mg de
     * um frasco), então as doses são refeitas enchendo os frascos em sequência, maior dose e
     * maior frasco primeiro. Cada frasco é partido no máximo uma vez entre dois pacientes:
     * transferências ≤ pacientes + frascos − 1, e o resultado é determinístico.
     */
    static List<Alocacao> redistribuir(List<Double> doses, List<TipoFrasco> tipos, List<FrascoAberto> abertos) {
        List<Alocacao> alocacoes = new ArrayList<>();
        if (abertos.isEmpty()) {
            return alocacoes;
        }
        List<FrascoAberto> frascos = abertos.stream()
                .sorted(Comparator.comparingDouble((FrascoAberto f) -> -tipos.get(f.tipo()).volumeMg())
                        .thenComparingInt(FrascoAberto::tipo)
                        .thenComparingInt(FrascoAberto::numero))
                .toList();
        List<Integer> pacientes = IntStream.range(0, doses.size()).boxed()
                .sorted(Comparator.comparingDouble((Integer i) -> -doses.get(i)).thenComparingInt(i -> i))
                .toList();

        int f = 0;
        double restante = tipos.get(frascos.get(0).tipo()).volumeMg();
        for (int i : pacientes) {
            double falta = doses.get(i);
            while (falta > EPS && f < frascos.size()) {
                FrascoAberto frasco = frascos.get(f);
                double tira = Math.min(falta, restante);
                if (tira > EPS) {
                    alocacoes.add(new Alocacao(i, frasco.tipo(), frasco.numero(),
                            Math.min(1, tira / tipos.get(frasco.tipo()).volumeMg())));
                }
                falta -= tira;
                restante -= tira;
                if (restante <= EPS && ++f < frascos.size()) {
                    restante = tipos.get(frascos.get(f).tipo()).volumeMg();
                }
            }
        }
        return alocacoes;
    }

    /**
     * Linha de base "empírica": cada paciente recebe a combinação de frascos mais barata
     * só para ele, sem dividir frasco com ninguém. A economia do modelo é o ganho de dividir.
     */
    // ponytail: cada paciente vê o estoque cheio (não desconta o que o anterior usou); descontar se o baseline precisar respeitar estoque
    public static double custoSemCompartilhamento(List<Double> doses, List<TipoFrasco> tipos) {
        return doses.stream().mapToDouble(d -> resolver(List.of(d), tipos).custoTotal()).sum();
    }
}
