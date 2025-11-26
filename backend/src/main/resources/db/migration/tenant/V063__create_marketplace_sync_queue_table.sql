-- Story 5.4: Stock Synchronization to Mercado Livre - AC1
-- Tabela para fila de sincronização (alternativa ao Redis para simplicidade)

CREATE TABLE marketplace_sync_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    product_id UUID NOT NULL,
    variant_id UUID,
    marketplace VARCHAR(50) NOT NULL,
    sync_type VARCHAR(20) NOT NULL DEFAULT 'STOCK', -- STOCK, PRICE
    priority INT DEFAULT 0, -- 0=normal, 1=high (manual sync)
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    last_error TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,

    CONSTRAINT fk_sync_queue_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT unique_sync_queue_item UNIQUE (tenant_id, product_id, variant_id, marketplace, sync_type, status)
);

-- Índices para performance
CREATE INDEX idx_sync_queue_status_priority ON marketplace_sync_queue(status, priority DESC, created_at ASC);
CREATE INDEX idx_sync_queue_tenant_id ON marketplace_sync_queue(tenant_id);
CREATE INDEX idx_sync_queue_product_id ON marketplace_sync_queue(product_id);
CREATE INDEX idx_sync_queue_created_at ON marketplace_sync_queue(created_at);

COMMENT ON TABLE marketplace_sync_queue IS 'Fila de sincronização de estoque/preços com marketplaces';
COMMENT ON COLUMN marketplace_sync_queue.priority IS '0=normal (auto), 1=high (manual sync)';
COMMENT ON COLUMN marketplace_sync_queue.status IS 'PENDING=aguardando, PROCESSING=em processamento, COMPLETED=concluído, FAILED=falhou após retries';
COMMENT ON CONSTRAINT unique_sync_queue_item ON marketplace_sync_queue IS 'Evita duplicatas na fila (deduplicação)';
