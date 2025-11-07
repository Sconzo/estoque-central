-- V012__create_cart_tables.sql
-- Story 4.2: Shopping Cart
-- Creates tables for shopping cart management

-- ============================================================
-- Table: carts
-- Stores shopping cart sessions
-- ============================================================
CREATE TABLE IF NOT EXISTS carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Customer reference (null for guest carts)
    customer_id UUID REFERENCES customers(id),

    -- Session info
    session_id VARCHAR(255),
    user_id UUID,

    -- Cart status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- Totals (calculated from items)
    subtotal NUMERIC(15, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    shipping_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total NUMERIC(15, 2) NOT NULL DEFAULT 0,

    -- Applied discounts
    coupon_code VARCHAR(50),
    discount_percentage NUMERIC(5, 2),

    -- Location for inventory check
    location_id UUID REFERENCES locations(id),

    -- Notes
    notes TEXT,

    -- Conversion tracking
    converted_to_order_id UUID,
    converted_at TIMESTAMP,

    -- Expiration
    expires_at TIMESTAMP,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_cart_status CHECK (status IN ('ACTIVE', 'ABANDONED', 'CONVERTED', 'EXPIRED')),
    CONSTRAINT check_positive_totals CHECK (
        subtotal >= 0 AND discount_amount >= 0 AND
        tax_amount >= 0 AND shipping_amount >= 0 AND total >= 0
    )
);

-- ============================================================
-- Table: cart_items
-- Stores items in shopping cart
-- ============================================================
CREATE TABLE IF NOT EXISTS cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Cart reference
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,

    -- Product reference (can be simple product or variant)
    product_id UUID NOT NULL REFERENCES products(id),
    product_variant_id UUID REFERENCES product_variants(id),

    -- Quantity
    quantity NUMERIC(15, 3) NOT NULL,

    -- Price at time of adding (snapshot)
    unit_price NUMERIC(15, 2) NOT NULL,
    subtotal NUMERIC(15, 2) NOT NULL,

    -- Discount on this item
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total NUMERIC(15, 2) NOT NULL,

    -- Availability check
    available_quantity NUMERIC(15, 3),
    is_available BOOLEAN NOT NULL DEFAULT true,

    -- Custom options/notes
    custom_options JSONB,
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_positive_quantity CHECK (quantity > 0),
    CONSTRAINT check_positive_price CHECK (unit_price >= 0),
    CONSTRAINT unique_product_per_cart UNIQUE (cart_id, product_id, product_variant_id)
);

-- ============================================================
-- Indexes
-- ============================================================

