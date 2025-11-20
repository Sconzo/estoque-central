-- =====================================================
-- Migration V020: Dashboard Views
-- Story 6.1 - Dashboard Gerencial
-- =====================================================
-- Description: Creates optimized views for management dashboard
--              - Daily sales by channel
--              - Total inventory value
--              - Critical stock alerts (below minimum)
--              - Pending orders summary
-- =====================================================

-- =====================================================
-- View 1: Daily Sales by Channel
-- =====================================================
-- Shows sales breakdown by sales channel for current day
CREATE OR REPLACE VIEW v_daily_sales_by_channel AS
SELECT
    o.sales_channel,
    COUNT(DISTINCT o.id) AS order_count,
    COUNT(oi.id) AS item_count,
    SUM(oi.quantity) AS total_quantity,
    SUM(oi.total_price) AS total_sales,
    ROUND(AVG(o.total_amount), 2) AS average_ticket,
    MIN(o.order_date) AS first_order_time,
    MAX(o.order_date) AS last_order_time
FROM orders o
INNER JOIN order_items oi ON oi.order_id = o.id
WHERE DATE(o.order_date) = CURRENT_DATE
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
  AND o.ativo = true
  AND oi.ativo = true
GROUP BY o.sales_channel
ORDER BY total_sales DESC;

COMMENT ON VIEW v_daily_sales_by_channel IS 'Daily sales breakdown by sales channel (STORE, ONLINE, MARKETPLACE, PHONE, WHATSAPP)';

-- =====================================================
-- View 2: Inventory Value Summary
-- =====================================================
-- Shows total inventory value across all locations
CREATE OR REPLACE VIEW v_inventory_value_summary AS
SELECT
    i.location_id,
    l.code AS location_code,
    l.name AS location_name,
    l.location_type,
    COUNT(DISTINCT i.product_id) AS unique_products,
    SUM(i.quantity_available) AS total_quantity,
    SUM(
        COALESCE(i.quantity_available, 0) * COALESCE(pc.average_cost, 0)
    ) AS total_value_at_cost,
    ROUND(AVG(pc.average_cost), 2) AS average_product_cost
FROM inventory i
INNER JOIN locations l ON l.id = i.location_id
LEFT JOIN product_costs pc ON pc.product_id = i.product_id
    AND pc.location_id = i.location_id
WHERE i.ativo = true
  AND l.ativo = true
  AND i.quantity_available > 0
GROUP BY i.location_id, l.code, l.name, l.location_type
ORDER BY total_value_at_cost DESC;

COMMENT ON VIEW v_inventory_value_summary IS 'Total inventory value by location using weighted average cost';

-- =====================================================
-- View 3: Critical Stock Products (Below Minimum)
-- =====================================================
-- Shows products that are below minimum stock level (ruptura)
CREATE OR REPLACE VIEW v_critical_stock_products AS
SELECT
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,
    i.location_id,
    l.code AS location_code,
    l.name AS location_name,
    i.quantity_available AS current_quantity,
    i.minimum_quantity,
    i.maximum_quantity,
    i.reorder_point,
    (i.minimum_quantity - i.quantity_available) AS quantity_needed,
    CASE
        WHEN i.quantity_available <= 0 THEN 'OUT_OF_STOCK'
        WHEN i.quantity_available < i.reorder_point THEN 'CRITICAL'
        WHEN i.quantity_available < i.minimum_quantity THEN 'LOW'
        ELSE 'OK'
    END AS alert_level,
    COALESCE(pc.average_cost, 0) AS unit_cost,
    COALESCE(pc.average_cost, 0) * (i.minimum_quantity - i.quantity_available) AS replenishment_cost,
    p.updated_at AS last_updated
FROM inventory i
INNER JOIN products p ON p.id = i.product_id
INNER JOIN categories c ON c.id = p.category_id
INNER JOIN locations l ON l.id = i.location_id
LEFT JOIN product_costs pc ON pc.product_id = i.product_id
    AND pc.location_id = i.location_id
WHERE i.ativo = true
  AND p.ativo = true
  AND l.ativo = true
  AND i.quantity_available < i.minimum_quantity
ORDER BY
    CASE
        WHEN i.quantity_available <= 0 THEN 1
        WHEN i.quantity_available < i.reorder_point THEN 2
        WHEN i.quantity_available < i.minimum_quantity THEN 3
        ELSE 4
    END,
    i.quantity_available ASC;

COMMENT ON VIEW v_critical_stock_products IS 'Products below minimum stock level requiring replenishment';

-- =====================================================
-- View 4: Pending Orders Summary
-- =====================================================
-- Shows summary of orders pending fulfillment by channel
CREATE OR REPLACE VIEW v_pending_orders_summary AS
SELECT
    o.sales_channel,
    o.status,
    COUNT(DISTINCT o.id) AS order_count,
    SUM(o.total_items) AS total_items,
    SUM(o.total_amount) AS total_value,
    ROUND(AVG(o.total_amount), 2) AS average_order_value,
    MIN(o.order_date) AS oldest_order_date,
    MAX(o.order_date) AS newest_order_date,
    COUNT(
        CASE
            WHEN o.order_date < CURRENT_DATE - INTERVAL '2 days' THEN 1
        END
    ) AS overdue_count
FROM orders o
WHERE o.status IN ('PENDING', 'PROCESSING', 'CONFIRMED', 'READY_TO_SHIP')
  AND o.ativo = true
