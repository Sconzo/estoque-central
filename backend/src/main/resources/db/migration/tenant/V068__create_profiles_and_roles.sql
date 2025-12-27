-- ============================================================================
-- Migration V068: Create Profiles and Profile Roles in Tenant Schema
-- ============================================================================
-- Purpose: Move RBAC profiles to tenant schema for per-company configurability
--
-- Story: 7-2 PostgreSQL Multi-Tenant Schema-per-Tenant
-- AC2: Each tenant schema must have profiles and profile_roles tables
-- AC2: Seed default profiles: Admin, Gerente, Vendedor
--
-- IMPORTANT: This migration runs in EACH TENANT SCHEMA, not public schema.
-- Each company gets its own isolated profiles configuration.
-- ============================================================================

-- ============================================================================
-- Table: profiles
-- ============================================================================
-- Purpose: Tenant-specific user profiles (groups of roles)
-- Each tenant can define custom profiles for their organization

CREATE TABLE IF NOT EXISTS profiles (
    -- Primary key: UUID
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Profile name (unique per tenant schema)
    -- Examples: "Admin", "Gerente", "Vendedor", "Supervisor Regional"
    nome VARCHAR(100) UNIQUE NOT NULL,

    -- Human-readable description
    descricao TEXT,

    -- Whether profile is active
    ativo BOOLEAN DEFAULT true NOT NULL,

    -- Audit timestamps
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ============================================================================
-- Table: profile_roles
-- ============================================================================
-- Purpose: Many-to-Many relationship between profiles and global roles
-- Links tenant profiles to system-wide roles (from public.roles)

CREATE TABLE IF NOT EXISTS profile_roles (
    -- FK to profiles.id (in this tenant schema)
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,

    -- FK to public.roles.id (global roles table)
    -- Note: This references the PUBLIC schema roles table
    role_id UUID NOT NULL,

    -- Composite primary key
    PRIMARY KEY (profile_id, role_id)
);

-- ============================================================================
-- Indexes
-- ============================================================================

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

-- ============================================================================
-- Comments
-- ============================================================================

COMMENT ON TABLE profiles IS
'Tenant-specific user profiles. Each company defines custom profiles for their organization.';

COMMENT ON COLUMN profiles.nome IS
'Profile name (e.g., "Admin", "Gerente", "Vendedor"). Unique within this tenant.';

COMMENT ON TABLE profile_roles IS
'Many-to-Many join table linking tenant profiles to global system roles (public.roles).';

-- ============================================================================
-- Seed Data: Default Profiles for New Tenant
-- ============================================================================
-- AC2 Requirement: Seed default profiles Admin, Gerente, Vendedor

DO $$
DECLARE
    admin_profile_id UUID;
    gerente_profile_id UUID;
    vendedor_profile_id UUID;

    role_admin_id UUID;
    role_gerente_id UUID;
    role_vendedor_id UUID;
    role_relatorios_id UUID;
    role_estoquista_id UUID;
BEGIN
    -- Insert default profiles
    INSERT INTO profiles (nome, descricao, ativo)
    VALUES ('Admin', 'Administrador com acesso total ao sistema', true)
    ON CONFLICT (nome) DO NOTHING;

    INSERT INTO profiles (nome, descricao, ativo)
    VALUES ('Gerente', 'Gerente com acesso a gestão e relatórios', true)
    ON CONFLICT (nome) DO NOTHING;

    INSERT INTO profiles (nome, descricao, ativo)
    VALUES ('Vendedor', 'Vendedor com acesso a vendas e estoque', true)
    ON CONFLICT (nome) DO NOTHING;

    -- Get profile IDs
    SELECT id INTO admin_profile_id FROM profiles WHERE nome = 'Admin';
    SELECT id INTO gerente_profile_id FROM profiles WHERE nome = 'Gerente';
    SELECT id INTO vendedor_profile_id FROM profiles WHERE nome = 'Vendedor';

    -- Get role IDs from public.roles (seeded in V002)
    SELECT id INTO role_admin_id FROM public.roles WHERE nome = 'ADMIN';
    SELECT id INTO role_gerente_id FROM public.roles WHERE nome = 'GERENTE';
    SELECT id INTO role_vendedor_id FROM public.roles WHERE nome = 'VENDEDOR';
    SELECT id INTO role_relatorios_id FROM public.roles WHERE nome = 'RELATORIOS';
    SELECT id INTO role_estoquista_id FROM public.roles WHERE nome = 'ESTOQUISTA';

    -- Assign roles to profiles

    -- Admin profile: All permissions
    INSERT INTO profile_roles (profile_id, role_id)
    VALUES (admin_profile_id, role_admin_id)
    ON CONFLICT DO NOTHING;

    -- Gerente profile: Management + Reports
    INSERT INTO profile_roles (profile_id, role_id)
    VALUES
        (gerente_profile_id, role_gerente_id),
        (gerente_profile_id, role_relatorios_id),
        (gerente_profile_id, role_vendedor_id),
        (gerente_profile_id, role_estoquista_id)
    ON CONFLICT DO NOTHING;

    -- Vendedor profile: Sales only
    INSERT INTO profile_roles (profile_id, role_id)
    VALUES (vendedor_profile_id, role_vendedor_id)
    ON CONFLICT DO NOTHING;

END $$;

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
