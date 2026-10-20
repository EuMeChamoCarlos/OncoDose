-- =====================================================================
-- oncoDoseAPI - V8__prescricao_aplicacao
-- A prescrição (liberação do dia: APTO/NAO_APTO + dose final) passa a
-- apontar para a aplicação da agenda. Com aplicação, o dia do preparo é
-- aplicacao.data_agendada; sem aplicação (legado), continua data_prescricao.
-- Uma aplicação tem no máximo uma prescrição.
-- =====================================================================

ALTER TABLE prescricao
    ADD COLUMN aplicacao_id UUID,
    ADD CONSTRAINT uk_prescricao_aplicacao UNIQUE (aplicacao_id),
    ADD CONSTRAINT fk_prescricao_aplicacao
        FOREIGN KEY (aplicacao_id) REFERENCES aplicacao (id) ON DELETE RESTRICT;
