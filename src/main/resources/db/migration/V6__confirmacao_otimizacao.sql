-- =====================================================================
-- oncoDoseAPI - V6__confirmacao_otimizacao
-- Confirmar = o preparo foi feito; é quando o estoque baixa. Uma otimização
-- CONFIRMADA por medicamento e dia (não se prepara o mesmo dia duas vezes).
-- =====================================================================

ALTER TABLE otimizacao
    DROP CONSTRAINT chk_otimizacao_status,
    ADD CONSTRAINT chk_otimizacao_status CHECK (status IN ('CONCLUIDA', 'CONFIRMADA', 'INFACTIVEL', 'ERRO')),
    ADD COLUMN confirmada_em TIMESTAMP;

-- Rede de segurança contra corrida entre confirmações de ids diferentes do mesmo dia.
CREATE UNIQUE INDEX uk_otimizacao_confirmada
    ON otimizacao (medicamento_id, data_referencia)
    WHERE status = 'CONFIRMADA';
