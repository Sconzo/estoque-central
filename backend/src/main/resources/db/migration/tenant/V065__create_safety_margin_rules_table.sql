-- V065: Create safety_margin_rules table
-- Story 5.7: Configurable Safety Stock Margin - AC1

CREATE TABLE IF NOT EXISTS safety_margin_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Scope configuration
    marketplace VARCHAR(50) NOT NULL,
    product_id UUID NULL,
    category_id UUID NULL,

    -- Margin configuration
    margin_percentage DECIMAL(5,2) NOT NULL DEFAULT 100.00,

    -- Priority (1=product specific, 2=category, 3=marketplace global)
    priority SMALLINT NOT NULL,

    -- Audit fields
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id UUID NULL,

    -- Constraints
    CONSTRAINT safety_margin_rules_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT safety_margin_rules_product_fk FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT safety_margin_rules_category_fk FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT safety_margin_rules_margin_check CHECK (margin_percentage >= 0 AND margin_percentage <= 100),
    CONSTRAINT safety_margin_rules_priority_check CHECK (priority IN (1, 2, 3)),
    -- Ensure only one scope field is set based on priority
    CONSTRAINT safety_margin_rules_scope_check CHECK (
        (priority = 1 AND product_id IS NOT NULL AND category_id IS NULL) OR
        (priority = 2 AND category_id IS NOT NULL AND product_id IS NULL) OR
        (priority = 3 AND product_id IS NULL AND category_id IS NULL)
    )
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_safety_margin_rules_tenant ON safety_margin_rules(tenant_id);
CREATE INDEX IF NOT EXISTS idx_safety_margin_rules_marketplace ON safety_margin_rules(tenant_id, marketplace);
CREATE INDEX IF NOT EXISTS idx_safety_margin_rules_product ON safety_margin_rules(tenant_id, product_id) WHERE product_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_safety_margin_rules_category ON safety_margin_rules(tenant_id, category_id) WHERE category_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_safety_margin_rules_priority ON safety_margin_rules(tenant_id, marketplace, priority);

-- Unique constraint: one rule per scope
CREATE UNIQUE INDEX IF NOT EXISTS idx_safety_margin_rules_unique_product
    ON safety_margin_rules(tenant_id, marketplace, product_id)
    WHERE product_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_safety_margin_rules_unique_category
    ON safety_margin_rules(tenant_id, marketplace, category_id)
    WHERE category_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_safety_margin_rules_unique_global
    ON safety_margin_rules(tenant_id, marketplace)
    WHERE product_id IS NULL AND category_id IS NULL;

-- Comments
COMMENT ON TABLE safety_margin_rules IS 'Story 5.7: Configurable safety margin rules for marketplace stock sync';
COMMENT ON COLUMN safety_margin_rules.priority IS '1=product specific, 2=category, 3=marketplace global';
COMMENT ON COLUMN safety_margin_rules.margin_percentage IS 'Percentage of available stock to publish (0-100)';
