package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.otimizacao;

public enum StatusOtimizacao {
    /** Plano calculado; não mexe no estoque. */
    CONCLUIDA,
    /** Preparo feito; estoque já baixado. */
    CONFIRMADA,
    /** Plano recusado pelo profissional; não mexe no estoque nem nas aplicações. */
    RECUSADA,
    INFACTIVEL,
    ERRO
}
