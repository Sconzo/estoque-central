-- V017__create_mobile_receiving_tables.sql
-- Story 5.3: Mobile Receiving with Scanner
-- Creates tables for mobile receiving workflow and barcode scanning

-- ============================================================
-- Table: mobile_receiving_sessions
-- Stores mobile receiving sessions
-- ============================================================
CREATE TABLE IF NOT EXISTS mobile_receiving_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Session identification
    session_number VARCHAR(50) NOT NULL,

    -- PO reference
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id),

    -- User and device
    user_id UUID NOT NULL,
    device_id VARCHAR(100),
    device_name VARCHAR(200),

    -- Location
    location_id UUID NOT NULL REFERENCES locations(id),

    -- Session status
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',

    -- Totals
    total_items_expected INTEGER NOT NULL DEFAULT 0,
    total_items_scanned INTEGER NOT NULL DEFAULT 0,
    total_quantity_expected NUMERIC(15, 3) NOT NULL DEFAULT 0,
    total_quantity_scanned NUMERIC(15, 3) NOT NULL DEFAULT 0,

    -- Session timing
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    duration_seconds INTEGER,

    -- Notes
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT unique_session_number_per_tenant UNIQUE (tenant_id, session_number),
    CONSTRAINT check_session_status CHECK (status IN (
        'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'CANCELLED'
    ))
);

-- ============================================================
-- Table: mobile_receiving_scans
-- Stores individual barcode scans
-- ============================================================
CREATE TABLE IF NOT EXISTS mobile_receiving_scans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Session reference
    mobile_receiving_session_id UUID NOT NULL REFERENCES mobile_receiving_sessions(id) ON DELETE CASCADE,

    -- PO item reference
    purchase_order_item_id UUID REFERENCES purchase_order_items(id),

    -- Product reference
    product_id UUID REFERENCES products(id),
    product_variant_id UUID REFERENCES product_variants(id),

    -- Scan details
    barcode VARCHAR(200) NOT NULL,
    barcode_type VARCHAR(50),
    scan_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Product info (snapshot for unmatched scans)
    product_sku VARCHAR(100),
    product_name VARCHAR(200),

    -- Quantity
    quantity_scanned NUMERIC(15, 3) NOT NULL DEFAULT 1,

    -- Match status
    match_status VARCHAR(30) NOT NULL DEFAULT 'MATCHED',
    match_confidence INTEGER,

    -- Batch/lot tracking
    batch_number VARCHAR(100),
    expiry_date DATE,

    -- Quality check
    quality_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    quality_notes TEXT,

    -- Location
    location_id UUID REFERENCES locations(id),

    -- Photo evidence
    photo_url TEXT,

    -- Notes
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_match_status CHECK (match_status IN (
        'MATCHED', 'UNMATCHED', 'MULTIPLE_MATCHES', 'MANUAL_MATCH'
    )),
    CONSTRAINT check_quality_status CHECK (quality_status IN (
        'PENDING', 'APPROVED', 'REJECTED', 'HOLD'
    ))
);

-- ============================================================
-- Table: barcode_mappings
-- Stores barcode to product mappings
-- ============================================================
CREATE TABLE IF NOT EXISTS barcode_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Product reference
    product_id UUID NOT NULL REFERENCES products(id),
    product_variant_id UUID REFERENCES product_variants(id),

    -- Barcode
    barcode VARCHAR(200) NOT NULL,
    barcode_type VARCHAR(50) NOT NULL,

    -- Is primary barcode
    is_primary BOOLEAN NOT NULL DEFAULT false,

    -- Status
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,

    -- Constraints
    CONSTRAINT unique_barcode_mapping_per_tenant UNIQUE (tenant_id, barcode),
    CONSTRAINT check_barcode_type CHECK (barcode_type IN (
        'EAN13', 'EAN8', 'UPC', 'CODE128', 'CODE39', 'QR', 'DATAMATRIX', 'CUSTOM'
    ))
);

-- ============================================================
-- Indexes
-- ============================================================

-- Mobile receiving sessions indexes
CREATE INDEX idx_mobile_sessions_tenant ON mobile_receiving_sessions(tenant_id);
CREATE INDEX idx_mobile_sessions_po ON mobile_receiving_sessions(purchase_order_id);
CREATE INDEX idx_mobile_sessions_user ON mobile_receiving_sessions(user_id);
CREATE INDEX idx_mobile_sessions_location ON mobile_receiving_sessions(location_id);
CREATE INDEX idx_mobile_sessions_status ON mobile_receiving_sessions(status);
CREATE INDEX idx_mobile_sessions_started ON mobile_receiving_sessions(started_at DESC);

