-- V019__create_stock_transfers_tables.sql
-- Story 5.5: Stock Transfers
-- Creates tables for stock transfers between locations

-- ============================================================
-- Table: stock_transfers
-- Stores stock transfer header information
-- ============================================================
CREATE TABLE IF NOT EXISTS stock_transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Transfer identification
    transfer_number VARCHAR(50) NOT NULL,

    -- Locations
    source_location_id UUID NOT NULL REFERENCES locations(id),
    destination_location_id UUID NOT NULL REFERENCES locations(id),

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    -- Transfer details
    transfer_type VARCHAR(30) NOT NULL DEFAULT 'STANDARD',
    reason VARCHAR(100),
    notes TEXT,

    -- Dates
    requested_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expected_date DATE,
    shipped_date DATE,
    received_date DATE,

    -- Approval
    approved_by UUID,
    approved_at TIMESTAMP,

    -- Shipping details
    shipping_method VARCHAR(50),
    tracking_number VARCHAR(100),
    carrier VARCHAR(100),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT unique_transfer_number_per_tenant UNIQUE (tenant_id, transfer_number),
    CONSTRAINT check_different_locations CHECK (source_location_id != destination_location_id),
    CONSTRAINT check_transfer_status CHECK (status IN (
        'DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'IN_TRANSIT',
        'RECEIVED', 'PARTIALLY_RECEIVED', 'CANCELLED', 'REJECTED'
    )),
    CONSTRAINT check_transfer_type CHECK (transfer_type IN (
        'STANDARD', 'EMERGENCY', 'REBALANCING', 'RETURN'
    ))
);

-- ============================================================
-- Table: stock_transfer_items
-- Stores items being transferred
-- ============================================================
CREATE TABLE IF NOT EXISTS stock_transfer_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Transfer reference
    stock_transfer_id UUID NOT NULL REFERENCES stock_transfers(id) ON DELETE CASCADE,

    -- Product reference
    product_id UUID NOT NULL REFERENCES products(id),
    product_variant_id UUID REFERENCES product_variants(id),

    -- Product info (snapshot)
    product_sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(200) NOT NULL,

    -- Quantities
    quantity_requested NUMERIC(15, 3) NOT NULL,
    quantity_shipped NUMERIC(15, 3) NOT NULL DEFAULT 0,
    quantity_received NUMERIC(15, 3) NOT NULL DEFAULT 0,
    quantity_damaged NUMERIC(15, 3) NOT NULL DEFAULT 0,

    -- Unit of measure
    unit_of_measure VARCHAR(20) NOT NULL DEFAULT 'UN',

    -- Cost tracking
    unit_cost NUMERIC(15, 4),

    -- Batch/lot tracking
    batch_number VARCHAR(100),
    expiry_date DATE,

    -- Notes
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_positive_quantity_requested CHECK (quantity_requested > 0),
    CONSTRAINT check_shipped_quantities CHECK (
        quantity_shipped >= 0 AND
        quantity_shipped <= quantity_requested
    ),
    CONSTRAINT check_received_quantities CHECK (
        quantity_received >= 0 AND
        quantity_damaged >= 0 AND
        (quantity_received + quantity_damaged) <= quantity_shipped
    )
);

-- ============================================================
-- Table: stock_transfer_status_history
-- Tracks status changes over time
-- ============================================================
CREATE TABLE IF NOT EXISTS stock_transfer_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Transfer reference
    stock_transfer_id UUID NOT NULL REFERENCES stock_transfers(id) ON DELETE CASCADE,

    -- Status change
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,

    -- Details
    comment TEXT,

    -- Who & When
    changed_by UUID,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_from_status_transfer CHECK (from_status IN (
        'DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'IN_TRANSIT',
        'RECEIVED', 'PARTIALLY_RECEIVED', 'CANCELLED', 'REJECTED'
    )),
    CONSTRAINT check_to_status_transfer CHECK (to_status IN (
        'DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'IN_TRANSIT',
        'RECEIVED', 'PARTIALLY_RECEIVED', 'CANCELLED', 'REJECTED'
    ))
);

