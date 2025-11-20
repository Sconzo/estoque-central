-- V018__create_cost_tracking_tables.sql
-- Story 5.4: Weighted Average Cost Calculation
-- Creates tables and functions for automatic weighted average cost tracking

-- ============================================================
-- Table: product_costs
-- Stores current cost per product/location
-- ============================================================
CREATE TABLE IF NOT EXISTS product_costs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Product reference
    product_id UUID NOT NULL REFERENCES products(id),
    product_variant_id UUID REFERENCES product_variants(id),

    -- Location reference
    location_id UUID NOT NULL REFERENCES locations(id),

    -- Current costs
    average_cost NUMERIC(15, 4) NOT NULL DEFAULT 0,
    last_cost NUMERIC(15, 4),

    -- Stock tracking for cost calculation
    current_quantity NUMERIC(15, 3) NOT NULL DEFAULT 0,
    total_value NUMERIC(15, 2) NOT NULL DEFAULT 0,

    -- Statistics
    total_purchases NUMERIC(15, 3) NOT NULL DEFAULT 0,
    total_purchase_value NUMERIC(15, 2) NOT NULL DEFAULT 0,

    -- Last movement
    last_movement_date TIMESTAMP,
    last_movement_type VARCHAR(30),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT unique_product_cost_location UNIQUE (product_id, product_variant_id, location_id),
    CONSTRAINT check_positive_values CHECK (
        average_cost >= 0 AND current_quantity >= 0 AND total_value >= 0
    )
);

-- ============================================================
-- Table: cost_history
-- Tracks historical cost changes
-- ============================================================
CREATE TABLE IF NOT EXISTS cost_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Product reference
    product_id UUID NOT NULL REFERENCES products(id),
    product_variant_id UUID REFERENCES product_variants(id),

    -- Location reference
    location_id UUID NOT NULL REFERENCES locations(id),

    -- Cost change
    old_average_cost NUMERIC(15, 4),
    new_average_cost NUMERIC(15, 4) NOT NULL,
    cost_change_percentage NUMERIC(10, 4),

    -- Movement that caused the change
    inventory_movement_id UUID REFERENCES inventory_movements(id),
    movement_type VARCHAR(30),
    movement_quantity NUMERIC(15, 3),
    movement_cost NUMERIC(15, 4),

    -- Stock at time of change
    quantity_before NUMERIC(15, 3),
    quantity_after NUMERIC(15, 3),
    value_before NUMERIC(15, 2),
    value_after NUMERIC(15, 2),

    -- Change details
    change_reason VARCHAR(100),
    notes TEXT,

    -- Audit fields
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by UUID,

    -- Constraints
    CONSTRAINT check_cost_change CHECK (new_average_cost >= 0)
);

-- ============================================================
-- Indexes
-- ============================================================

-- Product costs indexes
CREATE INDEX idx_product_costs_tenant ON product_costs(tenant_id);
CREATE INDEX idx_product_costs_product ON product_costs(product_id);
CREATE INDEX idx_product_costs_variant ON product_costs(product_variant_id) WHERE product_variant_id IS NOT NULL;
CREATE INDEX idx_product_costs_location ON product_costs(location_id);
CREATE INDEX idx_product_costs_updated ON product_costs(updated_at DESC);

-- Cost history indexes
CREATE INDEX idx_cost_history_tenant ON cost_history(tenant_id);
CREATE INDEX idx_cost_history_product ON cost_history(product_id);
CREATE INDEX idx_cost_history_variant ON cost_history(product_variant_id) WHERE product_variant_id IS NOT NULL;
CREATE INDEX idx_cost_history_location ON cost_history(location_id);
CREATE INDEX idx_cost_history_movement ON cost_history(inventory_movement_id);
CREATE INDEX idx_cost_history_changed ON cost_history(changed_at DESC);

-- ============================================================
-- Triggers for updated_at
-- ============================================================
CREATE TRIGGER trigger_product_costs_updated_at
    BEFORE UPDATE ON product_costs
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Function: Calculate weighted average cost
-- ============================================================
CREATE OR REPLACE FUNCTION calculate_weighted_average_cost(
    prod_id UUID,
    var_id UUID,
    loc_id UUID,
    movement_qty NUMERIC(15, 3),
    movement_unit_cost NUMERIC(15, 4),
    movement_type VARCHAR(30)
)
RETURNS NUMERIC(15, 4) AS $$
DECLARE
    current_qty NUMERIC(15, 3);
    current_avg_cost NUMERIC(15, 4);
    current_value NUMERIC(15, 2);
    new_qty NUMERIC(15, 3);
    new_value NUMERIC(15, 2);
    new_avg_cost NUMERIC(15, 4);
