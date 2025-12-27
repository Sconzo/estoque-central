-- Migration V049: Create sale_items table
-- Story 4.3: NFCe Emission and Stock Decrease

-- TODO: Migration temporarily disabled - duplicate of V039
-- The sale_items table was already created in V039__create_sale_items_table.sql
-- This migration attempts to create the same table and indexes again, causing conflicts.
-- Need to either:
--   1. Remove V049 completely, OR
--   2. Merge unique content from V049 into V039

-- Placeholder to allow migration to complete
SELECT 1 AS placeholder;
