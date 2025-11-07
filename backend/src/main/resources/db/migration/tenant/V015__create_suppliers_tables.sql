-- V015__create_suppliers_tables.sql
-- Story 5.1: Supplier Management
-- Creates tables for supplier management with fiscal data

-- ============================================================
-- Table: suppliers
-- Stores supplier/vendor information
-- ============================================================
CREATE TABLE IF NOT EXISTS suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Basic identification
    supplier_code VARCHAR(50) NOT NULL,
    supplier_type VARCHAR(20) NOT NULL DEFAULT 'BUSINESS',

    -- Business details
    company_name VARCHAR(200) NOT NULL,
    trade_name VARCHAR(200),
    cnpj VARCHAR(18),

    -- Individual details (for MEI - Microempreendedor Individual)
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    cpf VARCHAR(14),

    -- Contact information
    email VARCHAR(200),
    phone VARCHAR(20),
    mobile VARCHAR(20),
    website VARCHAR(255),

    -- Address
    street VARCHAR(255),
    number VARCHAR(20),
    complement VARCHAR(100),
    neighborhood VARCHAR(100),
    city VARCHAR(100),
    state VARCHAR(50),
    postal_code VARCHAR(10),
    country VARCHAR(50) NOT NULL DEFAULT 'Brasil',

    -- Fiscal data (Brasil)
    state_registration VARCHAR(50),        -- Inscrição Estadual
    municipal_registration VARCHAR(50),    -- Inscrição Municipal
    tax_regime VARCHAR(50),                -- Regime tributário
    icms_taxpayer BOOLEAN NOT NULL DEFAULT true,

    -- Bank details
    bank_name VARCHAR(100),
    bank_code VARCHAR(10),
    bank_branch VARCHAR(20),
    bank_account VARCHAR(30),
    bank_account_type VARCHAR(20),
    pix_key VARCHAR(200),

    -- Business details
    payment_terms VARCHAR(100),            -- Ex: "30/60/90 dias"
    default_payment_method VARCHAR(50),    -- Ex: "BANK_TRANSFER", "BOLETO"
    credit_limit NUMERIC(15, 2),
    average_delivery_days INTEGER,
    minimum_order_value NUMERIC(15, 2),

    -- Status and classification
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    supplier_category VARCHAR(50),         -- Ex: "ELECTRONICS", "FOOD", "CLOTHING"
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    is_preferred BOOLEAN NOT NULL DEFAULT false,

    -- Internal notes
    notes TEXT,
    internal_notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Constraints
    CONSTRAINT unique_supplier_code_per_tenant UNIQUE (tenant_id, supplier_code),
    CONSTRAINT unique_cnpj_per_tenant UNIQUE (tenant_id, cnpj),
    CONSTRAINT unique_cpf_per_tenant UNIQUE (tenant_id, cpf),
    CONSTRAINT check_supplier_type CHECK (supplier_type IN ('INDIVIDUAL', 'BUSINESS')),
    CONSTRAINT check_supplier_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED', 'PENDING_APPROVAL')),
    CONSTRAINT check_tax_regime CHECK (tax_regime IN (
        'SIMPLES_NACIONAL', 'LUCRO_PRESUMIDO', 'LUCRO_REAL', 'MEI', 'OUTROS'
    )),
    CONSTRAINT check_bank_account_type CHECK (bank_account_type IN (
        'CHECKING', 'SAVINGS'
    ))
);

-- ============================================================
-- Table: supplier_contacts
-- Stores multiple contact persons for suppliers
-- ============================================================
CREATE TABLE IF NOT EXISTS supplier_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Supplier reference
    supplier_id UUID NOT NULL REFERENCES suppliers(id) ON DELETE CASCADE,

    -- Contact details
    name VARCHAR(200) NOT NULL,
    role VARCHAR(100),
    department VARCHAR(100),
    email VARCHAR(200),
    phone VARCHAR(20),
    mobile VARCHAR(20),
    is_primary BOOLEAN NOT NULL DEFAULT false,

    -- Notes
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT true
);

