-- V006__create_products_table.sql
-- Story 2.2: Simple Products CRUD
-- Creates products table with support for simple products (no variants)

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Product type (for future expansion: SIMPLE, VARIANT_PARENT, VARIANT)
    type VARCHAR(20) NOT NULL DEFAULT 'SIMPLE',

    -- Basic product information
    name VARCHAR(200) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    barcode VARCHAR(100),
    description TEXT,

    -- Category relationship
    category_id UUID NOT NULL REFERENCES categories(id),

    -- Pricing
    price NUMERIC(15, 2) NOT NULL,
    cost NUMERIC(15, 2),

    -- Unit of measure
    unit VARCHAR(20) NOT NULL DEFAULT 'UN',

    -- Inventory control flag
    controls_inventory BOOLEAN NOT NULL DEFAULT true,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- Audit fields
    ativo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT unique_sku_per_tenant UNIQUE (tenant_id, sku),
    CONSTRAINT unique_barcode_per_tenant UNIQUE (tenant_id, barcode),
    CONSTRAINT check_product_type CHECK (type IN ('SIMPLE', 'VARIANT_PARENT', 'VARIANT')),
    CONSTRAINT check_product_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONTINUED')),
    CONSTRAINT check_positive_price CHECK (price >= 0),
    CONSTRAINT check_positive_cost CHECK (cost IS NULL OR cost >= 0)
);

-- Indexes for performance
CREATE INDEX idx_products_tenant_id ON products(tenant_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_sku ON products(tenant_id, sku);
CREATE INDEX idx_products_barcode ON products(tenant_id, barcode) WHERE barcode IS NOT NULL;
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_status ON products(status) WHERE ativo = true;
CREATE INDEX idx_products_type ON products(type);

-- Insert example products for testing (will use existing category IDs from V005)
-- Electronics → Computers → Notebooks
INSERT INTO products (id, tenant_id, type, name, sku, barcode, description, category_id, price, cost, unit, controls_inventory, status)
SELECT
    gen_random_uuid(),
    (SELECT id FROM tenants LIMIT 1),
    'SIMPLE',
    'Notebook Dell Inspiron 15',
    'NOTE-DELL-I15-001',
    '7891234567890',
    'Notebook Dell Inspiron 15 - Intel Core i7, 16GB RAM, 512GB SSD',
    (SELECT id FROM categories WHERE name = 'Notebooks' LIMIT 1),
    4500.00,
    3200.00,
    'UN',
    true,
    'ACTIVE'
WHERE EXISTS (SELECT 1 FROM categories WHERE name = 'Notebooks');

INSERT INTO products (id, tenant_id, type, name, sku, barcode, description, category_id, price, cost, unit, controls_inventory, status)
SELECT
    gen_random_uuid(),
    (SELECT id FROM tenants LIMIT 1),
    'SIMPLE',
    'Mouse Logitech MX Master 3',
    'MOUSE-LOG-MX3-001',
    '7891234567891',
    'Mouse sem fio Logitech MX Master 3 - Ergonômico',
    (SELECT id FROM categories WHERE name = 'Periféricos' LIMIT 1),
    350.00,
    200.00,
    'UN',
    true,
    'ACTIVE'
WHERE EXISTS (SELECT 1 FROM categories WHERE name = 'Periféricos');

INSERT INTO products (id, tenant_id, type, name, sku, barcode, description, category_id, price, cost, unit, controls_inventory, status)
SELECT
    gen_random_uuid(),
    (SELECT id FROM tenants LIMIT 1),
    'SIMPLE',
    'Teclado Mecânico Keychron K8',
    'KEYB-KEYCH-K8-001',
    NULL,
    'Teclado mecânico sem fio Keychron K8 - Switch Brown',
    (SELECT id FROM categories WHERE name = 'Periféricos' LIMIT 1),
    650.00,
    450.00,
    'UN',
    true,
    'ACTIVE'
WHERE EXISTS (SELECT 1 FROM categories WHERE name = 'Periféricos');
