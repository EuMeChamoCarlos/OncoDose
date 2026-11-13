package br.edu.ufersa.pw.oncotimize.domain.exception;

/**
 * Protocolo ou aplicação em estado inválido (→ 400). Ex: dose, intervalo ou janela fora da regra.
 */
public class ProtocoloInvalidoException extends DomainException {

    private final String codigoProtocolo;
    private final String motivo;

    public ProtocoloInvalidoException(String codigoProtocolo, String motivo) {
        super("Protocolo inválido! codigo=" + codigoProtocolo + " motivo=" + motivo);
        this.codigoProtocolo = codigoProtocolo;
        this.motivo = motivo;
    }

    public String getCodigoProtocolo() {
        return codigoProtocolo;
    }

    public String getMotivo() {
        return motivo;
    }
}
