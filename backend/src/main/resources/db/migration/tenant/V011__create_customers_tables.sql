-- V011__create_customers_tables.sql
-- Story 4.1: Customer Management
-- Creates tables for customer management with addresses and contact info

-- ============================================================
-- Table: customers
-- Stores customer information
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Customer type
    customer_type VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL',

    -- Personal info (for INDIVIDUAL)
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    cpf VARCHAR(14),

    -- Company info (for BUSINESS)
    company_name VARCHAR(200),
    cnpj VARCHAR(18),
    trade_name VARCHAR(200),

    -- Contact
    email VARCHAR(200),
    phone VARCHAR(50),
    mobile VARCHAR(50),

    -- Birth date / Foundation date
    birth_date DATE,

    -- Tax info
    tax_id VARCHAR(50),
    state_registration VARCHAR(50),

    -- Customer segmentation
    customer_segment VARCHAR(50),
    loyalty_tier VARCHAR(20),
    credit_limit NUMERIC(15, 2),

    -- Preferences
    accepts_marketing BOOLEAN NOT NULL DEFAULT true,
    preferred_language VARCHAR(10) DEFAULT 'pt-BR',

    -- Notes
    notes TEXT,

    -- Status
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT check_customer_type CHECK (customer_type IN ('INDIVIDUAL', 'BUSINESS')),
    CONSTRAINT check_individual_fields CHECK (
        customer_type != 'INDIVIDUAL' OR (first_name IS NOT NULL AND last_name IS NOT NULL)
    ),
    CONSTRAINT check_business_fields CHECK (
        customer_type != 'BUSINESS' OR company_name IS NOT NULL
    ),
    CONSTRAINT unique_cpf_per_tenant UNIQUE (tenant_id, cpf),
    CONSTRAINT unique_cnpj_per_tenant UNIQUE (tenant_id, cnpj),
    CONSTRAINT unique_email_per_tenant UNIQUE (tenant_id, email)
);

-- ============================================================
-- Table: customer_addresses
-- Stores multiple addresses per customer
-- ============================================================
CREATE TABLE IF NOT EXISTS customer_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Customer reference
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,

    -- Address type
    address_type VARCHAR(20) NOT NULL DEFAULT 'SHIPPING',

    -- Address details
    street VARCHAR(200) NOT NULL,
    number VARCHAR(20),
    complement VARCHAR(100),
    neighborhood VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(50) NOT NULL DEFAULT 'Brazil',

    -- Contact at this address
    contact_name VARCHAR(200),
    contact_phone VARCHAR(50),

    -- Default address flag
    is_default BOOLEAN NOT NULL DEFAULT false,

    -- Status
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_address_type CHECK (address_type IN ('BILLING', 'SHIPPING', 'BOTH'))
);

-- ============================================================
-- Table: customer_contacts
-- Stores additional contact persons
-- ============================================================
CREATE TABLE IF NOT EXISTS customer_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Customer reference
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,

    -- Contact info
    name VARCHAR(200) NOT NULL,
    role VARCHAR(100),
    email VARCHAR(200),
    phone VARCHAR(50),
    mobile VARCHAR(50),

    -- Is primary contact
    is_primary BOOLEAN NOT NULL DEFAULT false,

    -- Status
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Indexes
-- ============================================================

-- Customers indexes
CREATE INDEX idx_customers_tenant ON customers(tenant_id);
CREATE INDEX idx_customers_type ON customers(customer_type) WHERE ativo = true;
CREATE INDEX idx_customers_email ON customers(email) WHERE ativo = true;
CREATE INDEX idx_customers_cpf ON customers(cpf) WHERE cpf IS NOT NULL;
CREATE INDEX idx_customers_cnpj ON customers(cnpj) WHERE cnpj IS NOT NULL;
CREATE INDEX idx_customers_segment ON customers(customer_segment) WHERE ativo = true;
CREATE INDEX idx_customers_name ON customers(first_name, last_name) WHERE ativo = true;
CREATE INDEX idx_customers_company ON customers(company_name) WHERE ativo = true;

-- Addresses indexes
CREATE INDEX idx_addresses_tenant ON customer_addresses(tenant_id);
CREATE INDEX idx_addresses_customer ON customer_addresses(customer_id);
CREATE INDEX idx_addresses_type ON customer_addresses(address_type) WHERE ativo = true;
CREATE INDEX idx_addresses_default ON customer_addresses(customer_id, is_default) WHERE is_default = true;
CREATE INDEX idx_addresses_postal ON customer_addresses(postal_code);

-- Contacts indexes
CREATE INDEX idx_contacts_customer ON customer_contacts(customer_id);
CREATE INDEX idx_contacts_primary ON customer_contacts(customer_id, is_primary) WHERE is_primary = true;

-- ============================================================
-- Triggers for updated_at
-- ============================================================
CREATE TRIGGER trigger_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

CREATE TRIGGER trigger_addresses_updated_at
    BEFORE UPDATE ON customer_addresses
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Initial data: Create sample customers
-- ============================================================

-- Individual customer
INSERT INTO customers (
    tenant_id, customer_type, first_name, last_name, cpf,
    email, phone, mobile, birth_date, accepts_marketing
)
SELECT
    t.id,
    'INDIVIDUAL',
    'João',
    'Silva',
    '123.456.789-00',
    'joao.silva@email.com',
    '(11) 3456-7890',
    '(11) 98765-4321',
    '1985-05-15',
    true
FROM tenants t
LIMIT 1;

-- Business customer
INSERT INTO customers (
    tenant_id, customer_type, company_name, cnpj, trade_name,
    email, phone, accepts_marketing
)
SELECT
    t.id,
    'BUSINESS',
    'Empresa XYZ Ltda',
    '12.345.678/0001-90',
    'XYZ Store',
    'contato@empresaxyz.com',
    '(11) 3000-0000',
    true
FROM tenants t
LIMIT 1;

-- Add addresses for João Silva
INSERT INTO customer_addresses (
    tenant_id, customer_id, address_type,
    street, number, neighborhood, city, state, postal_code,
    is_default
)
SELECT
    t.id,
    c.id,
    'BOTH',
    'Rua das Flores',
    '123',
    'Centro',
    'São Paulo',
    'SP',
    '01234-567',
    true
FROM tenants t
CROSS JOIN customers c
WHERE c.email = 'joao.silva@email.com'
LIMIT 1;

-- ============================================================
-- View: Customer summary
-- ============================================================
CREATE OR REPLACE VIEW v_customer_summary AS
SELECT
    c.id AS customer_id,
    c.customer_type,
    CASE
        WHEN c.customer_type = 'INDIVIDUAL' THEN c.first_name || ' ' || c.last_name
        ELSE c.company_name
    END AS customer_name,
    c.email,
    c.phone,
    c.cpf,
    c.cnpj,
    COUNT(DISTINCT a.id) AS address_count,
    COUNT(DISTINCT co.id) AS contact_count,
    c.customer_segment,
    c.loyalty_tier,
    c.ativo
FROM customers c
LEFT JOIN customer_addresses a ON c.id = a.customer_id AND a.ativo = true
LEFT JOIN customer_contacts co ON c.id = co.customer_id AND co.ativo = true
GROUP BY c.id, c.customer_type, c.first_name, c.last_name, c.company_name,
         c.email, c.phone, c.cpf, c.cnpj, c.customer_segment, c.loyalty_tier, c.ativo;
