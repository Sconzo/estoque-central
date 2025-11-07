-- =====================================================
-- Migration V026: Stock Alerts System
-- Story 6.7 - Alertas Automáticos de Estoque Mínimo
-- =====================================================
-- Description: Creates automated stock alert system
--              - Alert rules and history
--              - Views for alert monitoring
--              - Function for alert generation
-- =====================================================

-- =====================================================
-- Table 1: Stock Alert Rules
-- =====================================================
-- Configuration for automatic alerts
CREATE TABLE IF NOT EXISTS stock_alert_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID,
    category_id UUID,
    location_id UUID,
    alert_type VARCHAR(30) NOT NULL, -- 'OUT_OF_STOCK', 'BELOW_MINIMUM', 'BELOW_REORDER', 'EXCESS'
    enabled BOOLEAN NOT NULL DEFAULT true,
    notify_users TEXT[], -- Array of user IDs to notify
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    ativo BOOLEAN NOT NULL DEFAULT true,

    CONSTRAINT check_at_least_one_filter CHECK (
        product_id IS NOT NULL OR
        category_id IS NOT NULL OR
        location_id IS NOT NULL
    ),
    CONSTRAINT fk_stock_alert_rules_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_alert_rules_category FOREIGN KEY (category_id)
        REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_alert_rules_location FOREIGN KEY (location_id)
        REFERENCES locations(id) ON DELETE CASCADE
);

COMMENT ON TABLE stock_alert_rules IS 'Configuration for automatic stock alerts';

-- =====================================================
-- Table 2: Stock Alerts
-- =====================================================
-- Generated alerts
CREATE TABLE IF NOT EXISTS stock_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_rule_id UUID,
    inventory_id UUID NOT NULL,
    product_id UUID NOT NULL,
    location_id UUID NOT NULL,
    alert_type VARCHAR(30) NOT NULL,
    severity VARCHAR(20) NOT NULL, -- 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    message TEXT NOT NULL,
    current_quantity NUMERIC(15, 3) NOT NULL,
    minimum_quantity NUMERIC(15, 3),
    reorder_point NUMERIC(15, 3),
    quantity_needed NUMERIC(15, 3),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- 'OPEN', 'ACKNOWLEDGED', 'RESOLVED', 'DISMISSED'
    acknowledged_at TIMESTAMP,
    acknowledged_by UUID,
    resolved_at TIMESTAMP,
    resolved_by UUID,
    resolution_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ativo BOOLEAN NOT NULL DEFAULT true,

    CONSTRAINT fk_stock_alerts_rule FOREIGN KEY (alert_rule_id)
        REFERENCES stock_alert_rules(id) ON DELETE SET NULL,
    CONSTRAINT fk_stock_alerts_inventory FOREIGN KEY (inventory_id)
        REFERENCES inventory(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_alerts_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_alerts_location FOREIGN KEY (location_id)
        REFERENCES locations(id) ON DELETE CASCADE
);

CREATE INDEX idx_stock_alerts_status ON stock_alerts(status, created_at DESC)
    WHERE ativo = true;

CREATE INDEX idx_stock_alerts_product_location ON stock_alerts(product_id, location_id, status)
    WHERE ativo = true;

CREATE INDEX idx_stock_alerts_severity ON stock_alerts(severity, status, created_at DESC)
    WHERE ativo = true;

COMMENT ON TABLE stock_alerts IS 'Generated stock alerts requiring attention';

-- =====================================================
-- View 1: Active Stock Alerts
-- =====================================================
CREATE OR REPLACE VIEW v_active_stock_alerts AS
SELECT
    sa.id AS alert_id,
    sa.alert_type,
    sa.severity,
    sa.status,
    sa.message,
    sa.created_at AS alert_created_at,

    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,

    l.id AS location_id,
    l.code AS location_code,
    l.name AS location_name,
    l.location_type,

    sa.current_quantity,
    sa.minimum_quantity,
    sa.reorder_point,
    sa.quantity_needed,

    -- Cost impact
    COALESCE(pc.average_cost, 0) AS unit_cost,
    COALESCE(sa.quantity_needed * pc.average_cost, 0) AS replenishment_cost,

    -- Priority score (1-10, higher = more urgent)
    CASE sa.severity
        WHEN 'CRITICAL' THEN 10
        WHEN 'HIGH' THEN 7
        WHEN 'MEDIUM' THEN 4
        ELSE 2
    END +
    CASE sa.alert_type
        WHEN 'OUT_OF_STOCK' THEN 3
        WHEN 'BELOW_REORDER' THEN 2
        WHEN 'BELOW_MINIMUM' THEN 1
        ELSE 0
    END AS priority_score,

    EXTRACT(HOURS FROM (NOW() - sa.created_at)) AS hours_since_alert

