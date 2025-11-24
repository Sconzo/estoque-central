-- Migration V048: Create sales table
-- Story 4.3: NFCe Emission and Stock Decrease

CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    sale_number VARCHAR(20) NOT NULL,
    customer_id UUID,
    stock_location_id UUID NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_amount_received DECIMAL(10, 2) NOT NULL,
    change_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    nfce_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    nfce_key VARCHAR(44),
    nfce_xml TEXT,
    created_by_user_id UUID NOT NULL,
    sale_date TIMESTAMP NOT NULL DEFAULT NOW(),
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_sales_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_sales_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_sales_stock_location FOREIGN KEY (stock_location_id) REFERENCES stock_locations(id),
    CONSTRAINT fk_sales_user FOREIGN KEY (created_by_user_id) REFERENCES users(id),

    CONSTRAINT chk_payment_method CHECK (payment_method IN ('DINHEIRO', 'DEBITO', 'CREDITO', 'PIX')),
    CONSTRAINT chk_nfce_status CHECK (nfce_status IN ('PENDING', 'EMITTED', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_total_amount_positive CHECK (total_amount >= 0),
    CONSTRAINT chk_payment_received_positive CHECK (payment_amount_received >= 0),
    CONSTRAINT chk_change_positive CHECK (change_amount >= 0),

    CONSTRAINT uk_sales_tenant_number UNIQUE (tenant_id, sale_number)
);

-- Indexes for query performance
CREATE INDEX idx_sales_tenant_id ON sales(tenant_id);
CREATE INDEX idx_sales_sale_number ON sales(tenant_id, sale_number);
CREATE INDEX idx_sales_nfce_status ON sales(tenant_id, nfce_status);
CREATE INDEX idx_sales_customer_id ON sales(tenant_id, customer_id);
CREATE INDEX idx_sales_sale_date ON sales(tenant_id, sale_date DESC);

-- Comments
COMMENT ON TABLE sales IS 'Story 4.3: Sales transactions with NFCe emission';
COMMENT ON COLUMN sales.sale_number IS 'Auto-generated: SALE-YYYYMM-0001';
COMMENT ON COLUMN sales.nfce_status IS 'PENDING: awaiting emission, EMITTED: success, FAILED: retry needed, CANCELLED: cancelled';
COMMENT ON COLUMN sales.nfce_key IS 'NFCe access key (44 digits) from SEFAZ';
COMMENT ON COLUMN sales.nfce_xml IS 'Full NFCe XML for audit trail';
