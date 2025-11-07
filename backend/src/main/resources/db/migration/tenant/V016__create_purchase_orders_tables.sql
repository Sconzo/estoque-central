-- V016__create_purchase_orders_tables.sql
-- Story 5.2: Purchase Orders
-- Creates tables for purchase order management

-- ============================================================
-- Table: purchase_orders
-- Stores purchase order header information
-- ============================================================
CREATE TABLE IF NOT EXISTS purchase_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Order identification
    po_number VARCHAR(50) NOT NULL,

    -- Supplier reference
    supplier_id UUID NOT NULL REFERENCES suppliers(id),

    -- Location where goods will be received
    location_id UUID REFERENCES locations(id),

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    -- Totals
    subtotal NUMERIC(15, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    shipping_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    other_costs NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total NUMERIC(15, 2) NOT NULL DEFAULT 0,

    -- Payment details
    payment_method VARCHAR(50),
    payment_terms VARCHAR(100),
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    -- Important dates
    order_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expected_delivery_date DATE,
    approved_at TIMESTAMP,
    sent_to_supplier_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    -- Approval
    approved_by UUID,
    approval_notes TEXT,

    -- Notes
    notes TEXT,
    internal_notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT unique_po_number_per_tenant UNIQUE (tenant_id, po_number),
    CONSTRAINT check_po_status CHECK (status IN (
        'DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'SENT_TO_SUPPLIER',
        'PARTIALLY_RECEIVED', 'RECEIVED', 'CANCELLED', 'CLOSED'
    )),
    CONSTRAINT check_payment_status CHECK (payment_status IN (
        'PENDING', 'PARTIAL', 'PAID'
    )),
    CONSTRAINT check_positive_totals CHECK (
        subtotal >= 0 AND discount_amount >= 0 AND
        tax_amount >= 0 AND shipping_amount >= 0 AND
        other_costs >= 0 AND total >= 0
    )
);

-- ============================================================
-- Table: purchase_order_items
-- Stores line items for each purchase order
-- ============================================================
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- PO reference
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,

    -- Product reference
    product_id UUID NOT NULL REFERENCES products(id),
    product_variant_id UUID REFERENCES product_variants(id),

    -- Product info (snapshot)
    product_sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    supplier_sku VARCHAR(100),

    -- Quantity
    quantity_ordered NUMERIC(15, 3) NOT NULL,
    quantity_received NUMERIC(15, 3) NOT NULL DEFAULT 0,
    quantity_cancelled NUMERIC(15, 3) NOT NULL DEFAULT 0,

    -- Unit of measure
    unit_of_measure VARCHAR(20) NOT NULL DEFAULT 'UN',

    -- Pricing
    unit_cost NUMERIC(15, 2) NOT NULL,
    subtotal NUMERIC(15, 2) NOT NULL,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total NUMERIC(15, 2) NOT NULL,

    -- Notes
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_positive_quantity_ordered CHECK (quantity_ordered > 0),
    CONSTRAINT check_positive_unit_cost CHECK (unit_cost >= 0),
    CONSTRAINT check_received_quantities CHECK (
        quantity_received >= 0 AND
        quantity_cancelled >= 0 AND
        (quantity_received + quantity_cancelled) <= quantity_ordered
    )
);

-- ============================================================
-- Table: purchase_order_receipts
-- Tracks receipt/receiving of purchase orders
-- ============================================================
CREATE TABLE IF NOT EXISTS purchase_order_receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- PO reference
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id),

    -- Receipt identification
    receipt_number VARCHAR(50) NOT NULL,

    -- Location where goods were received
    location_id UUID NOT NULL REFERENCES locations(id),

    -- Receipt details
    receipt_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    invoice_number VARCHAR(100),
    invoice_date DATE,
    invoice_value NUMERIC(15, 2),

    -- Quality check
    quality_check_status VARCHAR(30),
    quality_check_notes TEXT,

    -- Notes
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    received_by UUID,

    -- Constraints
    CONSTRAINT unique_receipt_number_per_tenant UNIQUE (tenant_id, receipt_number),
    CONSTRAINT check_quality_status CHECK (quality_check_status IN (
        'PENDING', 'APPROVED', 'REJECTED', 'PARTIAL'
    ))
);

