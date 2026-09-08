package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "prescricao")
public class Prescricao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_prescricao", nullable = false, unique = true)
    private String codigoPrescricao; // Identificador anonimizado do paciente (índice i)[cite: 4]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "dose_mg", nullable = false)
    private Double doseMg; // Representa d_i (demanda do paciente i em mg)[cite: 1]

    @Column(nullable = false)
    private String status; // "Apto" ou "Não apto"[cite: 4]

    @Column(name = "data_prescricao", nullable = false)
    private LocalDate dataPrescricao;
}