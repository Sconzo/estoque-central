-- Migration V061: Create marketplace_listings table
-- Story 5.2: Import Products from Mercado Livre - AC1
-- Maps local products/variants to marketplace listings (anúncios)

CREATE TABLE marketplace_listings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    product_id UUID NOT NULL,
    variant_id UUID,
    marketplace VARCHAR(50) NOT NULL,
    listing_id_marketplace VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    price NUMERIC(15, 2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_sync_at TIMESTAMP,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_marketplace_listing UNIQUE (tenant_id, marketplace, listing_id_marketplace)
);

-- Indexes for performance
CREATE INDEX idx_ml_tenant_id ON marketplace_listings(tenant_id);
CREATE INDEX idx_ml_product_id ON marketplace_listings(product_id);
CREATE INDEX idx_ml_variant_id ON marketplace_listings(variant_id);
CREATE INDEX idx_ml_marketplace ON marketplace_listings(marketplace);
CREATE INDEX idx_ml_listing_id ON marketplace_listings(listing_id_marketplace);
CREATE INDEX idx_ml_status ON marketplace_listings(status);

-- Foreign key constraints
-- Note: Spring Data JDBC doesn't require explicit FKs, but adding for data integrity
-- Uncomment if strict referential integrity is needed
-- ALTER TABLE marketplace_listings ADD CONSTRAINT fk_ml_product
--     FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

-- Comments
COMMENT ON TABLE marketplace_listings IS 'Maps products/variants to marketplace listings (ML, Shopee, etc)';
COMMENT ON COLUMN marketplace_listings.listing_id_marketplace IS 'Listing ID from marketplace (e.g., MLB123456789)';
COMMENT ON COLUMN marketplace_listings.variant_id IS 'NULL for simple products, variant UUID for products with variations';
COMMENT ON COLUMN marketplace_listings.status IS 'Listing status: ACTIVE, PAUSED, CLOSED, ERROR';
