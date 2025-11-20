-- =====================================================
-- Migration V022: Sales Report Views
-- Story 6.3 - Relatório de Vendas por Período e Canal
-- =====================================================
-- Description: Creates views for sales reporting by period and channel
--              - Sales by period (day, week, month)
--              - Sales by channel with breakdown
--              - Sales performance trends
-- =====================================================

-- =====================================================
-- View 1: Sales Details
-- =====================================================
-- Detailed sales information with customer and items
CREATE OR REPLACE VIEW v_sales_details AS
SELECT
    o.id AS order_id,
    o.order_number,
    o.order_date,
    DATE(o.order_date) AS order_date_only,
    o.sales_channel,
    o.status,
    o.payment_status,

    -- Customer info
    c.id AS customer_id,
    c.full_name AS customer_name,
    c.customer_type,

    -- Order totals
    o.subtotal,
    o.discount_amount,
    o.shipping_cost,
    o.total_amount,
    o.total_items,

    -- Items details
    (
        SELECT COUNT(*)
        FROM order_items
        WHERE order_id = o.id AND ativo = true
    ) AS item_count,

    (
        SELECT SUM(quantity)
        FROM order_items
        WHERE order_id = o.id AND ativo = true
    ) AS total_quantity,

    -- Payment info
    p.payment_method,
    p.payment_status AS payment_status_detail,
    p.paid_amount,

    -- Timestamps
    o.created_at,
    o.updated_at

FROM orders o
LEFT JOIN customers c ON c.id = o.customer_id
LEFT JOIN payments p ON p.order_id = o.id
WHERE o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
ORDER BY o.order_date DESC;

COMMENT ON VIEW v_sales_details IS 'Detailed sales information with customer and payment data';

-- =====================================================
-- View 2: Sales by Date and Channel
-- =====================================================
-- Daily sales aggregated by channel
CREATE OR REPLACE VIEW v_sales_by_date_and_channel AS
SELECT
    DATE(o.order_date) AS sale_date,
    o.sales_channel,

    -- Order metrics
    COUNT(DISTINCT o.id) AS order_count,
    COUNT(DISTINCT o.customer_id) AS unique_customers,

    -- Item metrics
    SUM(o.total_items) AS total_items,
    COALESCE(SUM((
        SELECT SUM(quantity)
        FROM order_items oi
        WHERE oi.order_id = o.id AND oi.ativo = true
    )), 0) AS total_quantity,

    -- Financial metrics
    SUM(o.subtotal) AS total_subtotal,
    SUM(o.discount_amount) AS total_discount,
    SUM(o.shipping_cost) AS total_shipping,
    SUM(o.total_amount) AS total_sales,
    ROUND(AVG(o.total_amount), 2) AS average_ticket,
    MIN(o.total_amount) AS min_ticket,
    MAX(o.total_amount) AS max_ticket,

    -- Time metrics
    MIN(o.order_date) AS first_sale_time,
    MAX(o.order_date) AS last_sale_time

FROM orders o
WHERE o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
GROUP BY DATE(o.order_date), o.sales_channel
ORDER BY sale_date DESC, total_sales DESC;

COMMENT ON VIEW v_sales_by_date_and_channel IS 'Daily sales aggregated by channel';

-- =====================================================
-- View 3: Sales by Period Summary
-- =====================================================
-- Sales summary for different time periods
CREATE OR REPLACE VIEW v_sales_by_period AS
SELECT
    DATE(o.order_date) AS sale_date,
    EXTRACT(YEAR FROM o.order_date) AS sale_year,
    EXTRACT(MONTH FROM o.order_date) AS sale_month,
    EXTRACT(WEEK FROM o.order_date) AS sale_week,
    TO_CHAR(o.order_date, 'YYYY-MM') AS year_month,
    TO_CHAR(o.order_date, 'YYYY-"W"IW') AS year_week,

    -- Aggregated metrics
    COUNT(DISTINCT o.id) AS order_count,
    COUNT(DISTINCT o.customer_id) AS unique_customers,
    SUM(o.total_items) AS total_items,
    SUM(o.total_amount) AS total_sales,
    ROUND(AVG(o.total_amount), 2) AS average_ticket,

    -- Payment status
    COUNT(CASE WHEN o.payment_status = 'PAID' THEN 1 END) AS paid_orders,
    COUNT(CASE WHEN o.payment_status = 'PENDING' THEN 1 END) AS pending_payment_orders,
    SUM(CASE WHEN o.payment_status = 'PAID' THEN o.total_amount ELSE 0 END) AS paid_amount

