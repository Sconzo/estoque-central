-- ============================================================================
-- Migration V004: Update usuarios Table - Add profile_id (Applied to TENANT schemas)
-- ============================================================================
-- Purpose: Adds profile_id column to usuarios table
--
-- This migration is applied to EACH tenant schema (tenant_{uuid}).
-- Users will now have a profile instead of a single role VARCHAR.
--
-- IMPORTANT: This migration runs in TENANT schemas, not public schema.
-- ============================================================================

-- Add profile_id column (without FK constraint)
-- Note: FK constraint will be added in V069 after profiles table is created in V068
ALTER TABLE usuarios
ADD COLUMN IF NOT EXISTS profile_id UUID;

-- Create index for profile_id lookups
CREATE INDEX IF NOT EXISTS idx_usuarios_profile_id
ON usuarios(profile_id);

-- Drop old 'role' VARCHAR column (replaced by profile system)
-- IMPORTANT: This assumes Story 1.4 was deployed and 'role' column exists
-- If migrating fresh, this will fail (expected - can be ignored)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'usuarios'
        AND column_name = 'role'
    ) THEN
        ALTER TABLE usuarios DROP COLUMN role;
    END IF;
END $$;

-- ============================================================================
-- Comments (PostgreSQL Documentation)
-- ============================================================================

COMMENT ON COLUMN usuarios.profile_id IS
'Profile ID this user belongs to. FK to profiles.id (added in V069). Determines user roles/permissions.';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
