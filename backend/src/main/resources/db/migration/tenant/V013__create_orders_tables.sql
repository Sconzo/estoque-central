-- V013__create_orders_tables.sql
-- Story 4.3: Order Processing
-- Creates tables for order management and fulfillment

-- ============================================================
-- Table: orders
-- Stores order header information
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Order identification
    order_number VARCHAR(50) NOT NULL,

    -- Customer reference
    customer_id UUID NOT NULL REFERENCES customers(id),
    user_id UUID,

    -- Cart conversion tracking
    cart_id UUID REFERENCES carts(id),

    -- Order status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    -- Totals (snapshot from cart)
    subtotal NUMERIC(15, 2) NOT NULL,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    shipping_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total NUMERIC(15, 2) NOT NULL,

    -- Applied discounts
    coupon_code VARCHAR(50),
    discount_percentage NUMERIC(5, 2),

    -- Shipping information
    shipping_address_id UUID REFERENCES customer_addresses(id),
    billing_address_id UUID REFERENCES customer_addresses(id),
    shipping_method VARCHAR(50),
    tracking_number VARCHAR(100),

    -- Payment information
    payment_method VARCHAR(50),
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMP,

    -- Fulfillment
    location_id UUID REFERENCES locations(id),
    fulfilled_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    -- Notes
    customer_notes TEXT,
    internal_notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT unique_order_number_per_tenant UNIQUE (tenant_id, order_number),
    CONSTRAINT check_order_status CHECK (status IN (
        'PENDING', 'CONFIRMED', 'PROCESSING', 'READY_TO_SHIP',
        'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED', 'FAILED'
    )),
    CONSTRAINT check_payment_status CHECK (payment_status IN (
        'PENDING', 'AUTHORIZED', 'CAPTURED', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED'
    )),
    CONSTRAINT check_positive_totals CHECK (
        subtotal >= 0 AND discount_amount >= 0 AND
        tax_amount >= 0 AND shipping_amount >= 0 AND total >= 0
    )
);

-- ============================================================
-- Table: order_items
-- Stores line items for each order
-- ============================================================
CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Order reference
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,

    -- Product reference (snapshot)
    product_id UUID NOT NULL REFERENCES products(id),
    product_variant_id UUID REFERENCES product_variants(id),
    product_name VARCHAR(200) NOT NULL,
    product_sku VARCHAR(100) NOT NULL,
    variant_name VARCHAR(200),
    variant_sku VARCHAR(100),

    -- Quantity and pricing (snapshot)
    quantity NUMERIC(15, 3) NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    subtotal NUMERIC(15, 2) NOT NULL,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total NUMERIC(15, 2) NOT NULL,

    -- Fulfillment tracking
    quantity_fulfilled NUMERIC(15, 3) NOT NULL DEFAULT 0,
    quantity_cancelled NUMERIC(15, 3) NOT NULL DEFAULT 0,
    quantity_refunded NUMERIC(15, 3) NOT NULL DEFAULT 0,

    -- Inventory reservation
    inventory_reserved BOOLEAN NOT NULL DEFAULT false,
    inventory_fulfilled BOOLEAN NOT NULL DEFAULT false,

    -- Custom options
    custom_options JSONB,
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_positive_quantity CHECK (quantity > 0),
    CONSTRAINT check_positive_price CHECK (unit_price >= 0),
    CONSTRAINT check_fulfillment_quantities CHECK (
        quantity_fulfilled >= 0 AND
        quantity_cancelled >= 0 AND
        quantity_refunded >= 0 AND
        (quantity_fulfilled + quantity_cancelled) <= quantity
    )
);

-- ============================================================
-- Table: order_status_history
-- Tracks order status changes over time
-- ============================================================
CREATE TABLE IF NOT EXISTS order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Order reference
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,

    -- Status change
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,

    -- Details
    comment TEXT,
    notify_customer BOOLEAN NOT NULL DEFAULT false,
    notified_at TIMESTAMP,

    -- Who made the change
    changed_by UUID,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_from_status CHECK (from_status IN (
        'PENDING', 'CONFIRMED', 'PROCESSING', 'READY_TO_SHIP',
        'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED', 'FAILED'
    )),
    CONSTRAINT check_to_status CHECK (to_status IN (
        'PENDING', 'CONFIRMED', 'PROCESSING', 'READY_TO_SHIP',
        'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED', 'FAILED'
    ))
);

