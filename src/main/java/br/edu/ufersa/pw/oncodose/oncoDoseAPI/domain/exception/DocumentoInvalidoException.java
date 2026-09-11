package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

/**
 * Documento (CPF/CNPJ) inválido (→ 400).
 */
public class DocumentoInvalidoException extends DomainException {

    private final String documento;

    public DocumentoInvalidoException(String documento) {
        super("Documento inválido!");
        this.documento = documento;
    }

    public String getDocumento() {
        return documento;
    }
}