FROM orders o
WHERE o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
GROUP BY
    DATE(o.order_date),
    EXTRACT(YEAR FROM o.order_date),
    EXTRACT(MONTH FROM o.order_date),
    EXTRACT(WEEK FROM o.order_date),
    year_month,
    year_week
ORDER BY sale_date DESC;

COMMENT ON VIEW v_sales_by_period IS 'Sales summary by different time periods (day, week, month)';

-- =====================================================
-- View 4: Sales by Channel Summary
-- =====================================================
-- Complete sales statistics by channel
CREATE OR REPLACE VIEW v_sales_by_channel_summary AS
SELECT
    o.sales_channel,

    -- Order metrics
    COUNT(DISTINCT o.id) AS total_orders,
    COUNT(DISTINCT o.customer_id) AS unique_customers,
    ROUND(COUNT(DISTINCT o.id)::NUMERIC / COUNT(DISTINCT o.customer_id), 2) AS orders_per_customer,

    -- Item metrics
    SUM(o.total_items) AS total_items,
    ROUND(AVG(o.total_items), 2) AS average_items_per_order,

    -- Financial metrics
    SUM(o.subtotal) AS total_subtotal,
    SUM(o.discount_amount) AS total_discount,
    SUM(o.shipping_cost) AS total_shipping,
    SUM(o.total_amount) AS total_sales,
    ROUND(AVG(o.total_amount), 2) AS average_ticket,
    MIN(o.total_amount) AS min_ticket,
    MAX(o.total_amount) AS max_ticket,

    -- Discount analysis
    ROUND((SUM(o.discount_amount) / NULLIF(SUM(o.subtotal), 0) * 100), 2) AS discount_percentage,

    -- Time metrics
    MIN(o.order_date) AS first_sale,
    MAX(o.order_date) AS last_sale,

    -- Status breakdown
    COUNT(CASE WHEN o.status = 'PENDING' THEN 1 END) AS pending_orders,
    COUNT(CASE WHEN o.status = 'CONFIRMED' THEN 1 END) AS confirmed_orders,
    COUNT(CASE WHEN o.status = 'DELIVERED' THEN 1 END) AS delivered_orders,
    COUNT(CASE WHEN o.payment_status = 'PAID' THEN 1 END) AS paid_orders

FROM orders o
WHERE o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
GROUP BY o.sales_channel
ORDER BY total_sales DESC;

COMMENT ON VIEW v_sales_by_channel_summary IS 'Complete sales statistics by channel';

-- =====================================================
-- View 5: Sales by Month and Channel
-- =====================================================
-- Monthly sales breakdown by channel
CREATE OR REPLACE VIEW v_sales_by_month_and_channel AS
SELECT
    TO_CHAR(o.order_date, 'YYYY-MM') AS year_month,
    EXTRACT(YEAR FROM o.order_date) AS year,
    EXTRACT(MONTH FROM o.order_date) AS month,
    o.sales_channel,

    COUNT(DISTINCT o.id) AS order_count,
    COUNT(DISTINCT o.customer_id) AS unique_customers,
    SUM(o.total_amount) AS total_sales,
    ROUND(AVG(o.total_amount), 2) AS average_ticket,

    -- Growth metrics (compare to same month data)
    LAG(SUM(o.total_amount)) OVER (
        PARTITION BY o.sales_channel
        ORDER BY TO_CHAR(o.order_date, 'YYYY-MM')
    ) AS previous_month_sales

