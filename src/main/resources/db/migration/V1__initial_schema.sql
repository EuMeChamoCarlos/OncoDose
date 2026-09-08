-- =====================================================================
-- oncoDoseAPI - V1__initial_schema
-- Mapea las entidades JPA del dominio (módulo auth/usuarios + quimioterápico).
-- Requiere PostgreSQL 13+ (usa gen_random_uuid()).
-- =====================================================================

-- ============================ AUTHN / USUÁRIOS ============================

CREATE TABLE auths (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username    VARCHAR(255) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE profiles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    photo       TEXT,
    document    VARCHAR(255),
    name        VARCHAR(255),
    phone       VARCHAR(255),
    birth_date  DATE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE privileges (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                  VARCHAR(255),
    is_signature_revoked  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status      VARCHAR(50),
    auth_id     UUID UNIQUE,
    profile_id  UUID UNIQUE,
    role_id     UUID,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE privileges_on_users (
    user_id       UUID NOT NULL,
    privilege_id  UUID NOT NULL,
    PRIMARY KEY (user_id, privilege_id)
);

CREATE TABLE privileges_on_roles (
    role_id       UUID NOT NULL,
    privilege_id  UUID NOT NULL,
    PRIMARY KEY (role_id, privilege_id)
);

-- ====================== MÓDULO QUIMIOTERÁPICO ======================

CREATE TABLE medicamento (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome            VARCHAR(255) NOT NULL UNIQUE,
    codigo_interno  VARCHAR(255) UNIQUE
);

CREATE TABLE apresentacao_frasco (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medicamento_id       UUID NOT NULL,
    volume_mg            DOUBLE PRECISION NOT NULL,
    custo                DOUBLE PRECISION NOT NULL,
    quantidade_estoque   INTEGER NOT NULL
);

CREATE TABLE prescricao (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_prescricao  VARCHAR(255) NOT NULL UNIQUE,
    medicamento_id     UUID NOT NULL,
    dose_mg            DOUBLE PRECISION NOT NULL,
    status             VARCHAR(255) NOT NULL,
    data_prescricao    DATE NOT NULL
);

-- ========================= FORZA CLAVES EXTRANJERAS =========================

ALTER TABLE users
    ADD CONSTRAINT fk_users_auth
    FOREIGN KEY (auth_id) REFERENCES auths (id) ON DELETE CASCADE;

ALTER TABLE users
    ADD CONSTRAINT fk_users_profile
    FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE CASCADE;

ALTER TABLE users
    ADD CONSTRAINT fk_users_role
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT;

ALTER TABLE privileges_on_users
    ADD CONSTRAINT fk_pon_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE privileges_on_users
    ADD CONSTRAINT fk_pon_privilege
    FOREIGN KEY (privilege_id) REFERENCES privileges (id) ON DELETE CASCADE;

ALTER TABLE privileges_on_roles
    ADD CONSTRAINT fk_por_role
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE;

ALTER TABLE privileges_on_roles
    ADD CONSTRAINT fk_por_privilege
    FOREIGN KEY (privilege_id) REFERENCES privileges (id) ON DELETE CASCADE;

ALTER TABLE apresentacao_frasco
    ADD CONSTRAINT fk_frasco_medicamento
    FOREIGN KEY (medicamento_id) REFERENCES medicamento (id) ON DELETE CASCADE;

ALTER TABLE prescricao
    ADD CONSTRAINT fk_prescricao_medicamento
    FOREIGN KEY (medicamento_id) REFERENCES medicamento (id) ON DELETE CASCADE;