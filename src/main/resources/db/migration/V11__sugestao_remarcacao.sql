-- =====================================================================
-- oncoDoseAPI - V11__sugestao_remarcacao
-- Sugestões de remarcação gravadas e decididas por um profissional, com
-- auditoria (quem, quando, motivo). Dois tipos: AGENDA (juntar pacientes
-- do mesmo medicamento) e NAO_APTO (paciente não apto que precisa voltar
-- depois de uma data mínima). Também: motivo da alteração na aplicação,
-- recusa do plano de otimização e nova prescrição para a mesma aplicação
-- em outro dia (o paciente não apto volta com outra prescrição).
-- Quem decide é gravado pelo login (sem FK: o histórico não depende do
-- cadastro de usuários).
-- =====================================================================

CREATE TABLE sugestao_remarcacao (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aplicacao_id       UUID NOT NULL,
    tipo               VARCHAR(20) NOT NULL,
    data_origem        DATE NOT NULL,
    data_destino       DATE NOT NULL,
    economia_estimada  NUMERIC(19, 4) NOT NULL,
    motivo_remarcacao  VARCHAR(255),
    status             VARCHAR(20) NOT NULL,
    gerada_em          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decidida_em        TIMESTAMP,
    decidida_por       VARCHAR(255),
    motivo_decisao     VARCHAR(500),
    CONSTRAINT chk_sugestao_tipo CHECK (tipo IN ('AGENDA', 'NAO_APTO')),
    CONSTRAINT chk_sugestao_status CHECK (status IN ('SUGERIDA', 'ACEITA', 'RECUSADA', 'EXPIRADA')),
    CONSTRAINT fk_sugestao_aplicacao FOREIGN KEY (aplicacao_id) REFERENCES aplicacao (id) ON DELETE CASCADE
);

CREATE INDEX idx_sugestao_aplicacao_status ON sugestao_remarcacao (aplicacao_id, status);

ALTER TABLE aplicacao
    ADD COLUMN motivo_alteracao VARCHAR(255);

ALTER TABLE otimizacao
    DROP CONSTRAINT chk_otimizacao_status,
    ADD CONSTRAINT chk_otimizacao_status CHECK (status IN ('CONCLUIDA', 'CONFIRMADA', 'RECUSADA', 'INFACTIVEL', 'ERRO')),
    ADD COLUMN recusada_em TIMESTAMP,
    ADD COLUMN recusada_por VARCHAR(255),
    ADD COLUMN motivo_recusa VARCHAR(500);

ALTER TABLE prescricao
    DROP CONSTRAINT uk_prescricao_aplicacao_medicamento,
    ADD CONSTRAINT uk_prescricao_aplicacao_medicamento_data UNIQUE (aplicacao_id, medicamento_id, data_prescricao);
