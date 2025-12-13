-- PostgreSQL Init Script - Development Data
-- This script runs automatically when the PostgreSQL container is created for the first time
-- ⚠️ FOR DEVELOPMENT ONLY - DO NOT USE IN PRODUCTION

-- 1. Create default tenant
INSERT INTO tenants (id, nome, schema_name, email, ativo)
VALUES (
    '00000000-0000-0000-0000-000000000000',
    'Empresa Desenvolvimento',
    'tenant_00000000_0000_0000_0000_000000000000',
    'dev@localhost',
    true
) ON CONFLICT (id) DO NOTHING;

-- 2. Create tenant schema
CREATE SCHEMA IF NOT EXISTS tenant_00000000_0000_0000_0000_000000000000;

-- 3. Create usuarios table in public schema (temporary fix for multi-tenancy)
-- This allows login to work while we fix the tenant schema switching
CREATE TABLE IF NOT EXISTS public.usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    google_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    picture_url VARCHAR(500),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'GERENTE', 'VENDEDOR', 'ESTOQUISTA')),
    ativo BOOLEAN NOT NULL DEFAULT true,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Add tenant_id and profile_id columns
ALTER TABLE public.usuarios
    ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    ADD COLUMN IF NOT EXISTS profile_id UUID;

-- 5. Create indexes
CREATE INDEX IF NOT EXISTS idx_usuarios_google_id ON public.usuarios(google_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON public.usuarios(email);
CREATE INDEX IF NOT EXISTS idx_usuarios_role ON public.usuarios(role);
CREATE INDEX IF NOT EXISTS idx_usuarios_ativo ON public.usuarios(ativo) WHERE ativo = true;

-- 6. Add comments
COMMENT ON TABLE public.usuarios IS 'Usuários do sistema - autenticação via Google OAuth 2.0';
COMMENT ON COLUMN public.usuarios.id IS 'ID único do usuário (UUID)';
COMMENT ON COLUMN public.usuarios.google_id IS 'Google ID único do usuário';
COMMENT ON COLUMN public.usuarios.email IS 'Email do usuário (obtido do Google)';
COMMENT ON COLUMN public.usuarios.nome IS 'Nome completo do usuário';
COMMENT ON COLUMN public.usuarios.picture_url IS 'URL da foto de perfil do Google';
COMMENT ON COLUMN public.usuarios.role IS 'Papel/cargo do usuário no sistema';
COMMENT ON COLUMN public.usuarios.tenant_id IS 'ID do tenant/empresa do usuário';
COMMENT ON COLUMN public.usuarios.profile_id IS 'ID do perfil de permissões (RBAC)';
COMMENT ON COLUMN public.usuarios.ativo IS 'Se o usuário está ativo no sistema';
COMMENT ON COLUMN public.usuarios.data_criacao IS 'Data de criação do registro';
COMMENT ON COLUMN public.usuarios.data_atualizacao IS 'Data da última atualização';

-- 7. Create default admin user (OPTIONAL - comment out if not needed)
-- Replace with your Google account details
INSERT INTO public.usuarios (id, google_id, email, nome, tenant_id, ativo, role)
VALUES (
    'ae97014f-af85-413a-9877-27b5b913c271',
    '101996235938930217570',
    'deagle090@gmail.com',
    'Rodrigo Polydoro',
    '00000000-0000-0000-0000-000000000000',
    true,
    'ADMIN'
) ON CONFLICT (google_id) DO NOTHING;

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ Development data initialized successfully!';
    RAISE NOTICE '   - Tenant created: 00000000-0000-0000-0000-000000000000';
    RAISE NOTICE '   - Schema created: tenant_00000000_0000_0000_0000_000000000000';
    RAISE NOTICE '   - Usuarios table created in public schema';
    RAISE NOTICE '   - Admin user created: deagle090@gmail.com';
END $$;
