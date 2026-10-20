package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.CicloInvalidoException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Uma aplicação projetada do ciclo. Nasce só por {@link Ciclo#novo}; a data agendada só muda
 * por {@link Ciclo#reagendar}, que conhece as vizinhas.
 */
@Getter
@Entity
@Table(name = "aplicacao")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Aplicacao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciclo_id", nullable = false)
    private Ciclo ciclo;

    @Column(nullable = false)
    private Integer numero;

    @Column(name = "data_prevista", nullable = false)
    private LocalDate dataPrevista;

    @Column(name = "data_agendada", nullable = false)
    private LocalDate dataAgendada;

    @Column(name = "dias_antes", nullable = false)
    private Integer diasAntes;

    @Column(name = "dias_depois", nullable = false)
    private Integer diasDepois;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAplicacao status = StatusAplicacao.AGENDADA;

    Aplicacao(Ciclo ciclo, int numero, LocalDate dataPrevista, int diasAntes, int diasDepois) {
        this.ciclo = ciclo;
        this.numero = numero;
        this.dataPrevista = dataPrevista;
        this.dataAgendada = dataPrevista;
        validarJanela(diasAntes, diasDepois);
        this.diasAntes = diasAntes;
        this.diasDepois = diasDepois;
    }

    /**
     * Autorização médica de quanto a aplicação pode antecipar/adiar em torno da data prevista.
     * "Só adiar" é diasAntes = 0; "sem flexibilidade" é 0/0.
     */
    public void definirJanela(int diasAntes, int diasDepois) {
        if (status != StatusAplicacao.AGENDADA) {
            throw new CicloInvalidoException(ciclo.getCodigoCiclo(), "aplicação " + numero + " já está " + status);
        }
        validarJanela(diasAntes, diasDepois);
        if (dataAgendada.isBefore(dataPrevista.minusDays(diasAntes)) || dataAgendada.isAfter(dataPrevista.plusDays(diasDepois))) {
            throw new CicloInvalidoException(ciclo.getCodigoCiclo(),
                    "aplicação " + numero + " já está agendada para " + dataAgendada + ", fora da nova janela");
        }
        this.diasAntes = diasAntes;
        this.diasDepois = diasDepois;
    }

    /** Preparo confirmado. Idempotente: confirmar de novo não muda nada. */
    public void marcarRealizada() {
        if (status == StatusAplicacao.CANCELADA) {
            throw new CicloInvalidoException(ciclo.getCodigoCiclo(), "aplicação " + numero + " está cancelada");
        }
        this.status = StatusAplicacao.REALIZADA;
    }

    public LocalDate inicioJanela() {
        return dataPrevista.minusDays(diasAntes);
    }

    public LocalDate fimJanela() {
        return dataPrevista.plusDays(diasDepois);
    }

    boolean janelaPermite(LocalDate data) {
        return !data.isBefore(inicioJanela()) && !data.isAfter(fimJanela());
    }

    void agendarPara(LocalDate data) {
        this.dataAgendada = data;
    }

    private void validarJanela(int diasAntes, int diasDepois) {
        if (diasAntes < 0 || diasDepois < 0) {
            throw new CicloInvalidoException(ciclo.getCodigoCiclo(), "janela não pode ser negativa");
        }
    }
}