-- ============================================================
-- Indexes
-- ============================================================

-- Orders indexes
CREATE INDEX idx_orders_tenant ON orders(tenant_id);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_location ON orders(location_id);
CREATE INDEX idx_orders_created ON orders(created_at DESC);
CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_orders_cart ON orders(cart_id) WHERE cart_id IS NOT NULL;

-- Order items indexes
CREATE INDEX idx_order_items_tenant ON order_items(tenant_id);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
CREATE INDEX idx_order_items_variant ON order_items(product_variant_id) WHERE product_variant_id IS NOT NULL;

-- Order status history indexes
CREATE INDEX idx_order_status_history_tenant ON order_status_history(tenant_id);
CREATE INDEX idx_order_status_history_order ON order_status_history(order_id);
CREATE INDEX idx_order_status_history_changed_at ON order_status_history(changed_at DESC);

-- ============================================================
-- Triggers for updated_at
-- ============================================================
CREATE TRIGGER trigger_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

CREATE TRIGGER trigger_order_items_updated_at
    BEFORE UPDATE ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Function: Generate unique order number
-- ============================================================
CREATE OR REPLACE FUNCTION generate_order_number(tenant_uuid UUID)
RETURNS VARCHAR AS $$
DECLARE
    current_year VARCHAR(4);
    order_count INTEGER;
    order_num VARCHAR(50);
BEGIN
    current_year := EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::VARCHAR;

    -- Count orders in current year for this tenant
    SELECT COUNT(*) + 1
    INTO order_count
    FROM orders
    WHERE tenant_id = tenant_uuid
      AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_TIMESTAMP);

    -- Format: ORD-2025-00001
    order_num := 'ORD-' || current_year || '-' || LPAD(order_count::TEXT, 5, '0');

    RETURN order_num;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Reserve inventory for order
-- ============================================================
CREATE OR REPLACE FUNCTION reserve_order_inventory(order_uuid UUID)
RETURNS BOOLEAN AS $$
DECLARE
    item_record RECORD;
    available_qty NUMERIC(15, 3);
    reservation_id UUID;
BEGIN
    -- Check if all items have sufficient inventory
    FOR item_record IN
        SELECT
            oi.id AS item_id,
            oi.product_id,
            oi.product_variant_id,
            oi.quantity,
            o.location_id
        FROM order_items oi
        INNER JOIN orders o ON oi.order_id = o.id
        WHERE oi.order_id = order_uuid
          AND oi.inventory_reserved = false
    LOOP
        -- Get available quantity
        SELECT COALESCE(available_quantity, 0)
        INTO available_qty
        FROM inventory
        WHERE (
            (item_record.product_variant_id IS NULL AND product_id = item_record.product_id) OR
            (item_record.product_variant_id IS NOT NULL AND product_id = item_record.product_variant_id)
        )
        AND location_id = item_record.location_id;

        -- Check if sufficient quantity available
        IF available_qty < item_record.quantity THEN
            RETURN false;
        END IF;

        -- Create reservation
        INSERT INTO inventory_reservations (
            id, tenant_id, inventory_id, order_id, order_item_id,
            reserved_quantity, expires_at
        )
        SELECT
            gen_random_uuid(),
            i.tenant_id,
            i.id,
            order_uuid,
            item_record.item_id,
            item_record.quantity,
            CURRENT_TIMESTAMP + INTERVAL '7 days'
        FROM inventory i
        WHERE (
            (item_record.product_variant_id IS NULL AND i.product_id = item_record.product_id) OR
            (item_record.product_variant_id IS NOT NULL AND i.product_id = item_record.product_variant_id)
        )
        AND i.location_id = item_record.location_id;

        -- Mark item as reserved
        UPDATE order_items
        SET inventory_reserved = true,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = item_record.item_id;
    END LOOP;

    RETURN true;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Fulfill order inventory
