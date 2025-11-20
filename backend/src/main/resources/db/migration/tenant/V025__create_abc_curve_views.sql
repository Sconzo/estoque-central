-- =====================================================
-- Migration V025: ABC Curve Analysis Views
-- Story 6.6 - Curva ABC de Produtos
-- =====================================================
-- Description: Creates views for ABC/Pareto analysis
--              - Classification A/B/C by revenue
--              - Cumulative percentage calculation
--              - Category-specific ABC analysis
-- =====================================================

-- =====================================================
-- View 1: ABC Curve Analysis (Revenue-based)
-- =====================================================
-- Products classified into A/B/C categories using Pareto principle
CREATE OR REPLACE VIEW v_abc_curve_analysis AS
WITH product_revenue AS (
    SELECT
        p.id AS product_id,
        p.sku,
        p.name AS product_name,
        c.name AS category_name,
        SUM(oi.total_price) AS total_revenue,
        SUM(oi.quantity) AS total_quantity_sold,
        COUNT(DISTINCT oi.order_id) AS order_count
    FROM order_items oi
    INNER JOIN orders o ON o.id = oi.order_id
    INNER JOIN products p ON p.id = oi.product_id
    INNER JOIN categories c ON c.id = p.category_id
    WHERE oi.ativo = true
      AND o.ativo = true
      AND o.status NOT IN ('CANCELLED', 'REJECTED')
      AND p.ativo = true
    GROUP BY p.id, p.sku, p.name, c.name
),
ranked_products AS (
    SELECT
        product_id,
        sku,
        product_name,
        category_name,
        total_revenue,
        total_quantity_sold,
        order_count,
        ROW_NUMBER() OVER (ORDER BY total_revenue DESC) AS revenue_rank,
        SUM(total_revenue) OVER () AS total_system_revenue
    FROM product_revenue
),
cumulative_products AS (
    SELECT
        product_id,
        sku,
        product_name,
        category_name,
        total_revenue,
        total_quantity_sold,
        order_count,
        revenue_rank,
        total_system_revenue,
        SUM(total_revenue) OVER (ORDER BY revenue_rank) AS cumulative_revenue,
        ROUND(
            (SUM(total_revenue) OVER (ORDER BY revenue_rank) / total_system_revenue * 100),
            2
        ) AS cumulative_percentage,
        ROUND((total_revenue / total_system_revenue * 100), 2) AS revenue_percentage
    FROM ranked_products
)
SELECT
    product_id,
    sku,
    product_name,
    category_name,
    total_revenue,
    total_quantity_sold,
    order_count,
    revenue_rank,
    cumulative_revenue,
    cumulative_percentage,
    revenue_percentage,
    CASE
        WHEN cumulative_percentage <= 80 THEN 'A'
        WHEN cumulative_percentage <= 95 THEN 'B'
        ELSE 'C'
    END AS abc_class,
    -- Current stock for reference
    (
        SELECT SUM(quantity_available)
        FROM inventory
        WHERE product_id = cumulative_products.product_id AND ativo = true
    ) AS current_stock
FROM cumulative_products
ORDER BY revenue_rank;

COMMENT ON VIEW v_abc_curve_analysis IS 'ABC/Pareto analysis: A (80% revenue), B (15% revenue), C (5% revenue)';

-- =====================================================
-- View 2: ABC Summary Statistics
-- =====================================================
-- Summary metrics for each ABC class
CREATE OR REPLACE VIEW v_abc_summary_statistics AS
WITH abc_data AS (
    SELECT * FROM v_abc_curve_analysis
)
SELECT
    abc_class,
    COUNT(*) AS product_count,
    ROUND((COUNT(*)::NUMERIC / (SELECT COUNT(*) FROM abc_data) * 100), 2) AS product_percentage,
    SUM(total_revenue) AS total_revenue,
    ROUND((SUM(total_revenue) / (SELECT SUM(total_revenue) FROM abc_data) * 100), 2) AS revenue_percentage,
    SUM(total_quantity_sold) AS total_quantity_sold,
    ROUND(AVG(total_revenue), 2) AS avg_revenue_per_product,
    ROUND(AVG(order_count), 2) AS avg_orders_per_product,
    MIN(revenue_rank) AS best_rank,
    MAX(revenue_rank) AS worst_rank
FROM abc_data
GROUP BY abc_class
ORDER BY abc_class;

COMMENT ON VIEW v_abc_summary_statistics IS 'Summary statistics for each ABC class';

