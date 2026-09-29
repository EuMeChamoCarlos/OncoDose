-- =====================================================================
-- oncoDoseAPI - V5__prescricao_regras
-- Prescrição anonimizada (LGPD): só código PRE0000-000000, dose > 0 e
-- status APTO/NAO_APTO. Apagar medicamento não apaga mais prescrições.
-- =====================================================================

UPDATE prescricao
SET status = CASE WHEN upper(status) = 'APTO' THEN 'APTO' ELSE 'NAO_APTO' END;

ALTER TABLE prescricao
    ADD CONSTRAINT chk_prescricao_status CHECK (status IN ('APTO', 'NAO_APTO')),
    ADD CONSTRAINT chk_prescricao_codigo CHECK (codigo_prescricao ~ '^PRE[0-9]{4}-[0-9]{6}$'),
    ADD CONSTRAINT chk_prescricao_dose CHECK (dose_mg > 0),
    DROP CONSTRAINT fk_prescricao_medicamento,
    ADD CONSTRAINT fk_prescricao_medicamento
        FOREIGN KEY (medicamento_id) REFERENCES medicamento (id) ON DELETE RESTRICT;
