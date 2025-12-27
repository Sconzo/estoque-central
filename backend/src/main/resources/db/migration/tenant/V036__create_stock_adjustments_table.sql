-- Story 3.5: Stock Adjustment (Ajuste de Estoque)
-- Migration: V036__create_stock_adjustments_table.sql
-- Creates stock_adjustments table for manual stock adjustments with audit trail

CREATE TABLE IF NOT EXISTS stock_adjustments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    adjustment_number VARCHAR(20) NOT NULL,
    product_id UUID,
    variant_id UUID,
    stock_location_id UUID NOT NULL,
    adjustment_type VARCHAR(10) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    reason_code VARCHAR(50) NOT NULL,
    reason_description TEXT NOT NULL,
    adjusted_by_user_id UUID NOT NULL,
    adjustment_date DATE NOT NULL,
    balance_before DECIMAL(10,2),
    balance_after DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_adjustments_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT fk_adjustments_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE RESTRICT,
    CONSTRAINT fk_adjustments_location FOREIGN KEY (stock_location_id) REFERENCES locations(id) ON DELETE RESTRICT,

    -- Constraints
    CONSTRAINT uq_adjustment_number_per_tenant UNIQUE (tenant_id, adjustment_number),
    CONSTRAINT chk_adjustment_type CHECK (adjustment_type IN ('INCREASE', 'DECREASE')),
    CONSTRAINT chk_reason_code CHECK (reason_code IN ('INVENTORY', 'LOSS', 'DAMAGE', 'THEFT', 'ERROR', 'OTHER')),
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0)
);

-- Indexes for performance
CREATE INDEX idx_adjustments_tenant_id ON stock_adjustments(tenant_id);
CREATE INDEX idx_adjustments_product_id ON stock_adjustments(product_id);
CREATE INDEX idx_adjustments_location_id ON stock_adjustments(stock_location_id);
CREATE INDEX idx_adjustments_adjustment_date ON stock_adjustments(adjustment_date);
CREATE INDEX idx_adjustments_adjustment_number ON stock_adjustments(tenant_id, adjustment_number);

-- Comments
COMMENT ON TABLE stock_adjustments IS 'Manual stock adjustments for inventory corrections, losses, damages, theft, and errors (Story 3.5)';
COMMENT ON COLUMN stock_adjustments.adjustment_number IS 'Auto-generated adjustment number format: ADJ-YYYYMM-0001';
COMMENT ON COLUMN stock_adjustments.adjustment_type IS 'INCREASE (manual entry) or DECREASE (manual exit)';
COMMENT ON COLUMN stock_adjustments.reason_code IS 'Predefined reason codes for adjustment categorization';
COMMENT ON COLUMN stock_adjustments.reason_description IS 'Detailed justification (mandatory, min 10 characters)';
COMMENT ON COLUMN stock_adjustments.balance_before IS 'Stock quantity before adjustment';
COMMENT ON COLUMN stock_adjustments.balance_after IS 'Stock quantity after adjustment';
