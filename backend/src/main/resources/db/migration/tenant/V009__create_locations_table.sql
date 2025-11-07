-- V009__create_locations_table.sql
-- Story 3.2: Inventory Locations
-- Creates locations table for multi-location inventory management

-- ============================================================
-- Table: locations
-- Stores warehouse/store locations
-- ============================================================
CREATE TABLE IF NOT EXISTS locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Location details
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,

    -- Location type
    type VARCHAR(20) NOT NULL DEFAULT 'WAREHOUSE',

    -- Address
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(50),
    postal_code VARCHAR(20),
    country VARCHAR(50),

    -- Contact
    phone VARCHAR(50),
    email VARCHAR(200),

    -- Manager
    manager_name VARCHAR(200),
    manager_id UUID,

    -- Settings
    is_default BOOLEAN NOT NULL DEFAULT false,
    allow_negative_stock BOOLEAN NOT NULL DEFAULT false,

    -- Status
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT unique_location_code_per_tenant UNIQUE (tenant_id, code),
    CONSTRAINT check_location_type CHECK (type IN ('WAREHOUSE', 'STORE', 'DISTRIBUTION_CENTER', 'SUPPLIER', 'CUSTOMER', 'TRANSIT', 'QUARANTINE'))
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_locations_tenant ON locations(tenant_id);
CREATE INDEX idx_locations_code ON locations(tenant_id, code);
CREATE INDEX idx_locations_type ON locations(type) WHERE ativo = true;
CREATE INDEX idx_locations_default ON locations(is_default) WHERE is_default = true AND ativo = true;

-- ============================================================
-- Trigger for updated_at
-- ============================================================
CREATE TRIGGER trigger_locations_updated_at
    BEFORE UPDATE ON locations
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Update inventory table to reference locations
-- ============================================================

-- Drop old location VARCHAR column
ALTER TABLE inventory DROP COLUMN IF EXISTS location;

-- Add FK to locations table
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS location_id UUID REFERENCES locations(id);

-- Update unique constraint to use location_id
ALTER TABLE inventory DROP CONSTRAINT IF EXISTS unique_product_location;
ALTER TABLE inventory ADD CONSTRAINT unique_product_location_id UNIQUE (product_id, location_id);

-- Add index for location_id
CREATE INDEX IF NOT EXISTS idx_inventory_location_id ON inventory(location_id);

-- ============================================================
-- Update inventory_movements table
-- ============================================================

-- Drop old location VARCHAR column
ALTER TABLE inventory_movements DROP COLUMN IF EXISTS location;

-- Add FK to locations table for source/destination
ALTER TABLE inventory_movements ADD COLUMN IF NOT EXISTS location_id UUID REFERENCES locations(id);

-- For transfers, add destination location
ALTER TABLE inventory_movements ADD COLUMN IF NOT EXISTS destination_location_id UUID REFERENCES locations(id);

-- Update type constraint to ensure TRANSFER has destination
ALTER TABLE inventory_movements DROP CONSTRAINT IF EXISTS check_movement_type;
ALTER TABLE inventory_movements ADD CONSTRAINT check_movement_type
    CHECK (type IN ('IN', 'OUT', 'ADJUSTMENT', 'TRANSFER', 'RESERVE', 'UNRESERVE'));

-- Add index
CREATE INDEX IF NOT EXISTS idx_movements_location_id ON inventory_movements(location_id);
CREATE INDEX IF NOT EXISTS idx_movements_destination_id ON inventory_movements(destination_location_id) WHERE destination_location_id IS NOT NULL;

-- ============================================================
-- Initial data: Create default location
-- ============================================================
INSERT INTO locations (tenant_id, code, name, description, type, is_default)
SELECT
    t.id,
    'MAIN',
    'Main Warehouse',
    'Default main warehouse location',
    'WAREHOUSE',
    true
FROM tenants t
ON CONFLICT (tenant_id, code) DO NOTHING;

-- ============================================================
-- Migrate existing inventory to default location
-- ============================================================
UPDATE inventory
SET location_id = (
    SELECT id FROM locations
    WHERE tenant_id = inventory.tenant_id
    AND is_default = true
    LIMIT 1
)
WHERE location_id IS NULL;

-- ============================================================
-- Migrate existing movements to default location
-- ============================================================
UPDATE inventory_movements
SET location_id = (
    SELECT id FROM locations
    WHERE tenant_id = inventory_movements.tenant_id
    AND is_default = true
    LIMIT 1
)
WHERE location_id IS NULL;

-- ============================================================
-- Add NOT NULL constraint after migration
-- ============================================================
ALTER TABLE inventory ALTER COLUMN location_id SET NOT NULL;
ALTER TABLE inventory_movements ALTER COLUMN location_id SET NOT NULL;

-- ============================================================
-- Example: Create additional locations
-- ============================================================

-- Store location
INSERT INTO locations (tenant_id, code, name, description, type, city, state, country)
SELECT
    t.id,
    'STORE-01',
    'Store Downtown',
    'Main retail store - Downtown location',
    'STORE',
    'São Paulo',
    'SP',
    'Brazil'
FROM tenants t
WHERE EXISTS (SELECT 1 FROM tenants)
ON CONFLICT (tenant_id, code) DO NOTHING;

-- Distribution center
INSERT INTO locations (tenant_id, code, name, description, type, city, state, country)
SELECT
    t.id,
    'DC-01',
    'Distribution Center North',
    'Regional distribution center - North region',
    'DISTRIBUTION_CENTER',
    'São Paulo',
    'SP',
    'Brazil'
FROM tenants t
WHERE EXISTS (SELECT 1 FROM tenants)
ON CONFLICT (tenant_id, code) DO NOTHING;

-- ============================================================
-- View: Inventory summary by location
-- ============================================================
CREATE OR REPLACE VIEW v_inventory_by_location AS
SELECT
    l.id AS location_id,
    l.code AS location_code,
    l.name AS location_name,
    l.type AS location_type,
    COUNT(DISTINCT i.product_id) AS product_count,
    SUM(i.quantity) AS total_quantity,
    SUM(i.reserved_quantity) AS total_reserved,
    SUM(i.available_quantity) AS total_available,
    COUNT(CASE WHEN i.quantity <= i.min_quantity THEN 1 END) AS low_stock_count
FROM locations l
LEFT JOIN inventory i ON l.id = i.location_id AND i.quantity > 0
WHERE l.ativo = true
GROUP BY l.id, l.code, l.name, l.type;
