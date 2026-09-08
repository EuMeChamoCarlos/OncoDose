package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @OneToMany(mappedBy = "otimizacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlocacaoFrasco> alocacoes = new ArrayList<>();
}