-- ============================================================
-- Table: supplier_products
-- Links suppliers to products they can supply with pricing
-- ============================================================
CREATE TABLE IF NOT EXISTS supplier_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- References
    supplier_id UUID NOT NULL REFERENCES suppliers(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,

    -- Supplier's product identification
    supplier_sku VARCHAR(100),
    supplier_product_name VARCHAR(200),

    -- Pricing
    cost_price NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    last_price_update TIMESTAMP,

    -- Lead time and availability
    lead_time_days INTEGER,
    minimum_order_quantity NUMERIC(15, 3),
    is_preferred_supplier BOOLEAN NOT NULL DEFAULT false,
    is_available BOOLEAN NOT NULL DEFAULT true,

    -- Notes
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Constraints
    CONSTRAINT unique_supplier_product UNIQUE (supplier_id, product_id),
    CONSTRAINT check_positive_cost_price CHECK (cost_price > 0)
);

-- ============================================================
-- Table: supplier_price_history
-- Tracks historical pricing from suppliers
-- ============================================================
CREATE TABLE IF NOT EXISTS supplier_price_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- References
    supplier_product_id UUID NOT NULL REFERENCES supplier_products(id) ON DELETE CASCADE,
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    product_id UUID NOT NULL REFERENCES products(id),

    -- Price data
    old_price NUMERIC(15, 2),
    new_price NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',

    -- Change details
    change_percentage NUMERIC(10, 2),
    change_reason VARCHAR(100),
    notes TEXT,

    -- Audit fields
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by UUID
);

-- ============================================================
-- Indexes
-- ============================================================

-- Suppliers indexes
CREATE INDEX idx_suppliers_tenant ON suppliers(tenant_id);
CREATE INDEX idx_suppliers_code ON suppliers(supplier_code);
CREATE INDEX idx_suppliers_status ON suppliers(status);
CREATE INDEX idx_suppliers_type ON suppliers(supplier_type);
CREATE INDEX idx_suppliers_category ON suppliers(supplier_category);
CREATE INDEX idx_suppliers_cnpj ON suppliers(cnpj) WHERE cnpj IS NOT NULL;
CREATE INDEX idx_suppliers_active ON suppliers(ativo) WHERE ativo = true;

-- Supplier contacts indexes
CREATE INDEX idx_supplier_contacts_tenant ON supplier_contacts(tenant_id);
CREATE INDEX idx_supplier_contacts_supplier ON supplier_contacts(supplier_id);
CREATE INDEX idx_supplier_contacts_primary ON supplier_contacts(is_primary) WHERE is_primary = true;

-- Supplier products indexes
CREATE INDEX idx_supplier_products_tenant ON supplier_products(tenant_id);
CREATE INDEX idx_supplier_products_supplier ON supplier_products(supplier_id);
CREATE INDEX idx_supplier_products_product ON supplier_products(product_id);
CREATE INDEX idx_supplier_products_preferred ON supplier_products(is_preferred_supplier) WHERE is_preferred_supplier = true;
CREATE INDEX idx_supplier_products_available ON supplier_products(is_available) WHERE is_available = true;

-- Supplier price history indexes
CREATE INDEX idx_supplier_price_history_tenant ON supplier_price_history(tenant_id);
CREATE INDEX idx_supplier_price_history_supplier_product ON supplier_price_history(supplier_product_id);
CREATE INDEX idx_supplier_price_history_supplier ON supplier_price_history(supplier_id);
CREATE INDEX idx_supplier_price_history_product ON supplier_price_history(product_id);
CREATE INDEX idx_supplier_price_history_changed ON supplier_price_history(changed_at DESC);

-- ============================================================
-- Triggers for updated_at
-- ============================================================
CREATE TRIGGER trigger_suppliers_updated_at
    BEFORE UPDATE ON suppliers
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

CREATE TRIGGER trigger_supplier_contacts_updated_at
    BEFORE UPDATE ON supplier_contacts
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