-- Mobile receiving scans indexes
CREATE INDEX idx_mobile_scans_tenant ON mobile_receiving_scans(tenant_id);
CREATE INDEX idx_mobile_scans_session ON mobile_receiving_scans(mobile_receiving_session_id);
CREATE INDEX idx_mobile_scans_po_item ON mobile_receiving_scans(purchase_order_item_id);
CREATE INDEX idx_mobile_scans_product ON mobile_receiving_scans(product_id);
CREATE INDEX idx_mobile_scans_barcode ON mobile_receiving_scans(barcode);
CREATE INDEX idx_mobile_scans_match_status ON mobile_receiving_scans(match_status);
CREATE INDEX idx_mobile_scans_quality ON mobile_receiving_scans(quality_status);
CREATE INDEX idx_mobile_scans_timestamp ON mobile_receiving_scans(scan_timestamp DESC);

-- Barcode mappings indexes
CREATE INDEX idx_barcode_mappings_tenant ON barcode_mappings(tenant_id);
CREATE INDEX idx_barcode_mappings_product ON barcode_mappings(product_id);
CREATE INDEX idx_barcode_mappings_variant ON barcode_mappings(product_variant_id) WHERE product_variant_id IS NOT NULL;
CREATE INDEX idx_barcode_mappings_barcode ON barcode_mappings(barcode);
CREATE INDEX idx_barcode_mappings_primary ON barcode_mappings(is_primary) WHERE is_primary = true;
CREATE INDEX idx_barcode_mappings_active ON barcode_mappings(is_active) WHERE is_active = true;

-- ============================================================
-- Triggers for updated_at
-- ============================================================
CREATE TRIGGER trigger_mobile_sessions_updated_at
    BEFORE UPDATE ON mobile_receiving_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

CREATE TRIGGER trigger_barcode_mappings_updated_at
    BEFORE UPDATE ON barcode_mappings
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Function: Generate unique session number
-- ============================================================
CREATE OR REPLACE FUNCTION generate_session_number(tenant_uuid UUID)
RETURNS VARCHAR AS $$
DECLARE
    date_str VARCHAR(8);
    session_count INTEGER;
    session_num VARCHAR(50);
BEGIN
    date_str := TO_CHAR(CURRENT_DATE, 'YYYYMMDD');

    -- Count sessions today for this tenant
    SELECT COUNT(*) + 1
    INTO session_count
    FROM mobile_receiving_sessions
    WHERE tenant_id = tenant_uuid
      AND DATE(started_at) = CURRENT_DATE;

    -- Format: MR-20251106-0001
    session_num := 'MR-' || date_str || '-' || LPAD(session_count::TEXT, 4, '0');

    RETURN session_num;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Match barcode to product
-- ============================================================
CREATE OR REPLACE FUNCTION match_barcode_to_product(
    barcode_value VARCHAR(200),
    tenant_uuid UUID
)
RETURNS TABLE(
    product_id UUID,
    product_variant_id UUID,
    product_sku VARCHAR,
    product_name VARCHAR,
    match_confidence INTEGER
) AS $$
BEGIN
    -- Try exact barcode match first
    RETURN QUERY
    SELECT
        bm.product_id,
        bm.product_variant_id,
        COALESCE(pv.sku, p.sku) AS product_sku,
        COALESCE(pv.name, p.name) AS product_name,
        100 AS match_confidence
    FROM barcode_mappings bm
    INNER JOIN products p ON bm.product_id = p.id
    LEFT JOIN product_variants pv ON bm.product_variant_id = pv.id
    WHERE bm.barcode = barcode_value
      AND bm.tenant_id = tenant_uuid
      AND bm.is_active = true
    LIMIT 1;

    -- If no exact match, try SKU match
    IF NOT FOUND THEN
        RETURN QUERY
        SELECT
            p.id AS product_id,
            NULL::UUID AS product_variant_id,
            p.sku AS product_sku,
            p.name AS product_name,
            80 AS match_confidence
        FROM products p
        WHERE p.sku = barcode_value
          AND p.tenant_id = tenant_uuid
          AND p.ativo = true
        LIMIT 1;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Process mobile scan
