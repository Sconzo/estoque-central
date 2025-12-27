-- Migration V053: Create tenant_settings table
-- Story 4.6: Stock Reservation and Automatic Release
-- Stores tenant-specific configuration settings

CREATE TABLE tenant_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_settings_key UNIQUE (tenant_id, setting_key)
);

-- Index for faster lookups by tenant and key
CREATE INDEX idx_tenant_settings_tenant_id ON tenant_settings(tenant_id);
CREATE INDEX idx_tenant_settings_key ON tenant_settings(setting_key);

-- Seed default setting: auto-release sales orders after 7 days
-- TODO: Seed data commented out - references non-existent 'tenants' table in tenant schema
-- Default settings should be created via application logic after tenant provisioning
--
-- INSERT INTO tenant_settings (tenant_id, setting_key, setting_value)
-- SELECT DISTINCT tenant_id, 'sales_order_auto_release_days', '7'
-- FROM tenants
-- ON CONFLICT (tenant_id, setting_key) DO NOTHING;

COMMENT ON TABLE tenant_settings IS 'Tenant-specific configuration settings (Story 4.6)';
COMMENT ON COLUMN tenant_settings.setting_key IS 'Setting identifier (e.g., sales_order_auto_release_days)';
COMMENT ON COLUMN tenant_settings.setting_value IS 'Setting value stored as text';
