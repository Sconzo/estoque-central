-- V077__create_stock_summary_view.sql
-- Creates v_stock_summary view used by StockService.getAllStock()
-- Joins inventory with locations, products, and product_variants
-- to provide a denormalized view for stock queries with filters.

CREATE OR REPLACE VIEW v_stock_summary AS
SELECT
    i.id,
    i.tenant_id,
    i.product_id,
    i.variant_id,
    i.location_id,
    l.name                                                        AS location_name,
    l.code                                                        AS location_code,
    COALESCE(p.name, pp.name || ' - ' || pv.sku)                AS product_name,
    COALESCE(p.sku,  pv.sku)                                     AS sku,
    i.quantity                                                    AS quantity_available,
    i.reserved_quantity,
    i.available_quantity                                          AS quantity_for_sale,
    i.min_quantity                                                AS minimum_quantity,
    i.max_quantity                                                AS maximum_quantity,
    CASE
        WHEN i.min_quantity IS NULL OR i.min_quantity = 0 THEN 'NOT_SET'
        WHEN i.available_quantity >= i.min_quantity              THEN 'OK'
        WHEN i.available_quantity >= i.min_quantity * 0.2        THEN 'LOW'
        ELSE 'CRITICAL'
    END                                                           AS stock_status,
    CASE
        WHEN i.min_quantity IS NULL OR i.min_quantity = 0 THEN 0.0
        ELSE ROUND((i.available_quantity * 100.0 / i.min_quantity)::numeric, 2)
    END                                                           AS percentage_of_minimum
FROM inventory i
INNER JOIN locations l  ON i.location_id        = l.id
LEFT  JOIN products p   ON i.product_id         = p.id
LEFT  JOIN product_variants pv ON i.variant_id  = pv.id
LEFT  JOIN products pp  ON pv.parent_product_id = pp.id;
