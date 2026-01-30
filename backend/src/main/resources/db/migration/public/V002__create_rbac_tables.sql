-- ============================================================================
-- Migration V004: Create RBAC Tables (Applied to PUBLIC schema)
-- ============================================================================
-- Purpose: Creates tables for Role-Based Access Control system
--
-- This migration is applied to the PUBLIC schema.
-- Tables: roles, profiles, profile_roles
--
-- Model: Role → Profile → User (Many-to-Many → One-to-Many)
-- - Roles: Global permissions (e.g., ADMIN, GERENTE, VENDEDOR)
-- - Profiles: Tenant-specific groups of roles (e.g., "Gerente Loja")
-- - Users: Have ONE profile, inherit all roles from that profile
-- ============================================================================

-- Create roles table (global, shared across all tenants)
CREATE TABLE IF NOT EXISTS roles (
    -- Primary key: UUID
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Role name (unique globally)
    -- Examples: ADMIN, GERENTE, VENDEDOR, ESTOQUISTA, OPERADOR_PDV
    nome VARCHAR(100) UNIQUE NOT NULL,

    -- Human-readable description
    descricao TEXT,

    -- Category for grouping (GESTAO, OPERACIONAL, SISTEMA)
    categoria VARCHAR(50) CHECK (categoria IN ('GESTAO', 'OPERACIONAL', 'SISTEMA')),

    -- Whether role is active
    ativo BOOLEAN DEFAULT true NOT NULL,

    -- Audit timestamp
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create profiles table (tenant-specific)
CREATE TABLE IF NOT EXISTS profiles (
    -- Primary key: UUID
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Tenant this profile belongs to
    -- FK to public.tenants.id
    tenant_id UUID NOT NULL REFERENCES public.tenants(id) ON DELETE CASCADE,

    -- Profile name (unique per tenant)
    -- Examples: "Gerente Loja", "Vendedor Senior", "Caixa"
    nome VARCHAR(100) NOT NULL,

    -- Human-readable description
    descricao TEXT,

    -- Whether profile is active
    ativo BOOLEAN DEFAULT true NOT NULL,

    -- Audit timestamps
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Unique constraint: same profile name cannot exist twice in same tenant
    UNIQUE (tenant_id, nome)
);

-- Create profile_roles join table (Many-to-Many)
CREATE TABLE IF NOT EXISTS profile_roles (
    -- FK to profiles.id
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,

    -- FK to roles.id
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,

    -- Composite primary key
    PRIMARY KEY (profile_id, role_id)
);

-- ============================================================================
-- Indexes
-- ============================================================================

-- Index for finding profiles by tenant
CREATE INDEX IF NOT EXISTS idx_profiles_tenant_id
ON profiles(tenant_id);

-- Index for finding active profiles
CREATE INDEX IF NOT EXISTS idx_profiles_ativo
ON profiles(ativo)
WHERE ativo = true;

-- Index for finding roles by profile (join queries)
CREATE INDEX IF NOT EXISTS idx_profile_roles_profile_id
ON profile_roles(profile_id);

-- Index for finding profiles by role (reverse join)
CREATE INDEX IF NOT EXISTS idx_profile_roles_role_id
ON profile_roles(role_id);

-- Index for finding active roles
CREATE INDEX IF NOT EXISTS idx_roles_ativo
ON roles(ativo)
WHERE ativo = true;

-- Index for finding roles by category
CREATE INDEX IF NOT EXISTS idx_roles_categoria
ON roles(categoria);

-- ============================================================================
-- Seed Data: Insert Default Roles
-- ============================================================================

INSERT INTO roles (nome, descricao, categoria) VALUES
    ('ADMIN', 'Administrador com acesso total ao sistema', 'SISTEMA'),
    ('GERENTE', 'Gerente com acesso a relatórios e configurações', 'GESTAO'),
    ('VENDEDOR', 'Vendedor com acesso a vendas B2B/B2C', 'OPERACIONAL'),
    ('ESTOQUISTA', 'Estoquista com acesso a estoque e compras', 'OPERACIONAL'),
    ('OPERADOR_PDV', 'Operador de PDV (caixa)', 'OPERACIONAL'),
    ('COMPRADOR', 'Responsável por compras e fornecedores', 'OPERACIONAL'),
    ('FISCAL', 'Acesso a emissão fiscal e documentos', 'OPERACIONAL'),
    ('RELATORIOS', 'Acesso a relatórios e dashboards', 'GESTAO'),
    ('MARKETPLACES', 'Acesso a integrações com marketplaces', 'OPERACIONAL')
ON CONFLICT (nome) DO NOTHING;

-- ============================================================================
-- Comments (PostgreSQL Documentation)
-- ============================================================================

COMMENT ON TABLE roles IS
'Global roles/permissions. Shared across all tenants. Defines what actions users can perform.';

COMMENT ON COLUMN roles.nome IS
'Role name (e.g., ADMIN, GERENTE, VENDEDOR). Unique globally.';

COMMENT ON COLUMN roles.categoria IS
'Role category for grouping (GESTAO, OPERACIONAL, SISTEMA)';

COMMENT ON TABLE profiles IS
'Tenant-specific profiles. Groups multiple roles. Users are assigned ONE profile.';

COMMENT ON COLUMN profiles.tenant_id IS
'Tenant this profile belongs to. FK to public.tenants.id.';

COMMENT ON COLUMN profiles.nome IS
'Profile name (e.g., "Gerente Loja", "Vendedor Senior"). Unique per tenant.';

COMMENT ON TABLE profile_roles IS
'Many-to-Many join table linking profiles to roles. One profile can have multiple roles.';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
