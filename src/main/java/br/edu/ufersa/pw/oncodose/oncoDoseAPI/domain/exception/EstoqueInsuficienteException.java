package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception;

import java.util.UUID;

/**
 * Estoque insuficiente para atender a demanda (→ 422).
 * Carrega solicitado vs. disponível em vez de String solta.
 */
public class EstoqueInsuficienteException extends DomainException {

    private final UUID medicamentoId;
    private final Double quantidadeSolicitada;
    private final Double quantidadeDisponivel;

    public EstoqueInsuficienteException(UUID medicamentoId, Double quantidadeSolicitada, Double quantidadeDisponivel) {
        super("Estoque insuficiente! medicamentoId=" + medicamentoId
                + " solicitada=" + quantidadeSolicitada
                + " disponível=" + quantidadeDisponivel);
        this.medicamentoId = medicamentoId;
        this.quantidadeSolicitada = quantidadeSolicitada;
        this.quantidadeDisponivel = quantidadeDisponivel;
    }

    public UUID getMedicamentoId() {
        return medicamentoId;
    }

    public Double getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }

    public Double getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }
}