CREATE TRIGGER trigger_supplier_products_updated_at
    BEFORE UPDATE ON supplier_products
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Trigger: Track price changes automatically
-- ============================================================
CREATE OR REPLACE FUNCTION track_supplier_price_change()
RETURNS TRIGGER AS $$
BEGIN
    -- Only track if price actually changed
    IF OLD.cost_price IS DISTINCT FROM NEW.cost_price THEN
        INSERT INTO supplier_price_history (
            tenant_id,
            supplier_product_id,
            supplier_id,
            product_id,
            old_price,
            new_price,
            change_percentage,
            changed_at,
            changed_by
        )
        VALUES (
            NEW.tenant_id,
            NEW.id,
            NEW.supplier_id,
            NEW.product_id,
            OLD.cost_price,
            NEW.cost_price,
            CASE
                WHEN OLD.cost_price > 0 THEN
                    ((NEW.cost_price - OLD.cost_price) / OLD.cost_price * 100)
                ELSE NULL
            END,
            CURRENT_TIMESTAMP,
            NEW.updated_by
        );

        NEW.last_price_update := CURRENT_TIMESTAMP;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_track_supplier_price_change
    BEFORE UPDATE ON supplier_products
    FOR EACH ROW
    EXECUTE FUNCTION track_supplier_price_change();

