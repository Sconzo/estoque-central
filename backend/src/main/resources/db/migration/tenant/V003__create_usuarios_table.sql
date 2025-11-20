-- ============================================================================
-- Migration V003: Create usuarios Table (Applied to each tenant schema)
-- ============================================================================
-- Purpose: Creates the usuarios table for Google OAuth authenticated users
--
-- This migration is applied to EACH tenant schema (tenant_{uuid}).
-- Users are tenant-specific - each tenant has its own isolated user base.
--
-- IMPORTANT: This migration runs in TENANT schemas, not public schema.
-- ============================================================================

-- Create usuarios table
CREATE TABLE IF NOT EXISTS usuarios (
    -- Primary key: UUID v4
    id UUID PRIMARY KEY,

    -- Google OAuth ID (sub claim from Google ID token)
    -- UNIQUE per tenant
    google_id VARCHAR(255) UNIQUE NOT NULL,

    -- User email from Google
    -- UNIQUE per tenant
    email VARCHAR(255) UNIQUE NOT NULL,

    -- User name from Google
    nome VARCHAR(255) NOT NULL,

    -- Google profile picture URL
    picture_url VARCHAR(500),

    -- User role for RBAC
    -- Possible values: ADMIN, GERENTE, VENDEDOR, ESTOQUISTA
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'GERENTE', 'VENDEDOR', 'ESTOQUISTA')),

    -- Whether user is active
    -- Inactive users cannot login
    ativo BOOLEAN DEFAULT true NOT NULL,

    -- Audit timestamps
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ============================================================================
-- Indexes
-- ============================================================================

-- Index for fast lookup by Google ID (used in OAuth login flow)
CREATE INDEX IF NOT EXISTS idx_usuarios_google_id
ON usuarios(google_id);

-- Index for email lookup
CREATE INDEX IF NOT EXISTS idx_usuarios_email
ON usuarios(email);

-- Index for filtering active users
CREATE INDEX IF NOT EXISTS idx_usuarios_ativo
ON usuarios(ativo)
WHERE ativo = true;

-- Index for role-based queries
CREATE INDEX IF NOT EXISTS idx_usuarios_role
ON usuarios(role);

-- ============================================================================
-- Comments (PostgreSQL Documentation)
-- ============================================================================

COMMENT ON TABLE usuarios IS
'Authenticated users via Google OAuth 2.0. Users are tenant-specific.';

COMMENT ON COLUMN usuarios.id IS
'User UUID (primary key)';

COMMENT ON COLUMN usuarios.google_id IS
'Google user ID (sub claim from Google ID token). Unique per tenant.';

COMMENT ON COLUMN usuarios.email IS
'User email from Google. Unique per tenant.';

COMMENT ON COLUMN usuarios.nome IS
'User full name from Google';

COMMENT ON COLUMN usuarios.picture_url IS
'Google profile picture URL';

COMMENT ON COLUMN usuarios.role IS
'User role for RBAC (ADMIN, GERENTE, VENDEDOR, ESTOQUISTA)';

COMMENT ON COLUMN usuarios.ativo IS
'Whether user is active and can login';

COMMENT ON COLUMN usuarios.data_criacao IS
'Timestamp when user was created';

COMMENT ON COLUMN usuarios.data_atualizacao IS
'Timestamp when user was last updated';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
