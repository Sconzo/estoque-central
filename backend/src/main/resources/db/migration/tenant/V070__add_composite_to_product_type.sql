-- =====================================================
-- Migration V068: Add COMPOSITE to product type constraint
-- Description: Updates check_product_type constraint to include
--              COMPOSITE type, enabling kit/BOM products
-- =====================================================

-- Drop existing constraint
ALTER TABLE products
DROP CONSTRAINT IF EXISTS check_product_type;

-- Add updated constraint with COMPOSITE
ALTER TABLE products
ADD CONSTRAINT check_product_type
    CHECK (type IN ('SIMPLE', 'VARIANT_PARENT', 'VARIANT', 'COMPOSITE'));

-- Comment
COMMENT ON CONSTRAINT check_product_type ON products IS 'Valid product types: SIMPLE, VARIANT_PARENT, VARIANT, COMPOSITE';
