package br.edu.ufersa.pw.oncotimize.domain.prescricao;

import br.edu.ufersa.pw.oncotimize.domain.exception.PrescricaoInvalidaException;
import br.edu.ufersa.pw.oncotimize.domain.medicamento.Medicamento;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.Aplicacao;
import br.edu.ufersa.pw.oncotimize.domain.protocolo.StatusAplicacao;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade rica: uma linha da prescrição anonimizada (LGPD), uma por quimioterápico. Guarda só
 * código, medicamento, dose (d_i do modelo) e status; nasce por {@link #nova} e muda por
 * {@link #atualizar}. O mesmo código pode se repetir em outro medicamento.
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

    @Column(name = "codigo_prescricao", nullable = false)
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

    /** Aplicação da agenda que esta linha libera; null no legado (sem protocolo). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacao_id")
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
            if (!aplicacao.preve(medicamento)) {
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

    /**
     * Linhas com o mesmo código são a mesma prescrição: mesma aplicação, mesmo status (apto ou não)
     * e mesma data. Só a dose muda de um medicamento para outro.
     */
    public void exigirMesmaPrescricaoQue(Prescricao outra) {
        boolean mesmaAplicacao = aplicacao == null ? outra.aplicacao == null
                : outra.aplicacao != null && Objects.equals(aplicacao.getId(), outra.aplicacao.getId());
        if (!mesmaAplicacao || status != outra.status || !dataPrescricao.equals(outra.dataPrescricao)) {
            throw new PrescricaoInvalidaException(codigoPrescricao,
                    "linhas da mesma prescrição precisam ter a mesma aplicação, status e data");
        }
    }

    /** Código e medicamento são a identidade da linha; o resto pode ser corrigido. */
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
