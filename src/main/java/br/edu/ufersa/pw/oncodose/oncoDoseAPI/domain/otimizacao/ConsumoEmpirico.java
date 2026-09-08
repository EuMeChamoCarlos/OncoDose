package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "consumo_empirico",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_consumo_empirico",
                columnNames = {"medicamento_id", "data_referencia", "apresentacao_id"}))
public class ConsumoEmpirico {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apresentacao_id", nullable = false)
    private Frascos apresentacao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "custo_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal custoUnitario;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
