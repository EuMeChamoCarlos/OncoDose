package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.agenda;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.EstadoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.protocolo.Aplicacao;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Sugestão de remarcar uma aplicação, sempre decidida por um profissional (aceite ou recusa registrados com
 * quem, quando e motivo). AGENDA: juntar pacientes do mesmo medicamento, dentro da janela autorizada.
 * NAO_APTO: paciente não apto que volta a partir de uma data mínima; a janela não se aplica, só o
 * intervalo mínimo com as aplicações vizinhas.
 */
@Getter
@Entity
@Table(name = "sugestao_remarcacao")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SugestaoRemarcacao {

    public enum Tipo { AGENDA, NAO_APTO }

    public enum Status { SUGERIDA, ACEITA, RECUSADA, EXPIRADA }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacao_id", nullable = false)
    private Aplicacao aplicacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    @Column(name = "data_origem", nullable = false)
    private LocalDate dataOrigem;

    @Column(name = "data_destino", nullable = false)
    private LocalDate dataDestino;

    @Column(name = "economia_estimada", nullable = false, precision = 19, scale = 4)
    private BigDecimal economiaEstimada;

    /** Por que o paciente precisa remarcar (só NAO_APTO), ex: "imunidade baixa". */
    @Column(name = "motivo_remarcacao")
    private String motivoRemarcacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.SUGERIDA;

    @Column(name = "gerada_em", nullable = false)
    private LocalDateTime geradaEm = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    @Column(name = "decidida_em")
    private LocalDateTime decididaEm;

    /** Login de quem aceitou ou recusou. */
    @Column(name = "decidida_por")
    private String decididaPor;

    @Column(name = "motivo_decisao", length = 500)
    private String motivoDecisao;

    public static SugestaoRemarcacao daAgenda(Aplicacao aplicacao, LocalDate destino, BigDecimal economia) {
        return nova(aplicacao, Tipo.AGENDA, destino, economia, null);
    }

    public static SugestaoRemarcacao deNaoApto(Aplicacao aplicacao, LocalDate destino, BigDecimal economia, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new EstadoInvalidoException("Sugestão", aplicacao.getId(), "motivo da remarcação é obrigatório");
        }
        return nova(aplicacao, Tipo.NAO_APTO, destino, economia, motivo);
    }

    private static SugestaoRemarcacao nova(Aplicacao aplicacao, Tipo tipo, LocalDate destino, BigDecimal economia, String motivo) {
        SugestaoRemarcacao s = new SugestaoRemarcacao();
        s.aplicacao = aplicacao;
        s.tipo = tipo;
        s.dataOrigem = aplicacao.getDataAgendada();
        s.dataDestino = destino;
        s.economiaEstimada = economia;
        s.motivoRemarcacao = motivo;
        return s;
    }

    /** Remarca a aplicação na mesma transação. A aplicação não pode ter mudado de data desde a sugestão. */
    public void aceitar(String usuario, String motivo) {
        exigirPendente();
        if (!aplicacao.getDataAgendada().equals(dataOrigem)) {
            throw new EstadoInvalidoException("Sugestão", id,
                    "a aplicação mudou de data desde a sugestão (" + aplicacao.getDataAgendada() + ")");
        }
        if (tipo == Tipo.AGENDA) {
            aplicacao.getProtocolo().reagendar(aplicacao.getNumero(), dataDestino, "Sugestão da agenda aceita");
        } else {
            aplicacao.getProtocolo().remarcar(aplicacao.getNumero(), dataDestino, "Não apto: " + motivoRemarcacao);
        }
        decidir(Status.ACEITA, usuario, motivo);
    }

    public void recusar(String usuario, String motivo) {
        exigirPendente();
        decidir(Status.RECUSADA, usuario, motivo);
    }

    /** Uma sugestão nova (ou aceita) para a mesma aplicação torna esta obsoleta. */
    public void expirar() {
        if (status == Status.SUGERIDA) {
            status = Status.EXPIRADA;
        }
    }

    private void exigirPendente() {
        if (status != Status.SUGERIDA) {
            throw new EstadoInvalidoException("Sugestão", id, "já está " + status);
        }
    }

    private void decidir(Status novo, String usuario, String motivo) {
        this.status = novo;
        this.decididaPor = usuario;
        this.decididaEm = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        this.motivoDecisao = motivo == null || motivo.isBlank() ? null : motivo;
    }
}
