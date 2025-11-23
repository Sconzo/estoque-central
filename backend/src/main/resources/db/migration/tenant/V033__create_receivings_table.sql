-- Story 3.4: Create receivings table for purchase order receiving processing
-- Migration: V033__create_receivings_table.sql

CREATE TABLE IF NOT EXISTS receivings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    receiving_number VARCHAR(20) NOT NULL,
    purchase_order_id UUID NOT NULL,
    stock_location_id UUID NOT NULL,
    receiving_date DATE NOT NULL,
    received_by_user_id UUID NOT NULL,
    notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_receivings_tenant FOREIGN KEY (tenant_id) REFERENCES public.tenants(id),
    CONSTRAINT fk_receivings_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    CONSTRAINT fk_receivings_stock_location FOREIGN KEY (stock_location_id) REFERENCES stock_locations(id),
    CONSTRAINT chk_receiving_status CHECK (status IN ('COMPLETED', 'CANCELLED')),
    CONSTRAINT uq_receiving_number_per_tenant UNIQUE (tenant_id, receiving_number)
);

-- Indexes for performance
CREATE INDEX idx_receivings_tenant_id ON receivings(tenant_id);
CREATE INDEX idx_receivings_purchase_order_id ON receivings(purchase_order_id);
CREATE INDEX idx_receivings_receiving_number ON receivings(receiving_number);
CREATE INDEX idx_receivings_receiving_date ON receivings(receiving_date);
CREATE INDEX idx_receivings_stock_location_id ON receivings(stock_location_id);

-- Comment
COMMENT ON TABLE receivings IS 'Stores purchase order receiving transactions (Story 3.4)';
COMMENT ON COLUMN receivings.receiving_number IS 'Auto-generated receiving number: RCV-YYYYMM-0001';
COMMENT ON COLUMN receivings.status IS 'Receiving status: COMPLETED or CANCELLED';
