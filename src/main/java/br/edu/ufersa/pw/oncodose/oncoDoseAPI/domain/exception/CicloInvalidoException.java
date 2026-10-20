package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

/**
 * Ciclo ou aplicação em estado inválido (→ 400). Ex: dose, intervalo ou janela fora da regra.
 */
public class CicloInvalidoException extends DomainException {

    private final String codigoCiclo;
    private final String motivo;

    public CicloInvalidoException(String codigoCiclo, String motivo) {
        super("Ciclo inválido! codigo=" + codigoCiclo + " motivo=" + motivo);
        this.codigoCiclo = codigoCiclo;
        this.motivo = motivo;
    }

    public String getCodigoCiclo() {
        return codigoCiclo;
    }

    public String getMotivo() {
        return motivo;
    }
}
