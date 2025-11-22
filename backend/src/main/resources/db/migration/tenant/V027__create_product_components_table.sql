-- =====================================================
-- Migration V027: Create product_components table
-- Story: 2.4 - Composite Products / Kits (BOM)
-- Description: Creates table to manage Bill of Materials (BOM)
--              for composite products/kits
-- =====================================================

-- Create product_components table
CREATE TABLE IF NOT EXISTS product_components (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    component_product_id UUID NOT NULL,
    quantity_required DECIMAL(10,3) NOT NULL CHECK (quantity_required > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Foreign Keys
    CONSTRAINT fk_product_components_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_product_components_component
        FOREIGN KEY (component_product_id)
        REFERENCES products(id)
        ON DELETE RESTRICT,

    -- Prevent duplicate components in same product
    CONSTRAINT uq_product_component_pair
        UNIQUE (product_id, component_product_id),

    -- Prevent product from being its own component
    CONSTRAINT chk_no_self_reference
        CHECK (product_id != component_product_id)
);

-- Indexes for performance
CREATE INDEX idx_product_components_product_id
    ON product_components(product_id);

CREATE INDEX idx_product_components_component_id
    ON product_components(component_product_id);

-- Comments
COMMENT ON TABLE product_components IS 'Bill of Materials (BOM) - defines which components make up a composite product/kit';
COMMENT ON COLUMN product_components.product_id IS 'The composite product (the kit)';
COMMENT ON COLUMN product_components.component_product_id IS 'The component product (part of the kit)';
COMMENT ON COLUMN product_components.quantity_required IS 'How many units of this component are needed for one unit of the composite product';