-- ============================================================
-- Function: Get preferred supplier for product
-- ============================================================
CREATE OR REPLACE FUNCTION get_preferred_supplier(product_uuid UUID)
RETURNS TABLE(
    supplier_id UUID,
    supplier_name VARCHAR,
    supplier_sku VARCHAR,
    cost_price NUMERIC,
    lead_time_days INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        s.id,
        s.company_name,
        sp.supplier_sku,
        sp.cost_price,
        sp.lead_time_days
    FROM supplier_products sp
    INNER JOIN suppliers s ON sp.supplier_id = s.id
    WHERE sp.product_id = product_uuid
      AND sp.is_preferred_supplier = true
      AND sp.is_available = true
      AND sp.ativo = true
      AND s.status = 'ACTIVE'
      AND s.ativo = true
    ORDER BY sp.cost_price ASC
    LIMIT 1;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Get all suppliers for product
-- ============================================================
CREATE OR REPLACE FUNCTION get_product_suppliers(product_uuid UUID)
RETURNS TABLE(
    supplier_id UUID,
    supplier_name VARCHAR,
    supplier_sku VARCHAR,
    cost_price NUMERIC,
    lead_time_days INTEGER,
    is_preferred BOOLEAN,
    last_price_update TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        s.id,
        s.company_name,
        sp.supplier_sku,
        sp.cost_price,
        sp.lead_time_days,
        sp.is_preferred_supplier,
        sp.last_price_update
    FROM supplier_products sp
    INNER JOIN suppliers s ON sp.supplier_id = s.id
    WHERE sp.product_id = product_uuid
      AND sp.is_available = true
      AND sp.ativo = true
      AND s.status = 'ACTIVE'
      AND s.ativo = true
    ORDER BY sp.is_preferred_supplier DESC, sp.cost_price ASC;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- View: Supplier summary
-- ============================================================
CREATE OR REPLACE VIEW v_supplier_summary AS
SELECT
    s.id AS supplier_id,
    s.supplier_code,
    s.company_name,
    s.trade_name,
    s.cnpj,
    s.email,
    s.phone,
    s.city,
    s.state,
    s.status,
    s.supplier_category,
    s.rating,
    s.is_preferred,
    COUNT(DISTINCT sp.product_id) AS products_count,
    COUNT(DISTINCT sc.id) AS contacts_count,
    s.payment_terms,
    s.average_delivery_days,
    s.created_at
FROM suppliers s
LEFT JOIN supplier_products sp ON s.id = sp.supplier_id AND sp.ativo = true
LEFT JOIN supplier_contacts sc ON s.id = sc.supplier_id AND sc.ativo = true
WHERE s.ativo = true
GROUP BY
    s.id, s.supplier_code, s.company_name, s.trade_name,
    s.cnpj, s.email, s.phone, s.city, s.state, s.status,
    s.supplier_category, s.rating, s.is_preferred, s.payment_terms,
    s.average_delivery_days, s.created_at;

-- ============================================================
-- View: Supplier products with details
-- ============================================================
CREATE OR REPLACE VIEW v_supplier_products_detail AS
SELECT
    sp.id AS supplier_product_id,
    s.id AS supplier_id,
    s.supplier_code,
    s.company_name AS supplier_name,
    p.id AS product_id,
    p.sku AS product_sku,
    p.name AS product_name,
    sp.supplier_sku,
    sp.supplier_product_name,
    sp.cost_price,
    sp.currency,
    sp.lead_time_days,
    sp.minimum_order_quantity,
    sp.is_preferred_supplier,
    sp.is_available,
    sp.last_price_update,
    p.price AS current_selling_price,
    CASE
        WHEN p.price > 0 THEN
            ROUND(((p.price - sp.cost_price) / p.price * 100), 2)
        ELSE NULL
    END AS margin_percentage
FROM supplier_products sp
INNER JOIN suppliers s ON sp.supplier_id = s.id
INNER JOIN products p ON sp.product_id = p.id
WHERE sp.ativo = true
  AND s.ativo = true
  AND p.ativo = true;

-- ============================================================
-- View: Recent price changes
-- ============================================================
CREATE OR REPLACE VIEW v_supplier_price_changes AS
SELECT
    sph.id AS price_change_id,
    s.company_name AS supplier_name,
    p.sku AS product_sku,
    p.name AS product_name,
    sph.old_price,
    sph.new_price,
    sph.change_percentage,
    sph.change_reason,
    sph.changed_at,
    CASE
        WHEN sph.change_percentage > 0 THEN 'INCREASE'
        WHEN sph.change_percentage < 0 THEN 'DECREASE'
        ELSE 'NO_CHANGE'
    END AS change_direction
FROM supplier_price_history sph
INNER JOIN suppliers s ON sph.supplier_id = s.id
INNER JOIN products p ON sph.product_id = p.id
ORDER BY sph.changed_at DESC;

-- ============================================================
-- Initial data: Create sample suppliers
-- ============================================================

-- Create sample supplier: Distribuidora Tech
INSERT INTO suppliers (
    tenant_id, supplier_code, supplier_type, company_name, trade_name,
    cnpj, email, phone, city, state, status, supplier_category,
    payment_terms, average_delivery_days, rating, is_preferred
)
SELECT
    t.id,
    'SUP-001',
    'BUSINESS',
    'Distribuidora Tech Ltda',
    'Tech Distribuidora',
    '12.345.678/0001-90',
    'comercial@techdist.com.br',
    '(11) 3456-7890',
    'São Paulo',
    'SP',
    'ACTIVE',
    'ELECTRONICS',
    '30/60 dias',
    7,
    5,
    true
FROM tenants t
LIMIT 1;

-- Create sample supplier: Fornecedor Nacional
INSERT INTO suppliers (
    tenant_id, supplier_code, supplier_type, company_name, trade_name,
    cnpj, email, phone, city, state, status, supplier_category,
    payment_terms, average_delivery_days, rating
)
SELECT
    t.id,
    'SUP-002',
    'BUSINESS',
    'Fornecedor Nacional S.A.',
    'FN Comercial',
    '98.765.432/0001-10',
    'vendas@fnacional.com.br',
    '(11) 9876-5432',
    'São Paulo',
    'SP',
    'ACTIVE',
    'GENERAL',
    '30 dias',
    5,
    4
FROM tenants t
LIMIT 1;

-- Link suppliers to products
INSERT INTO supplier_products (
    tenant_id, supplier_id, product_id, supplier_sku,
    cost_price, lead_time_days, is_preferred_supplier
)
SELECT
    t.id,
    s.id,
    p.id,
    'TECH-DELL-NOTE-001',
    3800.00,
    7,
    true
FROM tenants t
CROSS JOIN suppliers s
CROSS JOIN products p
WHERE s.supplier_code = 'SUP-001'
  AND p.sku = 'NOTE-DELL-I15-001'
LIMIT 1;

-- Create sample contact
INSERT INTO supplier_contacts (
    tenant_id, supplier_id, name, role, email, phone, is_primary
)
SELECT
    t.id,
    s.id,
    'Carlos Souza',
    'Gerente Comercial',
    'carlos.souza@techdist.com.br',
    '(11) 3456-7891',
    true
FROM tenants t
CROSS JOIN suppliers s
WHERE s.supplier_code = 'SUP-001'
LIMIT 1;
