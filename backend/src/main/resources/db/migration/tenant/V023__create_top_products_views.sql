-- =====================================================
-- Migration V023: Top Products Report Views
-- Story 6.4 - Relatório de Produtos Mais Vendidos
-- =====================================================
-- Description: Creates views for top-selling products analysis
--              - Ranking by quantity and revenue
--              - Performance by category and channel
--              - Period comparison
-- =====================================================

-- =====================================================
-- View 1: Top Products by Quantity
-- =====================================================
-- Products ranked by quantity sold
CREATE OR REPLACE VIEW v_top_products_by_quantity AS
SELECT
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,
    p.unit_of_measure,

    -- Sales metrics
    COUNT(DISTINCT oi.order_id) AS order_count,
    COUNT(DISTINCT o.customer_id) AS unique_customers,
    SUM(oi.quantity) AS total_quantity_sold,
    SUM(oi.total_price) AS total_revenue,

    -- Pricing
    ROUND(AVG(oi.unit_price), 2) AS average_price,
    MIN(oi.unit_price) AS min_price,
    MAX(oi.unit_price) AS max_price,

    -- Performance
    ROUND(SUM(oi.total_price) / NULLIF(SUM(oi.quantity), 0), 2) AS revenue_per_unit,
    ROUND(SUM(oi.quantity) / NULLIF(COUNT(DISTINCT oi.order_id), 0), 2) AS avg_quantity_per_order,

    -- Inventory
    (
        SELECT SUM(quantity_available)
        FROM inventory
        WHERE product_id = p.id AND ativo = true
    ) AS current_stock,

    -- Dates
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
GROUP BY p.id, p.sku, p.name, c.name, p.unit_of_measure
ORDER BY total_quantity_sold DESC;

COMMENT ON VIEW v_top_products_by_quantity IS 'Products ranked by total quantity sold';

-- =====================================================
-- View 2: Top Products by Revenue
-- =====================================================
-- Products ranked by revenue
CREATE OR REPLACE VIEW v_top_products_by_revenue AS
SELECT
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,

    -- Sales metrics
    COUNT(DISTINCT oi.order_id) AS order_count,
    SUM(oi.quantity) AS total_quantity_sold,
    SUM(oi.total_price) AS total_revenue,
    ROUND(AVG(oi.unit_price), 2) AS average_price,

    -- Revenue analysis
    ROUND(
        SUM(oi.total_price) * 100.0 / (
            SELECT SUM(total_price)
            FROM order_items oi2
            INNER JOIN orders o2 ON o2.id = oi2.order_id
            WHERE oi2.ativo = true
              AND o2.ativo = true
              AND o2.status NOT IN ('CANCELLED', 'REJECTED')
        ),
        2
    ) AS revenue_percentage,

    -- Inventory
    (
        SELECT SUM(quantity_available)
        FROM inventory
        WHERE product_id = p.id AND ativo = true
    ) AS current_stock,

    -- Cost analysis (if available)
    (
        SELECT AVG(average_cost)
        FROM product_costs
        WHERE product_id = p.id
    ) AS average_cost,

    -- Profit estimation
    SUM(oi.total_price) - (
        SUM(oi.quantity) * COALESCE((
            SELECT AVG(average_cost)
            FROM product_costs
            WHERE product_id = p.id
        ), 0)
    ) AS estimated_profit

FROM order_items oi
INNER JOIN orders o ON o.id = oi.order_id
INNER JOIN products p ON p.id = oi.product_id
INNER JOIN categories c ON c.id = p.category_id
WHERE oi.ativo = true
  AND o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
  AND p.ativo = true
GROUP BY p.id, p.sku, p.name, c.name
ORDER BY total_revenue DESC;

COMMENT ON VIEW v_top_products_by_revenue IS 'Products ranked by total revenue with profit estimation';

-- =====================================================
-- View 3: Top Products by Category
-- =====================================================
-- Top products grouped by category
CREATE OR REPLACE VIEW v_top_products_by_category AS
SELECT
    c.id AS category_id,
    c.name AS category_name,
    p.id AS product_id,
    p.sku,
    p.name AS product_name,

    SUM(oi.quantity) AS total_quantity_sold,
    SUM(oi.total_price) AS total_revenue,
    COUNT(DISTINCT oi.order_id) AS order_count,

    -- Rank within category
    ROW_NUMBER() OVER (
        PARTITION BY c.id
        ORDER BY SUM(oi.total_price) DESC
    ) AS rank_in_category

