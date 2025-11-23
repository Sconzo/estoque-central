-- Story 4.3: NFCe Emission and Stock Decrease - Sale Items table
-- Itens das vendas

CREATE TABLE sale_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID NOT NULL,

    -- Product reference
    product_id UUID NOT NULL,
    variant_id UUID, -- NULL se produto simples

    -- Quantity and pricing
    quantity NUMERIC(15, 3) NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL, -- Preço unitário no momento da venda (snapshot)
    total_price NUMERIC(15, 2) NOT NULL, -- quantity * unit_price
    discount NUMERIC(15, 2) DEFAULT 0,

    -- Audit
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_sale_items_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_sale_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT check_quantity_positive CHECK (quantity > 0),
    CONSTRAINT check_unit_price_positive CHECK (unit_price >= 0),
    CONSTRAINT check_total_price_positive CHECK (total_price >= 0)
);

-- Indexes
CREATE INDEX idx_sale_items_sale_id ON sale_items(sale_id);
CREATE INDEX idx_sale_items_product_id ON sale_items(product_id);
CREATE INDEX idx_sale_items_variant_id ON sale_items(variant_id);

-- Comments
COMMENT ON TABLE sale_items IS 'Story 4.3: Itens das vendas (produtos vendidos)';
COMMENT ON COLUMN sale_items.unit_price IS 'Snapshot do preço no momento da venda (imutável)';
COMMENT ON COLUMN sale_items.total_price IS 'Total do item: quantity * unit_price - discount';
