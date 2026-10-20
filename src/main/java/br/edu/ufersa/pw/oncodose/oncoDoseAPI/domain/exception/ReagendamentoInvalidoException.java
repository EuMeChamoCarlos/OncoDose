package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

import java.time.LocalDate;

/**
 * Remarcação que fere a janela autorizada ou o intervalo mínimo do ciclo (→ 422).
 */
public class ReagendamentoInvalidoException extends DomainException {

    private final String codigoCiclo;
    private final int numero;
    private final LocalDate novaData;

    public ReagendamentoInvalidoException(String codigoCiclo, int numero, LocalDate novaData, String motivo) {
        super("Reagendamento inválido! ciclo=" + codigoCiclo + " aplicacao=" + numero
                + " novaData=" + novaData + " motivo=" + motivo);
        this.codigoCiclo = codigoCiclo;
        this.numero = numero;
        this.novaData = novaData;
    }

    public String getCodigoCiclo() {
        return codigoCiclo;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getNovaData() {
        return novaData;
    }
}
