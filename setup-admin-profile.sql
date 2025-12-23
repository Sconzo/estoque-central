-- ============================================================================
-- Script: Setup Admin Profile
-- ============================================================================
-- Purpose: Creates an "Administrador" profile with ADMIN role and assigns
--          it to a user by email
--
-- IMPORTANT: Before running, update the email below (line 19)
--
-- Usage:
--   Using Docker:
--     docker exec -i estoque-central-postgres psql -U postgres -d estoque_central < setup-admin-profile.sql
--
--   Using psql directly:
--     psql -h localhost -p 5433 -U postgres -d estoque_central -f setup-admin-profile.sql
-- ============================================================================

DO $$
DECLARE
    v_tenant_id UUID;
    v_user_id UUID;
    v_profile_id UUID;
    v_admin_role_id UUID;
    -- ⚠️ ALTERE O EMAIL ABAIXO COM O SEU EMAIL DE LOGIN ⚠️
    v_user_email VARCHAR := 'usuario@exemplo.com'; -- ← ALTERE AQUI
BEGIN
    -- ========================================================================
    -- Step 1: Find the user by email
    -- ========================================================================
    RAISE NOTICE 'Searching for user with email: %', v_user_email;

    -- Get user from public.usuarios (OAuth users)
    SELECT id, tenant_id INTO v_user_id, v_tenant_id
    FROM public.usuarios
    WHERE email = v_user_email
    LIMIT 1;

    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'User not found with email: %. Please update the email in the script.', v_user_email;
    END IF;

    RAISE NOTICE 'Found user: % (tenant: %)', v_user_id, v_tenant_id;

    -- ========================================================================
    -- Step 2: Check if "Administrador" profile already exists for this tenant
    -- ========================================================================
    SELECT id INTO v_profile_id
    FROM public.profiles
    WHERE tenant_id = v_tenant_id
      AND nome = 'Administrador'
    LIMIT 1;

    IF v_profile_id IS NOT NULL THEN
        RAISE NOTICE 'Profile "Administrador" already exists: %', v_profile_id;
    ELSE
        -- ====================================================================
        -- Step 3: Create "Administrador" profile
        -- ====================================================================
        INSERT INTO public.profiles (tenant_id, nome, descricao, ativo)
        VALUES (
            v_tenant_id,
            'Administrador',
            'Perfil com acesso total ao sistema (todas as permissões)',
            true
        )
        RETURNING id INTO v_profile_id;

        RAISE NOTICE 'Created new profile "Administrador": %', v_profile_id;
    END IF;

    -- ========================================================================
    -- Step 4: Get ADMIN role ID
    -- ========================================================================
    SELECT id INTO v_admin_role_id
    FROM public.roles
    WHERE nome = 'ADMIN'
    LIMIT 1;

    IF v_admin_role_id IS NULL THEN
        RAISE EXCEPTION 'ADMIN role not found. Please run migrations first.';
    END IF;

    RAISE NOTICE 'Found ADMIN role: %', v_admin_role_id;

    -- ========================================================================
    -- Step 5: Assign ADMIN role to profile (if not already assigned)
    -- ========================================================================
    IF NOT EXISTS (
        SELECT 1 FROM public.profile_roles
        WHERE profile_id = v_profile_id
          AND role_id = v_admin_role_id
    ) THEN
        INSERT INTO public.profile_roles (profile_id, role_id)
        VALUES (v_profile_id, v_admin_role_id);

        RAISE NOTICE 'Assigned ADMIN role to profile';
    ELSE
        RAISE NOTICE 'ADMIN role already assigned to profile';
    END IF;

    -- ========================================================================
    -- Step 6: Update user's profile_id
    -- ========================================================================
    UPDATE public.usuarios
    SET profile_id = v_profile_id
    WHERE id = v_user_id;

    RAISE NOTICE 'Updated user profile_id to: %', v_profile_id;

    -- ========================================================================
    -- Step 7: Verify assignment in tenant schema
    -- ========================================================================
    -- Check if user exists in tenant schema too
    DECLARE
        v_tenant_schema VARCHAR := 'tenant_' || REPLACE(v_tenant_id::TEXT, '-', '');
        v_sql TEXT;
    BEGIN
        v_sql := format('
            UPDATE %I.usuarios
            SET profile_id = %L
            WHERE email = %L
        ', v_tenant_schema, v_profile_id, v_user_email);

        EXECUTE v_sql;
        RAISE NOTICE 'Updated user in tenant schema: %', v_tenant_schema;
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Could not update tenant schema (may not exist): %', SQLERRM;
    END;

    -- ========================================================================
    -- Success!
    -- ========================================================================
    RAISE NOTICE '';
    RAISE NOTICE '✓ SUCCESS! User % now has Administrador profile with ADMIN role', v_user_email;
    RAISE NOTICE '✓ You can now access the user management pages at /usuarios/profiles';
    RAISE NOTICE '';

END $$;
