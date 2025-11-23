-- Story 3.4: Create receiving_items table for detailed receiving line items
-- Migration: V034__create_receiving_items_table.sql

CREATE TABLE IF NOT EXISTS receiving_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receiving_id UUID NOT NULL,
    purchase_order_item_id UUID NOT NULL,
    product_id UUID,
    variant_id UUID,
    quantity_received DECIMAL(10,2) NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    new_weighted_average_cost DECIMAL(10,2),
    notes TEXT,

    CONSTRAINT fk_receiving_items_receiving FOREIGN KEY (receiving_id) REFERENCES receivings(id) ON DELETE CASCADE,
    CONSTRAINT fk_receiving_items_po_item FOREIGN KEY (purchase_order_item_id) REFERENCES purchase_order_items(id),
    CONSTRAINT fk_receiving_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_receiving_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT chk_quantity_received_positive CHECK (quantity_received > 0)
);

-- Indexes for performance
CREATE INDEX idx_receiving_items_receiving_id ON receiving_items(receiving_id);
CREATE INDEX idx_receiving_items_po_item_id ON receiving_items(purchase_order_item_id);
CREATE INDEX idx_receiving_items_product_id ON receiving_items(product_id);

-- Comment
COMMENT ON TABLE receiving_items IS 'Detailed line items for each receiving transaction (Story 3.4)';
COMMENT ON COLUMN receiving_items.unit_cost IS 'Unit cost from purchase order';
COMMENT ON COLUMN receiving_items.new_weighted_average_cost IS 'Calculated weighted average cost after this receiving';
