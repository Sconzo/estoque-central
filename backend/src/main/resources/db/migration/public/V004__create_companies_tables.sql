-- ============================================================================
-- Migration V004: Create Companies and Company Users Tables (Public Schema)
-- ============================================================================
-- Purpose: Creates tables for self-service multi-tenant company management
--
-- This migration supports Epics 8-10:
-- - Epic 8: Self-service company creation
-- - Epic 9: Multi-company context switching
-- - Epic 10: Collaborator management with RBAC
--
-- IMPORTANT: These tables live in the PUBLIC schema and are shared across
-- all tenants. They manage company metadata and user-company associations.
-- ============================================================================

-- ============================================================================
-- Table: companies
-- ============================================================================
-- Purpose: Stores company (tenant) information for self-service registration
-- Each company maps to a tenant with its own isolated schema

CREATE TABLE IF NOT EXISTS public.companies (
    -- Primary key: UUID for consistency
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Tenant UUID (maps to tenant schema)
    -- Generated when tenant schema is provisioned
    tenant_id UUID UNIQUE,

    -- PostgreSQL schema name for this company/tenant
    -- Format: tenant_{uuid_without_hyphens}
    -- Set after successful schema provisioning
    schema_name VARCHAR(255) UNIQUE,

    -- Company business name (required)
    name VARCHAR(255) NOT NULL,

    -- Brazilian tax ID (CNPJ) - must be unique across all companies
    cnpj VARCHAR(18) UNIQUE NOT NULL,

    -- Company contact email (required)
    email VARCHAR(255) NOT NULL,

    -- Company phone (optional)
    phone VARCHAR(20),

    -- User ID of the company owner (creator)
    -- FK to public.users.id
    owner_user_id UUID NOT NULL,

    -- Timestamp when company was created
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Timestamp when company was last updated
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Whether company is active (soft delete flag)
    -- Inactive companies cannot be accessed
    active BOOLEAN DEFAULT true NOT NULL
);

-- ============================================================================
-- Table: company_users
-- ============================================================================
-- Purpose: Many-to-many association between users and companies with roles
-- Enables multi-company access per user with different roles per company

CREATE TABLE IF NOT EXISTS public.company_users (
    -- Primary key: UUID for consistency
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- FK to public.companies.id
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,

    -- FK to public.users.id (user who has access to this company)
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,

    -- Role of user in this company (ADMIN, USER, etc.)
    -- This is a simple string role for now; can be enhanced with FK to roles table
    role VARCHAR(50) NOT NULL DEFAULT 'USER',

    -- Timestamp when user was invited to the company
    invited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Timestamp when user accepted the invitation (NULL = pending)
    accepted_at TIMESTAMP,

    -- Whether this association is active (soft delete)
    active BOOLEAN DEFAULT false NOT NULL,

    -- Unique constraint: user can only be associated once per company
    UNIQUE (company_id, user_id)
);

-- ============================================================================
-- Indexes
-- ============================================================================

-- Index for fast tenant_id lookup (schema routing)
CREATE INDEX IF NOT EXISTS idx_companies_tenant_id
ON public.companies(tenant_id);

-- Index for fast schema_name lookup (tenant context resolution)
CREATE INDEX IF NOT EXISTS idx_companies_schema_name
ON public.companies(schema_name);

-- Index for fast CNPJ lookup (uniqueness check during registration)
CREATE INDEX IF NOT EXISTS idx_companies_cnpj
ON public.companies(cnpj);

-- Index for filtering active companies
CREATE INDEX IF NOT EXISTS idx_companies_active
ON public.companies(active)
WHERE active = true;

-- Index for finding companies by owner
CREATE INDEX IF NOT EXISTS idx_companies_owner_user_id
ON public.companies(owner_user_id);

-- Index for email lookup
CREATE INDEX IF NOT EXISTS idx_companies_email
ON public.companies(email);

-- Index for finding all companies for a user (Epic 9 - company selection)
CREATE INDEX IF NOT EXISTS idx_company_users_user_id
ON public.company_users(user_id);

-- Index for finding all users in a company (Epic 10 - collaborator management)
CREATE INDEX IF NOT EXISTS idx_company_users_company_id
ON public.company_users(company_id);

-- Index for filtering active company-user associations
CREATE INDEX IF NOT EXISTS idx_company_users_active
ON public.company_users(active)
WHERE active = true;

-- Composite index for fast user-company-role lookups (RBAC checks)
CREATE INDEX IF NOT EXISTS idx_company_users_user_company_role
ON public.company_users(user_id, company_id, role)
WHERE active = true;

-- ============================================================================
-- Comments (PostgreSQL Documentation)
-- ============================================================================

COMMENT ON TABLE public.companies IS
'Company metadata for self-service multi-tenant system. Each company can have multiple users (collaborators).';

COMMENT ON COLUMN public.companies.id IS
'Company ID (primary key, auto-incremented)';

COMMENT ON COLUMN public.companies.name IS
'Company business name';

COMMENT ON COLUMN public.companies.cnpj IS
'Brazilian tax ID (CNPJ) - must be unique across all companies';

COMMENT ON COLUMN public.companies.email IS
'Company contact email address';

COMMENT ON COLUMN public.companies.phone IS
'Company phone number (optional)';

COMMENT ON COLUMN public.companies.owner_user_id IS
'User ID of the company owner (creator). FK to public.users.id.';

COMMENT ON COLUMN public.companies.created_at IS
'Timestamp when company was created';

COMMENT ON COLUMN public.companies.updated_at IS
'Timestamp when company metadata was last updated';

COMMENT ON COLUMN public.companies.active IS
'Whether company is active. Inactive companies cannot be accessed (soft delete).';

COMMENT ON TABLE public.company_users IS
'Many-to-many association between users and companies with roles. Enables multi-company access.';

COMMENT ON COLUMN public.company_users.id IS
'Association ID (primary key, auto-incremented)';

COMMENT ON COLUMN public.company_users.company_id IS
'Company ID. FK to public.companies.id.';

COMMENT ON COLUMN public.company_users.user_id IS
'User ID. FK to public.users.id.';

COMMENT ON COLUMN public.company_users.role IS
'Role of user in this company (e.g., ADMIN, USER, MANAGER)';

COMMENT ON COLUMN public.company_users.invited_at IS
'Timestamp when user was invited to join the company';

COMMENT ON COLUMN public.company_users.accepted_at IS
'Timestamp when user accepted the invitation (NULL = pending invitation)';

COMMENT ON COLUMN public.company_users.active IS
'Whether this user-company association is active (soft delete)';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
