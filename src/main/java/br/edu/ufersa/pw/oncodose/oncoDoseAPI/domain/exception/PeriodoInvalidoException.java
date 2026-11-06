package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

import java.time.LocalDate;

/**
 * Período de consulta inválido (→ 400). Ex: fim antes do início ou período longo demais para o planejador.
 */
public class PeriodoInvalidoException extends DomainException {

    private final LocalDate inicio;
    private final LocalDate fim;

    public PeriodoInvalidoException(LocalDate inicio, LocalDate fim, String motivo) {
        super("Período inválido! inicio=" + inicio + " fim=" + fim + " motivo=" + motivo);
        this.inicio = inicio;
        this.fim = fim;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFim() {
        return fim;
    }
}
