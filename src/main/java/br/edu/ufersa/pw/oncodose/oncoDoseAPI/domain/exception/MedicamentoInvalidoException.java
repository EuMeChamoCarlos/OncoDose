package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

/**
 * Medicamento em estado inválido (→ 400). Invariante violada no nascimento ou em comportamento.
 */
public class MedicamentoInvalidoException extends DomainException {

    private final String campo;
    private final String motivo;

    public MedicamentoInvalidoException(String campo, String motivo) {
        super("Medicamento inválido! campo=" + campo + " motivo=" + motivo);
        this.campo = campo;
        this.motivo = motivo;
    }

    public String getCampo() {
        return campo;
    }

    public String getMotivo() {
        return motivo;
    }
}
