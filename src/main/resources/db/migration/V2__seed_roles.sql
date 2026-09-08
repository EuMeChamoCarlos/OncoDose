-- =====================================================================
-- oncoDoseAPI - V2__seed_roles
-- Popula a tabela roles com os papéis padrão do sistema.
-- =====================================================================

INSERT INTO roles (name, created_at)
VALUES ('USER', CURRENT_TIMESTAMP),
       ('ADMIN', CURRENT_TIMESTAMP);