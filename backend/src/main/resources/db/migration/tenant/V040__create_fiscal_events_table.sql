-- Story 4.3: NFCe Emission and Stock Decrease - Fiscal Events (Audit)
-- Registro imutável de eventos fiscais (NFR16: retenção 5 anos)

CREATE TABLE fiscal_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Event classification
    event_type VARCHAR(50) NOT NULL, -- NFCE_EMITTED, NFCE_CANCELLED, NFCE_FAILED, NFCE_RETRY

    -- Related entities
    sale_id UUID,
    nfce_key VARCHAR(44), -- Chave de acesso da NFCe
    xml_snapshot TEXT, -- Snapshot do XML (se disponível)

    -- Event details
    error_message TEXT, -- Mensagem de erro (se evento de falha)
    http_status_code INTEGER, -- Status HTTP da resposta do middleware
    retry_count INTEGER DEFAULT 0, -- Contador de tentativas de retry

    -- Audit
    user_id UUID, -- Usuário que iniciou a operação
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_fiscal_events_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_fiscal_events_tenant_id ON fiscal_events(tenant_id);
CREATE INDEX idx_fiscal_events_event_type ON fiscal_events(event_type);
CREATE INDEX idx_fiscal_events_sale_id ON fiscal_events(sale_id);
CREATE INDEX idx_fiscal_events_nfce_key ON fiscal_events(nfce_key);
CREATE INDEX idx_fiscal_events_timestamp ON fiscal_events(timestamp);

-- Comments
COMMENT ON TABLE fiscal_events IS 'Story 4.3: Auditoria imutável de eventos fiscais (NFR16: retenção 5 anos)';
COMMENT ON COLUMN fiscal_events.event_type IS 'Tipo de evento: NFCE_EMITTED, NFCE_CANCELLED, NFCE_FAILED, NFCE_RETRY';
COMMENT ON COLUMN fiscal_events.xml_snapshot IS 'Snapshot do XML da NFCe para auditoria';
COMMENT ON COLUMN fiscal_events.retry_count IS 'Quantidade de tentativas de reenvio (para eventos de retry)';

-- Particionamento futuro (opcional): particionar por timestamp (mensal/anual)
-- para melhor performance em queries de auditoria de longo prazo