-- ============================================================
-- Indexes
-- ============================================================

-- Stock transfers indexes
CREATE INDEX idx_stock_transfers_tenant ON stock_transfers(tenant_id);
CREATE INDEX idx_stock_transfers_source ON stock_transfers(source_location_id);
CREATE INDEX idx_stock_transfers_destination ON stock_transfers(destination_location_id);
CREATE INDEX idx_stock_transfers_status ON stock_transfers(status);
CREATE INDEX idx_stock_transfers_type ON stock_transfers(transfer_type);
CREATE INDEX idx_stock_transfers_requested ON stock_transfers(requested_date DESC);
CREATE INDEX idx_stock_transfers_number ON stock_transfers(transfer_number);

-- Transfer items indexes
CREATE INDEX idx_transfer_items_tenant ON stock_transfer_items(tenant_id);
CREATE INDEX idx_transfer_items_transfer ON stock_transfer_items(stock_transfer_id);
CREATE INDEX idx_transfer_items_product ON stock_transfer_items(product_id);
CREATE INDEX idx_transfer_items_variant ON stock_transfer_items(product_variant_id) WHERE product_variant_id IS NOT NULL;

-- Transfer status history indexes
CREATE INDEX idx_transfer_status_history_tenant ON stock_transfer_status_history(tenant_id);
CREATE INDEX idx_transfer_status_history_transfer ON stock_transfer_status_history(stock_transfer_id);
CREATE INDEX idx_transfer_status_history_changed ON stock_transfer_status_history(changed_at DESC);

-- ============================================================
-- Triggers for updated_at
-- ============================================================
CREATE TRIGGER trigger_stock_transfers_updated_at
    BEFORE UPDATE ON stock_transfers
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

CREATE TRIGGER trigger_transfer_items_updated_at
    BEFORE UPDATE ON stock_transfer_items
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Function: Generate unique transfer number
-- ============================================================
CREATE OR REPLACE FUNCTION generate_transfer_number(tenant_uuid UUID)
RETURNS VARCHAR AS $$
DECLARE
    current_year VARCHAR(4);
    transfer_count INTEGER;
    transfer_num VARCHAR(50);
BEGIN
    current_year := EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::VARCHAR;

    -- Count transfers in current year for this tenant
    SELECT COUNT(*) + 1
    INTO transfer_count
    FROM stock_transfers
    WHERE tenant_id = tenant_uuid
      AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_TIMESTAMP);

    -- Format: TRF-2025-00001
    transfer_num := 'TRF-' || current_year || '-' || LPAD(transfer_count::TEXT, 5, '0');

    RETURN transfer_num;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Update transfer status with history tracking