GROUP BY o.sales_channel, o.status
ORDER BY
    CASE o.status
        WHEN 'PENDING' THEN 1
        WHEN 'PROCESSING' THEN 2
        WHEN 'CONFIRMED' THEN 3
        WHEN 'READY_TO_SHIP' THEN 4
    END,
    o.sales_channel;

COMMENT ON VIEW v_pending_orders_summary IS 'Pending orders requiring action grouped by channel and status';

-- =====================================================
-- View 5: Dashboard Summary (Consolidated)
-- =====================================================
-- Single view with all key metrics for dashboard
CREATE OR REPLACE VIEW v_dashboard_summary AS
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
-- View 6: Sales Trend (Last 7 Days)
-- =====================================================
-- Shows daily sales trend for the last 7 days
CREATE OR REPLACE VIEW v_sales_trend_7days AS
SELECT
    DATE(o.order_date) AS sale_date,
    COUNT(DISTINCT o.id) AS order_count,
    COUNT(oi.id) AS item_count,
    SUM(oi.quantity) AS total_quantity,
    SUM(oi.total_price) AS total_sales,
    ROUND(AVG(o.total_amount), 2) AS average_ticket
FROM orders o
INNER JOIN order_items oi ON oi.order_id = o.id
WHERE o.order_date >= CURRENT_DATE - INTERVAL '7 days'
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
  AND o.ativo = true
  AND oi.ativo = true
GROUP BY DATE(o.order_date)
ORDER BY sale_date DESC;

COMMENT ON VIEW v_sales_trend_7days IS 'Daily sales metrics for last 7 days for trend analysis';

-- =====================================================
-- View 7: Top 10 Products Today
-- =====================================================
-- Shows best selling products for current day
CREATE OR REPLACE VIEW v_top_products_today AS
SELECT
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,
    COUNT(DISTINCT oi.order_id) AS order_count,
    SUM(oi.quantity) AS total_quantity_sold,
    SUM(oi.total_price) AS total_revenue,
    ROUND(AVG(oi.unit_price), 2) AS average_price,
    (
        SELECT SUM(quantity_available)
        FROM inventory
        WHERE product_id = p.id
          AND ativo = true
    ) AS current_stock
FROM order_items oi
INNER JOIN orders o ON o.id = oi.order_id
INNER JOIN products p ON p.id = oi.product_id
INNER JOIN categories c ON c.id = p.category_id
WHERE DATE(o.order_date) = CURRENT_DATE
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
  AND o.ativo = true
  AND oi.ativo = true
  AND p.ativo = true
GROUP BY p.id, p.sku, p.name, c.name
ORDER BY total_quantity_sold DESC
LIMIT 10;

COMMENT ON VIEW v_top_products_today IS 'Top 10 best-selling products for current day';

-- =====================================================
-- View 8: Purchase Orders Pending Approval
-- =====================================================
-- Shows purchase orders awaiting approval
CREATE OR REPLACE VIEW v_pending_purchase_orders AS
SELECT
    po.id AS po_id,
    po.po_number,
    po.status,
    s.company_name AS supplier_name,
    s.supplier_code,
    po.order_date,
    po.expected_delivery_date,
    po.total_items,
    po.total_amount,
    COUNT(poi.id) AS item_count,
    CURRENT_DATE - DATE(po.order_date) AS days_pending,
    CASE
        WHEN po.expected_delivery_date < CURRENT_DATE THEN true
        ELSE false
    END AS is_overdue
FROM purchase_orders po
INNER JOIN suppliers s ON s.id = po.supplier_id
LEFT JOIN purchase_order_items poi ON poi.purchase_order_id = po.id
WHERE po.status IN ('DRAFT', 'PENDING_APPROVAL')
  AND po.ativo = true
  AND s.ativo = true
GROUP BY po.id, po.po_number, po.status, s.company_name, s.supplier_code,
         po.order_date, po.expected_delivery_date, po.total_items, po.total_amount
ORDER BY
    CASE po.status
        WHEN 'PENDING_APPROVAL' THEN 1
        WHEN 'DRAFT' THEN 2
    END,
    po.order_date ASC;

COMMENT ON VIEW v_pending_purchase_orders IS 'Purchase orders pending approval or in draft status';

-- =====================================================
-- Function: Get Dashboard Snapshot
-- =====================================================
-- Returns complete dashboard data as JSON
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

-- =====================================================
-- Indexes for Performance
-- =====================================================

-- Index for daily sales queries
CREATE INDEX IF NOT EXISTS idx_orders_date_status_channel
    ON orders(order_date, status, sales_channel)
    WHERE ativo = true;

-- Index for inventory value calculations
CREATE INDEX IF NOT EXISTS idx_inventory_available_qty
    ON inventory(location_id, product_id, quantity_available)
    WHERE ativo = true AND quantity_available > 0;

-- Index for critical stock queries
CREATE INDEX IF NOT EXISTS idx_inventory_minimum_check
    ON inventory(location_id, product_id, quantity_available, minimum_quantity)
    WHERE ativo = true;

-- Index for pending orders
CREATE INDEX IF NOT EXISTS idx_orders_pending_status
    ON orders(status, sales_channel, order_date)
    WHERE status IN ('PENDING', 'PROCESSING', 'CONFIRMED', 'READY_TO_SHIP')
      AND ativo = true;

-- =====================================================
-- End of Migration V020
-- =====================================================