FROM orders o
WHERE o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
GROUP BY year_month, year, month, o.sales_channel
ORDER BY year_month DESC, total_sales DESC;

COMMENT ON VIEW v_sales_by_month_and_channel IS 'Monthly sales breakdown by channel with growth comparison';

-- =====================================================
-- View 6: Sales Trend (Last 30 Days)
-- =====================================================
-- Daily sales trend for last 30 days
CREATE OR REPLACE VIEW v_sales_trend_30days AS
SELECT
    DATE(o.order_date) AS sale_date,

    COUNT(DISTINCT o.id) AS order_count,
    COUNT(DISTINCT o.customer_id) AS unique_customers,
    SUM(o.total_amount) AS total_sales,
    ROUND(AVG(o.total_amount), 2) AS average_ticket,

    -- Moving average (7 days)
    ROUND(AVG(SUM(o.total_amount)) OVER (
        ORDER BY DATE(o.order_date)
        ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
    ), 2) AS moving_avg_7days

FROM orders o
WHERE o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
  AND o.order_date >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(o.order_date)
ORDER BY sale_date DESC;

COMMENT ON VIEW v_sales_trend_30days IS 'Daily sales trend for last 30 days with 7-day moving average';

-- =====================================================
-- View 7: Sales by Product and Channel
-- =====================================================
-- Product sales performance by channel
CREATE OR REPLACE VIEW v_sales_by_product_and_channel AS
SELECT
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,
    o.sales_channel,

    -- Order metrics
    COUNT(DISTINCT o.id) AS order_count,
    COUNT(DISTINCT oi.id) AS line_item_count,

    -- Quantity metrics
    SUM(oi.quantity) AS total_quantity_sold,
    ROUND(AVG(oi.quantity), 2) AS average_quantity_per_order,

    -- Financial metrics
    SUM(oi.total_price) AS total_revenue,
    ROUND(AVG(oi.unit_price), 2) AS average_unit_price,
    MIN(oi.unit_price) AS min_price,
    MAX(oi.unit_price) AS max_price,

    -- Time metrics
    MIN(o.order_date) AS first_sale_date,
    MAX(o.order_date) AS last_sale_date

FROM order_items oi
INNER JOIN orders o ON o.id = oi.order_id
INNER JOIN products p ON p.id = oi.product_id
INNER JOIN categories c ON c.id = p.category_id
WHERE oi.ativo = true
  AND o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
  AND p.ativo = true
GROUP BY p.id, p.sku, p.name, c.name, o.sales_channel
ORDER BY total_revenue DESC;

COMMENT ON VIEW v_sales_by_product_and_channel IS 'Product sales performance by channel';

-- =====================================================
-- View 8: Sales Performance Comparison
-- =====================================================
-- Compare current period vs previous period
CREATE OR REPLACE VIEW v_sales_performance_comparison AS
SELECT
    'Today' AS period_label,
    CURRENT_DATE AS period_start,
    CURRENT_DATE AS period_end,

    COUNT(DISTINCT o.id) AS order_count,
    SUM(o.total_amount) AS total_sales,
    ROUND(AVG(o.total_amount), 2) AS average_ticket
FROM orders o
WHERE DATE(o.order_date) = CURRENT_DATE
  AND o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')

UNION ALL

SELECT
    'Yesterday' AS period_label,
    CURRENT_DATE - 1 AS period_start,
    CURRENT_DATE - 1 AS period_end,

    COUNT(DISTINCT o.id) AS order_count,
    SUM(o.total_amount) AS total_sales,
    ROUND(AVG(o.total_amount), 2) AS average_ticket
FROM orders o
WHERE DATE(o.order_date) = CURRENT_DATE - 1
  AND o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')

UNION ALL

