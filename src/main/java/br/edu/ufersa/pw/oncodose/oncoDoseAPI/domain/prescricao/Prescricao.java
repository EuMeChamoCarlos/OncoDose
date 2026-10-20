package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.prescricao;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.Aplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo.StatusAplicacao;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.PrescricaoInvalidaException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.medicamento.Medicamento;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade rica: prescrição anonimizada (LGPD). Guarda só código, medicamento,
 * dose (d_i do modelo) e status; nasce por {@link #nova} e muda por {@link #atualizar}.
 */
@Getter
@Entity
@Table(name = "prescricao")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Prescricao {
    /** Formato anonimizado do projeto do CEP, ex: PRE2026-000123. */
    public static final String PADRAO_CODIGO = "^PRE\\d{4}-\\d{6}$";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_prescricao", nullable = false, unique = true)
    private String codigoPrescricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "dose_mg", nullable = false)
    private Double doseMg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPrescricao status;

    /** Data de emissão. O dia do preparo é a data agendada da aplicação, quando houver. */
    @Column(name = "data_prescricao", nullable = false)
    private LocalDate dataPrescricao;

    /** Aplicação da agenda que esta prescrição libera; null no legado (sem ciclo). */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacao_id", unique = true)
    private Aplicacao aplicacao;

    /** aplicacao é opcional e, como código e medicamento, não muda depois. */
    public static Prescricao nova(String codigoPrescricao, Medicamento medicamento, Double doseMg,
                                  StatusPrescricao status, LocalDate dataPrescricao, Aplicacao aplicacao) {
        if (codigoPrescricao == null || !codigoPrescricao.matches(PADRAO_CODIGO)) {
            throw new PrescricaoInvalidaException(codigoPrescricao, "código fora do padrão PRE0000-000000");
        }
        if (medicamento == null) {
            throw new PrescricaoInvalidaException(codigoPrescricao, "medicamento obrigatório");
        }
        if (aplicacao != null) {
            if (!Objects.equals(aplicacao.getCiclo().getMedicamento().getId(), medicamento.getId())) {
                throw new PrescricaoInvalidaException(codigoPrescricao, "aplicação é de outro medicamento");
            }
            if (aplicacao.getStatus() != StatusAplicacao.AGENDADA) {
                throw new PrescricaoInvalidaException(codigoPrescricao, "aplicação já está " + aplicacao.getStatus());
            }
        }
        Prescricao prescricao = new Prescricao();
        prescricao.codigoPrescricao = codigoPrescricao;
        prescricao.medicamento = medicamento;
        prescricao.aplicacao = aplicacao;
        prescricao.atualizar(doseMg, status, dataPrescricao);
        return prescricao;
    }

    /** Código e medicamento são a identidade da prescrição; o resto pode ser corrigido. */
    public void atualizar(Double doseMg, StatusPrescricao status, LocalDate dataPrescricao) {
        if (doseMg == null || doseMg <= 0) {
            throw new PrescricaoInvalidaException(codigoPrescricao, "dose deve ser maior que zero");
        }
        if (status == null || dataPrescricao == null) {
            throw new PrescricaoInvalidaException(codigoPrescricao, "status e data são obrigatórios");
        }
        this.doseMg = doseMg;
        this.status = status;
        this.dataPrescricao = dataPrescricao;
    }
}