-- =====================================================
-- View 3: ABC Curve by Category
-- =====================================================
-- ABC analysis within each category
CREATE OR REPLACE VIEW v_abc_curve_by_category AS
WITH category_revenue AS (
    SELECT
        c.id AS category_id,
        c.name AS category_name,
        p.id AS product_id,
        p.sku,
        p.name AS product_name,
        SUM(oi.total_price) AS total_revenue,
        SUM(oi.quantity) AS total_quantity_sold
    FROM order_items oi
    INNER JOIN orders o ON o.id = oi.order_id
    INNER JOIN products p ON p.id = oi.product_id
    INNER JOIN categories c ON c.id = p.category_id
    WHERE oi.ativo = true
      AND o.ativo = true
      AND o.status NOT IN ('CANCELLED', 'REJECTED')
      AND p.ativo = true
    GROUP BY c.id, c.name, p.id, p.sku, p.name
),
ranked_by_category AS (
    SELECT
        category_id,
        category_name,
        product_id,
        sku,
        product_name,
        total_revenue,
        total_quantity_sold,
        ROW_NUMBER() OVER (PARTITION BY category_id ORDER BY total_revenue DESC) AS rank_in_category,
        SUM(total_revenue) OVER (PARTITION BY category_id) AS category_total_revenue
    FROM category_revenue
),
cumulative_by_category AS (
    SELECT
        category_id,
        category_name,
        product_id,
        sku,
        product_name,
        total_revenue,
        total_quantity_sold,
        rank_in_category,
        category_total_revenue,
        SUM(total_revenue) OVER (
            PARTITION BY category_id
            ORDER BY rank_in_category
        ) AS cumulative_revenue,
        ROUND(
            (SUM(total_revenue) OVER (
                PARTITION BY category_id
                ORDER BY rank_in_category
            ) / category_total_revenue * 100),
            2
        ) AS cumulative_percentage
    FROM ranked_by_category
)
SELECT
    category_id,
    category_name,
    product_id,
    sku,
    product_name,
    total_revenue,
    total_quantity_sold,
    rank_in_category,
    cumulative_percentage,
    CASE
        WHEN cumulative_percentage <= 80 THEN 'A'
        WHEN cumulative_percentage <= 95 THEN 'B'
        ELSE 'C'
    END AS abc_class_in_category
FROM cumulative_by_category
ORDER BY category_name, rank_in_category;

COMMENT ON VIEW v_abc_curve_by_category IS 'ABC analysis within each product category';

-- =====================================================
-- View 4: ABC Class A Products (Top Performers)
-- =====================================================
-- Focus on Class A products (top 80% revenue)
CREATE OR REPLACE VIEW v_abc_class_a_products AS
SELECT
    product_id,
    sku,
    product_name,
    category_name,
    total_revenue,
    total_quantity_sold,
    order_count,
    revenue_rank,
    cumulative_percentage,
    revenue_percentage,
    current_stock,
    -- Stock turnover for Class A (critical)
    CASE
        WHEN current_stock > 0 THEN
            ROUND(total_quantity_sold::NUMERIC / current_stock, 2)
        ELSE NULL
    END AS turnover_ratio,
    -- Restock urgency
    CASE
        WHEN current_stock <= 0 THEN 'CRITICAL'
        WHEN current_stock < (total_quantity_sold / 12) THEN 'HIGH' -- Less than 1 month supply
        WHEN current_stock < (total_quantity_sold / 6) THEN 'MEDIUM' -- Less than 2 months
        ELSE 'LOW'
    END AS restock_urgency
FROM v_abc_curve_analysis
WHERE abc_class = 'A'
ORDER BY revenue_rank;

COMMENT ON VIEW v_abc_class_a_products IS 'Class A products (top 80% revenue) with restock urgency';

-- =====================================================
-- View 5: ABC Class C Products (Candidates for Review)
-- =====================================================
-- Class C products that may need review
CREATE OR REPLACE VIEW v_abc_class_c_products AS
SELECT
    product_id,
    sku,
    product_name,
    category_name,
    total_revenue,
    total_quantity_sold,
    order_count,
    revenue_rank,
    cumulative_percentage,
    revenue_percentage,
    current_stock,
    -- Excess stock indicator
    CASE
        WHEN current_stock > total_quantity_sold THEN 'EXCESS'
        WHEN current_stock > (total_quantity_sold / 2) THEN 'HIGH'
        ELSE 'NORMAL'
    END AS stock_level,
    -- Value tied up in slow-moving stock
    COALESCE((
        SELECT SUM(i.quantity_available * pc.average_cost)
        FROM inventory i
        LEFT JOIN product_costs pc ON pc.product_id = i.product_id
            AND pc.location_id = i.location_id
        WHERE i.product_id = v_abc_curve_analysis.product_id
          AND i.ativo = true
    ), 0) AS inventory_value_at_cost