FROM stock_alerts sa
INNER JOIN products p ON p.id = sa.product_id
INNER JOIN categories c ON c.id = p.category_id
INNER JOIN locations l ON l.id = sa.location_id
LEFT JOIN product_costs pc ON pc.product_id = sa.product_id
    AND pc.location_id = sa.location_id
WHERE sa.ativo = true
  AND sa.status IN ('OPEN', 'ACKNOWLEDGED')
  AND p.ativo = true
  AND l.ativo = true
ORDER BY priority_score DESC, sa.created_at ASC;

COMMENT ON VIEW v_active_stock_alerts IS 'Active stock alerts requiring attention';

-- =====================================================
-- View 2: Stock Alerts Summary
-- =====================================================
CREATE OR REPLACE VIEW v_stock_alerts_summary AS
SELECT
    sa.alert_type,
    sa.severity,
    sa.status,
    COUNT(*) AS alert_count,
    SUM(sa.quantity_needed) AS total_quantity_needed,
    SUM(sa.quantity_needed * COALESCE(pc.average_cost, 0)) AS total_replenishment_cost,
    MIN(sa.created_at) AS oldest_alert,
    MAX(sa.created_at) AS newest_alert
FROM stock_alerts sa
LEFT JOIN product_costs pc ON pc.product_id = sa.product_id
    AND pc.location_id = sa.location_id
WHERE sa.ativo = true
  AND sa.status IN ('OPEN', 'ACKNOWLEDGED')
GROUP BY sa.alert_type, sa.severity, sa.status
ORDER BY
    CASE sa.severity
        WHEN 'CRITICAL' THEN 1
        WHEN 'HIGH' THEN 2
        WHEN 'MEDIUM' THEN 3
        ELSE 4
    END,
    alert_count DESC;

COMMENT ON VIEW v_stock_alerts_summary IS 'Summary of active stock alerts';

-- =====================================================
-- View 3: Alert History
-- =====================================================
CREATE OR REPLACE VIEW v_stock_alerts_history AS
SELECT
    sa.id AS alert_id,
    sa.alert_type,
    sa.severity,
    sa.status,
    p.sku,
    p.name AS product_name,
    l.name AS location_name,
    sa.current_quantity,
    sa.minimum_quantity,
    sa.quantity_needed,
    sa.created_at,
    sa.acknowledged_at,
    sa.resolved_at,
    EXTRACT(HOURS FROM (COALESCE(sa.resolved_at, NOW()) - sa.created_at)) AS hours_to_resolve,
    sa.resolution_notes
FROM stock_alerts sa
INNER JOIN products p ON p.id = sa.product_id
INNER JOIN locations l ON l.id = sa.location_id
WHERE sa.ativo = true
ORDER BY sa.created_at DESC;

COMMENT ON VIEW v_stock_alerts_history IS 'Complete history of stock alerts';

-- =====================================================
-- Function: Generate Stock Alerts
-- =====================================================
-- Scans inventory and generates alerts based on rules
CREATE OR REPLACE FUNCTION generate_stock_alerts()
RETURNS TABLE(
    alerts_generated INTEGER,
    alerts_by_type JSONB
) AS $$
DECLARE
    v_alerts_generated INTEGER := 0;
    v_alert_id UUID;
    v_alerts_by_type JSONB := '{}';