-- ============================================================
CREATE OR REPLACE FUNCTION process_mobile_scan(
    session_uuid UUID,
    barcode_value VARCHAR(200),
    quantity_value NUMERIC(15, 3) DEFAULT 1,
    batch_value VARCHAR(100) DEFAULT NULL,
    expiry_value DATE DEFAULT NULL
)
RETURNS UUID AS $$
DECLARE
    scan_uuid UUID;
    tenant_uuid UUID;
    po_uuid UUID;
    matched_product RECORD;
    matched_po_item UUID;
    scan_status VARCHAR(30);
BEGIN
    scan_uuid := gen_random_uuid();

    -- Get session details
    SELECT s.tenant_id, s.purchase_order_id
    INTO tenant_uuid, po_uuid
    FROM mobile_receiving_sessions s
    WHERE s.id = session_uuid;

    IF tenant_uuid IS NULL THEN
        RAISE EXCEPTION 'Session not found: %', session_uuid;
    END IF;

    -- Try to match barcode to product
    SELECT * INTO matched_product
    FROM match_barcode_to_product(barcode_value, tenant_uuid)
    LIMIT 1;

    IF matched_product.product_id IS NOT NULL THEN
        scan_status := 'MATCHED';

        -- Try to find matching PO item
        SELECT poi.id INTO matched_po_item
        FROM purchase_order_items poi
        WHERE poi.purchase_order_id = po_uuid
          AND poi.product_id = matched_product.product_id
          AND (
              matched_product.product_variant_id IS NULL OR
              poi.product_variant_id = matched_product.product_variant_id
          )
        LIMIT 1;
    ELSE
        scan_status := 'UNMATCHED';
    END IF;

    -- Insert scan record
    INSERT INTO mobile_receiving_scans (
        id, tenant_id, mobile_receiving_session_id,
        purchase_order_item_id, product_id, product_variant_id,
        barcode, scan_timestamp, quantity_scanned,
        match_status, match_confidence,
        batch_number, expiry_date,
        product_sku, product_name
    )
    VALUES (
        scan_uuid, tenant_uuid, session_uuid,
        matched_po_item, matched_product.product_id, matched_product.product_variant_id,
        barcode_value, CURRENT_TIMESTAMP, quantity_value,
        scan_status, matched_product.match_confidence,
        batch_value, expiry_value,
        matched_product.product_sku, matched_product.product_name
    );

    -- Update session totals
    UPDATE mobile_receiving_sessions
    SET total_items_scanned = total_items_scanned + 1,
        total_quantity_scanned = total_quantity_scanned + quantity_value,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = session_uuid;

    RETURN scan_uuid;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Complete mobile receiving session
-- ============================================================
CREATE OR REPLACE FUNCTION complete_mobile_receiving_session(session_uuid UUID)
RETURNS UUID AS $$
DECLARE
    receipt_uuid UUID;
    tenant_uuid UUID;
    po_uuid UUID;
    location_uuid UUID;
    receipt_num VARCHAR(50);
    scan_record RECORD;