-- ============================================================
-- Table: purchase_order_receipt_items
-- Details of items received in each receipt
-- ============================================================
CREATE TABLE IF NOT EXISTS purchase_order_receipt_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Receipt reference
    purchase_order_receipt_id UUID NOT NULL REFERENCES purchase_order_receipts(id) ON DELETE CASCADE,

    -- PO item reference
    purchase_order_item_id UUID NOT NULL REFERENCES purchase_order_items(id),

    -- Product reference
    product_id UUID NOT NULL REFERENCES products(id),
    product_variant_id UUID REFERENCES product_variants(id),

    -- Quantity received
    quantity_received NUMERIC(15, 3) NOT NULL,
    unit_cost NUMERIC(15, 2) NOT NULL,

    -- Quality check
    quantity_accepted NUMERIC(15, 3) NOT NULL DEFAULT 0,
    quantity_rejected NUMERIC(15, 3) NOT NULL DEFAULT 0,

    -- Batch/lot tracking
    batch_number VARCHAR(100),
    expiry_date DATE,

    -- Notes
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_positive_received CHECK (quantity_received > 0),
    CONSTRAINT check_accepted_rejected CHECK (
        quantity_accepted >= 0 AND
        quantity_rejected >= 0 AND
        (quantity_accepted + quantity_rejected) <= quantity_received
    )
);

-- ============================================================
-- Table: purchase_order_status_history
-- Tracks status changes over time
-- ============================================================
CREATE TABLE IF NOT EXISTS purchase_order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- PO reference
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,

    -- Status change
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,

    -- Details
    comment TEXT,

    -- Who & When
    changed_by UUID,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_from_status_po CHECK (from_status IN (
        'DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'SENT_TO_SUPPLIER',
        'PARTIALLY_RECEIVED', 'RECEIVED', 'CANCELLED', 'CLOSED'
    )),
    CONSTRAINT check_to_status_po CHECK (to_status IN (
        'DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'SENT_TO_SUPPLIER',
        'PARTIALLY_RECEIVED', 'RECEIVED', 'CANCELLED', 'CLOSED'
    ))
);

-- ============================================================
-- Indexes
-- ============================================================

-- Purchase orders indexes
CREATE INDEX idx_purchase_orders_tenant ON purchase_orders(tenant_id);
CREATE INDEX idx_purchase_orders_supplier ON purchase_orders(supplier_id);
CREATE INDEX idx_purchase_orders_location ON purchase_orders(location_id);
CREATE INDEX idx_purchase_orders_status ON purchase_orders(status);
CREATE INDEX idx_purchase_orders_payment_status ON purchase_orders(payment_status);
CREATE INDEX idx_purchase_orders_order_date ON purchase_orders(order_date DESC);
CREATE INDEX idx_purchase_orders_expected_delivery ON purchase_orders(expected_delivery_date);
CREATE INDEX idx_purchase_orders_po_number ON purchase_orders(po_number);

-- PO items indexes
CREATE INDEX idx_po_items_tenant ON purchase_order_items(tenant_id);
CREATE INDEX idx_po_items_po ON purchase_order_items(purchase_order_id);
CREATE INDEX idx_po_items_product ON purchase_order_items(product_id);
CREATE INDEX idx_po_items_variant ON purchase_order_items(product_variant_id) WHERE product_variant_id IS NOT NULL;

-- PO receipts indexes
CREATE INDEX idx_po_receipts_tenant ON purchase_order_receipts(tenant_id);
CREATE INDEX idx_po_receipts_po ON purchase_order_receipts(purchase_order_id);
CREATE INDEX idx_po_receipts_location ON purchase_order_receipts(location_id);
CREATE INDEX idx_po_receipts_date ON purchase_order_receipts(receipt_date DESC);
CREATE INDEX idx_po_receipts_invoice ON purchase_order_receipts(invoice_number) WHERE invoice_number IS NOT NULL;

-- PO receipt items indexes
CREATE INDEX idx_po_receipt_items_tenant ON purchase_order_receipt_items(tenant_id);
CREATE INDEX idx_po_receipt_items_receipt ON purchase_order_receipt_items(purchase_order_receipt_id);
CREATE INDEX idx_po_receipt_items_po_item ON purchase_order_receipt_items(purchase_order_item_id);
CREATE INDEX idx_po_receipt_items_product ON purchase_order_receipt_items(product_id);

-- PO status history indexes
CREATE INDEX idx_po_status_history_tenant ON purchase_order_status_history(tenant_id);
CREATE INDEX idx_po_status_history_po ON purchase_order_status_history(purchase_order_id);
CREATE INDEX idx_po_status_history_changed_at ON purchase_order_status_history(changed_at DESC);

-- ============================================================
-- Triggers for updated_at
-- ============================================================
CREATE TRIGGER trigger_purchase_orders_updated_at
    BEFORE UPDATE ON purchase_orders
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

