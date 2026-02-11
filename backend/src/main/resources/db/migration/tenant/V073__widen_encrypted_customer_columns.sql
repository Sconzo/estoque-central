-- V073: Widen encrypted customer columns
-- CPF, CNPJ and email are encrypted with AES-256-GCM (IV + ciphertext + auth tag + Base64)
-- Must drop all dependent views before ALTER, then recreate them

-- Drop all views that reference customers columns
DROP VIEW IF EXISTS v_customer_summary CASCADE;
DROP VIEW IF EXISTS v_order_summary CASCADE;
DROP VIEW IF EXISTS v_pending_orders CASCADE;
DROP VIEW IF EXISTS v_payment_summary CASCADE;

-- Widen encrypted columns
ALTER TABLE customers ALTER COLUMN cpf TYPE VARCHAR(255);
ALTER TABLE customers ALTER COLUMN cnpj TYPE VARCHAR(255);
ALTER TABLE customers ALTER COLUMN email TYPE VARCHAR(500);

-- Recreate v_customer_summary
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

-- Recreate v_order_summary
CREATE OR REPLACE VIEW v_order_summary AS
SELECT
    o.id AS order_id,
    o.order_number,
    o.customer_id,
    CASE
        WHEN c.customer_type = 'INDIVIDUAL'
            THEN c.first_name || ' ' || c.last_name
        ELSE c.company_name
    END AS customer_name,
    c.email AS customer_email,
    o.status,
    o.payment_status,
    COUNT(oi.id) AS item_count,
    SUM(oi.quantity) AS total_items,
    o.subtotal,
    o.discount_amount,
    o.tax_amount,
    o.shipping_amount,
    o.total,
    o.shipping_method,
    o.tracking_number,
    l.name AS fulfillment_location,
    o.created_at AS order_date,
    o.fulfilled_at,
    o.shipped_at,
    o.delivered_at
FROM orders o
INNER JOIN customers c ON o.customer_id = c.id
LEFT JOIN order_items oi ON o.id = oi.order_id
LEFT JOIN locations l ON o.location_id = l.id
GROUP BY
    o.id, o.order_number, o.customer_id, c.customer_type,
    c.first_name, c.last_name, c.company_name, c.email,
    o.status, o.payment_status, o.subtotal, o.discount_amount,
    o.tax_amount, o.shipping_amount, o.total, o.shipping_method,
    o.tracking_number, l.name, o.created_at, o.fulfilled_at,
    o.shipped_at, o.delivered_at;

-- Recreate v_pending_orders
CREATE OR REPLACE VIEW v_pending_orders AS
SELECT
    o.id AS order_id,
    o.order_number,
    o.customer_id,
    CASE
        WHEN c.customer_type = 'INDIVIDUAL'
            THEN c.first_name || ' ' || c.last_name
        ELSE c.company_name
    END AS customer_name,
    o.status,
    o.payment_status,
    o.total,
    o.created_at,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - o.created_at)) / 3600 AS hours_since_order,
    CASE
        WHEN o.status = 'PENDING' AND o.payment_status = 'PENDING' THEN 'AWAITING_PAYMENT'
        WHEN o.status = 'CONFIRMED' AND o.payment_status = 'CAPTURED' THEN 'READY_TO_PROCESS'
        WHEN o.status = 'PROCESSING' THEN 'IN_FULFILLMENT'
        WHEN o.status = 'READY_TO_SHIP' THEN 'READY_FOR_SHIPMENT'
        ELSE 'OTHER'
    END AS action_required
FROM orders o
INNER JOIN customers c ON o.customer_id = c.id
WHERE o.status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'READY_TO_SHIP')
ORDER BY o.created_at ASC;

-- Recreate v_payment_summary
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