FROM order_items oi
INNER JOIN orders o ON o.id = oi.order_id
INNER JOIN products p ON p.id = oi.product_id
INNER JOIN categories c ON c.id = p.category_id
WHERE oi.ativo = true
  AND o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
  AND p.ativo = true
GROUP BY c.id, c.name, p.id, p.sku, p.name
ORDER BY c.name, rank_in_category;

COMMENT ON VIEW v_top_products_by_category IS 'Top products ranked within each category';

-- =====================================================
-- View 4: Top Products by Channel
-- =====================================================
-- Product performance by sales channel
CREATE OR REPLACE VIEW v_top_products_by_channel AS
SELECT
    o.sales_channel,
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,

    COUNT(DISTINCT oi.order_id) AS order_count,
    SUM(oi.quantity) AS total_quantity_sold,
    SUM(oi.total_price) AS total_revenue,
    ROUND(AVG(oi.unit_price), 2) AS average_price,

    -- Rank within channel
    ROW_NUMBER() OVER (
        PARTITION BY o.sales_channel
        ORDER BY SUM(oi.total_price) DESC
    ) AS rank_in_channel

FROM order_items oi
INNER JOIN orders o ON o.id = oi.order_id
INNER JOIN products p ON p.id = oi.product_id
INNER JOIN categories c ON c.id = p.category_id
WHERE oi.ativo = true
  AND o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
  AND p.ativo = true
GROUP BY o.sales_channel, p.id, p.sku, p.name, c.name
ORDER BY o.sales_channel, rank_in_channel;

COMMENT ON VIEW v_top_products_by_channel IS 'Product performance ranked by sales channel';

-- =====================================================
-- View 5: Product Sales Performance Summary
-- =====================================================
-- Complete performance metrics for each product
CREATE OR REPLACE VIEW v_product_sales_performance AS
SELECT
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,

    -- Sales volume
    COUNT(DISTINCT oi.order_id) AS order_count,
    COUNT(DISTINCT o.customer_id) AS unique_customers,
    SUM(oi.quantity) AS total_quantity_sold,
    SUM(oi.total_price) AS total_revenue,

    -- Pricing metrics
    ROUND(AVG(oi.unit_price), 2) AS average_price,
    MIN(oi.unit_price) AS min_price,
    MAX(oi.unit_price) AS max_price,
    STDDEV(oi.unit_price) AS price_stddev,

    -- Performance ratios
    ROUND(SUM(oi.quantity) / NULLIF(COUNT(DISTINCT oi.order_id), 0), 2) AS units_per_order,
    ROUND(SUM(oi.total_price) / NULLIF(COUNT(DISTINCT oi.order_id), 0), 2) AS revenue_per_order,
    ROUND(SUM(oi.total_price) / NULLIF(COUNT(DISTINCT o.customer_id), 0), 2) AS revenue_per_customer,

    -- Inventory turnover
    (
        SELECT SUM(quantity_available)
        FROM inventory
        WHERE product_id = p.id AND ativo = true
    ) AS current_stock,

    CASE
        WHEN (SELECT SUM(quantity_available) FROM inventory WHERE product_id = p.id AND ativo = true) > 0
        THEN ROUND(
            SUM(oi.quantity) / NULLIF((
                SELECT SUM(quantity_available)
                FROM inventory
                WHERE product_id = p.id AND ativo = true
            ), 0),
            2
        )
        ELSE NULL
    END AS inventory_turnover_ratio,

    -- Time metrics
    MIN(o.order_date) AS first_sale_date,
    MAX(o.order_date) AS last_sale_date,
    EXTRACT(DAYS FROM (MAX(o.order_date) - MIN(o.order_date))) AS days_selling

FROM order_items oi
INNER JOIN orders o ON o.id = oi.order_id
INNER JOIN products p ON p.id = oi.product_id
INNER JOIN categories c ON c.id = p.category_id
WHERE oi.ativo = true
  AND o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
  AND p.ativo = true
GROUP BY p.id, p.sku, p.name, c.name
ORDER BY total_revenue DESC;

