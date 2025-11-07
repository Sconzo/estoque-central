-- =====================================================
-- Migration V021: Inventory Movement Report Views
-- Story 6.2 - Relatório de Movimentações de Estoque
-- =====================================================
-- Description: Creates views for detailed inventory movement reporting
--              - Movement history with product and location details
--              - Movement summary by type and period
--              - Stock balance tracking
-- =====================================================

-- =====================================================
-- View 1: Detailed Inventory Movements
-- =====================================================
-- Shows all inventory movements with complete details
CREATE OR REPLACE VIEW v_inventory_movements_detailed AS
SELECT
    im.id AS movement_id,
    im.movement_date,
    DATE(im.movement_date) AS movement_date_only,
    im.movement_type,
    im.quantity,
    im.unit_cost,
    (im.quantity * COALESCE(im.unit_cost, 0)) AS total_value,
    im.reference_type,
    im.reference_id,
    im.notes,

    -- Product details
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,
    p.unit_of_measure,

    -- Location details
    l.id AS location_id,
    l.code AS location_code,
    l.name AS location_name,
    l.location_type,

    -- Inventory state before/after (if available)
    i.quantity_available AS current_stock,
    i.minimum_quantity,
    i.maximum_quantity,

    -- User who made the movement
    im.created_by,
    im.created_at,

    -- Movement classification
    CASE
        WHEN im.movement_type IN ('PURCHASE', 'ADJUSTMENT_IN', 'TRANSFER_IN', 'RETURN_FROM_CUSTOMER') THEN 'IN'
        WHEN im.movement_type IN ('SALE', 'ADJUSTMENT_OUT', 'TRANSFER_OUT', 'RETURN_TO_SUPPLIER') THEN 'OUT'
        ELSE 'OTHER'
    END AS movement_direction

FROM inventory_movements im
INNER JOIN inventory i ON i.id = im.inventory_id
INNER JOIN products p ON p.id = i.product_id
INNER JOIN categories c ON c.id = p.category_id
INNER JOIN locations l ON l.id = i.location_id
WHERE im.ativo = true
  AND i.ativo = true
  AND p.ativo = true
  AND l.ativo = true
ORDER BY im.movement_date DESC, im.created_at DESC;

COMMENT ON VIEW v_inventory_movements_detailed IS 'Detailed inventory movements with product, location and user information';

-- =====================================================
-- View 2: Movement Summary by Type
-- =====================================================
-- Summarizes movements by type for quick analysis
CREATE OR REPLACE VIEW v_inventory_movements_summary_by_type AS
SELECT
    im.movement_type,
    CASE
        WHEN im.movement_type IN ('PURCHASE', 'ADJUSTMENT_IN', 'TRANSFER_IN', 'RETURN_FROM_CUSTOMER') THEN 'IN'
        WHEN im.movement_type IN ('SALE', 'ADJUSTMENT_OUT', 'TRANSFER_OUT', 'RETURN_TO_SUPPLIER') THEN 'OUT'
        ELSE 'OTHER'
    END AS movement_direction,
    COUNT(*) AS movement_count,
    SUM(ABS(im.quantity)) AS total_quantity,
    SUM(ABS(im.quantity) * COALESCE(im.unit_cost, 0)) AS total_value,
    ROUND(AVG(COALESCE(im.unit_cost, 0)), 2) AS average_unit_cost,
    MIN(im.movement_date) AS first_movement_date,
    MAX(im.movement_date) AS last_movement_date
FROM inventory_movements im
WHERE im.ativo = true
GROUP BY im.movement_type
ORDER BY total_value DESC;

COMMENT ON VIEW v_inventory_movements_summary_by_type IS 'Movement statistics grouped by movement type';