FROM v_abc_curve_analysis
WHERE abc_class = 'C'
ORDER BY inventory_value_at_cost DESC;

COMMENT ON VIEW v_abc_class_c_products IS 'Class C products (bottom 5% revenue) - candidates for discontinuation';

-- =====================================================
-- View 6: ABC Transition Analysis
-- =====================================================
-- Compare current period vs previous period ABC classification
CREATE OR REPLACE VIEW v_abc_transition_analysis AS
WITH current_period AS (
    SELECT
        product_id,
        sku,
        product_name,
        abc_class,
        revenue_rank,
        total_revenue
    FROM v_abc_curve_analysis
),
previous_period AS (
    -- Last 90 days excluding last 30 days
    SELECT
        p.id AS product_id,
        SUM(oi.total_price) AS total_revenue,
        ROW_NUMBER() OVER (ORDER BY SUM(oi.total_price) DESC) AS prev_revenue_rank
    FROM order_items oi
    INNER JOIN orders o ON o.id = oi.order_id
    INNER JOIN products p ON p.id = oi.product_id
    WHERE oi.ativo = true
      AND o.ativo = true
      AND o.status NOT IN ('CANCELLED', 'REJECTED')
      AND o.order_date >= CURRENT_DATE - INTERVAL '90 days'
      AND o.order_date < CURRENT_DATE - INTERVAL '30 days'
    GROUP BY p.id
)
SELECT
    c.product_id,
    c.sku,
    c.product_name,
    c.abc_class AS current_abc_class,
    c.revenue_rank AS current_rank,
    c.total_revenue AS current_revenue,
    p.prev_revenue_rank AS previous_rank,
    p.total_revenue AS previous_revenue,
    (c.revenue_rank - p.prev_revenue_rank) AS rank_change,
    CASE
        WHEN c.revenue_rank < p.prev_revenue_rank THEN 'IMPROVED'
        WHEN c.revenue_rank > p.prev_revenue_rank THEN 'DECLINED'
        ELSE 'STABLE'
    END AS trend
FROM current_period c
LEFT JOIN previous_period p ON p.product_id = c.product_id
ORDER BY ABS(c.revenue_rank - COALESCE(p.prev_revenue_rank, c.revenue_rank)) DESC;

COMMENT ON VIEW v_abc_transition_analysis IS 'Track ABC classification changes over time';

-- =====================================================
-- Function: Get ABC Report with Filters
-- =====================================================
CREATE OR REPLACE FUNCTION get_abc_report(
    abc_class_filter VARCHAR DEFAULT NULL, -- 'A', 'B', 'C'
    category_filter UUID DEFAULT NULL,
    min_revenue NUMERIC DEFAULT NULL
)
RETURNS TABLE(
    product_id UUID,
    sku VARCHAR,
    product_name VARCHAR,
    category_name VARCHAR,
    abc_class VARCHAR,
    total_revenue NUMERIC,
    revenue_percentage NUMERIC,
    cumulative_percentage NUMERIC,
    revenue_rank BIGINT,
    current_stock NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        v.product_id,
        v.sku,
        v.product_name,
        v.category_name,
        v.abc_class,
        v.total_revenue,
        v.revenue_percentage,
        v.cumulative_percentage,
        v.revenue_rank,
        v.current_stock
    FROM v_abc_curve_analysis v
    WHERE
        (abc_class_filter IS NULL OR v.abc_class = abc_class_filter)
        AND (category_filter IS NULL OR v.product_id IN (
            SELECT id FROM products WHERE category_id = category_filter
        ))
        AND (min_revenue IS NULL OR v.total_revenue >= min_revenue)
    ORDER BY v.revenue_rank;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_abc_report IS 'Get ABC analysis with flexible filtering';

-- =====================================================
-- Indexes for Performance
-- =====================================================

-- No additional indexes needed - using existing order_items indexes

-- =====================================================
-- End of Migration V025
-- =====================================================
