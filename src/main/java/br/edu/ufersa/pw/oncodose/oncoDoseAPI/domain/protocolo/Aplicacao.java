package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.ProtocoloInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Uma aplicação projetada do protocolo, como uma linha da tela Ciclos do Tasy. Nasce só por
 * {@link ProtocoloPaciente#novo}; a data agendada só muda por {@link ProtocoloPaciente#reagendar},
 * que conhece as vizinhas.
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
    @JoinColumn(name = "protocolo_id", nullable = false)
    private ProtocoloPaciente protocolo;

    /** Posição na sequência de todas as aplicações do protocolo: define as vizinhas. */
    @Column(nullable = false)
    private Integer numero;

    @Column(name = "ciclo_numero", nullable = false)
    private Integer cicloNumero;

    /** "Dia ciclo" do Tasy: 8 em D8. */
    @Column(name = "dia_ciclo", nullable = false)
    private Integer diaCiclo;

    @Column(name = "data_prevista", nullable = false)
    private LocalDate dataPrevista;

    @Column(name = "data_agendada", nullable = false)
    private LocalDate dataAgendada;

    @Column(name = "data_real")
    private LocalDate dataReal;

    @Column(name = "dias_antes", nullable = false)
    private Integer diasAntes;

    @Column(name = "dias_depois", nullable = false)
    private Integer diasDepois;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAplicacao status = StatusAplicacao.AGENDADA;

    Aplicacao(ProtocoloPaciente protocolo, int numero, int cicloNumero, int diaCiclo, LocalDate dataPrevista,
              int diasAntes, int diasDepois) {
        this.protocolo = protocolo;
        this.numero = numero;
        this.cicloNumero = cicloNumero;
        this.diaCiclo = diaCiclo;
        this.dataPrevista = dataPrevista;
        this.dataAgendada = dataPrevista;
        validarJanela(diasAntes, diasDepois);
        this.diasAntes = diasAntes;
        this.diasDepois = diasDepois;
    }

    /** O medicamento faz parte do protocolo e é aplicado neste dia do ciclo. */
    public boolean preve(Medicamento medicamento) {
        return protocolo.getMedicamentos().stream()
                .anyMatch(m -> Objects.equals(m.getMedicamento().getId(), medicamento.getId())
                        && m.aplicaNoDia(diaCiclo));
    }

    /**
     * Autorização médica de quanto a aplicação pode antecipar/adiar em torno da data prevista.
     * "Só adiar" é diasAntes = 0; "sem flexibilidade" é 0/0.
     */
    public void definirJanela(int diasAntes, int diasDepois) {
        if (status != StatusAplicacao.AGENDADA) {
            throw new ProtocoloInvalidoException(protocolo.getCodigoProtocolo(), "aplicação " + numero + " já está " + status);
        }
        validarJanela(diasAntes, diasDepois);
        if (dataAgendada.isBefore(dataPrevista.minusDays(diasAntes)) || dataAgendada.isAfter(dataPrevista.plusDays(diasDepois))) {
            throw new ProtocoloInvalidoException(protocolo.getCodigoProtocolo(),
                    "aplicação " + numero + " já está agendada para " + dataAgendada + ", fora da nova janela");
        }
        this.diasAntes = diasAntes;
        this.diasDepois = diasDepois;
    }

    /** Preparo confirmado. Idempotente: confirmar de novo não muda nada. */
    public void marcarRealizada() {
        if (status == StatusAplicacao.CANCELADA) {
            throw new ProtocoloInvalidoException(protocolo.getCodigoProtocolo(), "aplicação " + numero + " está cancelada");
        }
        if (status != StatusAplicacao.REALIZADA) {
            this.status = StatusAplicacao.REALIZADA;
            this.dataReal = dataAgendada;
        }
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
            throw new ProtocoloInvalidoException(protocolo.getCodigoProtocolo(), "janela não pode ser negativa");
        }
    }
}
