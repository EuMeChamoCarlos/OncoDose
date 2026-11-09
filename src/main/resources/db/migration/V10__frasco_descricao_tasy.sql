-- =====================================================================
-- oncoDoseAPI - V10__frasco_descricao_tasy
-- Nome da apresentação como aparece no Tasy (ex: "Paclitaxel 6mg/ml
-- Frasco-ampola 50ml"). Liga o cadastro de frascos à coluna Medicamento
-- do Tasy. Opcional: apresentações antigas ou simuladas podem não ter.
-- =====================================================================

ALTER TABLE apresentacao_frasco
    ADD COLUMN descricao_tasy VARCHAR(255);
