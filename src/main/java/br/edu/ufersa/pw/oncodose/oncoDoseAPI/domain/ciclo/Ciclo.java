package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.CicloInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.ReagendamentoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Entidade rica: ciclo de tratamento anonimizado (LGPD). Nasce por {@link #novo}, que já projeta
 * as aplicações a cada intervaloDias; remarcar passa por {@link #reagendar}, que guarda a janela
 * autorizada e o intervalo mínimo entre aplicações.
 */
@Getter
@Entity
@Table(name = "ciclo")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ciclo {
    /** Mesmo espírito do PRE0000-000000 da prescrição, ex: CIC2026-000001. */
    public static final String PADRAO_CODIGO = "^CIC\\d{4}-\\d{6}$";
    public static final int MAX_APLICACOES = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_ciclo", nullable = false, unique = true, length = 14)
    private String codigoCiclo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "dose_mg", nullable = false)
    private Double doseMg;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "total_aplicacoes", nullable = false)
    private Integer totalAplicacoes;

    /** Período do esquema (ex: 21 em "a cada 21 dias"): projeta as datas previstas. */
    @Column(name = "intervalo_dias", nullable = false)
    private Integer intervaloDias;

    /** Menor distância permitida entre aplicações vizinhas ao remarcar (ex: 19). */
    @Column(name = "intervalo_minimo_dias", nullable = false)
    private Integer intervaloMinimoDias;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // BatchSize: a listagem paginada carrega as aplicações de vários ciclos numa consulta só (sem N+1).
    @BatchSize(size = 50)
    @OrderBy("numero")
    @OneToMany(mappedBy = "ciclo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Aplicacao> aplicacoes = new ArrayList<>();

    // ponytail: só esquema regular (D1 a cada N dias); D1/D8 vira dois ciclos ou offsets por ciclo no futuro
    public static Ciclo novo(String codigoCiclo, Medicamento medicamento, Double doseMg, LocalDate dataInicio,
                             Integer totalAplicacoes, Integer intervaloDias, Integer intervaloMinimoDias,
                             int diasAntes, int diasDepois) {
        if (codigoCiclo == null || !codigoCiclo.matches(PADRAO_CODIGO)) {
            throw new CicloInvalidoException(codigoCiclo, "código fora do padrão CIC0000-000000");
        }
        if (medicamento == null) {
            throw new CicloInvalidoException(codigoCiclo, "medicamento obrigatório");
        }
        if (doseMg == null || doseMg <= 0) {
            throw new CicloInvalidoException(codigoCiclo, "dose deve ser maior que zero");
        }
        if (dataInicio == null) {
            throw new CicloInvalidoException(codigoCiclo, "data de início obrigatória");
        }
        if (totalAplicacoes == null || totalAplicacoes < 1 || totalAplicacoes > MAX_APLICACOES) {
            throw new CicloInvalidoException(codigoCiclo, "total de aplicações deve estar entre 1 e " + MAX_APLICACOES);
        }
        if (intervaloDias == null || intervaloDias < 1) {
            throw new CicloInvalidoException(codigoCiclo, "intervalo deve ser de pelo menos 1 dia");
        }
        if (intervaloMinimoDias == null || intervaloMinimoDias < 0 || intervaloMinimoDias > intervaloDias) {
            throw new CicloInvalidoException(codigoCiclo, "intervalo mínimo deve estar entre 0 e o intervalo");
        }
        Ciclo ciclo = new Ciclo();
        ciclo.codigoCiclo = codigoCiclo;
        ciclo.medicamento = medicamento;
        ciclo.doseMg = doseMg;
        ciclo.dataInicio = dataInicio;
        ciclo.totalAplicacoes = totalAplicacoes;
        ciclo.intervaloDias = intervaloDias;
        ciclo.intervaloMinimoDias = intervaloMinimoDias;
        for (int n = 1; n <= totalAplicacoes; n++) {
            LocalDate prevista = dataInicio.plusDays((long) (n - 1) * intervaloDias);
            ciclo.aplicacoes.add(new Aplicacao(ciclo, n, prevista, diasAntes, diasDepois));
        }
        return ciclo;
    }

    /** O que já aconteceu (ou foi cancelado) é histórico: ciclo assim não se apaga. */
    public boolean possuiAplicacaoEncerrada() {
        return aplicacoes.stream().anyMatch(a -> a.getStatus() != StatusAplicacao.AGENDADA);
    }

    /**
     * Move a aplicação para novaData se couber na janela autorizada e não ficar a menos de
     * intervaloMinimoDias das vizinhas (canceladas não contam). Usado pelo aceite de sugestão.
     */
    public void reagendar(int numero, LocalDate novaData) {
        Aplicacao alvo = aplicacoes.stream()
                .filter(a -> a.getNumero() == numero)
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aplicação", codigoCiclo + " nº " + numero));
        if (alvo.getStatus() != StatusAplicacao.AGENDADA) {
            throw new ReagendamentoInvalidoException(codigoCiclo, numero, novaData, "aplicação já está " + alvo.getStatus());
        }
        if (!alvo.janelaPermite(novaData)) {
            throw new ReagendamentoInvalidoException(codigoCiclo, numero, novaData,
                    "fora da janela " + alvo.inicioJanela() + " a " + alvo.fimJanela());
        }
        Optional<Aplicacao> anterior = vizinhas(numero).filter(a -> a.getNumero() < numero)
                .max(Comparator.comparing(Aplicacao::getNumero));
        if (anterior.isPresent()
                && ChronoUnit.DAYS.between(anterior.get().getDataAgendada(), novaData) < intervaloMinimoDias) {
            throw new ReagendamentoInvalidoException(codigoCiclo, numero, novaData,
                    "menos de " + intervaloMinimoDias + " dias da aplicação " + anterior.get().getNumero());
        }
        Optional<Aplicacao> seguinte = vizinhas(numero).filter(a -> a.getNumero() > numero)
                .min(Comparator.comparing(Aplicacao::getNumero));
        if (seguinte.isPresent()
                && ChronoUnit.DAYS.between(novaData, seguinte.get().getDataAgendada()) < intervaloMinimoDias) {
            throw new ReagendamentoInvalidoException(codigoCiclo, numero, novaData,
                    "menos de " + intervaloMinimoDias + " dias da aplicação " + seguinte.get().getNumero());
        }
        alvo.agendarPara(novaData);
    }

    private Stream<Aplicacao> vizinhas(int numero) {
        return aplicacoes.stream().filter(a -> a.getNumero() != numero && a.getStatus() != StatusAplicacao.CANCELADA);
    }
}
