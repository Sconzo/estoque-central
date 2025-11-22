-- V030__add_variant_support_to_inventory.sql
-- Story 2.7: Multi-Warehouse Stock Control
-- Adds variant support to inventory table

-- ============================================================
-- Add variant_id column to inventory table
-- ============================================================
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS variant_id UUID REFERENCES product_variants(id);

-- ============================================================
-- Update unique constraint to support variants
-- ============================================================

-- Drop old unique constraint
ALTER TABLE inventory DROP CONSTRAINT IF EXISTS unique_product_location_id;

-- Add new constraint: product_id OR variant_id must be NOT NULL
ALTER TABLE inventory ADD CONSTRAINT check_product_or_variant
    CHECK (product_id IS NOT NULL OR variant_id IS NOT NULL);

-- Add unique constraint for (tenant_id, product_id, variant_id, location_id)
-- This allows:
-- - Simple products: (tenant_id, product_id, NULL, location_id)
-- - Variants: (tenant_id, NULL, variant_id, location_id)
-- - Composite products: (tenant_id, product_id, NULL, location_id)
ALTER TABLE inventory ADD CONSTRAINT unique_inventory_product_variant_location
    UNIQUE NULLS NOT DISTINCT (tenant_id, product_id, variant_id, location_id);

-- ============================================================
-- Rename columns to match Story 2.7 naming convention
-- ============================================================

-- Rename min_quantity to minimum_quantity
ALTER TABLE inventory RENAME COLUMN min_quantity TO minimum_quantity;

-- Rename max_quantity to maximum_quantity (optional, not required by Story 2.7)
ALTER TABLE inventory RENAME COLUMN max_quantity TO maximum_quantity;

-- Rename quantity to quantity_available
ALTER TABLE inventory RENAME COLUMN quantity TO quantity_available;

-- Drop the generated column available_quantity (we'll recreate it as quantity_for_sale)
ALTER TABLE inventory DROP COLUMN IF EXISTS available_quantity;

-- Add quantity_for_sale as a generated column
ALTER TABLE inventory ADD COLUMN quantity_for_sale NUMERIC(15, 3)
    GENERATED ALWAYS AS (
        CASE
            WHEN (quantity_available - reserved_quantity) < 0 THEN 0
            ELSE (quantity_available - reserved_quantity)
        END
    ) STORED;

-- ============================================================
-- Update constraints with new column names
-- ============================================================

-- Drop old constraints
ALTER TABLE inventory DROP CONSTRAINT IF EXISTS check_quantity_positive;
ALTER TABLE inventory DROP CONSTRAINT IF EXISTS check_min_max;

-- Add new constraints
ALTER TABLE inventory ADD CONSTRAINT check_quantity_available_positive
    CHECK (quantity_available >= 0);

ALTER TABLE inventory ADD CONSTRAINT check_minimum_maximum
    CHECK (minimum_quantity IS NULL OR maximum_quantity IS NULL OR minimum_quantity <= maximum_quantity);

-- ============================================================
-- Update indexes with new column names
-- ============================================================

-- Drop old index
DROP INDEX IF EXISTS idx_inventory_low_stock;

-- Create new index for below minimum quantity
CREATE INDEX idx_inventory_below_minimum ON inventory(tenant_id, quantity_for_sale, minimum_quantity)
    WHERE minimum_quantity IS NOT NULL AND quantity_for_sale < minimum_quantity;

-- Add index for variant_id
CREATE INDEX idx_inventory_variant_id ON inventory(variant_id)
    WHERE variant_id IS NOT NULL;

-- Add composite index for product/variant queries
CREATE INDEX idx_inventory_tenant_product_variant ON inventory(tenant_id, product_id, variant_id);

-- ============================================================
-- Add comment explaining the inventory model
-- ============================================================
COMMENT ON TABLE inventory IS 'Multi-warehouse inventory control with support for simple products, variants, and composite products (BOM virtual)';
COMMENT ON COLUMN inventory.product_id IS 'Product ID for simple/composite products (NULL for variants)';
COMMENT ON COLUMN inventory.variant_id IS 'Variant ID for product variants (NULL for simple/composite products)';
COMMENT ON COLUMN inventory.location_id IS 'Stock location reference (warehouse, store, etc.)';
COMMENT ON COLUMN inventory.quantity_available IS 'Total quantity available in stock';
COMMENT ON COLUMN inventory.reserved_quantity IS 'Quantity reserved for pending orders';
COMMENT ON COLUMN inventory.quantity_for_sale IS 'Calculated: quantity_available - reserved_quantity (never negative)';
COMMENT ON COLUMN inventory.minimum_quantity IS 'Minimum stock threshold for alerts';
COMMENT ON COLUMN inventory.maximum_quantity IS 'Maximum stock capacity (optional)';

-- ============================================================
-- Create view for stock summary
-- ============================================================
CREATE OR REPLACE VIEW v_stock_summary AS
SELECT
    i.id,
    i.tenant_id,
    i.product_id,
    i.variant_id,
    i.location_id,
    COALESCE(p.name, pv_parent.name || ' - ' || pv.sku) AS product_name,
    COALESCE(p.sku, pv.sku) AS sku,
    l.name AS location_name,
    l.code AS location_code,
    i.quantity_available,
    i.reserved_quantity,
    i.quantity_for_sale,
    i.minimum_quantity,
    i.maximum_quantity,
    CASE
        WHEN i.minimum_quantity IS NULL THEN 'NOT_SET'
        WHEN i.quantity_for_sale >= i.minimum_quantity THEN 'OK'
        WHEN i.quantity_for_sale >= (i.minimum_quantity * 0.2) THEN 'LOW'
        ELSE 'CRITICAL'
    END AS stock_status,
    CASE
        WHEN i.minimum_quantity IS NULL OR i.minimum_quantity = 0 THEN NULL
        ELSE ROUND((i.quantity_for_sale * 100.0 / i.minimum_quantity), 2)
    END AS percentage_of_minimum
FROM inventory i
LEFT JOIN products p ON i.product_id = p.id
LEFT JOIN product_variants pv ON i.variant_id = pv.id
LEFT JOIN products pv_parent ON pv.parent_product_id = pv_parent.id
INNER JOIN locations l ON i.location_id = l.id
WHERE l.ativo = true;

COMMENT ON VIEW v_stock_summary IS 'Stock summary with calculated fields for dashboards and reports';

-- ============================================================
-- Migration: Create inventory records for existing variants
-- ============================================================

-- Create inventory records for all active product variants in all locations
-- This ensures every variant has an inventory record (even with 0 quantity)
INSERT INTO inventory (tenant_id, product_id, variant_id, location_id, quantity_available, reserved_quantity, minimum_quantity)
SELECT
    pv.tenant_id,
    NULL, -- product_id is NULL for variants
    pv.id AS variant_id,
    l.id AS location_id,
    0 AS quantity_available,
    0 AS reserved_quantity,
    10 AS minimum_quantity -- Default minimum quantity
FROM product_variants pv
CROSS JOIN locations l
WHERE pv.ativo = true
  AND l.ativo = true
  AND pv.tenant_id = l.tenant_id
ON CONFLICT ON CONSTRAINT unique_inventory_product_variant_location DO NOTHING;

-- ============================================================
-- Success message
-- ============================================================
DO $$
BEGIN
    RAISE NOTICE 'Migration V030 completed successfully: Variant support added to inventory table';
END $$;