BEGIN
    -- Get current cost data
    SELECT
        COALESCE(current_quantity, 0),
        COALESCE(average_cost, 0),
        COALESCE(total_value, 0)
    INTO current_qty, current_avg_cost, current_value
    FROM product_costs
    WHERE product_id = prod_id
      AND (product_variant_id = var_id OR (product_variant_id IS NULL AND var_id IS NULL))
      AND location_id = loc_id;

    -- If no record exists, use movement cost as initial
    IF NOT FOUND THEN
        current_qty := 0;
        current_avg_cost := 0;
        current_value := 0;
    END IF;

    -- Calculate based on movement type
    IF movement_type IN ('PURCHASE', 'ADJUSTMENT_IN', 'TRANSFER_IN', 'RETURN_FROM_CUSTOMER') THEN
        -- Inbound: Add to stock and recalculate weighted average
        new_qty := current_qty + movement_qty;
        new_value := current_value + (movement_qty * movement_unit_cost);

        IF new_qty > 0 THEN
            new_avg_cost := new_value / new_qty;
        ELSE
            new_avg_cost := current_avg_cost;
        END IF;

    ELSIF movement_type IN ('SALE', 'ADJUSTMENT_OUT', 'TRANSFER_OUT', 'RETURN_TO_SUPPLIER') THEN
        -- Outbound: Use current average cost, don't recalculate
        new_qty := current_qty - movement_qty;
        new_value := new_qty * current_avg_cost;
        new_avg_cost := current_avg_cost;

    ELSE
        -- Unknown movement type, don't change cost
        new_avg_cost := current_avg_cost;
    END IF;

    RETURN new_avg_cost;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Update product cost after movement
-- ============================================================
CREATE OR REPLACE FUNCTION update_product_cost_after_movement()
RETURNS TRIGGER AS $$
DECLARE
    tenant_uuid UUID;
    prod_id UUID;
    var_id UUID;
    loc_id UUID;
    old_avg_cost NUMERIC(15, 4);
    new_avg_cost NUMERIC(15, 4);
    old_qty NUMERIC(15, 3);
    new_qty NUMERIC(15, 3);
    old_value NUMERIC(15, 2);
    new_value NUMERIC(15, 2);
    cost_change_pct NUMERIC(10, 4);
