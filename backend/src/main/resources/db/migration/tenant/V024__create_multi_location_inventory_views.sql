-- =====================================================
-- Migration V024: Multi-Location Inventory Views
-- Story 6.5 - Relatório de Estoque Atual Multi-Depósito
-- =====================================================
-- Description: Creates views for multi-location inventory reporting
--              - Consolidated inventory across locations
--              - Detailed by location
--              - Stock distribution analysis
-- =====================================================

-- =====================================================
-- View 1: Consolidated Inventory (All Locations)
-- =====================================================
-- Total stock for each product across all locations
CREATE OR REPLACE VIEW v_inventory_consolidated AS
SELECT
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,
    p.unit_of_measure,

    -- Aggregated quantities
    COUNT(DISTINCT i.location_id) AS location_count,
    SUM(i.quantity_available) AS total_available,
    SUM(i.quantity_reserved) AS total_reserved,
    SUM(i.quantity_available + i.quantity_reserved) AS total_on_hand,
    SUM(i.minimum_quantity) AS total_minimum,
    SUM(i.maximum_quantity) AS total_maximum,

    -- Stock status
    CASE
        WHEN SUM(i.quantity_available) <= 0 THEN 'OUT_OF_STOCK'
        WHEN SUM(i.quantity_available) < SUM(i.minimum_quantity) THEN 'LOW'
        WHEN SUM(i.quantity_available) > SUM(i.maximum_quantity) THEN 'EXCESS'
        ELSE 'OK'
    END AS stock_status,

    -- Cost valuation
    COALESCE((
        SELECT SUM(i2.quantity_available * pc.average_cost)
        FROM inventory i2
        LEFT JOIN product_costs pc ON pc.product_id = i2.product_id
            AND pc.location_id = i2.location_id
        WHERE i2.product_id = p.id
          AND i2.ativo = true
    ), 0) AS total_value_at_cost,

    -- Average cost across locations
    COALESCE((
        SELECT AVG(pc.average_cost)
        FROM product_costs pc
        WHERE pc.product_id = p.id
    ), 0) AS average_cost

FROM products p
INNER JOIN categories c ON c.id = p.category_id
LEFT JOIN inventory i ON i.product_id = p.id AND i.ativo = true
WHERE p.ativo = true
GROUP BY p.id, p.sku, p.name, c.name, p.unit_of_measure
ORDER BY p.name;

COMMENT ON VIEW v_inventory_consolidated IS 'Consolidated inventory across all locations';

-- =====================================================
-- View 2: Inventory by Location (Detailed)
-- =====================================================
-- Stock details for each product at each location
CREATE OR REPLACE VIEW v_inventory_by_location AS
SELECT
    i.id AS inventory_id,
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,
    l.id AS location_id,
    l.code AS location_code,
    l.name AS location_name,
    l.location_type,

    -- Quantities
    i.quantity_available,
    i.quantity_reserved,
    (i.quantity_available + i.quantity_reserved) AS quantity_on_hand,
    i.minimum_quantity,
    i.maximum_quantity,
    i.reorder_point,

    -- Stock status
    CASE
        WHEN i.quantity_available <= 0 THEN 'OUT_OF_STOCK'
        WHEN i.quantity_available < i.reorder_point THEN 'CRITICAL'
        WHEN i.quantity_available < i.minimum_quantity THEN 'LOW'
        WHEN i.quantity_available > i.maximum_quantity THEN 'EXCESS'
        ELSE 'OK'
    END AS stock_status,

    -- Fill rate
    CASE
        WHEN i.maximum_quantity > 0 THEN
            ROUND((i.quantity_available::NUMERIC / i.maximum_quantity * 100), 2)
        ELSE 0
    END AS fill_rate_percentage,

    -- Cost valuation
    COALESCE(pc.average_cost, 0) AS unit_cost,
    COALESCE(i.quantity_available * pc.average_cost, 0) AS total_value_at_cost,

    -- Last movement
    (
        SELECT MAX(movement_date)
        FROM inventory_movements
        WHERE inventory_id = i.id AND ativo = true
    ) AS last_movement_date,

    -- Updated
    i.updated_at

