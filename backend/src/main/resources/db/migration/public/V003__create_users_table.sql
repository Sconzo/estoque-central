-- ============================================================================
-- Migration V003: Create Users Table (Public Schema)
-- ============================================================================
-- Purpose: Creates the users table in the public schema for authentication
--
-- This table stores user authentication information shared across all tenants.
-- Users can belong to multiple companies (tenants) via company_users table.
--
-- IMPORTANT: This table lives in the PUBLIC schema and is shared across
-- all tenants. It contains only authentication/identity data.
-- ============================================================================

-- Create users table in public schema
CREATE TABLE IF NOT EXISTS public.users (
    -- Primary key: UUID for consistency across all tables
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- User full name (required)
    nome VARCHAR(255) NOT NULL,

    -- User email (unique, used for login)
    email VARCHAR(255) UNIQUE NOT NULL,

    -- Google OAuth ID (unique, for Google Sign-In)
    google_id VARCHAR(255) UNIQUE,

    -- Whether user is active (soft delete flag)
    -- Inactive users cannot log in
    ativo BOOLEAN DEFAULT true NOT NULL,

    -- Timestamp of last successful login
    ultimo_login TIMESTAMP,

    -- Timestamp when user was created
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Timestamp when user was last updated
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ============================================================================
-- Indexes
-- ============================================================================

-- Index for fast email lookup (login, uniqueness check)
CREATE INDEX IF NOT EXISTS idx_users_email
ON public.users(email);

-- Index for Google ID lookup (OAuth authentication)
CREATE INDEX IF NOT EXISTS idx_users_google_id
ON public.users(google_id);

-- Index for filtering active users
CREATE INDEX IF NOT EXISTS idx_users_ativo
ON public.users(ativo)
WHERE ativo = true;

-- Index for sorting by last login (analytics, user management)
CREATE INDEX IF NOT EXISTS idx_users_ultimo_login
ON public.users(ultimo_login DESC NULLS LAST);

-- ============================================================================
-- Comments (PostgreSQL Documentation)
-- ============================================================================

COMMENT ON TABLE public.users IS
'User authentication and identity table. Shared across all tenants. Users can belong to multiple companies.';

COMMENT ON COLUMN public.users.id IS
'User ID (primary key, auto-incremented)';

COMMENT ON COLUMN public.users.nome IS
'User full name';

COMMENT ON COLUMN public.users.email IS
'User email address (unique, used for login)';

COMMENT ON COLUMN public.users.google_id IS
'Google OAuth ID (unique, for Google Sign-In integration)';

COMMENT ON COLUMN public.users.ativo IS
'Whether user is active. Inactive users cannot log in (soft delete).';

COMMENT ON COLUMN public.users.ultimo_login IS
'Timestamp of last successful login';

COMMENT ON COLUMN public.users.created_at IS
'Timestamp when user account was created';

COMMENT ON COLUMN public.users.updated_at IS
'Timestamp when user account was last updated';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
