-- Migration V052: Create sales_order_items table
-- Story 4.5: Sales Order B2B Interface
-- Line items for sales orders with product/variant, quantity, pricing, and reservation tracking

CREATE TABLE sales_order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sales_order_id UUID NOT NULL,
    product_id UUID,
    variant_id UUID,
    quantity_ordered DECIMAL(15, 3) NOT NULL,
    quantity_reserved DECIMAL(15, 3) NOT NULL DEFAULT 0,
    unit_price DECIMAL(15, 2) NOT NULL,
    total_price DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_sales_order_items_order FOREIGN KEY (sales_order_id)
        REFERENCES sales_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_sales_order_items_product FOREIGN KEY (product_id)
        REFERENCES products(id),
    CONSTRAINT fk_sales_order_items_variant FOREIGN KEY (variant_id)
        REFERENCES product_variants(id),

    CONSTRAINT chk_quantity_ordered CHECK (quantity_ordered > 0),
    CONSTRAINT chk_quantity_reserved CHECK (quantity_reserved >= 0 AND quantity_reserved <= quantity_ordered),
    CONSTRAINT chk_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_total_price CHECK (total_price >= 0),
    CONSTRAINT chk_product_or_variant CHECK (
        (product_id IS NOT NULL AND variant_id IS NULL) OR
        (product_id IS NULL AND variant_id IS NOT NULL)
    )
);

-- Indexes for performance
CREATE INDEX idx_sales_order_items_order ON sales_order_items(sales_order_id);
CREATE INDEX idx_sales_order_items_product ON sales_order_items(product_id) WHERE product_id IS NOT NULL;
CREATE INDEX idx_sales_order_items_variant ON sales_order_items(variant_id) WHERE variant_id IS NOT NULL;

-- Comments
COMMENT ON TABLE sales_order_items IS 'Story 4.5: Line items for B2B sales orders';
COMMENT ON COLUMN sales_order_items.quantity_reserved IS 'Quantity reserved in inventory (Story 4.6)';
COMMENT ON COLUMN sales_order_items.total_price IS 'Calculated: quantity_ordered * unit_price';
