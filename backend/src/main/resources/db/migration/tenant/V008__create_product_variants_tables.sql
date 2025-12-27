-- V008__create_product_variants_tables.sql
-- Story 2.3: Product Variants
-- Creates tables for product variants with attributes (size, color, etc.)

-- ============================================================
-- Table: variant_attributes
-- Stores attribute definitions (Color, Size, Material, etc.)
-- ============================================================
CREATE TABLE IF NOT EXISTS variant_attributes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Attribute details
    name VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,

    -- Attribute type for validation/UI
    type VARCHAR(20) NOT NULL DEFAULT 'TEXT',

    -- Sort order for display
    sort_order INTEGER NOT NULL DEFAULT 0,

    -- Status
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT unique_attribute_name_per_tenant UNIQUE (tenant_id, name),
    CONSTRAINT check_attribute_type CHECK (type IN ('TEXT', 'COLOR', 'SIZE', 'NUMBER'))
);

-- ============================================================
-- Table: variant_attribute_values
-- Stores possible values for each attribute
-- ============================================================
CREATE TABLE IF NOT EXISTS variant_attribute_values (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Attribute reference
    attribute_id UUID NOT NULL REFERENCES variant_attributes(id) ON DELETE CASCADE,

    -- Value details
    value VARCHAR(100) NOT NULL,
    display_value VARCHAR(100) NOT NULL,

    -- Optional color hex for COLOR type attributes
    color_hex VARCHAR(7),

    -- Sort order for display
    sort_order INTEGER NOT NULL DEFAULT 0,

    -- Status
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT unique_value_per_attribute UNIQUE (attribute_id, value)
);

-- ============================================================
-- Table: product_variants
-- Stores product variants (e.g., "T-shirt Red M")
-- ============================================================
CREATE TABLE IF NOT EXISTS product_variants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Parent product reference
    parent_product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,

    -- Variant SKU (auto-generated or manual)
    sku VARCHAR(100) NOT NULL,
    barcode VARCHAR(100),

    -- Variant-specific details
    name VARCHAR(200),
    description TEXT,

    -- Pricing (can override parent)
    price NUMERIC(15, 2),
    cost NUMERIC(15, 2),

    -- Image URL for variant
    image_url VARCHAR(500),

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT unique_variant_sku_per_tenant UNIQUE (tenant_id, sku),
    CONSTRAINT unique_variant_barcode_per_tenant UNIQUE (tenant_id, barcode),
    CONSTRAINT check_variant_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONTINUED')),
    CONSTRAINT check_variant_price CHECK (price IS NULL OR price >= 0),
    CONSTRAINT check_variant_cost CHECK (cost IS NULL OR cost >= 0)
);

-- ============================================================
-- Table: product_variant_attributes
-- Junction table: links variants to their attribute values
-- ============================================================
CREATE TABLE IF NOT EXISTS product_variant_attributes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Variant reference
    variant_id UUID NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,

    -- Attribute and value reference
    attribute_id UUID NOT NULL REFERENCES variant_attributes(id),
    attribute_value_id UUID NOT NULL REFERENCES variant_attribute_values(id),

    -- Constraints
    CONSTRAINT unique_variant_attribute UNIQUE (variant_id, attribute_id)
);

-- ============================================================
-- Indexes for performance
-- ============================================================

-- Variant attributes indexes
CREATE INDEX idx_variant_attributes_tenant ON variant_attributes(tenant_id);
CREATE INDEX idx_variant_attributes_name ON variant_attributes(tenant_id, name);

-- Variant attribute values indexes
CREATE INDEX idx_variant_values_attribute ON variant_attribute_values(attribute_id);
CREATE INDEX idx_variant_values_value ON variant_attribute_values(attribute_id, value);

-- Product variants indexes
CREATE INDEX idx_variants_tenant ON product_variants(tenant_id);
CREATE INDEX idx_variants_parent ON product_variants(parent_product_id);
CREATE INDEX idx_variants_sku ON product_variants(tenant_id, sku);
CREATE INDEX idx_variants_barcode ON product_variants(tenant_id, barcode) WHERE barcode IS NOT NULL;
CREATE INDEX idx_variants_status ON product_variants(status) WHERE ativo = true;

-- Variant attributes junction indexes
CREATE INDEX idx_variant_attrs_variant ON product_variant_attributes(variant_id);
CREATE INDEX idx_variant_attrs_attribute ON product_variant_attributes(attribute_id);
CREATE INDEX idx_variant_attrs_value ON product_variant_attributes(attribute_value_id);

-- ============================================================
-- Triggers for updated_at
-- ============================================================
CREATE TRIGGER trigger_variant_attributes_updated_at
    BEFORE UPDATE ON variant_attributes
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

