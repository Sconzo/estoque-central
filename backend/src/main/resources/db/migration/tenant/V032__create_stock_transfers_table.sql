-- V032__create_stock_transfers_table.sql
-- Story 2.9: Stock Transfer Between Locations
-- Creates table for tracking stock transfers between locations

-- ============================================================
-- Table: stock_transfers
-- Tracks transfers of stock between different locations
-- ============================================================
CREATE TABLE IF NOT EXISTS stock_transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Product/Variant reference (XOR constraint)
    product_id UUID REFERENCES products(id),
    variant_id UUID REFERENCES product_variants(id),

    -- Origin and destination locations
    origin_location_id UUID NOT NULL REFERENCES locations(id),
    destination_location_id UUID NOT NULL REFERENCES locations(id),

    -- Transfer details
    quantity NUMERIC(15, 3) NOT NULL,
    reason TEXT,

    -- Audit: who performed the transfer
    user_id UUID NOT NULL,

    -- Status tracking
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_product_or_variant_transfer CHECK (product_id IS NOT NULL OR variant_id IS NOT NULL),
    CONSTRAINT check_different_locations CHECK (origin_location_id != destination_location_id),
    CONSTRAINT check_positive_quantity CHECK (quantity > 0),
    CONSTRAINT check_transfer_status CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED'))
);

-- ============================================================
-- Indexes for performance
-- ============================================================

-- Primary indexes for lookups
CREATE INDEX idx_stock_transfers_tenant ON stock_transfers(tenant_id);
CREATE INDEX idx_stock_transfers_product ON stock_transfers(tenant_id, product_id) WHERE product_id IS NOT NULL;
CREATE INDEX idx_stock_transfers_variant ON stock_transfers(tenant_id, variant_id) WHERE variant_id IS NOT NULL;
CREATE INDEX idx_stock_transfers_origin ON stock_transfers(tenant_id, origin_location_id);
CREATE INDEX idx_stock_transfers_destination ON stock_transfers(tenant_id, destination_location_id);
CREATE INDEX idx_stock_transfers_user ON stock_transfers(user_id);

-- Timestamp index for chronological queries
CREATE INDEX idx_stock_transfers_created_at ON stock_transfers(created_at DESC);

-- Status index for filtering
CREATE INDEX idx_stock_transfers_status ON stock_transfers(status);

-- Composite index for common queries
CREATE INDEX idx_stock_transfers_product_created ON stock_transfers(product_id, created_at DESC);
CREATE INDEX idx_stock_transfers_locations ON stock_transfers(origin_location_id, destination_location_id);

-- ============================================================
-- Comments for documentation
-- ============================================================
COMMENT ON TABLE stock_transfers IS 'Tracks stock transfers between locations with complete audit trail';
COMMENT ON COLUMN stock_transfers.product_id IS 'Product ID (NULL for variants)';
COMMENT ON COLUMN stock_transfers.variant_id IS 'Variant ID (NULL for simple/composite products)';
COMMENT ON COLUMN stock_transfers.origin_location_id IS 'Source location for transfer';
COMMENT ON COLUMN stock_transfers.destination_location_id IS 'Destination location for transfer';
COMMENT ON COLUMN stock_transfers.quantity IS 'Quantity transferred (must be positive)';
COMMENT ON COLUMN stock_transfers.status IS 'Transfer status: PENDING, COMPLETED, CANCELLED';

-- ============================================================
-- View: Transfer history with enriched data
-- ============================================================
CREATE OR REPLACE VIEW v_stock_transfers_history AS
SELECT
    st.id,
    st.tenant_id,
    st.product_id,
    st.variant_id,
    COALESCE(p.name, pv_parent.name || ' - ' || pv.sku) AS product_name,
    COALESCE(p.sku, pv.sku) AS product_sku,
    st.origin_location_id,
    loc_origin.name AS origin_location_name,
    loc_origin.code AS origin_location_code,
    st.destination_location_id,
    loc_dest.name AS destination_location_name,
    loc_dest.code AS destination_location_code,
    st.quantity,
    st.reason,
    st.user_id,
    st.status,
    st.created_at,
    st.updated_at
FROM stock_transfers st
LEFT JOIN products p ON st.product_id = p.id
LEFT JOIN product_variants pv ON st.variant_id = pv.id
LEFT JOIN products pv_parent ON pv.parent_product_id = pv_parent.id
INNER JOIN locations loc_origin ON st.origin_location_id = loc_origin.id
INNER JOIN locations loc_dest ON st.destination_location_id = loc_dest.id
WHERE loc_origin.ativo = true AND loc_dest.ativo = true;

COMMENT ON VIEW v_stock_transfers_history IS 'Transfer history with enriched product and location information';

-- ============================================================
-- Function: Update timestamp on modification
-- ============================================================
CREATE OR REPLACE FUNCTION update_stock_transfer_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to update timestamp
CREATE TRIGGER update_stock_transfer_updated_at
    BEFORE UPDATE ON stock_transfers
    FOR EACH ROW
    EXECUTE FUNCTION update_stock_transfer_timestamp();

-- ============================================================
-- Success message
-- ============================================================
DO $$
BEGIN
    RAISE NOTICE 'Migration V032 completed successfully: stock_transfers table created';
END $$;
