-- =====================================================================
-- oncoDoseAPI - V4__remover_privileges
-- Privileges (assinatura por usuário/role) nunca foram usados na
-- autorização, que depende só da role. Remove as tabelas e as junções.
-- =====================================================================

DROP TABLE privileges_on_users;
DROP TABLE privileges_on_roles;
DROP TABLE privileges;
