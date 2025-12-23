-- ============================================================================
-- Migration V005: Add CNPJ column to tenants table
-- ============================================================================
-- Purpose: Add Brazilian tax ID (CNPJ) to tenants for compliance
--
-- Story: 7-2 PostgreSQL Multi-Tenant Schema-per-Tenant
-- AC1: public.tenants must include cnpj column
-- ============================================================================

-- Add cnpj column to public.tenants
ALTER TABLE public.tenants
ADD COLUMN IF NOT EXISTS cnpj VARCHAR(18) UNIQUE;

-- Create index for fast CNPJ lookup
CREATE INDEX IF NOT EXISTS idx_tenants_cnpj
ON public.tenants(cnpj);

-- Add comment
COMMENT ON COLUMN public.tenants.cnpj IS
'Brazilian tax ID (CNPJ) - must be unique across all tenants';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
