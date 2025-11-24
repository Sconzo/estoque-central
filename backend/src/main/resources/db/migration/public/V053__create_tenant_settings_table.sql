-- V053__create_tenant_settings_table.sql
-- Story 4.6: Stock Reservation and Automatic Release
-- Creates tenant_settings table for configurable settings per tenant

-- ============================================================
-- Table: tenant_settings
-- Stores configurable settings per tenant
-- ============================================================
CREATE TABLE IF NOT EXISTS tenant_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT unique_tenant_setting UNIQUE (tenant_id, setting_key)
);

-- ============================================================
-- Indexes for performance
-- ============================================================
CREATE INDEX idx_tenant_settings_tenant_id ON tenant_settings(tenant_id);
CREATE INDEX idx_tenant_settings_key ON tenant_settings(setting_key);

-- ============================================================
-- Function: Update tenant_settings updated_at timestamp
-- ============================================================
CREATE OR REPLACE FUNCTION update_tenant_settings_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_tenant_settings_updated_at
    BEFORE UPDATE ON tenant_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_tenant_settings_timestamp();

-- ============================================================
-- Seed default settings for existing tenants
-- ============================================================

-- Insert 'sales_order_auto_release_days' setting with default value of 7 days
-- for all existing tenants
INSERT INTO tenant_settings (tenant_id, setting_key, setting_value, created_at, updated_at)
SELECT
    id,
    'sales_order_auto_release_days',
    '7',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM tenants
ON CONFLICT (tenant_id, setting_key) DO NOTHING;

-- ============================================================
-- Comments
-- ============================================================
COMMENT ON TABLE tenant_settings IS 'Configurable settings per tenant for various features (Story 4.6)';
COMMENT ON COLUMN tenant_settings.tenant_id IS 'Reference to tenant';
COMMENT ON COLUMN tenant_settings.setting_key IS 'Setting identifier (e.g., sales_order_auto_release_days)';
COMMENT ON COLUMN tenant_settings.setting_value IS 'Setting value stored as text';

-- ============================================================
-- Success message
-- ============================================================
DO $$
BEGIN
    RAISE NOTICE 'Migration V053 completed successfully: tenant_settings table created';
END $$;
