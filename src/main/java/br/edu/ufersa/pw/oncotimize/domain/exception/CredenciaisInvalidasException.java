package br.edu.ufersa.pw.oncotimize.domain.exception;

/**
 * Credenciais inválidas (→ 401). Fail-fast no login.
 */
public class CredenciaisInvalidasException extends DomainException {

    public CredenciaisInvalidasException() {
        super("Credenciais inválidas");
    }
}