SELECT
    'Last 7 Days' AS period_label,
    CURRENT_DATE - 7 AS period_start,
    CURRENT_DATE AS period_end,

    COUNT(DISTINCT o.id) AS order_count,
    SUM(o.total_amount) AS total_sales,
    ROUND(AVG(o.total_amount), 2) AS average_ticket
FROM orders o
WHERE o.order_date >= CURRENT_DATE - INTERVAL '7 days'
  AND o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')

UNION ALL

SELECT
    'Last 30 Days' AS period_label,
    CURRENT_DATE - 30 AS period_start,
    CURRENT_DATE AS period_end,

    COUNT(DISTINCT o.id) AS order_count,
    SUM(o.total_amount) AS total_sales,
    ROUND(AVG(o.total_amount), 2) AS average_ticket
FROM orders o
WHERE o.order_date >= CURRENT_DATE - INTERVAL '30 days'
  AND o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')

ORDER BY period_start DESC;

COMMENT ON VIEW v_sales_performance_comparison IS 'Sales performance comparison across different time periods';

-- =====================================================
-- Function: Get Sales Report by Period and Channel
-- =====================================================
CREATE OR REPLACE FUNCTION get_sales_report_by_period(
    start_date DATE DEFAULT NULL,
    end_date DATE DEFAULT NULL,
    channel_filter VARCHAR DEFAULT NULL,
    group_by_period VARCHAR DEFAULT 'day' -- 'day', 'week', 'month'
)
RETURNS TABLE(
    period_key VARCHAR,
    sales_channel VARCHAR,
    order_count BIGINT,
    unique_customers BIGINT,
    total_sales NUMERIC,
    average_ticket NUMERIC,
    total_items BIGINT,
    total_quantity NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        CASE
            WHEN group_by_period = 'day' THEN TO_CHAR(o.order_date, 'YYYY-MM-DD')
            WHEN group_by_period = 'week' THEN TO_CHAR(o.order_date, 'YYYY-"W"IW')
            WHEN group_by_period = 'month' THEN TO_CHAR(o.order_date, 'YYYY-MM')
            ELSE TO_CHAR(o.order_date, 'YYYY-MM-DD')
        END AS period_key,
        o.sales_channel,
        COUNT(DISTINCT o.id)::BIGINT AS order_count,
        COUNT(DISTINCT o.customer_id)::BIGINT AS unique_customers,
        SUM(o.total_amount) AS total_sales,
        ROUND(AVG(o.total_amount), 2) AS average_ticket,
        SUM(o.total_items)::BIGINT AS total_items,
        COALESCE(SUM((
            SELECT SUM(quantity)
            FROM order_items oi2
            WHERE oi2.order_id = o.id AND oi2.ativo = true
        )), 0) AS total_quantity
    FROM orders o
    WHERE o.ativo = true
      AND o.status NOT IN ('CANCELLED', 'REJECTED')
      AND (start_date IS NULL OR DATE(o.order_date) >= start_date)
      AND (end_date IS NULL OR DATE(o.order_date) <= end_date)
      AND (channel_filter IS NULL OR o.sales_channel = channel_filter)
    GROUP BY period_key, o.sales_channel
    ORDER BY period_key DESC, total_sales DESC;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_sales_report_by_period IS 'Get sales report grouped by period (day/week/month) and channel';

-- =====================================================
-- Indexes for Performance
-- =====================================================

-- Index for date range queries
CREATE INDEX IF NOT EXISTS idx_orders_date_channel
    ON orders(order_date, sales_channel, status)
    WHERE ativo = true;

-- Index for customer analysis
CREATE INDEX IF NOT EXISTS idx_orders_customer_date
    ON orders(customer_id, order_date)
    WHERE ativo = true AND status NOT IN ('CANCELLED', 'REJECTED');

-- Index for payment status queries
CREATE INDEX IF NOT EXISTS idx_orders_payment_status
    ON orders(payment_status, order_date)
    WHERE ativo = true;

-- =====================================================
-- End of Migration V022
-- =====================================================
