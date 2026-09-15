package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

import java.util.UUID;

/**
 * Frasco indisponível para fracionamento (→ 422).
 */
public class FrascoIndisponivelException extends DomainException {

    private final UUID frascoId;
    private final String motivo;

    public FrascoIndisponivelException(UUID frascoId, String motivo) {
        super("Frasco indisponível! frascoId=" + frascoId + " motivo=" + motivo);
        this.frascoId = frascoId;
        this.motivo = motivo;
    }

    public UUID getFrascoId() {
        return frascoId;
    }

    public String getMotivo() {
        return motivo;
    }
}
