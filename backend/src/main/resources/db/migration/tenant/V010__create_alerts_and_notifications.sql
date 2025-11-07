-- V010__create_alerts_and_notifications.sql
-- Story 3.3: Stock Alerts & Notifications
-- Creates tables for stock alerts and notification system

-- ============================================================
-- Table: stock_alerts
-- Stores stock alert rules and configurations
-- ============================================================
CREATE TABLE IF NOT EXISTS stock_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Alert configuration
    name VARCHAR(200) NOT NULL,
    description TEXT,
    alert_type VARCHAR(50) NOT NULL,

    -- Target (product, location, category)
    product_id UUID REFERENCES products(id),
    location_id UUID REFERENCES locations(id),
    category_id UUID REFERENCES categories(id),

    -- Thresholds
    threshold_quantity NUMERIC(15, 3),
    threshold_percentage NUMERIC(5, 2),

    -- Notification channels
    notify_email BOOLEAN NOT NULL DEFAULT false,
    notify_webhook BOOLEAN NOT NULL DEFAULT false,
    notify_internal BOOLEAN NOT NULL DEFAULT true,

    -- Recipients
    email_recipients TEXT,  -- Comma-separated emails
    webhook_url VARCHAR(500),

    -- Frequency (to avoid spam)
    frequency_hours INTEGER NOT NULL DEFAULT 24,
    last_triggered_at TIMESTAMP,

    -- Status
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT check_alert_type CHECK (alert_type IN ('LOW_STOCK', 'OUT_OF_STOCK', 'EXCESS_STOCK', 'EXPIRING_SOON', 'CUSTOM')),
    CONSTRAINT check_has_target CHECK (product_id IS NOT NULL OR location_id IS NOT NULL OR category_id IS NOT NULL)
);

-- ============================================================
-- Table: alert_notifications
-- Stores notification history (audit trail)
-- ============================================================
CREATE TABLE IF NOT EXISTS alert_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Alert reference
    alert_id UUID REFERENCES stock_alerts(id) ON DELETE CASCADE,

    -- Notification details
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',

    -- Context
    product_id UUID REFERENCES products(id),
    location_id UUID REFERENCES locations(id),
    category_id UUID REFERENCES categories(id),

    -- Message
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,

    -- Thresholds at time of alert
    current_quantity NUMERIC(15, 3),
    threshold_quantity NUMERIC(15, 3),

    -- Delivery status
    email_sent BOOLEAN NOT NULL DEFAULT false,
    webhook_sent BOOLEAN NOT NULL DEFAULT false,
    email_sent_at TIMESTAMP,
    webhook_sent_at TIMESTAMP,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    resolved_at TIMESTAMP,
    resolved_by UUID,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'RESOLVED', 'DISMISSED')),
    CONSTRAINT check_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

-- ============================================================
-- Indexes
-- ============================================================

-- Stock alerts indexes
CREATE INDEX idx_alerts_tenant ON stock_alerts(tenant_id);
CREATE INDEX idx_alerts_product ON stock_alerts(product_id) WHERE product_id IS NOT NULL;
CREATE INDEX idx_alerts_location ON stock_alerts(location_id) WHERE location_id IS NOT NULL;
CREATE INDEX idx_alerts_category ON stock_alerts(category_id) WHERE category_id IS NOT NULL;
CREATE INDEX idx_alerts_type ON stock_alerts(alert_type) WHERE ativo = true;
CREATE INDEX idx_alerts_active ON stock_alerts(ativo, last_triggered_at) WHERE ativo = true;

-- Alert notifications indexes
CREATE INDEX idx_notifications_tenant ON alert_notifications(tenant_id);
CREATE INDEX idx_notifications_alert ON alert_notifications(alert_id);
CREATE INDEX idx_notifications_product ON alert_notifications(product_id) WHERE product_id IS NOT NULL;
CREATE INDEX idx_notifications_status ON alert_notifications(status);
CREATE INDEX idx_notifications_created ON alert_notifications(created_at DESC);
CREATE INDEX idx_notifications_unresolved ON alert_notifications(status) WHERE status NOT IN ('RESOLVED', 'DISMISSED');