-- =====================================================
-- View 3: Movement Summary by Product
-- =====================================================
-- Summarizes movements by product for inventory analysis
CREATE OR REPLACE VIEW v_inventory_movements_summary_by_product AS
SELECT
    p.id AS product_id,
    p.sku,
    p.name AS product_name,
    c.name AS category_name,

    -- Total movements
    COUNT(*) AS total_movements,

    -- IN movements
    COUNT(CASE WHEN im.movement_type IN ('PURCHASE', 'ADJUSTMENT_IN', 'TRANSFER_IN', 'RETURN_FROM_CUSTOMER') THEN 1 END) AS in_movements_count,
    SUM(CASE WHEN im.movement_type IN ('PURCHASE', 'ADJUSTMENT_IN', 'TRANSFER_IN', 'RETURN_FROM_CUSTOMER') THEN im.quantity ELSE 0 END) AS total_quantity_in,

    -- OUT movements
    COUNT(CASE WHEN im.movement_type IN ('SALE', 'ADJUSTMENT_OUT', 'TRANSFER_OUT', 'RETURN_TO_SUPPLIER') THEN 1 END) AS out_movements_count,
    SUM(CASE WHEN im.movement_type IN ('SALE', 'ADJUSTMENT_OUT', 'TRANSFER_OUT', 'RETURN_TO_SUPPLIER') THEN ABS(im.quantity) ELSE 0 END) AS total_quantity_out,

    -- Net movement
    SUM(im.quantity) AS net_quantity_change,

    -- Value
    SUM(ABS(im.quantity) * COALESCE(im.unit_cost, 0)) AS total_value_moved,

    -- Dates
    MIN(im.movement_date) AS first_movement_date,
    MAX(im.movement_date) AS last_movement_date,

    -- Current stock
    (SELECT SUM(quantity_available) FROM inventory WHERE product_id = p.id AND ativo = true) AS current_stock

FROM inventory_movements im
INNER JOIN inventory i ON i.id = im.inventory_id
INNER JOIN products p ON p.id = i.product_id
INNER JOIN categories c ON c.id = p.category_id
WHERE im.ativo = true
  AND i.ativo = true
  AND p.ativo = true
GROUP BY p.id, p.sku, p.name, c.name
ORDER BY total_value_moved DESC;

COMMENT ON VIEW v_inventory_movements_summary_by_product IS 'Movement statistics grouped by product';

-- =====================================================
-- View 4: Movement Summary by Location
-- =====================================================
-- Summarizes movements by location for warehouse analysis
CREATE OR REPLACE VIEW v_inventory_movements_summary_by_location AS
SELECT
    l.id AS location_id,
    l.code AS location_code,
    l.name AS location_name,
    l.location_type,

    -- Total movements
    COUNT(*) AS total_movements,

    -- IN movements
    COUNT(CASE WHEN im.movement_type IN ('PURCHASE', 'ADJUSTMENT_IN', 'TRANSFER_IN', 'RETURN_FROM_CUSTOMER') THEN 1 END) AS in_movements_count,
    SUM(CASE WHEN im.movement_type IN ('PURCHASE', 'ADJUSTMENT_IN', 'TRANSFER_IN', 'RETURN_FROM_CUSTOMER') THEN im.quantity ELSE 0 END) AS total_quantity_in,

    -- OUT movements
    COUNT(CASE WHEN im.movement_type IN ('SALE', 'ADJUSTMENT_OUT', 'TRANSFER_OUT', 'RETURN_TO_SUPPLIER') THEN 1 END) AS out_movements_count,
    SUM(CASE WHEN im.movement_type IN ('SALE', 'ADJUSTMENT_OUT', 'TRANSFER_OUT', 'RETURN_TO_SUPPLIER') THEN ABS(im.quantity) ELSE 0 END) AS total_quantity_out,

    -- Value
    SUM(ABS(im.quantity) * COALESCE(im.unit_cost, 0)) AS total_value_moved,

    -- Dates
    MIN(im.movement_date) AS first_movement_date,
    MAX(im.movement_date) AS last_movement_date

FROM inventory_movements im
INNER JOIN inventory i ON i.id = im.inventory_id
INNER JOIN locations l ON l.id = i.location_id
WHERE im.ativo = true
  AND i.ativo = true
  AND l.ativo = true
GROUP BY l.id, l.code, l.name, l.location_type
ORDER BY total_value_moved DESC;

COMMENT ON VIEW v_inventory_movements_summary_by_location IS 'Movement statistics grouped by location';