COMMENT ON VIEW v_product_sales_performance IS 'Complete performance metrics for all products';

-- =====================================================
-- View 6: Top Products Last 30 Days
-- =====================================================
-- Recent top performers
CREATE OR REPLACE VIEW v_top_products_last_30days AS
SELECT
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,

    COUNT(DISTINCT oi.order_id) AS order_count,
    SUM(oi.quantity) AS total_quantity_sold,
    SUM(oi.total_price) AS total_revenue,
    ROUND(AVG(oi.unit_price), 2) AS average_price,

    -- Current stock
    (
        SELECT SUM(quantity_available)
        FROM inventory
        WHERE product_id = p.id AND ativo = true
    ) AS current_stock

FROM order_items oi
INNER JOIN orders o ON o.id = oi.order_id
INNER JOIN products p ON p.id = oi.product_id
INNER JOIN categories c ON c.id = p.category_id
WHERE oi.ativo = true
  AND o.ativo = true
  AND o.status NOT IN ('CANCELLED', 'REJECTED')
  AND p.ativo = true
  AND o.order_date >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY p.id, p.sku, p.name, c.name
ORDER BY total_revenue DESC
LIMIT 100;

COMMENT ON VIEW v_top_products_last_30days IS 'Top 100 products by revenue in last 30 days';

-- =====================================================
-- Function: Get Top Products with Filters
-- =====================================================
CREATE OR REPLACE FUNCTION get_top_products_report(
    start_date DATE DEFAULT NULL,
    end_date DATE DEFAULT NULL,
    category_filter UUID DEFAULT NULL,
    channel_filter VARCHAR DEFAULT NULL,
    order_by_field VARCHAR DEFAULT 'revenue', -- 'revenue' or 'quantity'
    limit_rows INTEGER DEFAULT 50
)
RETURNS TABLE(
    product_id UUID,
    sku VARCHAR,
    product_name VARCHAR,
    category_name VARCHAR,
    order_count BIGINT,
    total_quantity_sold NUMERIC,
    total_revenue NUMERIC,
    average_price NUMERIC,
    current_stock NUMERIC,
    rank_position BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.id AS product_id,
        p.sku,
        p.name AS product_name,
        c.name AS category_name,
        COUNT(DISTINCT oi.order_id)::BIGINT AS order_count,
        SUM(oi.quantity) AS total_quantity_sold,
        SUM(oi.total_price) AS total_revenue,
        ROUND(AVG(oi.unit_price), 2) AS average_price,
        COALESCE((
            SELECT SUM(quantity_available)
            FROM inventory
            WHERE product_id = p.id AND ativo = true
        ), 0) AS current_stock,
        ROW_NUMBER() OVER (
            ORDER BY
                CASE
                    WHEN order_by_field = 'quantity' THEN SUM(oi.quantity)
                    ELSE SUM(oi.total_price)
                END DESC
        )::BIGINT AS rank_position
    FROM order_items oi
    INNER JOIN orders o ON o.id = oi.order_id
    INNER JOIN products p ON p.id = oi.product_id
    INNER JOIN categories c ON c.id = p.category_id
    WHERE oi.ativo = true
      AND o.ativo = true
      AND o.status NOT IN ('CANCELLED', 'REJECTED')
      AND p.ativo = true
      AND (start_date IS NULL OR DATE(o.order_date) >= start_date)
      AND (end_date IS NULL OR DATE(o.order_date) <= end_date)
      AND (category_filter IS NULL OR p.category_id = category_filter)
      AND (channel_filter IS NULL OR o.sales_channel = channel_filter)
    GROUP BY p.id, p.sku, p.name, c.name
    ORDER BY rank_position
    LIMIT limit_rows;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_top_products_report IS 'Get top products with flexible filtering and ranking';

-- =====================================================
-- Indexes for Performance
-- =====================================================

-- Index for product sales queries
CREATE INDEX IF NOT EXISTS idx_order_items_product_order
    ON order_items(product_id, order_id)
    WHERE ativo = true;

-- Index for order date filtering
CREATE INDEX IF NOT EXISTS idx_orders_date_status_items
    ON orders(order_date, status)
    WHERE ativo = true AND status NOT IN ('CANCELLED', 'REJECTED');

-- =====================================================
-- End of Migration V023
-- =====================================================
