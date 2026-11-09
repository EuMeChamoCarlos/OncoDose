package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

/**
 * Operação que o estado atual não permite (→ 422). Ex: aceitar sugestão já decidida, confirmar plano recusado.
 */
public class EstadoInvalidoException extends DomainException {

    private final String motivo;

    public EstadoInvalidoException(String recurso, Object id, String motivo) {
        super(recurso + " em estado inválido! id=" + id + " motivo=" + motivo);
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }
}