CREATE TRIGGER trigger_variants_updated_at
    BEFORE UPDATE ON product_variants
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Initial data: Create common attributes
-- ============================================================
-- TODO: Seed data commented out - references non-existent 'tenants' table in tenant schema
-- In schema-based tenancy, tenant_id should be provided by application logic
-- These seed attributes should be added via application after tenant provisioning
--
-- -- Insert Color attribute
-- INSERT INTO variant_attributes (tenant_id, name, display_name, type, sort_order)
-- SELECT
--     (SELECT id FROM tenants LIMIT 1),
--     'color',
--     'Color',
--     'COLOR',
--     1
-- WHERE EXISTS (SELECT 1 FROM tenants LIMIT 1);
--
-- -- Insert Size attribute
-- INSERT INTO variant_attributes (tenant_id, name, display_name, type, sort_order)
-- SELECT
--     (SELECT id FROM tenants LIMIT 1),
--     'size',
--     'Size',
--     'SIZE',
--     2
-- WHERE EXISTS (SELECT 1 FROM tenants LIMIT 1);
--
-- -- Insert common color values
-- INSERT INTO variant_attribute_values (tenant_id, attribute_id, value, display_value, color_hex, sort_order)
-- SELECT
--     (SELECT id FROM tenants LIMIT 1),
--     (SELECT id FROM variant_attributes WHERE name = 'color' LIMIT 1),
--     'red',
--     'Red',
--     '#FF0000',
--     1
-- WHERE EXISTS (SELECT id FROM variant_attributes WHERE name = 'color');
--
-- INSERT INTO variant_attribute_values (tenant_id, attribute_id, value, display_value, color_hex, sort_order)
-- SELECT
--     (SELECT id FROM tenants LIMIT 1),
--     (SELECT id FROM variant_attributes WHERE name = 'color' LIMIT 1),
--     'blue',
--     'Blue',
--     '#0000FF',
--     2
-- WHERE EXISTS (SELECT id FROM variant_attributes WHERE name = 'color');
--
-- INSERT INTO variant_attribute_values (tenant_id, attribute_id, value, display_value, color_hex, sort_order)
-- SELECT
--     (SELECT id FROM tenants LIMIT 1),
--     (SELECT id FROM variant_attributes WHERE name = 'color' LIMIT 1),
--     'black',
--     'Black',
--     '#000000',
--     3
-- WHERE EXISTS (SELECT id FROM variant_attributes WHERE name = 'color');
--
-- -- Insert common size values
-- INSERT INTO variant_attribute_values (tenant_id, attribute_id, value, display_value, sort_order)
-- SELECT
--     (SELECT id FROM tenants LIMIT 1),
--     (SELECT id FROM variant_attributes WHERE name = 'size' LIMIT 1),
--     'S',
--     'Small',
--     1
-- WHERE EXISTS (SELECT id FROM variant_attributes WHERE name = 'size');
--
-- INSERT INTO variant_attribute_values (tenant_id, attribute_id, value, display_value, sort_order)
-- SELECT
--     (SELECT id FROM tenants LIMIT 1),
--     (SELECT id FROM variant_attributes WHERE name = 'size' LIMIT 1),
--     'M',
--     'Medium',
--     2
-- WHERE EXISTS (SELECT id FROM variant_attributes WHERE name = 'size');
--
-- INSERT INTO variant_attribute_values (tenant_id, attribute_id, value, display_value, sort_order)
-- SELECT
--     (SELECT id FROM tenants LIMIT 1),
--     (SELECT id FROM variant_attributes WHERE name = 'size' LIMIT 1),
--     'L',
--     'Large',
--     3
-- WHERE EXISTS (SELECT id FROM variant_attributes WHERE name = 'size');

-- ============================================================
-- Update existing products table to support parent products
-- ============================================================

-- Add column to track if product has variants
ALTER TABLE products ADD COLUMN IF NOT EXISTS has_variants BOOLEAN NOT NULL DEFAULT false;

-- Add index for parent products
CREATE INDEX IF NOT EXISTS idx_products_has_variants ON products(has_variants) WHERE has_variants = true AND ativo = true;

-- ============================================================
-- Example: Create a variant parent product and its variants
-- ============================================================
-- TODO: Seed data commented out - references non-existent tables
-- Sample variant products should be added via application logic after tenant provisioning
--
-- -- Create parent product "T-shirt Basic"
-- INSERT INTO products (id, tenant_id, type, name, sku, description, category_id, price, cost, unit, controls_inventory, status, has_variants)
-- SELECT
--     gen_random_uuid(),
--     (SELECT id FROM tenants LIMIT 1),
--     'VARIANT_PARENT',
--     'T-shirt Basic',
--     'TSHIRT-BASIC-PARENT',
--     'Basic cotton t-shirt with multiple color and size options',
--     (SELECT id FROM categories WHERE name = 'Vestuário' LIMIT 1),
--     29.90,
--     15.00,
--     'UN',
--     true,
--     'ACTIVE',
--     true
-- WHERE EXISTS (SELECT 1 FROM categories WHERE name = 'Vestuário')
-- RETURNING id;

-- Note: Variants would be created via API endpoints with proper SKU generation
-- Example variant SKUs: TSHIRT-BASIC-RED-M, TSHIRT-BASIC-BLUE-L, etc.
