-- V071__drop_unused_tables.sql
-- Remove unused tables that have no Entity/Repository implementation
-- This cleanup reduces schema complexity and tenant provisioning time

-- ============================================================
-- Drop views that reference tables being removed
-- ============================================================

DROP VIEW IF EXISTS v_customer_summary CASCADE;
DROP VIEW IF EXISTS v_payment_summary CASCADE;
DROP VIEW IF EXISTS v_pending_payments CASCADE;
DROP VIEW IF EXISTS v_refund_summary CASCADE;
DROP VIEW IF EXISTS v_supplier_price_changes CASCADE;
DROP VIEW IF EXISTS v_mobile_receiving_sessions CASCADE;
DROP VIEW IF EXISTS v_mobile_receiving_scans CASCADE;
DROP VIEW IF EXISTS v_stock_transfer_items CASCADE;
DROP VIEW IF EXISTS v_active_alerts CASCADE;
DROP VIEW IF EXISTS v_product_costs CASCADE;
DROP VIEW IF EXISTS v_cost_changes CASCADE;
DROP VIEW IF EXISTS v_low_margin_products CASCADE;

-- ============================================================
-- Drop functions that reference tables being removed
-- ============================================================

DROP FUNCTION IF EXISTS track_supplier_price_change() CASCADE;
DROP FUNCTION IF EXISTS process_mobile_scan(UUID, VARCHAR, NUMERIC, VARCHAR, DATE) CASCADE;
DROP FUNCTION IF EXISTS complete_mobile_receiving_session(UUID) CASCADE;
DROP FUNCTION IF EXISTS match_barcode_to_product(VARCHAR, UUID) CASCADE;
DROP FUNCTION IF EXISTS calculate_weighted_average_cost(UUID, UUID, UUID, NUMERIC, NUMERIC, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS update_product_cost_after_movement() CASCADE;

-- ============================================================
-- Drop triggers
-- ============================================================

DROP TRIGGER IF EXISTS trigger_track_supplier_price_change ON supplier_products;
DROP TRIGGER IF EXISTS trigger_update_cost_after_movement ON inventory_movements;
DROP TRIGGER IF EXISTS trigger_product_costs_updated_at ON product_costs;
DROP TRIGGER IF EXISTS trigger_barcode_mappings_updated_at ON barcode_mappings;
DROP TRIGGER IF EXISTS trigger_payment_installments_updated_at ON payment_installments;

-- ============================================================
-- Drop tables in correct order (respecting foreign keys)
-- ============================================================

-- 1. Tables with no dependents first
DROP TABLE IF EXISTS customer_contacts CASCADE;
DROP TABLE IF EXISTS payment_installments CASCADE;
DROP TABLE IF EXISTS supplier_price_history CASCADE;
DROP TABLE IF EXISTS purchase_order_receipt_items CASCADE;
DROP TABLE IF EXISTS purchase_order_status_history CASCADE;
DROP TABLE IF EXISTS mobile_receiving_scans CASCADE;
DROP TABLE IF EXISTS stock_transfer_items CASCADE;
DROP TABLE IF EXISTS stock_transfer_status_history CASCADE;
DROP TABLE IF EXISTS alert_notifications CASCADE;
DROP TABLE IF EXISTS cost_history CASCADE;
DROP TABLE IF EXISTS product_costs CASCADE;
DROP TABLE IF EXISTS barcode_mappings CASCADE;

-- ============================================================
-- Recreate v_customer_summary without customer_contacts
-- ============================================================
CREATE OR REPLACE VIEW v_customer_summary AS
SELECT
    c.id AS customer_id,
    c.customer_type,
    CASE
        WHEN c.customer_type = 'INDIVIDUAL' THEN c.first_name || ' ' || c.last_name
        ELSE c.company_name
    END AS customer_name,
    c.email,
    c.phone,
    c.cpf,
    c.cnpj,
    COUNT(DISTINCT a.id) AS address_count,
    c.customer_segment,
    c.loyalty_tier,
    c.ativo
FROM customers c
LEFT JOIN customer_addresses a ON c.id = a.customer_id AND a.ativo = true
GROUP BY c.id, c.customer_type, c.first_name, c.last_name, c.company_name,
         c.email, c.phone, c.cpf, c.cnpj, c.customer_segment, c.loyalty_tier, c.ativo;

-- ============================================================
-- Recreate v_payment_summary without payment_installments reference
-- ============================================================
CREATE OR REPLACE VIEW v_payment_summary AS
SELECT
    p.id AS payment_id,
    p.payment_number,
    p.external_payment_id,
    o.order_number,
    o.customer_id,
    CASE
        WHEN c.customer_type = 'INDIVIDUAL'
            THEN c.first_name || ' ' || c.last_name
        ELSE c.company_name
    END AS customer_name,
    p.payment_method,
    p.payment_provider,
    p.amount,
    p.currency,
    p.status,
    p.card_brand,
    p.card_last_digits,
    p.created_at AS payment_date,
    p.authorized_at,
    p.captured_at,
    p.cancelled_at,
    p.expires_at,
    COALESCE(
        (SELECT SUM(amount)
         FROM payment_refunds
         WHERE payment_id = p.id
           AND status = 'COMPLETED'),
        0
    ) AS total_refunded
FROM payments p
INNER JOIN orders o ON p.order_id = o.id
INNER JOIN customers c ON o.customer_id = c.id;

-- ============================================================
-- Recreate v_stock_transfer_summary without items reference
-- ============================================================
CREATE OR REPLACE VIEW v_stock_transfer_summary AS
SELECT
    st.id AS transfer_id,
    st.transfer_number,
    st.transfer_type,
    sl.code AS source_location_code,
    sl.name AS source_location_name,
    dl.code AS destination_location_code,
    dl.name AS destination_location_name,
    st.status,
    st.requested_date,
    st.expected_date,
    st.shipped_date,
    st.received_date,
    st.reason,
    st.created_at
FROM stock_transfers st
INNER JOIN locations sl ON st.source_location_id = sl.id
INNER JOIN locations dl ON st.destination_location_id = dl.id;

-- ============================================================
-- Summary
-- ============================================================
-- Removed 12 unused tables:
-- 1. customer_contacts
-- 2. payment_installments
-- 3. supplier_price_history
-- 4. purchase_order_receipt_items
-- 5. purchase_order_status_history
-- 6. mobile_receiving_scans
-- 7. stock_transfer_items
-- 8. stock_transfer_status_history
-- 9. alert_notifications
-- 10. cost_history
-- 11. product_costs
-- 12. barcode_mappings