BEGIN
    -- Generate OUT_OF_STOCK alerts
    INSERT INTO stock_alerts (
        inventory_id,
        product_id,
        location_id,
        alert_type,
        severity,
        message,
        current_quantity,
        minimum_quantity,
        reorder_point,
        quantity_needed
    )
    SELECT
        i.id,
        i.product_id,
        i.location_id,
        'OUT_OF_STOCK',
        'CRITICAL',
        'Product ' || p.name || ' is OUT OF STOCK at ' || l.name,
        i.quantity_available,
        i.minimum_quantity,
        i.reorder_point,
        i.minimum_quantity
    FROM inventory i
    INNER JOIN products p ON p.id = i.product_id
    INNER JOIN locations l ON l.id = i.location_id
    WHERE i.ativo = true
      AND p.ativo = true
      AND l.ativo = true
      AND i.quantity_available <= 0
      AND NOT EXISTS (
          SELECT 1 FROM stock_alerts sa
          WHERE sa.inventory_id = i.id
            AND sa.alert_type = 'OUT_OF_STOCK'
            AND sa.status IN ('OPEN', 'ACKNOWLEDGED')
            AND sa.ativo = true
      );

    GET DIAGNOSTICS v_alerts_generated = ROW_COUNT;
    v_alerts_by_type := jsonb_set(v_alerts_by_type, '{OUT_OF_STOCK}', to_jsonb(v_alerts_generated));

    -- Generate BELOW_REORDER alerts
    v_alerts_generated := 0;
    INSERT INTO stock_alerts (
        inventory_id,
        product_id,
        location_id,
        alert_type,
        severity,
        message,
        current_quantity,
        minimum_quantity,
        reorder_point,
        quantity_needed
    )
    SELECT
        i.id,
        i.product_id,
        i.location_id,
        'BELOW_REORDER',
        'HIGH',
        'Product ' || p.name || ' is below reorder point at ' || l.name,
        i.quantity_available,
        i.minimum_quantity,
        i.reorder_point,
        (i.reorder_point - i.quantity_available)
    FROM inventory i
    INNER JOIN products p ON p.id = i.product_id
    INNER JOIN locations l ON l.id = i.location_id
    WHERE i.ativo = true
      AND p.ativo = true
      AND l.ativo = true
      AND i.quantity_available > 0
      AND i.quantity_available < i.reorder_point
      AND NOT EXISTS (
          SELECT 1 FROM stock_alerts sa
          WHERE sa.inventory_id = i.id
            AND sa.alert_type = 'BELOW_REORDER'
            AND sa.status IN ('OPEN', 'ACKNOWLEDGED')
            AND sa.ativo = true
      );

    GET DIAGNOSTICS v_alerts_generated = ROW_COUNT;
    v_alerts_by_type := jsonb_set(v_alerts_by_type, '{BELOW_REORDER}', to_jsonb(v_alerts_generated));

    -- Generate BELOW_MINIMUM alerts
    v_alerts_generated := 0;
    INSERT INTO stock_alerts (
        inventory_id,
        product_id,
        location_id,
        alert_type,
        severity,
        message,
        current_quantity,
        minimum_quantity,
        reorder_point,
        quantity_needed
    )
    SELECT
        i.id,
        i.product_id,
        i.location_id,
        'BELOW_MINIMUM',
        'MEDIUM',
        'Product ' || p.name || ' is below minimum at ' || l.name,
        i.quantity_available,
        i.minimum_quantity,
        i.reorder_point,
        (i.minimum_quantity - i.quantity_available)
    FROM inventory i
    INNER JOIN products p ON p.id = i.product_id
    INNER JOIN locations l ON l.id = i.location_id
    WHERE i.ativo = true
      AND p.ativo = true
      AND l.ativo = true
      AND i.quantity_available >= i.reorder_point
      AND i.quantity_available < i.minimum_quantity
      AND NOT EXISTS (
          SELECT 1 FROM stock_alerts sa
          WHERE sa.inventory_id = i.id
            AND sa.alert_type = 'BELOW_MINIMUM'
            AND sa.status IN ('OPEN', 'ACKNOWLEDGED')
            AND sa.ativo = true
      );

    GET DIAGNOSTICS v_alerts_generated = ROW_COUNT;
    v_alerts_by_type := jsonb_set(v_alerts_by_type, '{BELOW_MINIMUM}', to_jsonb(v_alerts_generated));

    -- Return summary
    RETURN QUERY SELECT
        (v_alerts_by_type->>'OUT_OF_STOCK')::INTEGER +
        (v_alerts_by_type->>'BELOW_REORDER')::INTEGER +
        (v_alerts_by_type->>'BELOW_MINIMUM')::INTEGER AS alerts_generated,
        v_alerts_by_type AS alerts_by_type;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION generate_stock_alerts IS 'Automatically generates stock alerts based on inventory levels';

-- =====================================================
-- Function: Acknowledge Alert
-- =====================================================
CREATE OR REPLACE FUNCTION acknowledge_stock_alert(
    alert_uuid UUID,
    user_uuid UUID
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE stock_alerts
    SET status = 'ACKNOWLEDGED',
        acknowledged_at = NOW(),
        acknowledged_by = user_uuid,
        updated_at = NOW()
    WHERE id = alert_uuid
      AND status = 'OPEN'
      AND ativo = true;

    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- Function: Resolve Alert
-- =====================================================
CREATE OR REPLACE FUNCTION resolve_stock_alert(
    alert_uuid UUID,
    user_uuid UUID,
    notes TEXT DEFAULT NULL
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE stock_alerts
    SET status = 'RESOLVED',
        resolved_at = NOW(),
        resolved_by = user_uuid,
        resolution_notes = notes,
        updated_at = NOW()
    WHERE id = alert_uuid
      AND status IN ('OPEN', 'ACKNOWLEDGED')
      AND ativo = true;

    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- End of Migration V026
-- =====================================================
