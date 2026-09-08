package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.frascos.Frascos;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao.Prescricao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "alocacao_frasco",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_alocacao",
                columnNames = {"otimizacao_id", "prescricao_id", "apresentacao_id", "numero_frasco"}))
public class AlocacaoFrasco {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "otimizacao_id", nullable = false)
    private Otimizacao otimizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescricao_id", nullable = false)
    private Prescricao prescricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apresentacao_id", nullable = false)
    private Frascos apresentacao;

    @Column(name = "numero_frasco", nullable = false)
    private Integer numeroFrasco;

    @Column(nullable = false)
    private Double fracao;

    @Transient
    public Double getVolumeUtilizadoMg() {
        if (fracao == null || apresentacao == null || apresentacao.getVolumeMg() == null) {
            return null;
        }
        return fracao * apresentacao.getVolumeMg();
    }
}
