package br.edu.ufersa.pw.oncotimize.domain.exception;

import java.time.LocalDate;

/**
 * Remarcação que fere a janela autorizada ou o intervalo mínimo do protocolo (→ 422).
 */
public class ReagendamentoInvalidoException extends DomainException {

    private final String codigoProtocolo;
    private final int numero;
    private final LocalDate novaData;

    public ReagendamentoInvalidoException(String codigoProtocolo, int numero, LocalDate novaData, String motivo) {
        super("Reagendamento inválido! protocolo=" + codigoProtocolo + " aplicacao=" + numero
                + " novaData=" + novaData + " motivo=" + motivo);
        this.codigoProtocolo = codigoProtocolo;
        this.numero = numero;
        this.novaData = novaData;
    }

    public String getCodigoProtocolo() {
        return codigoProtocolo;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getNovaData() {
        return novaData;
    }
}
