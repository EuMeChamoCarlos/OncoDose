package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.ciclo;

/** APTO/NAO_APTO não mora aqui: é a prescrição (liberação do dia) que decide se o paciente toma. */
public enum StatusAplicacao {
    AGENDADA,
    REALIZADA,
    CANCELADA
}