-- ============================================================
CREATE OR REPLACE FUNCTION update_transfer_status(
    transfer_uuid UUID,
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
    FROM stock_transfers
    WHERE id = transfer_uuid;

    IF current_status IS NULL THEN
        RAISE EXCEPTION 'Transfer not found: %', transfer_uuid;
    END IF;

    -- Don't update if status is the same
    IF current_status = new_status THEN
        RETURN;
    END IF;

    -- Update transfer status
    UPDATE stock_transfers
    SET status = new_status,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = user_id
    WHERE id = transfer_uuid;

    -- Add status history entry
    INSERT INTO stock_transfer_status_history (
        tenant_id, stock_transfer_id, from_status, to_status,
        comment, changed_by, changed_at
    )
    VALUES (
        tenant_uuid, transfer_uuid, current_status, new_status,
        status_comment, user_id, CURRENT_TIMESTAMP
    );

    -- Update specific timestamps based on status
    IF new_status = 'APPROVED' THEN
        UPDATE stock_transfers
        SET approved_at = CURRENT_TIMESTAMP,
            approved_by = user_id
        WHERE id = transfer_uuid;
    ELSIF new_status = 'IN_TRANSIT' THEN
        UPDATE stock_transfers
        SET shipped_date = CURRENT_DATE
        WHERE id = transfer_uuid;
    ELSIF new_status IN ('RECEIVED', 'PARTIALLY_RECEIVED') THEN
        UPDATE stock_transfers
        SET received_date = CURRENT_DATE
        WHERE id = transfer_uuid;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Ship transfer (create outbound movements)
-- ============================================================
CREATE OR REPLACE FUNCTION ship_transfer(transfer_uuid UUID)
RETURNS VOID AS $$
DECLARE
    transfer_record RECORD;
    item_record RECORD;
BEGIN
    -- Get transfer details
    SELECT st.tenant_id, st.source_location_id, st.transfer_number
    INTO transfer_record
    FROM stock_transfers st
    WHERE st.id = transfer_uuid;

    IF transfer_record.tenant_id IS NULL THEN
        RAISE EXCEPTION 'Transfer not found: %', transfer_uuid;
    END IF;

    -- Process each item
    FOR item_record IN
        SELECT
            sti.product_id,
            sti.product_variant_id,
            sti.quantity_requested,
            sti.unit_cost
        FROM stock_transfer_items sti
        WHERE sti.stock_transfer_id = transfer_uuid
    LOOP
        -- Create outbound movement from source
        INSERT INTO inventory_movements (
            id, tenant_id, inventory_id, movement_type, quantity,
            unit_cost, reference_type, reference_id, notes
        )
        SELECT
            gen_random_uuid(),
            transfer_record.tenant_id,
            i.id,
            'TRANSFER_OUT',
            -item_record.quantity_requested,
            item_record.unit_cost,
            'TRANSFER',
            transfer_uuid,
            'Transfer out: ' || transfer_record.transfer_number
        FROM inventory i
        WHERE (
            (item_record.product_variant_id IS NULL AND i.product_id = item_record.product_id) OR
            (item_record.product_variant_id IS NOT NULL AND i.product_id = item_record.product_variant_id)
        )
        AND i.location_id = transfer_record.source_location_id;

        -- Update shipped quantity
        UPDATE stock_transfer_items
        SET quantity_shipped = quantity_requested,
            updated_at = CURRENT_TIMESTAMP
        WHERE stock_transfer_id = transfer_uuid
          AND product_id = item_record.product_id
          AND (product_variant_id = item_record.product_variant_id OR
               (product_variant_id IS NULL AND item_record.product_variant_id IS NULL));
    END LOOP;

    -- Update transfer status
    PERFORM update_transfer_status(transfer_uuid, 'IN_TRANSIT', 'Transfer shipped');
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Receive transfer (create inbound movements)
-- ============================================================
CREATE OR REPLACE FUNCTION receive_transfer(transfer_uuid UUID)
RETURNS VOID AS $$
DECLARE
    transfer_record RECORD;
    item_record RECORD;
    total_requested NUMERIC(15, 3);
    total_received NUMERIC(15, 3);
BEGIN
    -- Get transfer details
    SELECT st.tenant_id, st.destination_location_id, st.transfer_number
    INTO transfer_record
    FROM stock_transfers st
    WHERE st.id = transfer_uuid;

    IF transfer_record.tenant_id IS NULL THEN
        RAISE EXCEPTION 'Transfer not found: %', transfer_uuid;
    END IF;

    -- Calculate totals
    SELECT
        SUM(quantity_requested),
        SUM(quantity_received)
    INTO total_requested, total_received
    FROM stock_transfer_items
    WHERE stock_transfer_id = transfer_uuid;

    -- Process each received item
    FOR item_record IN
        SELECT
            sti.product_id,
            sti.product_variant_id,
            sti.quantity_received,
            sti.unit_cost
        FROM stock_transfer_items sti
        WHERE sti.stock_transfer_id = transfer_uuid
          AND sti.quantity_received > 0
    LOOP
        -- Create inbound movement to destination
        INSERT INTO inventory_movements (
            id, tenant_id, inventory_id, movement_type, quantity,
            unit_cost, reference_type, reference_id, notes
        )
        SELECT
            gen_random_uuid(),
            transfer_record.tenant_id,
            i.id,
            'TRANSFER_IN',
            item_record.quantity_received,
            item_record.unit_cost,
            'TRANSFER',
            transfer_uuid,
            'Transfer in: ' || transfer_record.transfer_number
        FROM inventory i
        WHERE (
            (item_record.product_variant_id IS NULL AND i.product_id = item_record.product_id) OR
            (item_record.product_variant_id IS NOT NULL AND i.product_id = item_record.product_variant_id)
        )
        AND i.location_id = transfer_record.destination_location_id;
    END LOOP;

    -- Update transfer status based on quantities
    IF total_received >= total_requested THEN
        PERFORM update_transfer_status(transfer_uuid, 'RECEIVED', 'Transfer fully received');
    ELSIF total_received > 0 THEN
        PERFORM update_transfer_status(transfer_uuid, 'PARTIALLY_RECEIVED', 'Transfer partially received');
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- View: Transfer summary
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
    COUNT(sti.id) AS item_count,
    SUM(sti.quantity_requested) AS total_quantity_requested,
    SUM(sti.quantity_shipped) AS total_quantity_shipped,
    SUM(sti.quantity_received) AS total_quantity_received,
    st.requested_date,
    st.expected_date,
    st.shipped_date,
    st.received_date,
    st.reason,
    st.created_at
FROM stock_transfers st
INNER JOIN locations sl ON st.source_location_id = sl.id
INNER JOIN locations dl ON st.destination_location_id = dl.id
LEFT JOIN stock_transfer_items sti ON st.id = sti.stock_transfer_id
GROUP BY
    st.id, st.transfer_number, st.transfer_type,
    sl.code, sl.name, dl.code, dl.name,
    st.status, st.requested_date, st.expected_date,
    st.shipped_date, st.received_date, st.reason, st.created_at;

-- ============================================================
-- View: Transfer items detail
-- ============================================================
CREATE OR REPLACE VIEW v_stock_transfer_items AS
SELECT
    sti.id AS transfer_item_id,
    st.transfer_number,
    st.status AS transfer_status,
    sl.name AS source_location,
    dl.name AS destination_location,
    sti.product_sku,
    sti.product_name,
    sti.quantity_requested,
    sti.quantity_shipped,
    sti.quantity_received,
    sti.quantity_damaged,
    (sti.quantity_requested - sti.quantity_received - sti.quantity_damaged) AS quantity_pending,
    CASE
        WHEN sti.quantity_received = sti.quantity_requested THEN 'FULLY_RECEIVED'
        WHEN sti.quantity_received > 0 THEN 'PARTIALLY_RECEIVED'
        WHEN sti.quantity_shipped = sti.quantity_requested THEN 'IN_TRANSIT'
        WHEN sti.quantity_shipped > 0 THEN 'PARTIALLY_SHIPPED'
        ELSE 'PENDING'
    END AS item_status
FROM stock_transfer_items sti
INNER JOIN stock_transfers st ON sti.stock_transfer_id = st.id
INNER JOIN locations sl ON st.source_location_id = sl.id
INNER JOIN locations dl ON st.destination_location_id = dl.id;

-- ============================================================
-- View: Pending transfers
-- ============================================================
CREATE OR REPLACE VIEW v_pending_transfers AS
SELECT
    st.id AS transfer_id,
    st.transfer_number,
    st.transfer_type,
    sl.name AS source_location,
    dl.name AS destination_location,
    st.status,
    COUNT(sti.id) AS item_count,
    st.requested_date,
    st.expected_date,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - st.created_at)) / 86400 AS days_since_created,
    CASE
        WHEN st.status = 'DRAFT' THEN 'NEEDS_COMPLETION'
        WHEN st.status = 'PENDING_APPROVAL' THEN 'NEEDS_APPROVAL'
        WHEN st.status = 'APPROVED' THEN 'READY_TO_SHIP'
        WHEN st.status = 'IN_TRANSIT' THEN 'AWAITING_RECEIPT'
        WHEN st.status = 'PARTIALLY_RECEIVED' THEN 'AWAITING_REMAINING_ITEMS'
        ELSE 'OTHER'
    END AS action_required