FROM inventory i
INNER JOIN products p ON p.id = i.product_id
INNER JOIN categories c ON c.id = p.category_id
INNER JOIN locations l ON l.id = i.location_id
LEFT JOIN product_costs pc ON pc.product_id = i.product_id
    AND pc.location_id = i.location_id
WHERE i.ativo = true
  AND p.ativo = true
  AND l.ativo = true
ORDER BY p.name, l.name;

COMMENT ON VIEW v_inventory_by_location IS 'Detailed inventory for each product at each location';

-- =====================================================
-- View 3: Inventory Summary by Location
-- =====================================================
-- Aggregated metrics per location
CREATE OR REPLACE VIEW v_inventory_summary_by_location AS
SELECT
    l.id AS location_id,
    l.code AS location_code,
    l.name AS location_name,
    l.location_type,

    -- Product counts
    COUNT(DISTINCT i.product_id) AS unique_products,
    COUNT(DISTINCT p.category_id) AS unique_categories,

    -- Quantity metrics
    SUM(i.quantity_available) AS total_available,
    SUM(i.quantity_reserved) AS total_reserved,
    SUM(i.quantity_available + i.quantity_reserved) AS total_on_hand,

    -- Stock status counts
    COUNT(CASE WHEN i.quantity_available <= 0 THEN 1 END) AS out_of_stock_count,
    COUNT(CASE WHEN i.quantity_available > 0 AND i.quantity_available < i.reorder_point THEN 1 END) AS critical_count,
    COUNT(CASE WHEN i.quantity_available >= i.reorder_point AND i.quantity_available < i.minimum_quantity THEN 1 END) AS low_count,
    COUNT(CASE WHEN i.quantity_available > i.maximum_quantity THEN 1 END) AS excess_count,

    -- Value metrics
    COALESCE(SUM(i.quantity_available * pc.average_cost), 0) AS total_value_at_cost,
    COALESCE(ROUND(AVG(pc.average_cost), 2), 0) AS average_unit_cost

FROM locations l
LEFT JOIN inventory i ON i.location_id = l.id AND i.ativo = true
LEFT JOIN products p ON p.id = i.product_id AND p.ativo = true
LEFT JOIN product_costs pc ON pc.product_id = i.product_id
    AND pc.location_id = i.location_id
WHERE l.ativo = true
GROUP BY l.id, l.code, l.name, l.location_type
ORDER BY total_value_at_cost DESC;

COMMENT ON VIEW v_inventory_summary_by_location IS 'Aggregated inventory metrics per location';

-- =====================================================
-- View 4: Stock Distribution Analysis
-- =====================================================
-- How stock is distributed across locations
CREATE OR REPLACE VIEW v_stock_distribution AS
SELECT
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,

    -- Total across all locations
    SUM(i.quantity_available) AS total_available,

    -- Distribution by location
    jsonb_object_agg(
        l.code,
        jsonb_build_object(
            'location_name', l.name,
            'quantity', i.quantity_available,
            'percentage', ROUND(
                (i.quantity_available::NUMERIC / NULLIF(SUM(i.quantity_available) OVER (PARTITION BY p.id), 0) * 100),
                2
            )
        )
    ) AS distribution_by_location,

    -- Concentration metrics
    MAX(i.quantity_available) AS max_location_qty,
    MIN(i.quantity_available) AS min_location_qty,
    STDDEV(i.quantity_available) AS qty_std_deviation

FROM products p
INNER JOIN categories c ON c.id = p.category_id
INNER JOIN inventory i ON i.product_id = p.id AND i.ativo = true
INNER JOIN locations l ON l.id = i.location_id AND l.ativo = true
WHERE p.ativo = true
GROUP BY p.id, p.sku, p.name, c.name
HAVING SUM(i.quantity_available) > 0
ORDER BY total_available DESC;

COMMENT ON VIEW v_stock_distribution IS 'Stock distribution analysis across locations';