-- ============================================================
-- Trigger for updated_at
-- ============================================================
CREATE TRIGGER trigger_alerts_updated_at
    BEFORE UPDATE ON stock_alerts
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Function: Check and trigger stock alerts
-- ============================================================
CREATE OR REPLACE FUNCTION check_stock_alerts()
RETURNS TABLE(
    alert_id UUID,
    product_id UUID,
    location_id UUID,
    current_qty NUMERIC,
    threshold_qty NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        sa.id AS alert_id,
        i.product_id,
        i.location_id,
        i.quantity AS current_qty,
        sa.threshold_quantity AS threshold_qty
    FROM stock_alerts sa
    INNER JOIN inventory i ON (
        sa.product_id = i.product_id OR sa.product_id IS NULL
    ) AND (
        sa.location_id = i.location_id OR sa.location_id IS NULL
    )
    WHERE sa.ativo = true
      AND sa.alert_type = 'LOW_STOCK'
      AND i.quantity <= sa.threshold_quantity
      AND (
          sa.last_triggered_at IS NULL
          OR sa.last_triggered_at < NOW() - (sa.frequency_hours || ' hours')::INTERVAL
      );
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Initial data: Create default alerts
-- ============================================================

-- Global low stock alert (any product below min)
INSERT INTO stock_alerts (
    tenant_id, name, description, alert_type,
    notify_email, notify_internal,
    frequency_hours
)
SELECT
    t.id,
    'Global Low Stock Alert',
    'Alert when any product falls below minimum quantity',
    'LOW_STOCK',
    false,
    true,
    24
FROM tenants t
ON CONFLICT DO NOTHING;

-- Out of stock alert
INSERT INTO stock_alerts (
    tenant_id, name, description, alert_type,
    threshold_quantity,
    notify_email, notify_internal,
    frequency_hours
)
SELECT
    t.id,
    'Out of Stock Alert',
    'Critical alert when any product reaches zero quantity',
    'OUT_OF_STOCK',
    0,
    true,
    1  -- Alert every hour
FROM tenants t
ON CONFLICT DO NOTHING;

-- ============================================================
-- View: Active alerts summary
-- ============================================================
CREATE OR REPLACE VIEW v_active_alerts AS
SELECT
    sa.id AS alert_id,
    sa.name AS alert_name,
    sa.alert_type,
    COUNT(an.id) AS notification_count,
    MAX(an.created_at) AS last_notification,
    COUNT(CASE WHEN an.status = 'PENDING' THEN 1 END) AS pending_count,
    COUNT(CASE WHEN an.status NOT IN ('RESOLVED', 'DISMISSED') THEN 1 END) AS unresolved_count
FROM stock_alerts sa
LEFT JOIN alert_notifications an ON sa.id = an.alert_id
WHERE sa.ativo = true
GROUP BY sa.id, sa.name, sa.alert_type;

-- ============================================================
-- View: Current low stock products
-- ============================================================
CREATE OR REPLACE VIEW v_low_stock_products AS
SELECT
    p.id AS product_id,
    p.name AS product_name,
    p.sku,
    l.id AS location_id,
    l.name AS location_name,
    i.quantity,
    i.min_quantity,
    i.available_quantity,
    (i.min_quantity - i.quantity) AS shortage,
    CASE
        WHEN i.quantity = 0 THEN 'CRITICAL'
        WHEN i.quantity <= i.min_quantity * 0.25 THEN 'HIGH'
        WHEN i.quantity <= i.min_quantity * 0.50 THEN 'MEDIUM'
        ELSE 'LOW'
    END AS severity
FROM inventory i
INNER JOIN products p ON i.product_id = p.id
INNER JOIN locations l ON i.location_id = l.id
WHERE i.min_quantity IS NOT NULL
  AND i.quantity <= i.min_quantity
  AND p.ativo = true
  AND l.ativo = true
ORDER BY severity DESC, i.quantity ASC;
