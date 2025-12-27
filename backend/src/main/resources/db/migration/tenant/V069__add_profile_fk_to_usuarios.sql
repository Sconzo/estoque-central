-- ============================================================================
-- Migration V069: Add Foreign Key Constraint to usuarios.profile_id
-- ============================================================================
-- Purpose: Add FK constraint from usuarios.profile_id to profiles.id
--
-- This migration runs AFTER V068 creates the profiles table.
-- V004 created the profile_id column, but couldn't add the FK because
-- profiles table didn't exist yet.
--
-- IMPORTANT: This migration runs in TENANT schemas, not public schema.
-- ============================================================================

-- Add foreign key constraint from usuarios.profile_id to profiles.id
ALTER TABLE usuarios
ADD CONSTRAINT fk_usuarios_profile_id
FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE SET NULL;

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