FROM stock_transfers st
INNER JOIN locations sl ON st.source_location_id = sl.id
INNER JOIN locations dl ON st.destination_location_id = dl.id
LEFT JOIN stock_transfer_items sti ON st.id = sti.stock_transfer_id
WHERE st.status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'IN_TRANSIT', 'PARTIALLY_RECEIVED')
GROUP BY
    st.id, st.transfer_number, st.transfer_type,
    sl.name, dl.name, st.status, st.requested_date,
    st.expected_date, st.created_at
ORDER BY st.expected_date ASC NULLS LAST, st.created_at ASC;

-- ============================================================
-- Initial data: Create sample transfer
-- ============================================================
-- TODO: Seed data commented out - references non-existent 'tenants' table and SKUs
-- Sample transfers should be created via application logic after tenant provisioning
--
-- DO $$
-- DECLARE
--     sample_tenant_id UUID;
--     sample_source_location UUID;
--     sample_dest_location UUID;
--     sample_product_id UUID;
--     new_transfer_id UUID;
--     new_transfer_number VARCHAR(50);
-- BEGIN
--     -- Get sample data
--     SELECT id INTO sample_tenant_id FROM tenants LIMIT 1;
--
--     SELECT id INTO sample_source_location
--     FROM locations
--     WHERE code = 'MAIN'
--     LIMIT 1;
--
--     SELECT id INTO sample_dest_location
--     FROM locations
--     WHERE code != 'MAIN'
--     LIMIT 1;
--
--     SELECT id INTO sample_product_id
--     FROM products
--     WHERE sku = 'NOTE-DELL-I15-001'
--     LIMIT 1;
--
--     IF sample_tenant_id IS NOT NULL AND
--        sample_source_location IS NOT NULL AND
--        sample_dest_location IS NOT NULL AND
--        sample_product_id IS NOT NULL THEN
--
--         -- Generate transfer number
--         new_transfer_number := generate_transfer_number(sample_tenant_id);
--         new_transfer_id := gen_random_uuid();
--
--         -- Create transfer
--         INSERT INTO stock_transfers (
--             id, tenant_id, transfer_number,
--             source_location_id, destination_location_id,
--             status, transfer_type, reason,
--             requested_date, expected_date
--         )
--         VALUES (
--             new_transfer_id,
--             sample_tenant_id,
--             new_transfer_number,
--             sample_source_location,
--             sample_dest_location,
--             'APPROVED',
--             'REBALANCING',
--             'Stock rebalancing between locations',
--             CURRENT_DATE,
--             CURRENT_DATE + INTERVAL '2 days'
--         );
--
--         -- Create transfer item
--         INSERT INTO stock_transfer_items (
--             tenant_id, stock_transfer_id, product_id,
--             product_sku, product_name,
--             quantity_requested, unit_of_measure
--         )
--         SELECT
--             sample_tenant_id,
--             new_transfer_id,
--             p.id,
--             p.sku,
--             p.name,
--             3,
--             'UN'
--         FROM products p
--         WHERE p.id = sample_product_id;
--
--         -- Add status history
--         INSERT INTO stock_transfer_status_history (
--             tenant_id, stock_transfer_id, from_status, to_status, comment
--         )
--         VALUES (
--             sample_tenant_id,
--             new_transfer_id,
--             NULL,
--             'APPROVED',
--             'Sample transfer created and approved'
--         );
--     END IF;
-- END $$;
