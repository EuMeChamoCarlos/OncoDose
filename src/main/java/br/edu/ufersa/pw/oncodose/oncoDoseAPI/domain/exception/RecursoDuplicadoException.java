package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

/**
 * Conflito de unicidade (→ 409). Ex: nome/código/username já em uso.
 */
public class RecursoDuplicadoException extends DomainException {

    private final String recurso;
    private final String campo;
    private final String valor;

    public RecursoDuplicadoException(String recurso, String campo, String valor) {
        super(recurso + " já existe com este " + campo + "!");
        this.recurso = recurso;
        this.campo = campo;
        this.valor = valor;
    }

    public String getRecurso() {
        return recurso;
    }

    public String getCampo() {
        return campo;
    }

    public String getValor() {
        return valor;
    }
}
