-- V078__fix_default_consumer_insert.sql
-- Fix for V037 bug: V037 used `FROM tenants` but the tenants table is in
-- the public schema and was not accessible without the public. prefix.
-- This migration correctly inserts the default "Consumidor Final" customer
-- for the current tenant by using public.tenants with current_schema().
--
-- CPF and email are stored as NULL so CryptoService.decrypt() is not called
-- on plain-text values (decryptCustomer skips null/empty fields).

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
    NULL,
    NULL,
    '',
    true,
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM public.tenants t
WHERE t.schema_name = current_schema()
  AND NOT EXISTS (
      SELECT 1 FROM customers c
      WHERE c.tenant_id = t.id
        AND c.is_default_consumer = true
  );
