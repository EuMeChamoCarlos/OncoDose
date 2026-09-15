package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

/**
 * Regra de prescrição violada (→ 400). Ex: dose inválida, status inconsistente.
 */
public class PrescricaoInvalidaException extends DomainException {

    private final String codigoPrescricao;
    private final String motivo;

    public PrescricaoInvalidaException(String codigoPrescricao, String motivo) {
        super("Prescrição inválida! codigo=" + codigoPrescricao + " motivo=" + motivo);
        this.codigoPrescricao = codigoPrescricao;
        this.motivo = motivo;
    }

    public PrescricaoInvalidaException(String motivo) {
        super("Prescrição inválida! motivo=" + motivo);
        this.codigoPrescricao = null;
        this.motivo = motivo;
    }

    public String getCodigoPrescricao() {
        return codigoPrescricao;
    }

    public String getMotivo() {
        return motivo;
    }
}
