-- Migration V050: Create fiscal_events table for audit trail
-- Story 4.3: NFCe Emission and Stock Decrease
-- NFR16: 5-year retention for fiscal compliance

-- TODO: Migration temporarily disabled - duplicate of V040
-- The fiscal_events table was already created in V040__create_fiscal_events_table.sql
-- This migration attempts to create the same table and indexes again, causing conflicts.
-- Need to either:
--   1. Remove V050 completely, OR
--   2. Merge unique content from V050 into V040

-- Placeholder to allow migration to complete
SELECT 1 AS placeholder;
