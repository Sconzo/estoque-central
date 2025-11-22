-- =====================================================
-- Migration V028: Add bom_type to products
-- Story: 2.4 - Composite Products / Kits (BOM)
-- Description: Adds bom_type field to products table
--              to distinguish between VIRTUAL and PHYSICAL BOMs
-- =====================================================

-- Add bom_type column (nullable, only for COMPOSITE products)
ALTER TABLE products
ADD COLUMN bom_type VARCHAR(20);

-- Add check constraint for valid values
ALTER TABLE products
ADD CONSTRAINT chk_products_bom_type
    CHECK (bom_type IN ('VIRTUAL', 'PHYSICAL') OR bom_type IS NULL);

-- Add check constraint: if type=COMPOSITE, bom_type must be set
ALTER TABLE products
ADD CONSTRAINT chk_products_composite_requires_bom_type
    CHECK (
        (type = 'COMPOSITE' AND bom_type IS NOT NULL)
        OR
        (type != 'COMPOSITE' AND bom_type IS NULL)
    );

-- Comments
COMMENT ON COLUMN products.bom_type IS 'BOM type: VIRTUAL (stock calculated from components) or PHYSICAL (pre-assembled kits with own stock)';