CREATE TRIGGER trigger_po_items_updated_at
    BEFORE UPDATE ON purchase_order_items
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

CREATE TRIGGER trigger_po_receipts_updated_at
    BEFORE UPDATE ON purchase_order_receipts
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Function: Generate unique PO number
-- ============================================================
CREATE OR REPLACE FUNCTION generate_po_number(tenant_uuid UUID)
RETURNS VARCHAR AS $$
DECLARE
    current_year VARCHAR(4);
    po_count INTEGER;
    po_num VARCHAR(50);
BEGIN
    current_year := EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::VARCHAR;

    -- Count POs in current year for this tenant
    SELECT COUNT(*) + 1
    INTO po_count
    FROM purchase_orders
    WHERE tenant_id = tenant_uuid
      AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_TIMESTAMP);

    -- Format: PO-2025-00001
    po_num := 'PO-' || current_year || '-' || LPAD(po_count::TEXT, 5, '0');

    RETURN po_num;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Generate unique receipt number
-- ============================================================
CREATE OR REPLACE FUNCTION generate_receipt_number(tenant_uuid UUID)
RETURNS VARCHAR AS $$
DECLARE
    current_year VARCHAR(4);
    receipt_count INTEGER;
    receipt_num VARCHAR(50);
BEGIN
    current_year := EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::VARCHAR;

    -- Count receipts in current year for this tenant
    SELECT COUNT(*) + 1
    INTO receipt_count
    FROM purchase_order_receipts
    WHERE tenant_id = tenant_uuid
      AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_TIMESTAMP);

    -- Format: REC-2025-00001
    receipt_num := 'REC-' || current_year || '-' || LPAD(receipt_count::TEXT, 5, '0');

    RETURN receipt_num;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Calculate PO totals
-- ============================================================
CREATE OR REPLACE FUNCTION calculate_po_totals(po_uuid UUID)
RETURNS VOID AS $$
DECLARE
    po_subtotal NUMERIC(15, 2);
    po_total NUMERIC(15, 2);
BEGIN
    -- Calculate subtotal from items
    SELECT COALESCE(SUM(total), 0)
    INTO po_subtotal
    FROM purchase_order_items
    WHERE purchase_order_id = po_uuid;

    -- Calculate total (subtotal - discount + tax + shipping + other)
    SELECT
        po_subtotal - COALESCE(po.discount_amount, 0) +
        COALESCE(po.tax_amount, 0) +
        COALESCE(po.shipping_amount, 0) +
        COALESCE(po.other_costs, 0)
    INTO po_total
    FROM purchase_orders po
    WHERE po.id = po_uuid;

    -- Update PO totals
    UPDATE purchase_orders
    SET subtotal = po_subtotal,
        total = po_total,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = po_uuid;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Update PO status with history tracking