-- =====================================================
-- View 5: Daily Movement Summary
-- =====================================================
-- Daily summary for trend analysis
CREATE OR REPLACE VIEW v_inventory_movements_daily_summary AS
SELECT
    DATE(im.movement_date) AS movement_date,
    COUNT(*) AS total_movements,

    -- IN movements
    COUNT(CASE WHEN im.movement_type IN ('PURCHASE', 'ADJUSTMENT_IN', 'TRANSFER_IN', 'RETURN_FROM_CUSTOMER') THEN 1 END) AS in_movements_count,
    SUM(CASE WHEN im.movement_type IN ('PURCHASE', 'ADJUSTMENT_IN', 'TRANSFER_IN', 'RETURN_FROM_CUSTOMER') THEN im.quantity ELSE 0 END) AS total_quantity_in,
    SUM(CASE WHEN im.movement_type IN ('PURCHASE', 'ADJUSTMENT_IN', 'TRANSFER_IN', 'RETURN_FROM_CUSTOMER') THEN ABS(im.quantity) * COALESCE(im.unit_cost, 0) ELSE 0 END) AS total_value_in,

    -- OUT movements
    COUNT(CASE WHEN im.movement_type IN ('SALE', 'ADJUSTMENT_OUT', 'TRANSFER_OUT', 'RETURN_TO_SUPPLIER') THEN 1 END) AS out_movements_count,
    SUM(CASE WHEN im.movement_type IN ('SALE', 'ADJUSTMENT_OUT', 'TRANSFER_OUT', 'RETURN_TO_SUPPLIER') THEN ABS(im.quantity) ELSE 0 END) AS total_quantity_out,
    SUM(CASE WHEN im.movement_type IN ('SALE', 'ADJUSTMENT_OUT', 'TRANSFER_OUT', 'RETURN_TO_SUPPLIER') THEN ABS(im.quantity) * COALESCE(im.unit_cost, 0) ELSE 0 END) AS total_value_out,

    -- Net
    SUM(im.quantity) AS net_quantity_change,
    SUM(ABS(im.quantity) * COALESCE(im.unit_cost, 0)) AS total_value_moved

FROM inventory_movements im
WHERE im.ativo = true
GROUP BY DATE(im.movement_date)
ORDER BY movement_date DESC;

COMMENT ON VIEW v_inventory_movements_daily_summary IS 'Daily movement summary for trend analysis';

-- =====================================================
-- View 6: Movement Summary by Reference
-- =====================================================
-- Groups movements by reference (order, PO, transfer, etc)
CREATE OR REPLACE VIEW v_inventory_movements_by_reference AS
SELECT
    im.reference_type,
    im.reference_id,
    COUNT(*) AS movement_count,
    SUM(ABS(im.quantity)) AS total_quantity,
    SUM(ABS(im.quantity) * COALESCE(im.unit_cost, 0)) AS total_value,
    MIN(im.movement_date) AS first_movement_date,
    MAX(im.movement_date) AS last_movement_date,

    -- Get reference details based on type
    CASE im.reference_type
        WHEN 'ORDER' THEN (SELECT order_number FROM orders WHERE id = im.reference_id AND ativo = true)
        WHEN 'PURCHASE_ORDER' THEN (SELECT po_number FROM purchase_orders WHERE id = im.reference_id AND ativo = true)
        WHEN 'TRANSFER' THEN (SELECT transfer_number FROM stock_transfers WHERE id = im.reference_id AND ativo = true)
        ELSE NULL
    END AS reference_number

FROM inventory_movements im
WHERE im.ativo = true
  AND im.reference_type IS NOT NULL
  AND im.reference_id IS NOT NULL
GROUP BY im.reference_type, im.reference_id
ORDER BY first_movement_date DESC;

COMMENT ON VIEW v_inventory_movements_by_reference IS 'Movement statistics grouped by reference (order, PO, transfer)';

-- =====================================================
-- View 7: Recent Movements (Last 30 Days)
-- =====================================================
-- Quick view of recent movements
CREATE OR REPLACE VIEW v_inventory_movements_recent AS
SELECT
    im.movement_date,
    im.movement_type,
    p.sku,
    p.name AS product_name,
    l.name AS location_name,
    im.quantity,
    im.unit_cost,
    (im.quantity * COALESCE(im.unit_cost, 0)) AS total_value,
    im.reference_type,
    im.notes,
    CASE
        WHEN im.movement_type IN ('PURCHASE', 'ADJUSTMENT_IN', 'TRANSFER_IN', 'RETURN_FROM_CUSTOMER') THEN 'IN'
        WHEN im.movement_type IN ('SALE', 'ADJUSTMENT_OUT', 'TRANSFER_OUT', 'RETURN_TO_SUPPLIER') THEN 'OUT'
        ELSE 'OTHER'
    END AS movement_direction
