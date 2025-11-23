-- V037__add_default_consumer_customer.sql
-- Story 4.1: Customer Management - Default Consumer
-- Adds is_default_consumer flag and creates "Consumidor Final" default customer

-- ============================================================
-- Add is_default_consumer flag to customers table
-- ============================================================
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS is_default_consumer BOOLEAN NOT NULL DEFAULT false;

-- Create unique index to ensure only one default consumer per tenant
CREATE UNIQUE INDEX idx_customers_default_consumer
ON customers(tenant_id, is_default_consumer)
WHERE is_default_consumer = true;

-- ============================================================
-- Insert "Consumidor Final" default customer for each tenant
-- ============================================================
INSERT INTO customers (
    id,
    tenant_id,
    customer_type,
    first_name,
    last_name,
    cpf,
    email,
    phone,
    is_default_consumer,
    ativo,
    accepts_marketing,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    t.id,
    'INDIVIDUAL',
    'Consumidor',
    'Final',
    '000.000.000-00', -- CPF reservado para consumidor final
    'consumidor.final@sistema.local',
    '',
    true,
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM tenants t
WHERE NOT EXISTS (
    SELECT 1 FROM customers c
    WHERE c.tenant_id = t.id AND c.is_default_consumer = true
);

-- ============================================================
-- Add check constraint to protect default consumer
-- ============================================================
-- Note: Protection logic will be enforced in application layer
-- to prevent editing/deletion of default consumer

COMMENT ON COLUMN customers.is_default_consumer IS 'Flag indicating if this is the default "Consumidor Final" customer for PDV sales without customer identification. Cannot be deleted or edited.';
