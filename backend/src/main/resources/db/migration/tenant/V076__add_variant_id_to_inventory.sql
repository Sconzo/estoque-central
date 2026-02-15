-- V076__add_variant_id_to_inventory.sql
-- Adds variant_id column to inventory table for per-variant stock tracking.
-- Makes product_id nullable (variants have variant_id instead of product_id).

-- Add variant_id column
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS variant_id UUID;

-- Make product_id nullable (for variant inventory records)
ALTER TABLE inventory ALTER COLUMN product_id DROP NOT NULL;

-- Add unique constraint for variant + location
ALTER TABLE inventory ADD CONSTRAINT unique_variant_location_id
    UNIQUE (variant_id, location_id);

-- Add index for variant_id lookups
CREATE INDEX IF NOT EXISTS idx_inventory_variant_id ON inventory(variant_id)
    WHERE variant_id IS NOT NULL;
