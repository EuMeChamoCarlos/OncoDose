package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

/**
 * Base abstrata para todas as exceções de regra de negócio (unchecked).
 * Fail-fast: lançada no momento exato da violação, com contexto rico.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
