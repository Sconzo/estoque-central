-- ============================================================================
-- Migration V007: Enable PostgreSQL Extensions at Database Level
-- ============================================================================
-- Purpose: Install required PostgreSQL extensions once at database level
--          so they are available to all tenant schemas
--
-- Extensions:
--   - pg_trgm: Trigram matching for fuzzy text search
--   - uuid-ossp: UUID generation functions
--
-- IMPORTANT: This migration runs ONCE in the PUBLIC schema
--            Extensions created here are available to all schemas
-- ============================================================================

-- Enable trigram extension for fuzzy text search
-- This makes it available to all tenant schemas
CREATE EXTENSION IF NOT EXISTS pg_trgm SCHEMA public;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA public;

-- ============================================================================
-- Comments
-- ============================================================================

COMMENT ON EXTENSION pg_trgm IS
'Trigram matching extension for fuzzy text search - available to all tenant schemas';

COMMENT ON EXTENSION "uuid-ossp" IS
'UUID generation functions - available to all tenant schemas';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
