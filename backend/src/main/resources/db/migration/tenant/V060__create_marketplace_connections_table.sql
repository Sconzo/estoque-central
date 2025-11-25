-- Migration V060: Create marketplace_connections table
-- Story 5.1: Mercado Livre OAuth2 Authentication - AC1
-- Stores OAuth2 tokens and connection status for marketplace integrations

CREATE TABLE marketplace_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    marketplace VARCHAR(50) NOT NULL,
    user_id_marketplace VARCHAR(255) NOT NULL,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    token_expires_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONNECTED',
    last_sync_at TIMESTAMP,
    error_message TEXT,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_marketplace_connection UNIQUE (tenant_id, marketplace)
);

-- Indexes for performance
CREATE INDEX idx_mc_tenant_id ON marketplace_connections(tenant_id);
CREATE INDEX idx_mc_marketplace ON marketplace_connections(marketplace);
CREATE INDEX idx_mc_status ON marketplace_connections(status);
CREATE INDEX idx_mc_token_expires ON marketplace_connections(token_expires_at);

-- Comments
COMMENT ON TABLE marketplace_connections IS 'OAuth2 connections to marketplaces (Mercado Livre, etc)';
COMMENT ON COLUMN marketplace_connections.marketplace IS 'Marketplace name: MERCADO_LIVRE, SHOPEE, etc';
COMMENT ON COLUMN marketplace_connections.user_id_marketplace IS 'User ID from marketplace API';
COMMENT ON COLUMN marketplace_connections.access_token IS 'Encrypted OAuth2 access token';
COMMENT ON COLUMN marketplace_connections.refresh_token IS 'Encrypted OAuth2 refresh token';
COMMENT ON COLUMN marketplace_connections.status IS 'Connection status: CONNECTED, DISCONNECTED, ERROR';
