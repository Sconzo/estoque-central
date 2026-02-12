-- =====================================================
-- Migration V074: Recreate Dashboard Views
-- =====================================================
-- V071 dropped product_costs with CASCADE, which destroyed
-- v_inventory_value_summary, v_critical_stock_products,
-- v_dashboard_summary and get_dashboard_snapshot().
-- This migration recreates them without product_costs reference.
-- =====================================================

-- Drop in dependency order (safe if already gone)
DROP FUNCTION IF EXISTS get_dashboard_snapshot() CASCADE;
DROP VIEW IF EXISTS v_dashboard_summary CASCADE;
DROP VIEW IF EXISTS v_critical_stock_products CASCADE;
DROP VIEW IF EXISTS v_inventory_value_summary CASCADE;

-- =====================================================
-- View: Inventory Value Summary (without product_costs)
-- =====================================================
CREATE VIEW v_inventory_value_summary AS
SELECT
    i.location_id,
    l.code AS location_code,
    l.name AS location_name,
    l.type AS location_type,
    COUNT(DISTINCT i.product_id) AS unique_products,
    SUM(i.available_quantity) AS total_quantity,
    0::numeric AS total_value_at_cost,
    0::numeric AS average_product_cost
FROM inventory i
INNER JOIN locations l ON l.id = i.location_id
WHERE l.ativo = true
  AND i.available_quantity > 0
GROUP BY i.location_id, l.code, l.name, l.type
ORDER BY total_quantity DESC;

COMMENT ON VIEW v_inventory_value_summary IS 'Inventory summary by location (cost columns zeroed - product_costs removed in V071)';

-- =====================================================
-- View: Critical Stock Products (without product_costs)
-- =====================================================
CREATE VIEW v_critical_stock_products AS
SELECT
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,
    i.location_id,
    l.code AS location_code,
    l.name AS location_name,
    i.available_quantity AS current_quantity,
    i.min_quantity AS minimum_quantity,
    i.max_quantity AS maximum_quantity,
    i.min_quantity AS reorder_point,
    (i.min_quantity - i.available_quantity) AS quantity_needed,
    CASE
        WHEN i.available_quantity <= 0 THEN 'OUT_OF_STOCK'
        WHEN i.available_quantity < i.min_quantity * 0.25 THEN 'CRITICAL'
        WHEN i.available_quantity < i.min_quantity THEN 'LOW'
        ELSE 'OK'
    END AS alert_level,
    0::numeric AS unit_cost,
    0::numeric AS replenishment_cost,
    p.updated_at AS last_updated
FROM inventory i
INNER JOIN products p ON p.id = i.product_id
INNER JOIN categories c ON c.id = p.category_id
INNER JOIN locations l ON l.id = i.location_id
WHERE p.ativo = true
  AND l.ativo = true
  AND i.min_quantity IS NOT NULL
  AND i.available_quantity < i.min_quantity
ORDER BY
    CASE
        WHEN i.available_quantity <= 0 THEN 1
        WHEN i.available_quantity < i.min_quantity * 0.25 THEN 2
        WHEN i.available_quantity < i.min_quantity THEN 3
        ELSE 4
    END,
    i.available_quantity ASC;

COMMENT ON VIEW v_critical_stock_products IS 'Products below minimum stock level requiring replenishment';

-- =====================================================
-- View: Dashboard Summary (Consolidated)
-- =====================================================
CREATE VIEW v_dashboard_summary AS
SELECT
    -- Today's sales metrics
    (
        SELECT COALESCE(SUM(total_sales), 0)
        FROM v_daily_sales_by_channel
    ) AS daily_total_sales,
    (
        SELECT COALESCE(SUM(order_count), 0)
        FROM v_daily_sales_by_channel
    ) AS daily_order_count,
    (
        SELECT COALESCE(SUM(item_count), 0)
        FROM v_daily_sales_by_channel
    ) AS daily_item_count,

    -- Inventory metrics
    (
        SELECT COALESCE(SUM(total_value_at_cost), 0)
        FROM v_inventory_value_summary
    ) AS total_inventory_value,
    (
        SELECT COALESCE(SUM(total_quantity), 0)
        FROM v_inventory_value_summary
    ) AS total_inventory_quantity,
    (
        SELECT COALESCE(SUM(unique_products), 0)
        FROM v_inventory_value_summary
    ) AS total_unique_products,

    -- Critical stock alerts
    (
        SELECT COUNT(*)
        FROM v_critical_stock_products
        WHERE alert_level = 'OUT_OF_STOCK'
    ) AS out_of_stock_count,
    (
        SELECT COUNT(*)
        FROM v_critical_stock_products
        WHERE alert_level = 'CRITICAL'
    ) AS critical_stock_count,
    (
        SELECT COUNT(*)
        FROM v_critical_stock_products
        WHERE alert_level = 'LOW'
    ) AS low_stock_count,
    (
        SELECT COALESCE(SUM(replenishment_cost), 0)
        FROM v_critical_stock_products
    ) AS total_replenishment_cost,

    -- Pending orders
    (
        SELECT COALESCE(SUM(order_count), 0)
        FROM v_pending_orders_summary
    ) AS pending_orders_count,
    (
        SELECT COALESCE(SUM(total_value), 0)
        FROM v_pending_orders_summary
    ) AS pending_orders_value,
    (
        SELECT COALESCE(SUM(overdue_count), 0)
        FROM v_pending_orders_summary
    ) AS overdue_orders_count,

    -- Timestamp
    NOW() AS snapshot_time;

COMMENT ON VIEW v_dashboard_summary IS 'Consolidated dashboard metrics - single query for main dashboard cards';

-- =====================================================
-- Function: Get Dashboard Snapshot
-- =====================================================
CREATE OR REPLACE FUNCTION get_dashboard_snapshot()
RETURNS JSON AS $$
DECLARE
    result JSON;
BEGIN
    SELECT json_build_object(
        'summary', (SELECT row_to_json(s.*) FROM v_dashboard_summary s),
        'salesByChannel', (
            SELECT json_agg(row_to_json(sc.*))
            FROM v_daily_sales_by_channel sc
        ),
        'criticalStock', (
            SELECT json_agg(row_to_json(cs.*))
            FROM v_critical_stock_products cs
            LIMIT 20
        ),
        'pendingOrders', (
            SELECT json_agg(row_to_json(po.*))
            FROM v_pending_orders_summary po
        ),
        'salesTrend', (
            SELECT json_agg(row_to_json(st.*))
            FROM v_sales_trend_7days st
        ),
        'topProducts', (
            SELECT json_agg(row_to_json(tp.*))
            FROM v_top_products_today tp
        ),
        'inventoryByLocation', (
            SELECT json_agg(row_to_json(iv.*))
            FROM v_inventory_value_summary iv
        ),
        'pendingPurchaseOrders', (
            SELECT json_agg(row_to_json(ppo.*))
            FROM v_pending_purchase_orders ppo
            LIMIT 10
        )
    ) INTO result;

    RETURN result;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_dashboard_snapshot() IS 'Returns complete dashboard data as JSON in single query';