BEGIN
    -- Get movement details
    SELECT i.tenant_id, i.product_id, i.location_id
    INTO tenant_uuid, prod_id, loc_id
    FROM inventory i
    WHERE i.id = NEW.inventory_id;

    -- Determine product variant if applicable
    SELECT product_variant_id INTO var_id
    FROM inventory
    WHERE id = NEW.inventory_id;

    -- Get current cost info
    SELECT
        COALESCE(average_cost, 0),
        COALESCE(current_quantity, 0),
        COALESCE(total_value, 0)
    INTO old_avg_cost, old_qty, old_value
    FROM product_costs
    WHERE product_id = prod_id
      AND (product_variant_id = var_id OR (product_variant_id IS NULL AND var_id IS NULL))
      AND location_id = loc_id;

    IF NOT FOUND THEN
        old_avg_cost := 0;
        old_qty := 0;
        old_value := 0;
    END IF;

    -- Calculate new weighted average cost
    new_avg_cost := calculate_weighted_average_cost(
        prod_id,
        var_id,
        loc_id,
        ABS(NEW.quantity),
        COALESCE(NEW.unit_cost, old_avg_cost),
        NEW.movement_type
    );

    -- Calculate new quantity and value
    IF NEW.movement_type IN ('PURCHASE', 'ADJUSTMENT_IN', 'TRANSFER_IN', 'RETURN_FROM_CUSTOMER') THEN
        new_qty := old_qty + ABS(NEW.quantity);
        new_value := new_qty * new_avg_cost;
    ELSE
        new_qty := old_qty - ABS(NEW.quantity);
        new_value := new_qty * new_avg_cost;
    END IF;

    -- Ensure non-negative
    IF new_qty < 0 THEN new_qty := 0; END IF;
    IF new_value < 0 THEN new_value := 0; END IF;

    -- Calculate cost change percentage
    IF old_avg_cost > 0 THEN
        cost_change_pct := ((new_avg_cost - old_avg_cost) / old_avg_cost) * 100;
    ELSE
        cost_change_pct := NULL;
    END IF;

    -- Insert or update product_costs
    INSERT INTO product_costs (
        tenant_id, product_id, product_variant_id, location_id,
        average_cost, last_cost, current_quantity, total_value,
        last_movement_date, last_movement_type
    )
    VALUES (
        tenant_uuid, prod_id, var_id, loc_id,
        new_avg_cost, COALESCE(NEW.unit_cost, old_avg_cost),
        new_qty, new_value,
        NEW.movement_date, NEW.movement_type
    )
    ON CONFLICT (product_id, product_variant_id, location_id)
    DO UPDATE SET
        average_cost = new_avg_cost,
        last_cost = COALESCE(NEW.unit_cost, EXCLUDED.average_cost),
        current_quantity = new_qty,
        total_value = new_value,
        last_movement_date = NEW.movement_date,
        last_movement_type = NEW.movement_type,
        updated_at = CURRENT_TIMESTAMP;

    -- Track purchases specifically
    IF NEW.movement_type = 'PURCHASE' THEN
        UPDATE product_costs
        SET total_purchases = total_purchases + ABS(NEW.quantity),
            total_purchase_value = total_purchase_value + (ABS(NEW.quantity) * COALESCE(NEW.unit_cost, 0))
        WHERE product_id = prod_id
          AND (product_variant_id = var_id OR (product_variant_id IS NULL AND var_id IS NULL))
          AND location_id = loc_id;
    END IF;

    -- Record cost history if cost changed significantly (>0.01%)
    IF old_avg_cost IS DISTINCT FROM new_avg_cost AND
       (cost_change_pct IS NULL OR ABS(cost_change_pct) > 0.01) THEN
        INSERT INTO cost_history (
            tenant_id, product_id, product_variant_id, location_id,
            old_average_cost, new_average_cost, cost_change_percentage,
            inventory_movement_id, movement_type, movement_quantity, movement_cost,
            quantity_before, quantity_after, value_before, value_after
        )
        VALUES (
            tenant_uuid, prod_id, var_id, loc_id,
            old_avg_cost, new_avg_cost, cost_change_pct,
            NEW.id, NEW.movement_type, NEW.quantity, NEW.unit_cost,
            old_qty, new_qty, old_value, new_value
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger on inventory_movements
CREATE TRIGGER trigger_update_cost_after_movement
    AFTER INSERT ON inventory_movements
    FOR EACH ROW
    EXECUTE FUNCTION update_product_cost_after_movement();

-- ============================================================
-- View: Product costs with details
-- ============================================================
CREATE OR REPLACE VIEW v_product_costs AS
SELECT
    pc.id AS product_cost_id,
    p.sku AS product_sku,
    p.name AS product_name,
    pv.sku AS variant_sku,
    pv.name AS variant_name,
    l.code AS location_code,
    l.name AS location_name,
    pc.average_cost,
    pc.last_cost,
    pc.current_quantity,
    pc.total_value,
    pc.total_purchases,
    pc.total_purchase_value,
    CASE
        WHEN pc.total_purchases > 0 THEN
            pc.total_purchase_value / pc.total_purchases
        ELSE NULL
    END AS avg_purchase_cost,
    p.price AS selling_price,
    CASE
        WHEN pc.average_cost > 0 AND p.price > 0 THEN
            ROUND(((p.price - pc.average_cost) / p.price * 100), 2)
        ELSE NULL
    END AS margin_percentage,
    pc.last_movement_date,
    pc.last_movement_type,
    pc.updated_at
FROM product_costs pc
INNER JOIN products p ON pc.product_id = p.id
LEFT JOIN product_variants pv ON pc.product_variant_id = pv.id
LEFT JOIN locations l ON pc.location_id = l.id
WHERE p.ativo = true;

-- ============================================================
-- View: Cost changes summary
-- ============================================================
CREATE OR REPLACE VIEW v_cost_changes AS
SELECT
    ch.id AS change_id,
    p.sku AS product_sku,
    p.name AS product_name,
    l.name AS location_name,
    ch.old_average_cost,
    ch.new_average_cost,
    ch.cost_change_percentage,
    CASE
        WHEN ch.cost_change_percentage > 0 THEN 'INCREASE'
        WHEN ch.cost_change_percentage < 0 THEN 'DECREASE'
        ELSE 'NO_CHANGE'
    END AS change_direction,
    ch.movement_type,
    ch.movement_quantity,
    ch.quantity_before,
    ch.quantity_after,
    ch.value_before,
    ch.value_after,
    ch.changed_at
FROM cost_history ch
INNER JOIN products p ON ch.product_id = p.id
LEFT JOIN locations l ON ch.location_id = l.id
ORDER BY ch.changed_at DESC;

-- ============================================================
-- View: Products with low margin
-- ============================================================
CREATE OR REPLACE VIEW v_low_margin_products AS
SELECT
    p.sku,
    p.name,
    l.name AS location_name,
    pc.average_cost,
    p.price AS selling_price,
    ROUND(((p.price - pc.average_cost) / p.price * 100), 2) AS margin_percentage,
    pc.current_quantity,
    pc.total_value
FROM product_costs pc
INNER JOIN products p ON pc.product_id = p.id
INNER JOIN locations l ON pc.location_id = l.id
WHERE p.price > 0
  AND pc.average_cost > 0
  AND ((p.price - pc.average_cost) / p.price * 100) < 20  -- Less than 20% margin
  AND p.ativo = true
ORDER BY margin_percentage ASC;

-- ============================================================
-- Initial data: Initialize costs from existing inventory
-- ============================================================

-- Initialize product_costs from current inventory
INSERT INTO product_costs (
    tenant_id, product_id, product_variant_id, location_id,
    average_cost, current_quantity, total_value
)
SELECT
    i.tenant_id,
    i.product_id,
    NULL,
    i.location_id,
    COALESCE(p.cost, 0),
    COALESCE(i.quantity, 0),
    COALESCE(i.quantity * p.cost, 0)
FROM inventory i
INNER JOIN products p ON i.product_id = p.id
WHERE i.quantity > 0
  AND p.ativo = true
ON CONFLICT (product_id, product_variant_id, location_id) DO NOTHING;
