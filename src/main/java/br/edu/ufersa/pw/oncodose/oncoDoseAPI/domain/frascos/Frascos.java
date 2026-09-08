package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "apresentacao_frasco")
public class Frascos {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(nullable = false)
    private Double volumeMg; // Representa v_j (volume do frasco tipo j)

    @Column(nullable = false)
    private Double custo; // Representa c_j (custo do frasco tipo j)[cite: 1]

    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque; // Representa e_j (estoque disponível)[cite: 1]
}