BEGIN
    -- Get session details
    SELECT s.tenant_id, s.purchase_order_id, s.location_id
    INTO tenant_uuid, po_uuid, location_uuid
    FROM mobile_receiving_sessions s
    WHERE s.id = session_uuid;

    IF tenant_uuid IS NULL THEN
        RAISE EXCEPTION 'Session not found: %', session_uuid;
    END IF;

    -- Generate receipt number
    receipt_num := generate_receipt_number(tenant_uuid);
    receipt_uuid := gen_random_uuid();

    -- Create PO receipt
    INSERT INTO purchase_order_receipts (
        id, tenant_id, purchase_order_id, receipt_number,
        location_id, receipt_date, notes
    )
    VALUES (
        receipt_uuid, tenant_uuid, po_uuid, receipt_num,
        location_uuid, CURRENT_TIMESTAMP,
        'Created from mobile receiving session'
    );

    -- Create receipt items from scans (grouped by PO item)
    FOR scan_record IN
        SELECT
            mrs.purchase_order_item_id,
            mrs.product_id,
            mrs.product_variant_id,
            SUM(mrs.quantity_scanned) AS total_quantity,
            poi.unit_cost,
            mrs.batch_number,
            mrs.expiry_date
        FROM mobile_receiving_scans mrs
        INNER JOIN purchase_order_items poi ON mrs.purchase_order_item_id = poi.id
        WHERE mrs.mobile_receiving_session_id = session_uuid
          AND mrs.match_status = 'MATCHED'
          AND mrs.quality_status = 'APPROVED'
        GROUP BY
            mrs.purchase_order_item_id, mrs.product_id,
            mrs.product_variant_id, poi.unit_cost,
            mrs.batch_number, mrs.expiry_date
    LOOP
        INSERT INTO purchase_order_receipt_items (
            tenant_id, purchase_order_receipt_id,
            purchase_order_item_id, product_id, product_variant_id,
            quantity_received, unit_cost,
            quantity_accepted, quantity_rejected,
            batch_number, expiry_date
        )
        VALUES (
            tenant_uuid, receipt_uuid,
            scan_record.purchase_order_item_id,
            scan_record.product_id, scan_record.product_variant_id,
            scan_record.total_quantity, scan_record.unit_cost,
            scan_record.total_quantity, 0,
            scan_record.batch_number, scan_record.expiry_date
        );
    END LOOP;

    -- Process receipt (updates inventory)
    PERFORM process_po_receipt(receipt_uuid);

    -- Update session status
    UPDATE mobile_receiving_sessions
    SET status = 'COMPLETED',
        completed_at = CURRENT_TIMESTAMP,
        duration_seconds = EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - started_at))::INTEGER,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = session_uuid;

    RETURN receipt_uuid;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- View: Mobile receiving session summary
-- ============================================================
CREATE OR REPLACE VIEW v_mobile_receiving_sessions AS
SELECT
    mrs.id AS session_id,
    mrs.session_number,
    po.po_number,
    s.company_name AS supplier_name,
    l.name AS location_name,
    mrs.status,
    mrs.total_items_expected,
    mrs.total_items_scanned,
    mrs.total_quantity_expected,
    mrs.total_quantity_scanned,
    ROUND(
        CASE
            WHEN mrs.total_quantity_expected > 0 THEN
                (mrs.total_quantity_scanned / mrs.total_quantity_expected * 100)
            ELSE 0
        END, 2
    ) AS completion_percentage,
    mrs.started_at,
    mrs.completed_at,
    mrs.duration_seconds,
    mrs.user_id,
    mrs.device_name
FROM mobile_receiving_sessions mrs
INNER JOIN purchase_orders po ON mrs.purchase_order_id = po.id
INNER JOIN suppliers s ON po.supplier_id = s.id
LEFT JOIN locations l ON mrs.location_id = l.id;

-- ============================================================
-- View: Scan details with product info
-- ============================================================
CREATE OR REPLACE VIEW v_mobile_receiving_scans AS
SELECT
    mrs.id AS scan_id,
    ms.session_number,
    po.po_number,
    mrs.barcode,
    mrs.barcode_type,
    mrs.product_sku,
    mrs.product_name,
    mrs.quantity_scanned,
    mrs.match_status,
    mrs.match_confidence,
    mrs.quality_status,
    mrs.batch_number,
    mrs.expiry_date,
    mrs.scan_timestamp,
    mrs.notes
FROM mobile_receiving_scans mrs
INNER JOIN mobile_receiving_sessions ms ON mrs.mobile_receiving_session_id = ms.id
INNER JOIN purchase_orders po ON ms.purchase_order_id = po.id
ORDER BY mrs.scan_timestamp DESC;

-- ============================================================
-- Initial data: Create sample barcodes
-- ============================================================

-- Add barcodes for existing products
INSERT INTO barcode_mappings (
    tenant_id, product_id, barcode, barcode_type, is_primary
)
SELECT
    p.tenant_id,
    p.id,
    p.sku,
    'CUSTOM',
    true
FROM products p
WHERE p.ativo = true
ON CONFLICT (tenant_id, barcode) DO NOTHING;

-- Add EAN13 barcode for notebook
INSERT INTO barcode_mappings (
    tenant_id, product_id, barcode, barcode_type, is_primary
)
SELECT
    t.id,
    p.id,
    '7891234567890',
    'EAN13',
    false
FROM tenants t
CROSS JOIN products p
WHERE p.sku = 'NOTE-DELL-I15-001'
ON CONFLICT (tenant_id, barcode) DO NOTHING;