-- =====================================================
-- View 5: Low Stock Items by Location
-- =====================================================
-- Products below minimum at each location
CREATE OR REPLACE VIEW v_low_stock_by_location AS
SELECT
    l.id AS location_id,
    l.code AS location_code,
    l.name AS location_name,
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,

    i.quantity_available,
    i.minimum_quantity,
    i.reorder_point,
    (i.minimum_quantity - i.quantity_available) AS quantity_needed,

    CASE
        WHEN i.quantity_available <= 0 THEN 'OUT_OF_STOCK'
        WHEN i.quantity_available < i.reorder_point THEN 'CRITICAL'
        ELSE 'LOW'
    END AS urgency_level,

    COALESCE(pc.average_cost, 0) AS unit_cost,
    COALESCE((i.minimum_quantity - i.quantity_available) * pc.average_cost, 0) AS replenishment_cost

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
        ELSE 3
    END,
    l.name,
    i.quantity_available ASC;

COMMENT ON VIEW v_low_stock_by_location IS 'Low stock items requiring replenishment at each location';

-- =====================================================
-- View 6: Excess Stock by Location
-- =====================================================
-- Products above maximum at each location
CREATE OR REPLACE VIEW v_excess_stock_by_location AS
SELECT
    l.id AS location_id,
    l.code AS location_code,
    l.name AS location_name,
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,

    i.quantity_available,
    i.maximum_quantity,
    (i.quantity_available - i.maximum_quantity) AS excess_quantity,
    ROUND((i.quantity_available - i.maximum_quantity)::NUMERIC / i.maximum_quantity * 100, 2) AS excess_percentage,

    COALESCE(pc.average_cost, 0) AS unit_cost,
    COALESCE((i.quantity_available - i.maximum_quantity) * pc.average_cost, 0) AS excess_value

FROM inventory i
INNER JOIN products p ON p.id = i.product_id
INNER JOIN categories c ON c.id = p.category_id
INNER JOIN locations l ON l.id = i.location_id
LEFT JOIN product_costs pc ON pc.product_id = i.product_id
    AND pc.location_id = i.location_id
WHERE i.ativo = true
  AND p.ativo = true
  AND l.ativo = true
  AND i.quantity_available > i.maximum_quantity
ORDER BY excess_value DESC;

COMMENT ON VIEW v_excess_stock_by_location IS 'Excess stock items above maximum at each location';

-- =====================================================
-- Function: Get Inventory Report with Filters
-- =====================================================
CREATE OR REPLACE FUNCTION get_multi_location_inventory_report(
    location_filter UUID DEFAULT NULL,
    category_filter UUID DEFAULT NULL,
    stock_status_filter VARCHAR DEFAULT NULL, -- 'OUT_OF_STOCK', 'LOW', 'OK', 'EXCESS'
    show_only_available BOOLEAN DEFAULT false
)
RETURNS TABLE(
    product_id UUID,
    sku VARCHAR,
    product_name VARCHAR,
    category_name VARCHAR,
    location_id UUID,
    location_name VARCHAR,
    quantity_available NUMERIC,
    quantity_reserved NUMERIC,
    minimum_quantity NUMERIC,
    stock_status VARCHAR,
    unit_cost NUMERIC,
    total_value NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        v.product_id,
        v.sku,
        v.product_name,
        v.category_name,
        v.location_id,
        v.location_name,
        v.quantity_available,
        v.quantity_reserved,
        v.minimum_quantity,
        v.stock_status,
        v.unit_cost,
        v.total_value_at_cost
    FROM v_inventory_by_location v
    WHERE
        (location_filter IS NULL OR v.location_id = location_filter)
        AND (category_filter IS NULL OR v.product_id IN (
            SELECT id FROM products WHERE category_id = category_filter
        ))
        AND (stock_status_filter IS NULL OR v.stock_status = stock_status_filter)
        AND (NOT show_only_available OR v.quantity_available > 0)
    ORDER BY v.product_name, v.location_name;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_multi_location_inventory_report IS 'Get multi-location inventory with flexible filtering';

-- =====================================================
-- Indexes for Performance
-- =====================================================

-- Index for location-based queries
CREATE INDEX IF NOT EXISTS idx_inventory_location_product
    ON inventory(location_id, product_id, quantity_available)
    WHERE ativo = true;

-- Index for stock status queries
CREATE INDEX IF NOT EXISTS idx_inventory_stock_levels
    ON inventory(product_id, quantity_available, minimum_quantity, maximum_quantity)
    WHERE ativo = true;

-- =====================================================
-- End of Migration V024
-- =====================================================
