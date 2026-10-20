package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.CicloInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.ReagendamentoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CicloTest {

    private static final Medicamento PACLITAXEL = Medicamento.builder("Paclitaxel").build();
    private static final LocalDate INICIO = LocalDate.of(2026, 9, 14);

    private static Ciclo ciclo(int diasAntes, int diasDepois) {
        return Ciclo.novo("CIC2026-000010", PACLITAXEL, 175.0, INICIO, 6, 21, 19, diasAntes, diasDepois);
    }

    private static Aplicacao aplicacao(Ciclo ciclo, int numero) {
        return ciclo.getAplicacoes().get(numero - 1);
    }

    @Test
    void projetaAplicacoesACadaIntervalo() {
        Ciclo c = ciclo(0, 1);

        assertThat(c.getAplicacoes()).hasSize(6);
        assertThat(c.getAplicacoes()).extracting(Aplicacao::getNumero).containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(c.getAplicacoes()).extracting(Aplicacao::getDataPrevista).containsExactly(
                LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 26),
                LocalDate.of(2026, 11, 16), LocalDate.of(2026, 12, 7), LocalDate.of(2026, 12, 28));
        assertThat(c.getAplicacoes()).allSatisfy(a -> {
            assertThat(a.getDataAgendada()).isEqualTo(a.getDataPrevista());
            assertThat(a.getStatus()).isEqualTo(StatusAplicacao.AGENDADA);
            assertThat(a.getDiasAntes()).isZero();
            assertThat(a.getDiasDepois()).isEqualTo(1);
        });
    }

    @Test
    void recusaInvariantesQuebradas() {
        assertThatThrownBy(() -> Ciclo.novo("CIC-1", PACLITAXEL, 175.0, INICIO, 6, 21, 19, 0, 0))
                .isInstanceOf(CicloInvalidoException.class).hasMessageContaining("padrão");
        assertThatThrownBy(() -> Ciclo.novo("CIC2026-000010", null, 175.0, INICIO, 6, 21, 19, 0, 0))
                .isInstanceOf(CicloInvalidoException.class).hasMessageContaining("medicamento");
        assertThatThrownBy(() -> Ciclo.novo("CIC2026-000010", PACLITAXEL, 0.0, INICIO, 6, 21, 19, 0, 0))
                .isInstanceOf(CicloInvalidoException.class).hasMessageContaining("dose");
        assertThatThrownBy(() -> Ciclo.novo("CIC2026-000010", PACLITAXEL, 175.0, INICIO, 0, 21, 19, 0, 0))
                .isInstanceOf(CicloInvalidoException.class).hasMessageContaining("total");
        assertThatThrownBy(() -> Ciclo.novo("CIC2026-000010", PACLITAXEL, 175.0, INICIO, 101, 21, 19, 0, 0))
                .isInstanceOf(CicloInvalidoException.class).hasMessageContaining("total");
        assertThatThrownBy(() -> Ciclo.novo("CIC2026-000010", PACLITAXEL, 175.0, INICIO, 6, 0, 0, 0, 0))
                .isInstanceOf(CicloInvalidoException.class).hasMessageContaining("intervalo");
        assertThatThrownBy(() -> Ciclo.novo("CIC2026-000010", PACLITAXEL, 175.0, INICIO, 6, 21, 22, 0, 0))
                .isInstanceOf(CicloInvalidoException.class).hasMessageContaining("intervalo mínimo");
        assertThatThrownBy(() -> ciclo(-1, 0))
                .isInstanceOf(CicloInvalidoException.class).hasMessageContaining("negativa");
    }

    /** Exemplo da agenda: 3ª aplicação isolada em 26/10, janela +1 → pode ir para 27/10. */
    @Test
    void reagendaDentroDaJanela() {
        Ciclo c = ciclo(0, 1);

        c.reagendar(3, LocalDate.of(2026, 10, 27));

        assertThat(aplicacao(c, 3).getDataAgendada()).isEqualTo(LocalDate.of(2026, 10, 27));
        assertThat(aplicacao(c, 3).getDataPrevista()).isEqualTo(LocalDate.of(2026, 10, 26));
    }

    @Test
    void janelaZeroSoAceitaAPropriaData() {
        Ciclo c = ciclo(0, 0);

        c.reagendar(3, LocalDate.of(2026, 10, 26));
        assertThat(aplicacao(c, 3).getDataAgendada()).isEqualTo(LocalDate.of(2026, 10, 26));

        assertThatThrownBy(() -> c.reagendar(3, LocalDate.of(2026, 10, 27)))
                .isInstanceOf(ReagendamentoInvalidoException.class).hasMessageContaining("fora da janela");
        assertThatThrownBy(() -> c.reagendar(3, LocalDate.of(2026, 10, 25)))
                .isInstanceOf(ReagendamentoInvalidoException.class).hasMessageContaining("fora da janela");
    }

    /** Janela ±3, mínimo 19: ±2 dias cabe; ±3 deixa 18 dias de uma vizinha. */
    @Test
    void respeitaIntervaloMinimoComAsVizinhas() {
        Ciclo c = ciclo(3, 3);

        assertThatThrownBy(() -> c.reagendar(2, LocalDate.of(2026, 10, 2)))
                .isInstanceOf(ReagendamentoInvalidoException.class).hasMessageContaining("aplicação 1");
        assertThatThrownBy(() -> c.reagendar(2, LocalDate.of(2026, 10, 8)))
                .isInstanceOf(ReagendamentoInvalidoException.class).hasMessageContaining("aplicação 3");
        assertThat(aplicacao(c, 2).getDataAgendada()).isEqualTo(LocalDate.of(2026, 10, 5));

        c.reagendar(2, LocalDate.of(2026, 10, 7));
        assertThat(aplicacao(c, 2).getDataAgendada()).isEqualTo(LocalDate.of(2026, 10, 7));

        // A 3ª agora mede contra a 2ª já remarcada (07/10): antecipar 2 dias deixaria 17 dias.
        assertThatThrownBy(() -> c.reagendar(3, LocalDate.of(2026, 10, 24)))
                .isInstanceOf(ReagendamentoInvalidoException.class).hasMessageContaining("aplicação 2");
    }

    @Test
    void definirJanelaValidaENaoDeixaAgendadaDeFora() {
        Ciclo c = ciclo(0, 1);
        Aplicacao terceira = aplicacao(c, 3);

        terceira.definirJanela(2, 2);
        assertThat(terceira.inicioJanela()).isEqualTo(LocalDate.of(2026, 10, 24));
        assertThat(terceira.fimJanela()).isEqualTo(LocalDate.of(2026, 10, 28));

        assertThatThrownBy(() -> terceira.definirJanela(0, -1))
                .isInstanceOf(CicloInvalidoException.class).hasMessageContaining("negativa");

        c.reagendar(3, LocalDate.of(2026, 10, 27));
        assertThatThrownBy(() -> terceira.definirJanela(0, 0))
                .isInstanceOf(CicloInvalidoException.class).hasMessageContaining("fora da nova janela");
        assertThat(terceira.getDiasDepois()).isEqualTo(2);
    }

    /** Preparo confirmado: a aplicação vira histórico — não remarca, não muda janela, ciclo não se apaga. */
    @Test
    void aplicacaoRealizadaSaiDaAgenda() {
        Ciclo c = ciclo(0, 1);
        Aplicacao terceira = aplicacao(c, 3);

        terceira.marcarRealizada();
        terceira.marcarRealizada();

        assertThat(terceira.getStatus()).isEqualTo(StatusAplicacao.REALIZADA);
        assertThat(c.possuiAplicacaoEncerrada()).isTrue();
        assertThatThrownBy(() -> c.reagendar(3, LocalDate.of(2026, 10, 27)))
                .isInstanceOf(ReagendamentoInvalidoException.class).hasMessageContaining("REALIZADA");
        assertThatThrownBy(() -> terceira.definirJanela(1, 1))
                .isInstanceOf(CicloInvalidoException.class).hasMessageContaining("REALIZADA");
    }
}
