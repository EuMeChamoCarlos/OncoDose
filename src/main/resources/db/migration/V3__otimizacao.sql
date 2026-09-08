-- =====================================================================
-- oncoDoseAPI - V3__otimizacao
-- Resultado do modelo de Gê et al. (2023): solução x_ijk por instância
-- (medicamento, dia) + consumo empírico da farmácia para comparação.
-- Requiere PostgreSQL 13+ (usa gen_random_uuid()).
-- =====================================================================

CREATE TABLE otimizacao (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medicamento_id      UUID NOT NULL,
    data_referencia     DATE NOT NULL,
    status              VARCHAR(30) NOT NULL,
    custo_total         NUMERIC(19, 4) NOT NULL,
    desperdicio_mg      DOUBLE PRECISION NOT NULL,
    economia_vs_empirico NUMERIC(19, 4),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_otimizacao_status CHECK (status IN ('CONCLUIDA', 'INFACTIVEL', 'ERRO'))
);

CREATE TABLE alocacao_frasco (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    otimizacao_id    UUID NOT NULL,
    prescricao_id    UUID NOT NULL,
    apresentacao_id  UUID NOT NULL,
    numero_frasco    INTEGER NOT NULL,
    fracao           DOUBLE PRECISION NOT NULL,
    CONSTRAINT chk_alocacao_fracao CHECK (fracao >= 0 AND fracao <= 1),
    CONSTRAINT uk_alocacao UNIQUE (otimizacao_id, prescricao_id, apresentacao_id, numero_frasco)
);

CREATE TABLE consumo_empirico (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medicamento_id   UUID NOT NULL,
    data_referencia  DATE NOT NULL,
    apresentacao_id  UUID NOT NULL,
    quantidade       INTEGER NOT NULL,
    custo_unitario   NUMERIC(19, 4) NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_consumo_empirico UNIQUE (medicamento_id, data_referencia, apresentacao_id)
);

-- ========================= FORZA CLAVES EXTRANJERAS =========================

ALTER TABLE otimizacao
    ADD CONSTRAINT fk_otimizacao_medicamento
    FOREIGN KEY (medicamento_id) REFERENCES medicamento (id) ON DELETE RESTRICT;

ALTER TABLE alocacao_frasco
    ADD CONSTRAINT fk_alocacao_otimizacao
    FOREIGN KEY (otimizacao_id) REFERENCES otimizacao (id) ON DELETE CASCADE;

ALTER TABLE alocacao_frasco
    ADD CONSTRAINT fk_alocacao_prescricao
    FOREIGN KEY (prescricao_id) REFERENCES prescricao (id) ON DELETE RESTRICT;

ALTER TABLE alocacao_frasco
    ADD CONSTRAINT fk_alocacao_apresentacao
    FOREIGN KEY (apresentacao_id) REFERENCES apresentacao_frasco (id) ON DELETE RESTRICT;

ALTER TABLE consumo_empirico
    ADD CONSTRAINT fk_consumo_medicamento
    FOREIGN KEY (medicamento_id) REFERENCES medicamento (id) ON DELETE RESTRICT;

ALTER TABLE consumo_empirico
    ADD CONSTRAINT fk_consumo_apresentacao
    FOREIGN KEY (apresentacao_id) REFERENCES apresentacao_frasco (id) ON DELETE RESTRICT;
