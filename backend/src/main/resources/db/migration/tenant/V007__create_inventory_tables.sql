-- V007__create_inventory_tables.sql
-- Story 3.1: Basic Inventory Control
-- Creates inventory tracking tables with movement history

-- ============================================================
-- Table: inventory
-- Stores current inventory levels for each product
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Product reference
    product_id UUID NOT NULL REFERENCES products(id),

    -- Inventory levels
    quantity NUMERIC(15, 3) NOT NULL DEFAULT 0,
    reserved_quantity NUMERIC(15, 3) NOT NULL DEFAULT 0,
    available_quantity NUMERIC(15, 3) GENERATED ALWAYS AS (quantity - reserved_quantity) STORED,

    -- Min/Max levels for alerts
    min_quantity NUMERIC(15, 3),
    max_quantity NUMERIC(15, 3),

    -- Location (future: multi-location support)
    location VARCHAR(100) DEFAULT 'DEFAULT',

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT unique_product_location UNIQUE (product_id, location),
    CONSTRAINT check_quantity_positive CHECK (quantity >= 0),
    CONSTRAINT check_reserved_positive CHECK (reserved_quantity >= 0),
    CONSTRAINT check_reserved_not_exceed CHECK (reserved_quantity <= quantity),
    CONSTRAINT check_min_max CHECK (min_quantity IS NULL OR max_quantity IS NULL OR min_quantity <= max_quantity)
);

-- ============================================================
-- Table: inventory_movements
-- Stores history of all inventory movements
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Product reference
    product_id UUID NOT NULL REFERENCES products(id),

    -- Movement details
    type VARCHAR(20) NOT NULL,
    quantity NUMERIC(15, 3) NOT NULL,
    location VARCHAR(100) DEFAULT 'DEFAULT',

    -- Before/after quantities for audit
    quantity_before NUMERIC(15, 3) NOT NULL,
    quantity_after NUMERIC(15, 3) NOT NULL,

    -- Reason and notes
    reason VARCHAR(50),
    notes TEXT,

    -- Reference to external document (purchase order, sale, etc.)
    reference_type VARCHAR(50),
    reference_id UUID,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,

    -- Constraints
    CONSTRAINT check_movement_type CHECK (type IN ('IN', 'OUT', 'ADJUSTMENT', 'TRANSFER', 'RESERVE', 'UNRESERVE')),
    CONSTRAINT check_movement_reason CHECK (reason IN ('PURCHASE', 'SALE', 'RETURN', 'ADJUSTMENT', 'TRANSFER', 'DAMAGED', 'LOST', 'FOUND', 'INITIAL', 'RESERVATION', 'UNRESERVATION'))
);

-- ============================================================
-- Indexes for performance
-- ============================================================

-- Inventory indexes
CREATE INDEX idx_inventory_tenant_id ON inventory(tenant_id);
CREATE INDEX idx_inventory_product_id ON inventory(product_id);
CREATE INDEX idx_inventory_location ON inventory(location);
CREATE INDEX idx_inventory_low_stock ON inventory(quantity, min_quantity) WHERE min_quantity IS NOT NULL AND quantity <= min_quantity;

-- Inventory movements indexes
CREATE INDEX idx_movements_tenant_id ON inventory_movements(tenant_id);
CREATE INDEX idx_movements_product_id ON inventory_movements(product_id);
CREATE INDEX idx_movements_type ON inventory_movements(type);
CREATE INDEX idx_movements_created_at ON inventory_movements(created_at DESC);
CREATE INDEX idx_movements_reference ON inventory_movements(reference_type, reference_id) WHERE reference_type IS NOT NULL;

-- ============================================================
-- Function: Update inventory updated_at timestamp
-- ============================================================
CREATE OR REPLACE FUNCTION update_inventory_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_inventory_updated_at
    BEFORE UPDATE ON inventory
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Initial data: Create inventory records for existing products
-- ============================================================
INSERT INTO inventory (tenant_id, product_id, quantity, reserved_quantity, min_quantity, max_quantity, location)
SELECT
    p.tenant_id,
    p.id,
    0,
    0,
    10,  -- Default min quantity
    1000,  -- Default max quantity
    'DEFAULT'
FROM products p
WHERE p.controls_inventory = true
  AND p.ativo = true
ON CONFLICT (product_id, location) DO NOTHING;

-- ============================================================
-- Example: Add initial inventory for sample products
-- ============================================================
-- Add 50 units to "Notebook Dell Inspiron 15"
INSERT INTO inventory_movements (tenant_id, product_id, type, quantity, quantity_before, quantity_after, reason, notes, created_by)
SELECT
    p.tenant_id,
    p.id,
    'IN',
    50,
    0,
    50,
    'INITIAL',
    'Initial inventory setup',
    (SELECT created_by FROM products WHERE id = p.id)
FROM products p
WHERE p.sku = 'NOTE-DELL-I15-001'
  AND p.ativo = true;

UPDATE inventory
SET quantity = 50
WHERE product_id = (SELECT id FROM products WHERE sku = 'NOTE-DELL-I15-001' LIMIT 1)
  AND location = 'DEFAULT';

-- Add 100 units to "Mouse Logitech MX Master 3"
INSERT INTO inventory_movements (tenant_id, product_id, type, quantity, quantity_before, quantity_after, reason, notes, created_by)
SELECT
    p.tenant_id,
    p.id,
    'IN',
    100,
    0,
    100,
    'INITIAL',
    'Initial inventory setup',
    (SELECT created_by FROM products WHERE id = p.id)
FROM products p
WHERE p.sku = 'MOUSE-LOG-MX3-001'
  AND p.ativo = true;

UPDATE inventory
SET quantity = 100
WHERE product_id = (SELECT id FROM products WHERE sku = 'MOUSE-LOG-MX3-001' LIMIT 1)
  AND location = 'DEFAULT';

-- Add 75 units to "Teclado Mecânico Keychron K8"
INSERT INTO inventory_movements (tenant_id, product_id, type, quantity, quantity_before, quantity_after, reason, notes, created_by)
SELECT
    p.tenant_id,
    p.id,
    'IN',
    75,
    0,
    75,
    'INITIAL',
    'Initial inventory setup',
    (SELECT created_by FROM products WHERE id = p.id)
FROM products p
WHERE p.sku = 'KEYB-KEYCH-K8-001'
  AND p.ativo = true;

UPDATE inventory
SET quantity = 75
WHERE product_id = (SELECT id FROM products WHERE sku = 'KEYB-KEYCH-K8-001' LIMIT 1)
  AND location = 'DEFAULT';