-- ============================================================
CREATE OR REPLACE FUNCTION update_po_status(
    po_uuid UUID,
    new_status VARCHAR(30),
    status_comment TEXT DEFAULT NULL,
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
    FROM purchase_orders
    WHERE id = po_uuid;

    IF current_status IS NULL THEN
        RAISE EXCEPTION 'Purchase order not found: %', po_uuid;
    END IF;

    -- Don't update if status is the same
    IF current_status = new_status THEN
        RETURN;
    END IF;

    -- Update PO status
    UPDATE purchase_orders
    SET status = new_status,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = user_id
    WHERE id = po_uuid;

    -- Add status history entry
    INSERT INTO purchase_order_status_history (
        tenant_id, purchase_order_id, from_status, to_status,
        comment, changed_by, changed_at
    )
    VALUES (
        tenant_uuid, po_uuid, current_status, new_status,
        status_comment, user_id, CURRENT_TIMESTAMP
    );

    -- Update specific timestamps based on status
    IF new_status = 'APPROVED' THEN
        UPDATE purchase_orders
        SET approved_at = CURRENT_TIMESTAMP,
            approved_by = user_id
        WHERE id = po_uuid;
    ELSIF new_status = 'SENT_TO_SUPPLIER' THEN
        UPDATE purchase_orders
        SET sent_to_supplier_at = CURRENT_TIMESTAMP
        WHERE id = po_uuid;
    ELSIF new_status = 'CANCELLED' THEN
        UPDATE purchase_orders
        SET cancelled_at = CURRENT_TIMESTAMP
        WHERE id = po_uuid;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Process PO receipt and update inventory
-- ============================================================
CREATE OR REPLACE FUNCTION process_po_receipt(receipt_uuid UUID)
RETURNS VOID AS $$
DECLARE
    receipt_record RECORD;
    item_record RECORD;
    po_uuid UUID;
BEGIN
    -- Get receipt details
    SELECT por.purchase_order_id, por.location_id, por.tenant_id
    INTO receipt_record
    FROM purchase_order_receipts por
    WHERE por.id = receipt_uuid;

    po_uuid := receipt_record.purchase_order_id;

    -- Process each receipt item
    FOR item_record IN
        SELECT
            pori.purchase_order_item_id,
            pori.product_id,
            pori.product_variant_id,
            pori.quantity_accepted,
            pori.unit_cost,
            pori.batch_number,
            pori.expiry_date
        FROM purchase_order_receipt_items pori
        WHERE pori.purchase_order_receipt_id = receipt_uuid
          AND pori.quantity_accepted > 0
    LOOP
        -- Update quantity_received in PO item
        UPDATE purchase_order_items
        SET quantity_received = quantity_received + item_record.quantity_accepted,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = item_record.purchase_order_item_id;

        -- Create inventory movement (IN)
        INSERT INTO inventory_movements (
            id, tenant_id, inventory_id, movement_type, quantity,
            unit_cost, reference_type, reference_id, notes
        )
        SELECT
            gen_random_uuid(),
            receipt_record.tenant_id,
            i.id,
            'PURCHASE',
            item_record.quantity_accepted,
            item_record.unit_cost,
            'PURCHASE_ORDER',
            po_uuid,
            'Receipt from PO'
        FROM inventory i
        WHERE (
            (item_record.product_variant_id IS NULL AND i.product_id = item_record.product_id) OR
            (item_record.product_variant_id IS NOT NULL AND i.product_id = item_record.product_variant_id)
        )
        AND i.location_id = receipt_record.location_id;
    END LOOP;

    -- Update PO status based on received quantities
    UPDATE purchase_orders po
    SET status = CASE
        WHEN (
            SELECT SUM(poi.quantity_received)
            FROM purchase_order_items poi
            WHERE poi.purchase_order_id = po.id
        ) >= (
            SELECT SUM(poi.quantity_ordered)
            FROM purchase_order_items poi
            WHERE poi.purchase_order_id = po.id
        ) THEN 'RECEIVED'
        WHEN (
            SELECT SUM(poi.quantity_received)
            FROM purchase_order_items poi
            WHERE poi.purchase_order_id = po.id
        ) > 0 THEN 'PARTIALLY_RECEIVED'
        ELSE po.status
    END,
    updated_at = CURRENT_TIMESTAMP
    WHERE po.id = po_uuid;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- View: PO summary
-- ============================================================
CREATE OR REPLACE VIEW v_purchase_order_summary AS
SELECT
    po.id AS po_id,
    po.po_number,
    s.supplier_code,
    s.company_name AS supplier_name,
    po.status,
    po.payment_status,
    COUNT(poi.id) AS item_count,
    SUM(poi.quantity_ordered) AS total_quantity_ordered,
    SUM(poi.quantity_received) AS total_quantity_received,
    po.subtotal,
    po.discount_amount,
    po.total,
    l.name AS delivery_location,
    po.order_date,
    po.expected_delivery_date,
    po.approved_at,
    po.sent_to_supplier_at,
    po.created_at
FROM purchase_orders po
INNER JOIN suppliers s ON po.supplier_id = s.id
LEFT JOIN purchase_order_items poi ON po.id = poi.purchase_order_id
LEFT JOIN locations l ON po.location_id = l.id
GROUP BY
    po.id, po.po_number, s.supplier_code, s.company_name,
    po.status, po.payment_status, po.subtotal, po.discount_amount,
    po.total, l.name, po.order_date, po.expected_delivery_date,
    po.approved_at, po.sent_to_supplier_at, po.created_at;

-- ============================================================
-- View: PO items detail
-- ============================================================
CREATE OR REPLACE VIEW v_po_items_detail AS
SELECT
    poi.id AS po_item_id,
    po.po_number,
    po.status AS po_status,
    s.company_name AS supplier_name,
    poi.product_sku,
    poi.product_name,
    poi.supplier_sku,
    poi.quantity_ordered,
    poi.quantity_received,
    poi.quantity_cancelled,
    (poi.quantity_ordered - poi.quantity_received - poi.quantity_cancelled) AS quantity_pending,
    poi.unit_cost,
    poi.total,
    CASE
        WHEN poi.quantity_received = poi.quantity_ordered THEN 'FULLY_RECEIVED'
        WHEN poi.quantity_received > 0 THEN 'PARTIALLY_RECEIVED'
        WHEN poi.quantity_cancelled = poi.quantity_ordered THEN 'CANCELLED'
        ELSE 'PENDING'
    END AS receive_status
FROM purchase_order_items poi
INNER JOIN purchase_orders po ON poi.purchase_order_id = po.id
INNER JOIN suppliers s ON po.supplier_id = s.id;

-- ============================================================
-- View: Pending POs requiring action
-- ============================================================
CREATE OR REPLACE VIEW v_pending_purchase_orders AS
SELECT
    po.id AS po_id,
    po.po_number,
    s.company_name AS supplier_name,
    po.status,
    po.total,
    po.order_date,
    po.expected_delivery_date,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - po.created_at)) / 86400 AS days_since_created,
    CASE
        WHEN po.expected_delivery_date IS NOT NULL AND po.expected_delivery_date < CURRENT_DATE
            THEN true
        ELSE false
    END AS is_overdue,
    CASE
        WHEN po.status = 'DRAFT' THEN 'NEEDS_COMPLETION'
        WHEN po.status = 'PENDING_APPROVAL' THEN 'NEEDS_APPROVAL'
        WHEN po.status = 'APPROVED' THEN 'READY_TO_SEND'
        WHEN po.status = 'SENT_TO_SUPPLIER' THEN 'AWAITING_DELIVERY'
        WHEN po.status = 'PARTIALLY_RECEIVED' THEN 'AWAITING_REMAINING_ITEMS'
        ELSE 'OTHER'
    END AS action_required