-- ============================================================
CREATE OR REPLACE FUNCTION fulfill_order_inventory(order_uuid UUID)
RETURNS BOOLEAN AS $$
DECLARE
    item_record RECORD;
    movement_id UUID;
BEGIN
    -- Process each order item
    FOR item_record IN
        SELECT
            oi.id AS item_id,
            oi.product_id,
            oi.product_variant_id,
            oi.quantity,
            o.location_id,
            o.order_number
        FROM order_items oi
        INNER JOIN orders o ON oi.order_id = o.id
        WHERE oi.order_id = order_uuid
          AND oi.inventory_fulfilled = false
    LOOP
        -- Create outbound movement
        INSERT INTO inventory_movements (
            id, tenant_id, inventory_id, movement_type, quantity,
            reference_type, reference_id, notes
        )
        SELECT
            gen_random_uuid(),
            i.tenant_id,
            i.id,
            'SALE',
            -item_record.quantity,  -- Negative for outbound
            'ORDER',
            order_uuid,
            'Order fulfillment: ' || item_record.order_number
        FROM inventory i
        WHERE (
            (item_record.product_variant_id IS NULL AND i.product_id = item_record.product_id) OR
            (item_record.product_variant_id IS NOT NULL AND i.product_id = item_record.product_variant_id)
        )
        AND i.location_id = item_record.location_id;

        -- Mark item as fulfilled
        UPDATE order_items
        SET inventory_fulfilled = true,
            quantity_fulfilled = quantity,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = item_record.item_id;

        -- Release reservation
        DELETE FROM inventory_reservations
        WHERE order_id = order_uuid
          AND order_item_id = item_record.item_id;
    END LOOP;

    -- Update order fulfilled_at
    UPDATE orders
    SET fulfilled_at = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = order_uuid;

    RETURN true;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Update order status with history tracking
-- ============================================================
CREATE OR REPLACE FUNCTION update_order_status(
    order_uuid UUID,
    new_status VARCHAR(30),
    status_comment TEXT DEFAULT NULL,
    notify_customer BOOLEAN DEFAULT false,
    user_id UUID DEFAULT NULL
)
RETURNS VOID AS $$
DECLARE
    current_status VARCHAR(30);
    tenant_uuid UUID;
BEGIN
    -- Get current status and tenant
    SELECT status, tenant_id
    INTO current_status, tenant_uuid
    FROM orders
    WHERE id = order_uuid;

    IF current_status IS NULL THEN
        RAISE EXCEPTION 'Order not found: %', order_uuid;
    END IF;

    -- Don't update if status is the same
    IF current_status = new_status THEN
        RETURN;
    END IF;

    -- Update order status
    UPDATE orders
    SET status = new_status,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = user_id
    WHERE id = order_uuid;

    -- Add status history entry
    INSERT INTO order_status_history (
        tenant_id, order_id, from_status, to_status,
        comment, notify_customer, changed_by, changed_at
    )
    VALUES (
        tenant_uuid, order_uuid, current_status, new_status,
        status_comment, notify_customer, user_id, CURRENT_TIMESTAMP
    );

    -- Update specific timestamps based on status
    IF new_status = 'SHIPPED' THEN
        UPDATE orders SET shipped_at = CURRENT_TIMESTAMP WHERE id = order_uuid;
    ELSIF new_status = 'DELIVERED' THEN
        UPDATE orders SET delivered_at = CURRENT_TIMESTAMP WHERE id = order_uuid;
    ELSIF new_status = 'CANCELLED' THEN
        UPDATE orders SET cancelled_at = CURRENT_TIMESTAMP WHERE id = order_uuid;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- View: Order summary
-- ============================================================
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

