package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.PlanejadorAgenda.AplicacaoPlanejada;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.PlanejadorAgenda.Sugestao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.services.SolverFracionamento.TipoFrasco;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlanejadorAgendaTest {

    /** Paclitaxel do seed: 30 mg US$ 25, 100 mg US$ 70, 300 mg US$ 190. */
    private static final List<TipoFrasco> PACLITAXEL = List.of(
            new TipoFrasco(30, 25, 20), new TipoFrasco(100, 70, 20), new TipoFrasco(300, 190, 10));
    private static final List<Double> DOSES_27 = List.of(260.0, 175.0, 80.0, 135.0, 150.0, 210.0, 95.0, 120.0, 190.0);
    private static final LocalDate D26 = LocalDate.of(2026, 10, 26);
    private static final LocalDate D27 = LocalDate.of(2026, 10, 27);
    private static final LocalDate FIM = LocalDate.of(2026, 10, 30);

    /** Os 9 pacientes de 27/10, cada um no seu ciclo, sem flexibilidade (janela 0/0). */
    private static List<AplicacaoPlanejada> grupo27() {
        List<AplicacaoPlanejada> grupo = new ArrayList<>();
        for (int i = 0; i < DOSES_27.size(); i++) {
            grupo.add(new AplicacaoPlanejada(UUID.randomUUID(), UUID.randomUUID(), "CIC2026-00000" + (i + 1), 1,
                    DOSES_27.get(i), D27, D27, D27, 19, true));
        }
        return grupo;
    }

    /** CIC2026-000010 (175 mg, a cada 21 dias): nº 2 em 05/10 e nº 4 em 16/11 só como vizinhas da nº 3 (26/10). */
    private static List<AplicacaoPlanejada> ciclo10(int diasDepois, int intervaloMinimo) {
        UUID ciclo = UUID.randomUUID();
        LocalDate d05 = LocalDate.of(2026, 10, 5);
        LocalDate d16 = LocalDate.of(2026, 11, 16);
        return List.of(
                new AplicacaoPlanejada(UUID.randomUUID(), ciclo, "CIC2026-000010", 2, 175.0, d05, d05, d05, intervaloMinimo, false),
                new AplicacaoPlanejada(UUID.randomUUID(), ciclo, "CIC2026-000010", 3, 175.0, D26, D26, D26.plusDays(diasDepois),
                        intervaloMinimo, true),
                new AplicacaoPlanejada(UUID.randomUUID(), ciclo, "CIC2026-000010", 4, 175.0, d16, d16, d16, intervaloMinimo, false));
    }

    private static List<AplicacaoPlanejada> juntar(List<AplicacaoPlanejada> a, List<AplicacaoPlanejada> b) {
        List<AplicacaoPlanejada> todas = new ArrayList<>(a);
        todas.addAll(b);
        return todas;
    }

    /** Roda o planejador e confere as propriedades que valem sempre: ganho > 0, cada aplicação no máximo uma vez. */
    private static List<Sugestao> planejar(List<AplicacaoPlanejada> aplicacoes, Map<LocalDate, List<Double>> fixas,
                                           List<TipoFrasco> tipos, LocalDate inicio, LocalDate fim, Set<LocalDate> fechados) {
        List<Sugestao> sugestoes = PlanejadorAgenda.planejar(aplicacoes, fixas, tipos, inicio, fim, fechados);
        assertThat(sugestoes).allSatisfy(s -> assertThat(s.ganho()).isPositive());
        assertThat(sugestoes).extracting(Sugestao::aplicacaoId).doesNotHaveDuplicates();
        return sugestoes;
    }

    /** Exemplo da seção 1: 140 (sozinho em 26/10) + 925 (27/10) − 1020 (27/10 com ele) = US$ 45. */
    @Test
    void juntaOPacienteIsoladoAoGrupoDoDiaSeguinte() {
        List<Sugestao> s = planejar(juntar(grupo27(), ciclo10(1, 19)), Map.of(), PACLITAXEL, D26, FIM, Set.of());

        assertThat(s).hasSize(1);
        assertThat(s.get(0).codigoCiclo()).isEqualTo("CIC2026-000010");
        assertThat(s.get(0).numero()).isEqualTo(3);
        assertThat(s.get(0).origem()).isEqualTo(D26);
        assertThat(s.get(0).destino()).isEqualTo(D27);
        assertThat(s.get(0).ganho()).isCloseTo(45, within(1e-6));
    }

    @Test
    void janelaZeroNaoGeraSugestao() {
        assertThat(planejar(juntar(grupo27(), ciclo10(0, 19)), Map.of(), PACLITAXEL, D26, FIM, Set.of())).isEmpty();
    }

    /** Mínimo 21: ir para 27/10 deixaria 20 dias até a nº 4 (16/11). */
    @Test
    void respeitaIntervaloMinimoComAVizinha() {
        assertThat(planejar(juntar(grupo27(), ciclo10(1, 21)), Map.of(), PACLITAXEL, D26, FIM, Set.of())).isEmpty();
    }

    /** Preparo de 27/10 já confirmado: os frascos foram abertos, não há o que aproveitar. */
    @Test
    void diaFechadoNaoRecebePaciente() {
        assertThat(planejar(juntar(grupo27(), ciclo10(1, 19)), Map.of(), PACLITAXEL, D26, FIM, Set.of(D27))).isEmpty();
    }

    /** Estoque cobre os 1415 mg de 27/10 (1430 mg), mas não os 1590 mg com mais um paciente. */
    @Test
    void destinoSemEstoqueNaoRecebePaciente() {
        List<TipoFrasco> pouco = List.of(new TipoFrasco(30, 25, 1), new TipoFrasco(100, 70, 2), new TipoFrasco(300, 190, 4));

        assertThat(planejar(juntar(grupo27(), ciclo10(1, 19)), Map.of(), pouco, D26, FIM, Set.of())).isEmpty();
    }

    /** Pacientes do legado (sem aplicação) também dividem frasco: mesmo ganho com o grupo como dose fixa. */
    @Test
    void doseFixaDoLegadoEntraNoCustoDoDia() {
        List<Sugestao> s = planejar(ciclo10(1, 19), Map.of(D27, DOSES_27), PACLITAXEL, D26, FIM, Set.of());

        assertThat(s).hasSize(1);
        assertThat(s.get(0).ganho()).isCloseTo(45, within(1e-6));
    }

    /**
     * Ciclo semanal (mínimo 6): a nº 1 pode ir de 02/11 para 03/11 e a nº 2 de 09/11 para 08/11, cada uma
     * juntando-se a um grupo. Sozinhos os dois movimentos cabem; juntos deixariam só 5 dias entre elas.
     * Ganhos iguais: vence a primeira na ordem (nº 1) e a nº 2 é barrada pela data já simulada.
     */
    @Test
    void segundoMovimentoRespeitaADataJaSimuladaDaVizinha() {
        UUID ciclo = UUID.randomUUID();
        LocalDate d02 = LocalDate.of(2026, 11, 2);
        LocalDate d09 = LocalDate.of(2026, 11, 9);
        List<AplicacaoPlanejada> semanal = List.of(
                new AplicacaoPlanejada(UUID.randomUUID(), ciclo, "CIC2026-000020", 1, 175.0, d02, d02, d02.plusDays(1), 6, true),
                new AplicacaoPlanejada(UUID.randomUUID(), ciclo, "CIC2026-000020", 2, 175.0, d09, d09.minusDays(1), d09, 6, true));
        Map<LocalDate, List<Double>> grupos = Map.of(d02.plusDays(1), List.of(260.0, 175.0), d09.minusDays(1), List.of(260.0, 175.0));

        List<Sugestao> s = planejar(semanal, grupos, PACLITAXEL, d02, d09, Set.of());

        assertThat(s).hasSize(1);
        assertThat(s.get(0).numero()).isEqualTo(1);
        assertThat(s.get(0).destino()).isEqualTo(d02.plusDays(1));
        assertThat(ChronoUnit.DAYS.between(s.get(0).destino(), d09)).isGreaterThanOrEqualTo(6);
    }
}
