-- Story 5.4: Stock Synchronization to Mercado Livre - AC4
-- Tabela de logs de sincronização de marketplace

CREATE TABLE marketplace_sync_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    product_id UUID NOT NULL,
    variant_id UUID,
    marketplace VARCHAR(50) NOT NULL,
    sync_type VARCHAR(20) NOT NULL, -- STOCK, PRICE
    old_value DECIMAL(10, 2),
    new_value DECIMAL(10, 2),
    status VARCHAR(20) NOT NULL, -- SUCCESS, ERROR, PENDING
    error_message TEXT,
    retry_count INT DEFAULT 0,
    synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sync_logs_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Índices para performance
CREATE INDEX idx_sync_logs_tenant_id ON marketplace_sync_logs(tenant_id);
CREATE INDEX idx_sync_logs_product_id ON marketplace_sync_logs(product_id);
CREATE INDEX idx_sync_logs_marketplace ON marketplace_sync_logs(marketplace);
CREATE INDEX idx_sync_logs_status ON marketplace_sync_logs(status);
CREATE INDEX idx_sync_logs_created_at ON marketplace_sync_logs(created_at DESC);
CREATE INDEX idx_sync_logs_tenant_product ON marketplace_sync_logs(tenant_id, product_id, created_at DESC);

COMMENT ON TABLE marketplace_sync_logs IS 'Logs de sincronização de estoque e preços com marketplaces';
COMMENT ON COLUMN marketplace_sync_logs.sync_type IS 'Tipo de sincronização: STOCK (estoque) ou PRICE (preço)';
COMMENT ON COLUMN marketplace_sync_logs.status IS 'Status: SUCCESS, ERROR, PENDING';
COMMENT ON COLUMN marketplace_sync_logs.retry_count IS 'Número de tentativas de retry';
