package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

/**
 * Credenciais inválidas (→ 401). Fail-fast no login.
 */
public class CredenciaisInvalidasException extends DomainException {

    public CredenciaisInvalidasException() {
        super("Credenciais inválidas");
    }
}