-- ============================================================
-- View: Order items detail
-- ============================================================
CREATE OR REPLACE VIEW v_order_items_detail AS
SELECT
    oi.id AS order_item_id,
    oi.order_id,
    o.order_number,
    o.status AS order_status,
    oi.product_id,
    oi.product_name,
    oi.product_sku,
    oi.product_variant_id,
    oi.variant_name,
    oi.variant_sku,
    oi.quantity,
    oi.unit_price,
    oi.discount_amount,
    oi.tax_amount,
    oi.total,
    oi.quantity_fulfilled,
    oi.quantity_cancelled,
    oi.quantity_refunded,
    oi.inventory_reserved,
    oi.inventory_fulfilled,
    CASE
        WHEN oi.quantity_fulfilled = oi.quantity THEN 'FULFILLED'
        WHEN oi.quantity_cancelled = oi.quantity THEN 'CANCELLED'
        WHEN oi.quantity_fulfilled > 0 THEN 'PARTIALLY_FULFILLED'
        WHEN oi.inventory_reserved THEN 'RESERVED'
        ELSE 'PENDING'
    END AS fulfillment_status
FROM order_items oi
INNER JOIN orders o ON oi.order_id = o.id;

-- ============================================================
-- View: Pending orders requiring action
-- ============================================================
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

-- ============================================================
-- Initial data: Create sample order
-- ============================================================

-- Convert João Silva's cart to order
DO $$
DECLARE
    sample_cart_id UUID;
    sample_customer_id UUID;
    sample_tenant_id UUID;
    sample_location_id UUID;
    new_order_id UUID;
    new_order_number VARCHAR(50);
BEGIN
    -- Get the sample cart
    SELECT c.id, c.customer_id, c.tenant_id, c.location_id
    INTO sample_cart_id, sample_customer_id, sample_tenant_id, sample_location_id
    FROM carts c
    INNER JOIN customers cu ON c.customer_id = cu.id
    WHERE cu.email = 'joao.silva@email.com'
      AND c.status = 'ACTIVE'
    LIMIT 1;

    IF sample_cart_id IS NOT NULL THEN
        -- Generate order number
        new_order_number := generate_order_number(sample_tenant_id);
        new_order_id := gen_random_uuid();

        -- Create order from cart
        INSERT INTO orders (
            id, tenant_id, order_number, customer_id, cart_id,
            status, payment_status, subtotal, discount_amount,
            tax_amount, shipping_amount, total, location_id,
            shipping_method, payment_method
        )
        SELECT
            new_order_id,
            c.tenant_id,
            new_order_number,
            c.customer_id,
            c.id,
            'CONFIRMED',
            'CAPTURED',
            c.subtotal,
            c.discount_amount,
            c.tax_amount,
            c.shipping_amount,
            c.total,
            c.location_id,
            'STANDARD_SHIPPING',
            'CREDIT_CARD'
        FROM carts c
        WHERE c.id = sample_cart_id;

        -- Copy cart items to order items
        INSERT INTO order_items (
            tenant_id, order_id, product_id, product_variant_id,
            product_name, product_sku, quantity, unit_price,
            subtotal, discount_amount, total
        )
        SELECT
            ci.tenant_id,
            new_order_id,
            ci.product_id,
            ci.product_variant_id,
            p.name,
            COALESCE(pv.sku, p.sku),
            ci.quantity,
            ci.unit_price,
            ci.subtotal,
            ci.discount_amount,
            ci.total
        FROM cart_items ci
        INNER JOIN products p ON ci.product_id = p.id
        LEFT JOIN product_variants pv ON ci.product_variant_id = pv.id
        WHERE ci.cart_id = sample_cart_id;

        -- Mark cart as converted
        UPDATE carts
        SET status = 'CONVERTED',
            converted_to_order_id = new_order_id,
            converted_at = CURRENT_TIMESTAMP
        WHERE id = sample_cart_id;

        -- Add initial status history
        INSERT INTO order_status_history (
            tenant_id, order_id, from_status, to_status, comment
        )
        VALUES (
            sample_tenant_id,
            new_order_id,
            NULL,
            'CONFIRMED',
            'Order created from cart conversion'
        );

        -- Reserve inventory
        PERFORM reserve_order_inventory(new_order_id);
    END IF;
END $$;
