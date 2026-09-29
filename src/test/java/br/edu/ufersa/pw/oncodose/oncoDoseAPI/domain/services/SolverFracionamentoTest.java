package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.SolverFracionamento.Alocacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.SolverFracionamento.FrascoAberto;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.SolverFracionamento.Solucao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.SolverFracionamento.TipoFrasco;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SolverFracionamentoTest {

    /** Vetores completos do Fluorouracil em Gê et al. (2023). */
    @Test
    void fluorouracilDoArtigo() {
        List<Double> d = List.of(300.0, 570.0, 490.0, 1270.0);
        List<TipoFrasco> tipos = List.of(
                new TipoFrasco(250, 74, 20), new TipoFrasco(500, 126, 30), new TipoFrasco(1000, 400, 50));

        Solucao s = SolverFracionamento.resolver(d, tipos);

        // 2630 mg: 5×500 + 1×250 = 2750 mg por US$ 704, 120 mg de descarte.
        assertThat(s.otima()).isTrue();
        assertThat(s.custoTotal()).isCloseTo(704, within(1e-6));
        assertThat(s.desperdicioMg()).isCloseTo(120, within(1e-6));
        assertSolucaoValida(s, d, tipos);

        // Sem dividir frasco: 300→500 (126), 570→500+250 (200), 490→500 (126), 1270→3×500 (378).
        assertThat(SolverFracionamento.custoSemCompartilhamento(d, tipos)).isCloseTo(830, within(1e-6));
    }

    /** Gencitabina 08/11 do artigo: 3488 mg com frascos de 200 mg (US$ 16) e 1 g (US$ 48). */
    @Test
    void gencitabinaDoArtigo() {
        List<Double> d = List.of(3488.0);
        List<TipoFrasco> tipos = List.of(new TipoFrasco(200, 16, 20), new TipoFrasco(1000, 48, 20));

        Solucao s = SolverFracionamento.resolver(d, tipos);

        assertThat(s.custoTotal()).isCloseTo(192, within(1e-6));
        assertSolucaoValida(s, d, tipos);
    }

    /**
     * Gabarito independente: como qualquer paciente pode usar qualquer frasco aberto, o ótimo
     * é o do problema compacto min Σ c_j·n_j com Σ v_j·n_j ≥ D, resolvido aqui por força bruta.
     */
    @Test
    void bateComFormulacaoCompacta() {
        List<Double> d = List.of(45.0, 120.0, 260.0, 180.0, 75.0);
        List<TipoFrasco> tipos = List.of(
                new TipoFrasco(20, 5, 10), new TipoFrasco(80, 17, 10), new TipoFrasco(150, 30, 5));

        Solucao s = SolverFracionamento.resolver(d, tipos);

        assertThat(s.custoTotal()).isCloseTo(otimoCompacto(d, tipos), within(1e-6));
        assertSolucaoValida(s, d, tipos);
    }

    @Test
    void recusaEstoqueMenorQueDemanda() {
        assertThatThrownBy(() -> SolverFracionamento.resolver(
                List.of(600.0), List.of(new TipoFrasco(250, 74, 2))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** Dose exata por paciente, frasco nunca usado além de 100% e só frascos abertos. */
    private static void assertSolucaoValida(Solucao s, List<Double> d, List<TipoFrasco> tipos) {
        double[] recebido = new double[d.size()];
        Map<FrascoAberto, Double> usoPorFrasco = new HashMap<>();
        for (Alocacao a : s.alocacoes()) {
            recebido[a.paciente()] += a.fracao() * tipos.get(a.tipo()).volumeMg();
            usoPorFrasco.merge(new FrascoAberto(a.tipo(), a.numero()), a.fracao(), Double::sum);
        }
        for (int i = 0; i < d.size(); i++) {
            assertThat(recebido[i]).as("dose do paciente %d", i).isCloseTo(d.get(i), within(1e-4));
        }
        usoPorFrasco.forEach((frasco, uso) -> {
            assertThat(s.abertos()).contains(frasco);
            assertThat(uso).isLessThanOrEqualTo(1 + 1e-6);
        });
    }

    private static double otimoCompacto(List<Double> d, List<TipoFrasco> tipos) {
        double demanda = d.stream().mapToDouble(Double::doubleValue).sum();
        double melhor = Double.MAX_VALUE;
        for (int a = 0; a <= tipos.get(0).estoque(); a++) {
            for (int b = 0; b <= tipos.get(1).estoque(); b++) {
                for (int c = 0; c <= tipos.get(2).estoque(); c++) {
                    double volume = a * tipos.get(0).volumeMg() + b * tipos.get(1).volumeMg() + c * tipos.get(2).volumeMg();
                    if (volume + 1e-9 >= demanda) {
                        melhor = Math.min(melhor, a * tipos.get(0).custo() + b * tipos.get(1).custo() + c * tipos.get(2).custo());
                    }
                }
            }
        }
        return melhor;
    }
}
