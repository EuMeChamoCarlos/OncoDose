package br.edu.ufersa.pw.oncotimize.domain.otimizacao;

import br.edu.ufersa.pw.oncotimize.domain.exception.EstadoInvalidoException;
import br.edu.ufersa.pw.oncotimize.domain.medicamento.Medicamento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "otimizacao")
public class Otimizacao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusOtimizacao status = StatusOtimizacao.CONCLUIDA;

    @Column(name = "custo_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal custoTotal;

    @Column(name = "desperdicio_mg", nullable = false)
    private Double desperdicioMg;

    @Column(name = "economia_vs_empirico", precision = 19, scale = 4)
    private BigDecimal economiaVsEmpirico;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "confirmada_em")
    private LocalDateTime confirmadaEm;

    @OneToMany(mappedBy = "otimizacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlocacaoFrasco> alocacoes = new ArrayList<>();

    @Column(name = "recusada_em")
    private LocalDateTime recusadaEm;

    /** Login de quem recusou. */
    @Column(name = "recusada_por")
    private String recusadaPor;

    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;

    public boolean isConfirmada() {
        return status == StatusOtimizacao.CONFIRMADA;
    }

    public boolean isRecusada() {
        return status == StatusOtimizacao.RECUSADA;
    }

    /** Recusar de novo devolve o mesmo estado; plano com preparo confirmado não se recusa. */
    public void recusar(String usuario, String motivo) {
        if (isRecusada()) {
            return;
        }
        if (isConfirmada()) {
            throw new EstadoInvalidoException("Otimização", id, "preparo já confirmado; não pode ser recusada");
        }
        this.status = StatusOtimizacao.RECUSADA;
        this.recusadaPor = usuario;
        this.recusadaEm = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        this.motivoRecusa = motivo == null || motivo.isBlank() ? null : motivo;
    }

    /** Preparo feito: a baixa de estoque é responsabilidade de quem confirma (use case). */
    public void confirmar() {
        this.status = StatusOtimizacao.CONFIRMADA;
        // Milissegundos: a resposta da 1ª confirmação (memória) e da repetição (banco) saem idênticas.
        this.confirmadaEm = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }
}
