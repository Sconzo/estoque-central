-- ============================================================================
-- Migration: V005 - Create Categories Table (Hierarchical Tree)
-- Epic: 2 - Product Catalog & Inventory Foundation
-- Story: 2.1 - Hierarchical Product Categories
-- ============================================================================
-- Description:
--   Creates categories table with self-referencing parent_id for unlimited
--   hierarchy depth (tree structure). Each tenant has isolated categories.
--
-- Schema: TENANT-SPECIFIC (runs per tenant schema)
-- ============================================================================

-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    -- Primary key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Category data
    name VARCHAR(100) NOT NULL,
    description TEXT,

    -- Hierarchical structure (self-reference)
    parent_id UUID REFERENCES categories(id) ON DELETE CASCADE,

    -- Status
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID, -- FK to usuarios (not enforced to avoid circular dependency)
    updated_by UUID,

    -- Constraints
    CONSTRAINT unique_category_name_per_parent UNIQUE (name, parent_id)
);

-- Create indexes for performance
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_ativo ON categories(ativo);
CREATE INDEX idx_categories_name ON categories(name);

-- Create index for full-text search on name
CREATE INDEX idx_categories_name_trgm ON categories USING gin(name gin_trgm_ops);

-- Comments
COMMENT ON TABLE categories IS 'Product categories with hierarchical tree structure (self-referencing parent_id)';
COMMENT ON COLUMN categories.id IS 'Primary key (UUID)';
COMMENT ON COLUMN categories.name IS 'Category name (must be unique within same parent)';
COMMENT ON COLUMN categories.description IS 'Optional description of the category';
COMMENT ON COLUMN categories.parent_id IS 'Self-reference to parent category (NULL for root categories)';
COMMENT ON COLUMN categories.ativo IS 'Soft delete flag (true=active, false=inactive)';
COMMENT ON COLUMN categories.created_at IS 'Timestamp when category was created';
COMMENT ON COLUMN categories.updated_at IS 'Timestamp when category was last updated';
COMMENT ON COLUMN categories.created_by IS 'User ID who created the category';
COMMENT ON COLUMN categories.updated_by IS 'User ID who last updated the category';

-- Insert sample root categories for testing/demo
INSERT INTO categories (id, name, description, parent_id, ativo)
VALUES
    (gen_random_uuid(), 'Eletrônicos', 'Produtos eletrônicos e tecnologia', NULL, true),
    (gen_random_uuid(), 'Alimentos e Bebidas', 'Produtos alimentícios e bebidas', NULL, true),
    (gen_random_uuid(), 'Vestuário', 'Roupas, calçados e acessórios', NULL, true),
    (gen_random_uuid(), 'Casa e Decoração', 'Móveis e itens de decoração', NULL, true)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- Notes:
-- 1. Categories are tenant-specific (each tenant schema has its own)
-- 2. Unlimited depth hierarchy (category can have subcategory of subcategory...)
-- 3. ON DELETE CASCADE ensures deleting parent removes all children
-- 4. UNIQUE constraint on (name, parent_id) allows same name in different branches
--    Example: Both "Notebooks" and "Bebidas" can have child "Importados"
-- 5. Soft delete (ativo flag) preserves data for auditing
-- 6. pg_trgm index for fast fuzzy text search on category names
-- ============================================================================
