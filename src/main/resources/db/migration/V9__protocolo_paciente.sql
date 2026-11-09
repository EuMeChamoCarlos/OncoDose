-- =====================================================================
-- oncoDoseAPI - V9__protocolo_paciente
-- Aproxima o modelo das telas do Tasy, só com os campos que o modelo usa:
--   Protocolos  → protocolo_paciente (subtipo, dias_intervalo, ciclos)
--   Medicamentos→ protocolo_medicamento (dose em mg, via, dias de aplicação)
--   Ciclos      → aplicacao (ciclo_numero, dia_ciclo, data_real)
--   Prescrição  → uma linha por medicamento: chave (código, medicamento)
-- "Ciclo" no Tasy é a repetição numerada dentro do protocolo. Dados que já
-- existem são preservados: ciclo antigo vira protocolo de um medicamento,
-- D1, com um ciclo por aplicação. Anonimizado (LGPD): nenhum campo do Tasy
-- que identifique o paciente entra aqui.
-- =====================================================================

ALTER TABLE ciclo RENAME TO protocolo_paciente;
ALTER TABLE protocolo_paciente RENAME COLUMN codigo_ciclo TO codigo_protocolo;
ALTER TABLE protocolo_paciente RENAME COLUMN total_aplicacoes TO ciclos;
ALTER TABLE protocolo_paciente RENAME COLUMN intervalo_dias TO dias_intervalo;

ALTER TABLE protocolo_paciente
    DROP CONSTRAINT chk_ciclo_codigo,
    DROP CONSTRAINT chk_ciclo_total,
    DROP CONSTRAINT chk_ciclo_intervalo,
    DROP CONSTRAINT chk_ciclo_dose,
    ADD COLUMN medicacao_subtipo VARCHAR(100),
    ADD COLUMN grupo VARCHAR(100);

UPDATE protocolo_paciente
SET codigo_protocolo = 'PTC' || substr(codigo_protocolo, 4);

UPDATE protocolo_paciente p
SET medicacao_subtipo = m.nome
FROM medicamento m
WHERE m.id = p.medicamento_id;

CREATE TABLE protocolo_medicamento (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    protocolo_id    UUID NOT NULL,
    medicamento_id  UUID NOT NULL,
    dose_mg         DOUBLE PRECISION NOT NULL,
    via             VARCHAR(10) NOT NULL,
    dias_aplicacao  VARCHAR(255) NOT NULL,
    CONSTRAINT chk_protocolo_medicamento_dose CHECK (dose_mg > 0),
    CONSTRAINT chk_protocolo_medicamento_dias CHECK (dias_aplicacao ~ '^D[1-9][0-9]*(,D[1-9][0-9]*)*$'),
    CONSTRAINT uk_protocolo_medicamento UNIQUE (protocolo_id, medicamento_id),
    CONSTRAINT fk_protocolo_medicamento_protocolo
        FOREIGN KEY (protocolo_id) REFERENCES protocolo_paciente (id) ON DELETE CASCADE,
    CONSTRAINT fk_protocolo_medicamento_medicamento
        FOREIGN KEY (medicamento_id) REFERENCES medicamento (id) ON DELETE RESTRICT
);

INSERT INTO protocolo_medicamento (protocolo_id, medicamento_id, dose_mg, via, dias_aplicacao)
SELECT id, medicamento_id, dose_mg, 'IV', 'D1'
FROM protocolo_paciente;

ALTER TABLE protocolo_paciente
    DROP CONSTRAINT fk_ciclo_medicamento,
    DROP COLUMN medicamento_id,
    DROP COLUMN dose_mg,
    ALTER COLUMN medicacao_subtipo SET NOT NULL,
    ADD CONSTRAINT chk_protocolo_codigo CHECK (codigo_protocolo ~ '^PTC[0-9]{4}-[0-9]{6}$'),
    ADD CONSTRAINT chk_protocolo_ciclos CHECK (ciclos BETWEEN 1 AND 100),
    ADD CONSTRAINT chk_protocolo_intervalo CHECK (dias_intervalo >= 1 AND intervalo_minimo_dias >= 0);

ALTER TABLE aplicacao RENAME COLUMN ciclo_id TO protocolo_id;
ALTER TABLE aplicacao RENAME CONSTRAINT uk_aplicacao_ciclo_numero TO uk_aplicacao_protocolo_numero;
ALTER TABLE aplicacao RENAME CONSTRAINT fk_aplicacao_ciclo TO fk_aplicacao_protocolo;

ALTER TABLE aplicacao
    ADD COLUMN ciclo_numero INTEGER,
    ADD COLUMN dia_ciclo INTEGER,
    ADD COLUMN data_real DATE;

UPDATE aplicacao
SET ciclo_numero = numero,
    dia_ciclo = 1,
    data_real = CASE WHEN status = 'REALIZADA' THEN data_agendada END;

ALTER TABLE aplicacao
    ALTER COLUMN ciclo_numero SET NOT NULL,
    ALTER COLUMN dia_ciclo SET NOT NULL,
    ADD CONSTRAINT chk_aplicacao_ciclo_dia CHECK (ciclo_numero >= 1 AND dia_ciclo >= 1),
    ADD CONSTRAINT uk_aplicacao_ciclo_dia UNIQUE (protocolo_id, ciclo_numero, dia_ciclo);

-- Uma linha por quimioterápico da prescrição; o mesmo código pode aparecer em vários medicamentos.
ALTER TABLE prescricao
    DROP CONSTRAINT prescricao_codigo_prescricao_key,
    DROP CONSTRAINT uk_prescricao_aplicacao,
    ADD CONSTRAINT uk_prescricao_codigo_medicamento UNIQUE (codigo_prescricao, medicamento_id),
    ADD CONSTRAINT uk_prescricao_aplicacao_medicamento UNIQUE (aplicacao_id, medicamento_id);
