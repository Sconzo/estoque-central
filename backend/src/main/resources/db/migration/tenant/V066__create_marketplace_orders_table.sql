-- Migration V062: Create marketplace_orders table
-- Story 5.5: Import and Process Orders from Mercado Livre - AC1

CREATE TABLE marketplace_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    marketplace VARCHAR(50) NOT NULL,
    order_id_marketplace VARCHAR(255) NOT NULL,
    sale_id UUID,  -- FK to sales table (when order is paid and converted to sale)
    sales_order_id UUID,  -- FK to sales_orders table (when order is pending payment - reservation)
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255),
    customer_phone VARCHAR(50),
    total_amount NUMERIC(15, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
    payment_status VARCHAR(50),  -- ML payment status
    shipping_status VARCHAR(50),  -- ML shipping status
    ml_raw_data JSONB,  -- Store complete ML order JSON for debugging/audit
    imported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_marketplace_order UNIQUE (tenant_id, marketplace, order_id_marketplace)
);

CREATE INDEX idx_marketplace_orders_tenant_marketplace ON marketplace_orders(tenant_id, marketplace);
CREATE INDEX idx_marketplace_orders_status ON marketplace_orders(tenant_id, status);
CREATE INDEX idx_marketplace_orders_sale_id ON marketplace_orders(sale_id) WHERE sale_id IS NOT NULL;
CREATE INDEX idx_marketplace_orders_sales_order_id ON marketplace_orders(sales_order_id) WHERE sales_order_id IS NOT NULL;
CREATE INDEX idx_marketplace_orders_imported_at ON marketplace_orders(tenant_id, imported_at DESC);
