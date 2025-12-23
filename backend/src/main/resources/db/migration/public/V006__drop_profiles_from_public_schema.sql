-- ============================================================================
-- Migration V006: Drop Profiles Tables from Public Schema
-- ============================================================================
-- Purpose: Remove profiles/profile_roles from public schema after moving to tenant schemas
--
-- Story: 7-2 PostgreSQL Multi-Tenant Schema-per-Tenant
-- AC2: Profiles must live in tenant schemas for per-company configurability
--
-- IMPORTANT: This migration removes tables created in V002
-- Run this AFTER migrating existing data to tenant schemas (if any)
-- ============================================================================

-- Drop profile_roles join table first (has FK to profiles)
DROP TABLE IF EXISTS public.profile_roles CASCADE;

-- Drop profiles table
DROP TABLE IF EXISTS public.profiles CASCADE;

-- Note: public.roles table remains in public schema
-- Roles are global and shared across all tenants

-- ============================================================================
-- Comments
-- ============================================================================

COMMENT ON TABLE public.roles IS
'Global system roles shared across all tenants. Referenced by tenant-specific profile_roles tables.';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
