-- Migration V050: Create fiscal_events table for audit trail
-- Story 4.3: NFCe Emission and Stock Decrease
-- NFR16: 5-year retention for fiscal compliance

CREATE TABLE fiscal_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    sale_id UUID,
    nfce_key VARCHAR(44),
    xml_snapshot TEXT,
    error_message TEXT,
    user_id UUID,
    event_timestamp TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_fiscal_events_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_fiscal_events_sale FOREIGN KEY (sale_id) REFERENCES sales(id),
    CONSTRAINT fk_fiscal_events_user FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT chk_event_type CHECK (event_type IN ('NFCE_EMITTED', 'NFCE_CANCELLED', 'NFCE_FAILED', 'NFCE_RETRY'))
);

-- Indexes for audit queries
CREATE INDEX idx_fiscal_events_tenant_id ON fiscal_events(tenant_id);
CREATE INDEX idx_fiscal_events_sale_id ON fiscal_events(sale_id);
CREATE INDEX idx_fiscal_events_event_type ON fiscal_events(tenant_id, event_type);
CREATE INDEX idx_fiscal_events_timestamp ON fiscal_events(tenant_id, event_timestamp DESC);
CREATE INDEX idx_fiscal_events_nfce_key ON fiscal_events(nfce_key) WHERE nfce_key IS NOT NULL;

-- Comments
COMMENT ON TABLE fiscal_events IS 'Story 4.3: Immutable audit trail for fiscal events (NFR16: 5-year retention)';
COMMENT ON COLUMN fiscal_events.event_type IS 'NFCE_EMITTED, NFCE_CANCELLED, NFCE_FAILED, NFCE_RETRY';
COMMENT ON COLUMN fiscal_events.xml_snapshot IS 'Copy of XML at event time for audit';
COMMENT ON COLUMN fiscal_events.error_message IS 'Error details if event_type is FAILED';
