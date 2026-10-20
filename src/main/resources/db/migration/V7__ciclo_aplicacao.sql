-- =====================================================================
-- oncoDoseAPI - V7__ciclo_aplicacao
-- Agenda: o ciclo projeta N aplicações a cada intervalo_dias a partir de
-- data_inicio. Remarcar só dentro da janela [data_prevista - dias_antes,
-- data_prevista + dias_depois] e sem ficar a menos de intervalo_minimo_dias
-- das aplicações vizinhas. Anonimizado (LGPD): só código CIC0000-000000.
-- APTO/NAO_APTO continua na prescrição (liberação do dia), não aqui.
-- =====================================================================

CREATE TABLE ciclo (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_ciclo           VARCHAR(14) NOT NULL UNIQUE,
    medicamento_id         UUID NOT NULL,
    dose_mg                DOUBLE PRECISION NOT NULL,
    data_inicio            DATE NOT NULL,
    total_aplicacoes       INTEGER NOT NULL,
    intervalo_dias         INTEGER NOT NULL,
    intervalo_minimo_dias  INTEGER NOT NULL,
    created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_ciclo_codigo CHECK (codigo_ciclo ~ '^CIC[0-9]{4}-[0-9]{6}$'),
    CONSTRAINT chk_ciclo_dose CHECK (dose_mg > 0),
    CONSTRAINT chk_ciclo_total CHECK (total_aplicacoes BETWEEN 1 AND 100),
    CONSTRAINT chk_ciclo_intervalo CHECK (intervalo_dias >= 1 AND intervalo_minimo_dias BETWEEN 0 AND intervalo_dias)
);

CREATE TABLE aplicacao (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ciclo_id       UUID NOT NULL,
    numero         INTEGER NOT NULL,
    data_prevista  DATE NOT NULL,
    data_agendada  DATE NOT NULL,
    dias_antes     INTEGER NOT NULL DEFAULT 0,
    dias_depois    INTEGER NOT NULL DEFAULT 0,
    status         VARCHAR(20) NOT NULL,
    CONSTRAINT chk_aplicacao_numero CHECK (numero >= 1),
    CONSTRAINT chk_aplicacao_janela CHECK (dias_antes >= 0 AND dias_depois >= 0),
    CONSTRAINT chk_aplicacao_status CHECK (status IN ('AGENDADA', 'REALIZADA', 'CANCELADA')),
    CONSTRAINT uk_aplicacao_ciclo_numero UNIQUE (ciclo_id, numero)
);

-- ========================= FORZA CLAVES EXTRANJERAS =========================

ALTER TABLE ciclo
    ADD CONSTRAINT fk_ciclo_medicamento
    FOREIGN KEY (medicamento_id) REFERENCES medicamento (id) ON DELETE RESTRICT;

ALTER TABLE aplicacao
    ADD CONSTRAINT fk_aplicacao_ciclo
    FOREIGN KEY (ciclo_id) REFERENCES ciclo (id) ON DELETE CASCADE;