FROM inventory_movements im
INNER JOIN inventory i ON i.id = im.inventory_id
INNER JOIN products p ON p.id = i.product_id
INNER JOIN locations l ON l.id = i.location_id
WHERE im.ativo = true
  AND im.movement_date >= CURRENT_DATE - INTERVAL '30 days'
ORDER BY im.movement_date DESC
LIMIT 1000;

COMMENT ON VIEW v_inventory_movements_recent IS 'Recent inventory movements (last 30 days, max 1000 records)';

-- =====================================================
-- Function: Get Movement Report with Filters
-- =====================================================
-- Returns filtered movement report
CREATE OR REPLACE FUNCTION get_inventory_movement_report(
    start_date DATE DEFAULT NULL,
    end_date DATE DEFAULT NULL,
    product_uuid UUID DEFAULT NULL,
    location_uuid UUID DEFAULT NULL,
    movement_type_filter VARCHAR DEFAULT NULL,
    movement_direction_filter VARCHAR DEFAULT NULL,
    limit_rows INTEGER DEFAULT 1000
)
RETURNS TABLE(
    movement_id UUID,
    movement_date TIMESTAMP,
    movement_type VARCHAR,
    movement_direction VARCHAR,
    product_id UUID,
    sku VARCHAR,
    product_name VARCHAR,
    category_name VARCHAR,
    location_id UUID,
    location_code VARCHAR,
    location_name VARCHAR,
    quantity NUMERIC,
    unit_cost NUMERIC,
    total_value NUMERIC,
    reference_type VARCHAR,
    reference_id UUID,
    notes TEXT,
    created_by UUID,
    current_stock NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        v.movement_id,
        v.movement_date,
        v.movement_type,
        v.movement_direction,
        v.product_id,
        v.sku,
        v.product_name,
        v.category_name,
        v.location_id,
        v.location_code,
        v.location_name,
        v.quantity,
        v.unit_cost,
        v.total_value,
        v.reference_type,
        v.reference_id,
        v.notes,
        v.created_by,
        v.current_stock
    FROM v_inventory_movements_detailed v
    WHERE
        (start_date IS NULL OR DATE(v.movement_date) >= start_date)
        AND (end_date IS NULL OR DATE(v.movement_date) <= end_date)
        AND (product_uuid IS NULL OR v.product_id = product_uuid)
        AND (location_uuid IS NULL OR v.location_id = location_uuid)
        AND (movement_type_filter IS NULL OR v.movement_type = movement_type_filter)
        AND (movement_direction_filter IS NULL OR v.movement_direction = movement_direction_filter)
    ORDER BY v.movement_date DESC, v.created_at DESC
    LIMIT limit_rows;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_inventory_movement_report IS 'Returns filtered inventory movement report with optional filters';

-- =====================================================
-- Indexes for Performance
-- =====================================================

-- Index for movement date range queries
CREATE INDEX IF NOT EXISTS idx_inventory_movements_date
    ON inventory_movements(movement_date DESC)
    WHERE ativo = true;

-- Index for movement type filtering
CREATE INDEX IF NOT EXISTS idx_inventory_movements_type
    ON inventory_movements(movement_type, movement_date DESC)
    WHERE ativo = true;

-- Index for product filtering
CREATE INDEX IF NOT EXISTS idx_inventory_movements_inventory_product
    ON inventory_movements(inventory_id, movement_date DESC)
    WHERE ativo = true;

-- Index for reference lookups
CREATE INDEX IF NOT EXISTS idx_inventory_movements_reference
    ON inventory_movements(reference_type, reference_id, movement_date DESC)
    WHERE ativo = true AND reference_type IS NOT NULL;

-- =====================================================
-- End of Migration V021
-- =====================================================
