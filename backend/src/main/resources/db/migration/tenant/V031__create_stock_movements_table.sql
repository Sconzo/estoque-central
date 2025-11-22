-- V031__create_stock_movements_table.sql
-- Story 2.8: Stock Movement History
-- Creates immutable audit trail of all stock movements

-- ============================================================
-- Table: stock_movements
-- Immutable (append-only) audit trail of stock movements
-- ============================================================
CREATE TABLE IF NOT EXISTS stock_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Product/Variant reference (XOR constraint)
    product_id UUID REFERENCES products(id),
    variant_id UUID REFERENCES product_variants(id),
    stock_location_id UUID NOT NULL REFERENCES locations(id),

    -- Movement type
    type VARCHAR(50) NOT NULL,

    -- Quantity (positive = entry, negative = exit)
    quantity NUMERIC(15, 3) NOT NULL,

    -- Balance tracking (before and after movement)
    balance_before NUMERIC(15, 3) NOT NULL,
    balance_after NUMERIC(15, 3) NOT NULL,

    -- Audit: who performed the movement
    user_id UUID NOT NULL,

    -- Document reference (generic FK)
    document_type VARCHAR(50),
    document_id UUID,

    -- Reason/observation
    reason TEXT,

    -- Immutable timestamp
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_product_or_variant_movement CHECK (product_id IS NOT NULL OR variant_id IS NOT NULL),
    CONSTRAINT check_balance_calculation CHECK (balance_after = balance_before + quantity),
    CONSTRAINT check_movement_type CHECK (type IN (
        'ENTRY',            -- Manual entry
        'EXIT',             -- Manual exit
        'TRANSFER_OUT',     -- Transfer out
        'TRANSFER_IN',      -- Transfer in
        'ADJUSTMENT',       -- Inventory adjustment
        'SALE',             -- Sale (exit)
        'PURCHASE',         -- Purchase (entry)
        'RESERVE',          -- Reserve stock
        'RELEASE',          -- Release reservation
        'BOM_ASSEMBLY',     -- BOM assembly
        'BOM_DISASSEMBLY'   -- BOM disassembly
    ))
);

-- ============================================================
-- Indexes for performance
-- ============================================================

-- Primary indexes for lookups
CREATE INDEX idx_stock_movements_tenant ON stock_movements(tenant_id);
CREATE INDEX idx_stock_movements_product ON stock_movements(tenant_id, product_id) WHERE product_id IS NOT NULL;
CREATE INDEX idx_stock_movements_variant ON stock_movements(tenant_id, variant_id) WHERE variant_id IS NOT NULL;
CREATE INDEX idx_stock_movements_location ON stock_movements(tenant_id, stock_location_id);
CREATE INDEX idx_stock_movements_user ON stock_movements(user_id);

-- Timestamp index for chronological queries
CREATE INDEX idx_stock_movements_created_at ON stock_movements(created_at DESC);

-- Composite index for timeline queries
CREATE INDEX idx_stock_movements_product_location_created ON stock_movements(product_id, stock_location_id, created_at DESC);
CREATE INDEX idx_stock_movements_variant_location_created ON stock_movements(variant_id, stock_location_id, created_at DESC);

-- Document tracking index
CREATE INDEX idx_stock_movements_document ON stock_movements(document_type, document_id)
    WHERE document_type IS NOT NULL AND document_id IS NOT NULL;

-- Type filter index
CREATE INDEX idx_stock_movements_type ON stock_movements(type);

-- ============================================================
-- Comments for documentation
-- ============================================================
COMMENT ON TABLE stock_movements IS 'Immutable audit trail of all stock movements (append-only, no UPDATE or DELETE)';
COMMENT ON COLUMN stock_movements.product_id IS 'Product ID (NULL for variants)';
COMMENT ON COLUMN stock_movements.variant_id IS 'Variant ID (NULL for simple/composite products)';
COMMENT ON COLUMN stock_movements.type IS 'Movement type: ENTRY, EXIT, TRANSFER_OUT, TRANSFER_IN, ADJUSTMENT, SALE, PURCHASE, RESERVE, RELEASE, BOM_ASSEMBLY, BOM_DISASSEMBLY';
COMMENT ON COLUMN stock_movements.quantity IS 'Quantity moved (positive = entry, negative = exit)';
COMMENT ON COLUMN stock_movements.balance_before IS 'Stock balance before movement';
COMMENT ON COLUMN stock_movements.balance_after IS 'Stock balance after movement (must equal balance_before + quantity)';
COMMENT ON COLUMN stock_movements.document_type IS 'Type of source document (SALE, PURCHASE, TRANSFER, etc.)';
COMMENT ON COLUMN stock_movements.document_id IS 'ID of source document';

-- ============================================================
-- View: Latest balance per product/location
-- ============================================================
CREATE OR REPLACE VIEW v_stock_latest_balance AS
SELECT DISTINCT ON (tenant_id, COALESCE(product_id, variant_id), stock_location_id)
    tenant_id,
    product_id,
    variant_id,
    stock_location_id,
    balance_after AS latest_balance,
    created_at AS last_movement_at
FROM stock_movements
ORDER BY tenant_id, COALESCE(product_id, variant_id), stock_location_id, created_at DESC;

COMMENT ON VIEW v_stock_latest_balance IS 'Latest stock balance per product/variant and location (for validation)';

-- ============================================================
-- Function: Prevent UPDATE and DELETE
-- ============================================================
CREATE OR REPLACE FUNCTION prevent_stock_movement_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'stock_movements table is immutable: UPDATE and DELETE operations are not allowed';
END;
$$ LANGUAGE plpgsql;

-- Trigger to prevent UPDATE
CREATE TRIGGER prevent_stock_movement_update
    BEFORE UPDATE ON stock_movements
    FOR EACH ROW
    EXECUTE FUNCTION prevent_stock_movement_modification();

-- Trigger to prevent DELETE
CREATE TRIGGER prevent_stock_movement_delete
    BEFORE DELETE ON stock_movements
    FOR EACH ROW
    EXECUTE FUNCTION prevent_stock_movement_modification();

-- ============================================================
-- Success message
-- ============================================================
DO $$
BEGIN
    RAISE NOTICE 'Migration V031 completed successfully: stock_movements table created (immutable)';
END $$;
