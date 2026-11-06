package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.ProtocoloInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.ReagendamentoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.ProtocoloPaciente.MedicamentoNovo;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProtocoloPacienteTest {

    private static final Medicamento PACLITAXEL = Medicamento.builder("Paclitaxel").build();
    private static final Medicamento DOXORRUBICINA = Medicamento.builder("Doxorrubicina").build();
    private static final Medicamento CICLOFOSFAMIDA = Medicamento.builder("Ciclofosfamida").build();
    private static final LocalDate INICIO = LocalDate.of(2026, 9, 14);
    private static final String CODIGO = "PTC2026-000010";

    private static List<MedicamentoNovo> paclitaxel(String dias) {
        return List.of(new MedicamentoNovo(PACLITAXEL, 175.0, "IV", dias));
    }

    /** Esquema simples: D1 a cada 21 dias, 6 ciclos, mínimo de 19 dias. */
    private static ProtocoloPaciente protocolo(int diasAntes, int diasDepois) {
        return ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 6, 21, 19, diasAntes, diasDepois, paclitaxel("D1"));
    }

    private static Aplicacao aplicacao(ProtocoloPaciente protocolo, int numero) {
        return protocolo.getAplicacoes().get(numero - 1);
    }

    @Test
    void projetaAplicacoesACadaIntervalo() {
        ProtocoloPaciente p = protocolo(0, 1);

        assertThat(p.getAplicacoes()).hasSize(6);
        assertThat(p.getAplicacoes()).extracting(Aplicacao::getNumero).containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(p.getAplicacoes()).extracting(Aplicacao::getCicloNumero).containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(p.getAplicacoes()).extracting(Aplicacao::getDataPrevista).containsExactly(
                LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 26),
                LocalDate.of(2026, 11, 16), LocalDate.of(2026, 12, 7), LocalDate.of(2026, 12, 28));
        assertThat(p.getAplicacoes()).allSatisfy(a -> {
            assertThat(a.getDiaCiclo()).isEqualTo(1);
            assertThat(a.getDataAgendada()).isEqualTo(a.getDataPrevista());
            assertThat(a.getStatus()).isEqualTo(StatusAplicacao.AGENDADA);
            assertThat(a.getDiasAntes()).isZero();
            assertThat(a.getDiasDepois()).isEqualTo(1);
        });
    }

    /** Formato do TAX W no Tasy: 1 ciclo com 12 aplicações semanais, D1 a D78. */
    @Test
    void umCicloComVariosDiasDeAplicacao() {
        ProtocoloPaciente p = ProtocoloPaciente.novo(CODIGO, "TAX W", "Sistema Reprodutivo Feminino", INICIO, 1, 7, 5, 0, 0,
                paclitaxel("D1,D8,D15,D22,D29,D36,D43,D50,D57,D64,D71,D78"));

        assertThat(p.totalAplicacoes()).isEqualTo(12);
        assertThat(p.getAplicacoes()).extracting(Aplicacao::getCicloNumero).containsOnly(1);
        assertThat(aplicacao(p, 2).getDiaCiclo()).isEqualTo(8);
        assertThat(aplicacao(p, 2).getDataPrevista()).isEqualTo(INICIO.plusDays(7));
        assertThat(aplicacao(p, 12).getDiaCiclo()).isEqualTo(78);
        assertThat(aplicacao(p, 12).getDataPrevista()).isEqualTo(INICIO.plusDays(77));
    }

    /** Formato do AC no Tasy: 4 ciclos × D1 a cada 21 dias, dois quimioterápicos no mesmo dia. */
    @Test
    void doisMedicamentosNoMesmoDiaGeramUmaAplicacaoSo() {
        ProtocoloPaciente p = ProtocoloPaciente.novo(CODIGO, "AC", null, INICIO, 4, 21, 19, 0, 0, List.of(
                new MedicamentoNovo(DOXORRUBICINA, 90.0, "IV", "D1"),
                new MedicamentoNovo(CICLOFOSFAMIDA, 900.0, "IV", "D1")));

        assertThat(p.totalAplicacoes()).isEqualTo(4);
        assertThat(p.getMedicamentos()).hasSize(2);
        assertThat(aplicacao(p, 2).getDataPrevista()).isEqualTo(INICIO.plusDays(21));
        assertThat(aplicacao(p, 1).preve(DOXORRUBICINA)).isTrue();
        assertThat(aplicacao(p, 1).preve(CICLOFOSFAMIDA)).isTrue();
    }

    /** D1 e D8 com pausa: o ciclo seguinte começa em D29, e no D8 só um dos medicamentos é aplicado. */
    @Test
    void diasDiferentesPorMedicamentoComPausaEntreCiclos() {
        Medicamento salvoA = Medicamento.builder("A").build();
        ProtocoloPaciente p = ProtocoloPaciente.novo(CODIGO, "GEM", null, INICIO, 2, 28, 5, 0, 0, List.of(
                new MedicamentoNovo(salvoA, 100.0, "IV", "D1,D8"),
                new MedicamentoNovo(PACLITAXEL, 50.0, "IV", "D1")));

        assertThat(p.getAplicacoes()).extracting(Aplicacao::getDiaCiclo).containsExactly(1, 8, 1, 8);
        assertThat(p.getAplicacoes()).extracting(Aplicacao::getCicloNumero).containsExactly(1, 1, 2, 2);
        assertThat(p.getAplicacoes()).extracting(Aplicacao::getDataPrevista).containsExactly(
                INICIO, INICIO.plusDays(7), INICIO.plusDays(28), INICIO.plusDays(35));
    }

    @Test
    void recusaInvariantesQuebradas() {
        assertThatThrownBy(() -> ProtocoloPaciente.novo("PTC-1", "TAX", null, INICIO, 6, 21, 19, 0, 0, paclitaxel("D1")))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("padrão");
        assertThatThrownBy(() -> ProtocoloPaciente.novo(CODIGO, " ", null, INICIO, 6, 21, 19, 0, 0, paclitaxel("D1")))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("subtipo");
        assertThatThrownBy(() -> ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 6, 21, 19, 0, 0, List.of()))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("medicamento");
        assertThatThrownBy(() -> ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 6, 21, 19, 0, 0,
                List.of(new MedicamentoNovo(PACLITAXEL, 0.0, "IV", "D1"))))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("dose");
        assertThatThrownBy(() -> ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 6, 21, 19, 0, 0,
                List.of(new MedicamentoNovo(PACLITAXEL, 175.0, " ", "D1"))))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("via");
        assertThatThrownBy(() -> ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 6, 21, 19, 0, 0, paclitaxel("1,8")))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("dias de aplicação");
        assertThatThrownBy(() -> ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 6, 21, 19, 0, 0, List.of(
                new MedicamentoNovo(PACLITAXEL, 175.0, "IV", "D1"), new MedicamentoNovo(PACLITAXEL, 80.0, "IV", "D8"))))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("repetido");
        assertThatThrownBy(() -> ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 0, 21, 19, 0, 0, paclitaxel("D1")))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("ciclo");
        assertThatThrownBy(() -> ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 101, 21, 19, 0, 0, paclitaxel("D1")))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("total");
        assertThatThrownBy(() -> ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 6, 0, 0, 0, 0, paclitaxel("D1")))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("intervalo");
        assertThatThrownBy(() -> protocolo(-1, 0))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("negativa");
    }

    /** D8 num intervalo de 7 dias cairia junto com o D1 do ciclo seguinte. */
    @Test
    void recusaCiclosSobrepostos() {
        assertThatThrownBy(() -> ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 2, 7, 1, 0, 0, paclitaxel("D1,D8")))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("se sobreporiam");
    }

    /** O mínimo se mede pela menor distância real: 7 dias no D1/D8, mesmo com intervalo de 28. */
    @Test
    void intervaloMinimoNaoPassaDaMenorDistancia() {
        assertThatThrownBy(() -> ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 2, 28, 8, 0, 0, paclitaxel("D1,D8")))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("intervalo mínimo");
        assertThat(ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 2, 28, 7, 0, 0, paclitaxel("D1,D8"))
                .getIntervaloMinimoDias()).isEqualTo(7);
        assertThatThrownBy(() -> ProtocoloPaciente.novo(CODIGO, "TAX", null, INICIO, 6, 21, 22, 0, 0, paclitaxel("D1")))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("intervalo mínimo");
    }

    /** Exemplo da agenda: 3ª aplicação isolada em 26/10, janela +1 → pode ir para 27/10. */
    @Test
    void reagendaDentroDaJanela() {
        ProtocoloPaciente p = protocolo(0, 1);

        p.reagendar(3, LocalDate.of(2026, 10, 27));

        assertThat(aplicacao(p, 3).getDataAgendada()).isEqualTo(LocalDate.of(2026, 10, 27));
        assertThat(aplicacao(p, 3).getDataPrevista()).isEqualTo(LocalDate.of(2026, 10, 26));
    }

    @Test
    void janelaZeroSoAceitaAPropriaData() {
        ProtocoloPaciente p = protocolo(0, 0);

        p.reagendar(3, LocalDate.of(2026, 10, 26));
        assertThat(aplicacao(p, 3).getDataAgendada()).isEqualTo(LocalDate.of(2026, 10, 26));

        assertThatThrownBy(() -> p.reagendar(3, LocalDate.of(2026, 10, 27)))
                .isInstanceOf(ReagendamentoInvalidoException.class).hasMessageContaining("fora da janela");
        assertThatThrownBy(() -> p.reagendar(3, LocalDate.of(2026, 10, 25)))
                .isInstanceOf(ReagendamentoInvalidoException.class).hasMessageContaining("fora da janela");
    }

    /** Janela ±3, mínimo 19: ±2 dias cabe; ±3 deixa 18 dias de uma vizinha. */
    @Test
    void respeitaIntervaloMinimoComAsVizinhas() {
        ProtocoloPaciente p = protocolo(3, 3);

        assertThatThrownBy(() -> p.reagendar(2, LocalDate.of(2026, 10, 2)))
                .isInstanceOf(ReagendamentoInvalidoException.class).hasMessageContaining("aplicação 1");
        assertThatThrownBy(() -> p.reagendar(2, LocalDate.of(2026, 10, 8)))
                .isInstanceOf(ReagendamentoInvalidoException.class).hasMessageContaining("aplicação 3");
        assertThat(aplicacao(p, 2).getDataAgendada()).isEqualTo(LocalDate.of(2026, 10, 5));

        p.reagendar(2, LocalDate.of(2026, 10, 7));
        assertThat(aplicacao(p, 2).getDataAgendada()).isEqualTo(LocalDate.of(2026, 10, 7));

        // A 3ª agora mede contra a 2ª já remarcada (07/10): antecipar 2 dias deixaria 17 dias.
        assertThatThrownBy(() -> p.reagendar(3, LocalDate.of(2026, 10, 24)))
                .isInstanceOf(ReagendamentoInvalidoException.class).hasMessageContaining("aplicação 2");
    }

    @Test
    void definirJanelaValidaENaoDeixaAgendadaDeFora() {
        ProtocoloPaciente p = protocolo(0, 1);
        Aplicacao terceira = aplicacao(p, 3);

        terceira.definirJanela(2, 2);
        assertThat(terceira.inicioJanela()).isEqualTo(LocalDate.of(2026, 10, 24));
        assertThat(terceira.fimJanela()).isEqualTo(LocalDate.of(2026, 10, 28));

        assertThatThrownBy(() -> terceira.definirJanela(0, -1))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("negativa");

        p.reagendar(3, LocalDate.of(2026, 10, 27));
        assertThatThrownBy(() -> terceira.definirJanela(0, 0))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("fora da nova janela");
        assertThat(terceira.getDiasDepois()).isEqualTo(2);
    }

    /** Preparo confirmado: a aplicação vira histórico — não remarca, não muda janela, protocolo não se apaga. */
    @Test
    void aplicacaoRealizadaSaiDaAgenda() {
        ProtocoloPaciente p = protocolo(0, 1);
        Aplicacao terceira = aplicacao(p, 3);

        terceira.marcarRealizada();
        terceira.marcarRealizada();

        assertThat(terceira.getStatus()).isEqualTo(StatusAplicacao.REALIZADA);
        assertThat(terceira.getDataReal()).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(p.possuiAplicacaoEncerrada()).isTrue();
        assertThatThrownBy(() -> p.reagendar(3, LocalDate.of(2026, 10, 27)))
                .isInstanceOf(ReagendamentoInvalidoException.class).hasMessageContaining("REALIZADA");
        assertThatThrownBy(() -> terceira.definirJanela(1, 1))
                .isInstanceOf(ProtocoloInvalidoException.class).hasMessageContaining("REALIZADA");
    }
}
