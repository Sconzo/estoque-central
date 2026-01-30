-- Migration V051: Create sales_orders table for B2B sales
-- Story 4.5: Sales Order B2B Interface
-- Supports wholesale/B2B order processing with customer, location, and delivery tracking

CREATE TABLE sales_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    customer_id UUID NOT NULL,
    stock_location_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    order_date DATE NOT NULL DEFAULT CURRENT_DATE,
    delivery_date_expected DATE,
    payment_terms VARCHAR(20),
    total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    created_by_user_id UUID NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by UUID,

    CONSTRAINT fk_sales_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_sales_orders_location FOREIGN KEY (stock_location_id) REFERENCES locations(id),
    -- Note: created_by_user_id and updated_by reference public.users (cross-schema, no FK constraint)

    CONSTRAINT chk_status CHECK (status IN ('DRAFT', 'CONFIRMED', 'INVOICED', 'CANCELLED')),
    CONSTRAINT chk_payment_terms CHECK (payment_terms IN ('A_VISTA', 'DIAS_7', 'DIAS_14', 'DIAS_30', 'DIAS_60', 'DIAS_90')),
    CONSTRAINT chk_total_amount CHECK (total_amount >= 0)
);

-- Indexes for performance
CREATE INDEX idx_sales_orders_tenant_id ON sales_orders(tenant_id);
CREATE UNIQUE INDEX idx_sales_orders_order_number ON sales_orders(tenant_id, order_number);
CREATE INDEX idx_sales_orders_customer_id ON sales_orders(tenant_id, customer_id);
CREATE INDEX idx_sales_orders_status ON sales_orders(tenant_id, status);
CREATE INDEX idx_sales_orders_order_date ON sales_orders(tenant_id, order_date DESC);
CREATE INDEX idx_sales_orders_location ON sales_orders(tenant_id, stock_location_id);

-- Comments
COMMENT ON TABLE sales_orders IS 'Story 4.5: B2B Sales Orders with customer, location, and delivery tracking';
COMMENT ON COLUMN sales_orders.order_number IS 'Format: SO-YYYYMM-9999 (monthly sequence)';
COMMENT ON COLUMN sales_orders.status IS 'DRAFT, CONFIRMED, INVOICED, CANCELLED';
COMMENT ON COLUMN sales_orders.payment_terms IS 'A_VISTA, DIAS_7, DIAS_14, DIAS_30, DIAS_60, DIAS_90';
COMMENT ON COLUMN sales_orders.delivery_date_expected IS 'Expected delivery date for customer';