FROM purchase_orders po
INNER JOIN suppliers s ON po.supplier_id = s.id
WHERE po.status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'SENT_TO_SUPPLIER', 'PARTIALLY_RECEIVED')
ORDER BY po.expected_delivery_date ASC NULLS LAST, po.created_at ASC;

-- ============================================================
-- Initial data: Create sample PO
-- ============================================================

DO $$
DECLARE
    sample_supplier_id UUID;
    sample_tenant_id UUID;
    sample_location_id UUID;
    sample_product_id UUID;
    new_po_id UUID;
    new_po_number VARCHAR(50);
    new_po_item_id UUID;
BEGIN
    -- Get sample data
    SELECT s.id, s.tenant_id
    INTO sample_supplier_id, sample_tenant_id
    FROM suppliers s
    WHERE s.supplier_code = 'SUP-001'
    LIMIT 1;

    SELECT id INTO sample_location_id
    FROM locations
    WHERE code = 'MAIN'
    LIMIT 1;

    SELECT id INTO sample_product_id
    FROM products
    WHERE sku = 'NOTE-DELL-I15-001'
    LIMIT 1;

    IF sample_supplier_id IS NOT NULL AND sample_product_id IS NOT NULL THEN
        -- Generate PO number
        new_po_number := generate_po_number(sample_tenant_id);
        new_po_id := gen_random_uuid();

        -- Create PO
        INSERT INTO purchase_orders (
            id, tenant_id, po_number, supplier_id, location_id,
            status, order_date, expected_delivery_date,
            payment_method, payment_terms
        )
        VALUES (
            new_po_id,
            sample_tenant_id,
            new_po_number,
            sample_supplier_id,
            sample_location_id,
            'APPROVED',
            CURRENT_DATE,
            CURRENT_DATE + INTERVAL '7 days',
            'BANK_TRANSFER',
            '30 dias'
        );

        -- Create PO item
        new_po_item_id := gen_random_uuid();

        INSERT INTO purchase_order_items (
            id, tenant_id, purchase_order_id, product_id,
            product_sku, product_name, supplier_sku,
            quantity_ordered, unit_of_measure, unit_cost,
            subtotal, total
        )
        SELECT
            new_po_item_id,
            sample_tenant_id,
            new_po_id,
            p.id,
            p.sku,
            p.name,
            'TECH-DELL-NOTE-001',
            5,
            'UN',
            3800.00,
            19000.00,
            19000.00
        FROM products p
        WHERE p.id = sample_product_id;

        -- Calculate totals
        PERFORM calculate_po_totals(new_po_id);

        -- Add status history
        INSERT INTO purchase_order_status_history (
            tenant_id, purchase_order_id, from_status, to_status, comment
        )
        VALUES (
            sample_tenant_id,
            new_po_id,
            NULL,
            'APPROVED',
            'Sample PO created and approved'
        );
    END IF;
END $$;