-- Carts indexes
CREATE INDEX idx_carts_tenant ON carts(tenant_id);
CREATE INDEX idx_carts_customer ON carts(customer_id) WHERE customer_id IS NOT NULL;
CREATE INDEX idx_carts_session ON carts(session_id) WHERE session_id IS NOT NULL;
CREATE INDEX idx_carts_user ON carts(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_carts_status ON carts(status);
CREATE INDEX idx_carts_active ON carts(status, updated_at) WHERE status = 'ACTIVE';
CREATE INDEX idx_carts_expires ON carts(expires_at) WHERE expires_at IS NOT NULL AND status = 'ACTIVE';

-- Cart items indexes
CREATE INDEX idx_cart_items_tenant ON cart_items(tenant_id);
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON cart_items(product_id);
CREATE INDEX idx_cart_items_variant ON cart_items(product_variant_id) WHERE product_variant_id IS NOT NULL;

-- ============================================================
-- Triggers for updated_at
-- ============================================================
CREATE TRIGGER trigger_carts_updated_at
    BEFORE UPDATE ON carts
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

CREATE TRIGGER trigger_cart_items_updated_at
    BEFORE UPDATE ON cart_items
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Function: Calculate cart totals
-- ============================================================
CREATE OR REPLACE FUNCTION calculate_cart_totals(cart_uuid UUID)
RETURNS VOID AS $$
DECLARE
    cart_subtotal NUMERIC(15, 2);
    cart_total NUMERIC(15, 2);
BEGIN
    -- Calculate subtotal from items
    SELECT COALESCE(SUM(total), 0)
    INTO cart_subtotal
    FROM cart_items
    WHERE cart_id = cart_uuid;

    -- Calculate total (subtotal - discount + tax + shipping)
    SELECT
        cart_subtotal - COALESCE(c.discount_amount, 0) +
        COALESCE(c.tax_amount, 0) + COALESCE(c.shipping_amount, 0)
    INTO cart_total
    FROM carts c
    WHERE c.id = cart_uuid;

    -- Update cart totals
    UPDATE carts
    SET subtotal = cart_subtotal,
        total = cart_total,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = cart_uuid;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Check cart items availability
-- ============================================================
CREATE OR REPLACE FUNCTION check_cart_availability(cart_uuid UUID, loc_id UUID)
RETURNS TABLE(
    item_id UUID,
    product_id UUID,
    requested_qty NUMERIC,
    available_qty NUMERIC,
    is_available BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        ci.id AS item_id,
        ci.product_id,
        ci.quantity AS requested_qty,
        COALESCE(i.available_quantity, 0) AS available_qty,
        (COALESCE(i.available_quantity, 0) >= ci.quantity) AS is_available
    FROM cart_items ci
    LEFT JOIN inventory i ON (
        (ci.product_variant_id IS NULL AND i.product_id = ci.product_id) OR
        (ci.product_variant_id IS NOT NULL AND i.product_id = ci.product_variant_id)
    ) AND i.location_id = loc_id
    WHERE ci.cart_id = cart_uuid;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Expire old carts (scheduled job)
-- ============================================================
CREATE OR REPLACE FUNCTION expire_old_carts()
RETURNS INTEGER AS $$
DECLARE
    expired_count INTEGER;
BEGIN
    UPDATE carts
    SET status = 'EXPIRED',
        updated_at = CURRENT_TIMESTAMP
    WHERE status = 'ACTIVE'
      AND expires_at IS NOT NULL
      AND expires_at < CURRENT_TIMESTAMP;

    GET DIAGNOSTICS expired_count = ROW_COUNT;
    RETURN expired_count;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- View: Active carts summary
-- ============================================================
CREATE OR REPLACE VIEW v_active_carts AS
SELECT
    c.id AS cart_id,
    c.customer_id,
    CASE
        WHEN cu.customer_type = 'INDIVIDUAL'
            THEN cu.first_name || ' ' || cu.last_name
        ELSE cu.company_name
    END AS customer_name,
    c.status,
    COUNT(ci.id) AS item_count,
    c.subtotal,
    c.discount_amount,
    c.total,
    c.created_at,
    c.updated_at,
    c.expires_at,
    CASE
        WHEN c.expires_at IS NOT NULL AND c.expires_at < CURRENT_TIMESTAMP
            THEN true
        ELSE false
    END AS is_expired
FROM carts c
LEFT JOIN customers cu ON c.customer_id = cu.customer_id
LEFT JOIN cart_items ci ON c.id = ci.cart_id
WHERE c.status = 'ACTIVE'
GROUP BY c.id, c.customer_id, cu.customer_type, cu.first_name,
         cu.last_name, cu.company_name, c.status, c.subtotal,
         c.discount_amount, c.total, c.created_at, c.updated_at, c.expires_at;

-- ============================================================
-- View: Cart items with product details
-- ============================================================
CREATE OR REPLACE VIEW v_cart_items_detail AS
SELECT
    ci.id AS cart_item_id,
    ci.cart_id,
    ci.product_id,
    p.name AS product_name,
    p.sku AS product_sku,
    ci.product_variant_id,
    pv.name AS variant_name,
    pv.sku AS variant_sku,
    ci.quantity,
    ci.unit_price,
    ci.discount_amount,
    ci.total,
    ci.is_available,
    ci.available_quantity
FROM cart_items ci
INNER JOIN products p ON ci.product_id = p.id
LEFT JOIN product_variants pv ON ci.product_variant_id = pv.id;

-- ============================================================
-- Initial data: Create sample cart
-- ============================================================

-- Create active cart for João Silva
INSERT INTO carts (
    tenant_id, customer_id, status, location_id, expires_at
)
SELECT
    t.id,
    c.id,
    'ACTIVE',
    (SELECT id FROM locations WHERE code = 'MAIN' LIMIT 1),
    CURRENT_TIMESTAMP + INTERVAL '7 days'
FROM tenants t
CROSS JOIN customers c
WHERE c.email = 'joao.silva@email.com'
LIMIT 1;

-- Add sample items to cart
INSERT INTO cart_items (
    tenant_id, cart_id, product_id, quantity, unit_price, subtotal, total
)
SELECT
    t.id,
    cart.id,
    p.id,
    2,
    p.price,
    p.price * 2,
    p.price * 2
FROM tenants t
CROSS JOIN carts cart
CROSS JOIN products p
WHERE cart.customer_id = (SELECT id FROM customers WHERE email = 'joao.silva@email.com' LIMIT 1)
  AND p.sku = 'NOTE-DELL-I15-001'
LIMIT 1;

-- Update cart totals
SELECT calculate_cart_totals(c.id)
FROM carts c
WHERE c.customer_id = (SELECT id FROM customers WHERE email = 'joao.silva@email.com' LIMIT 1);
