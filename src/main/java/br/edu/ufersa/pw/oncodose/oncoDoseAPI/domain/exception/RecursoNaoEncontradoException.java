package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

import java.util.UUID;

/**
 * Recurso não encontrado (→ 404). Carrega tipo + identificador para contexto.
 */
public class RecursoNaoEncontradoException extends DomainException {

    private final String recurso;
    private final String identificador;

    public RecursoNaoEncontradoException(String recurso, UUID id) {
        super(recurso + " não encontrado(a)! id=" + id);
        this.recurso = recurso;
        this.identificador = String.valueOf(id);
    }

    public RecursoNaoEncontradoException(String recurso, String identificador) {
        super(recurso + " não encontrado(a)! identificador=" + identificador);
        this.recurso = recurso;
        this.identificador = identificador;
    }

    public String getRecurso() {
        return recurso;
    }

    public String getIdentificador() {
        return identificador;
    }
}
