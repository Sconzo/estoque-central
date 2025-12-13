-- ============================================================================
-- Migration V067: Add tenant_id column to usuarios table
-- ============================================================================
-- Purpose: Adds tenant_id column to usuarios table to match Usuario domain model
--
-- This migration is applied to EACH tenant schema (tenant_{uuid}).
-- While redundant in schema-per-tenant architecture (all users in a schema
-- belong to that tenant), the field is required by the Usuario domain class
-- to maintain referential integrity in the application layer.
--
-- IMPORTANT: This migration runs in TENANT schemas, not public schema.
-- ============================================================================

-- Add tenant_id column
ALTER TABLE usuarios
ADD COLUMN IF NOT EXISTS tenant_id UUID;

-- Set default value for existing rows to prevent NULL issues
-- Note: In schema-per-tenant, all users in this schema belong to the same tenant
-- We'll update this in the application layer during save operations

-- Add index for potential cross-tenant queries (future-proofing)
CREATE INDEX IF NOT EXISTS idx_usuarios_tenant_id
ON usuarios(tenant_id);

-- ============================================================================
-- Comments (PostgreSQL Documentation)
-- ============================================================================

COMMENT ON COLUMN usuarios.tenant_id IS
'Tenant ID this user belongs to. Matches the schema name (tenant_{uuid}).';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
