-- ============================================================================
-- Migration V001: Enable PostgreSQL Extensions for Tenant Schema
-- ============================================================================
-- Purpose: Extensions pg_trgm and uuid-ossp are created at database level
--          by public schema migration V054__enable_pg_extensions.sql
--          They are available to all tenant schemas automatically
--
-- IMPORTANT: This migration is kept for compatibility but does nothing
--            The extensions are already available from the public schema
-- ============================================================================

-- Extensions pg_trgm and uuid-ossp are created at database level
-- by public schema migration V054__enable_pg_extensions.sql
-- They are available to all tenant schemas automatically

-- This migration is kept for compatibility but does nothing
-- The extensions are already available from the public schema

SELECT 1 AS placeholder;

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
