package com.estoquecentral.marketplace.adapter.out;

import com.estoquecentral.marketplace.domain.MarketplaceListing;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MarketplaceListing entity
 * Story 5.2: Import Products from Mercado Livre
 */
@Repository
public interface MarketplaceListingRepository extends CrudRepository<MarketplaceListing, UUID> {

    /**
     * Find listing by tenant, marketplace and listing ID from marketplace
     */
    @Query("SELECT * FROM marketplace_listings " +
           "WHERE tenant_id = :tenantId " +
           "AND marketplace = CAST(:marketplace AS VARCHAR) " +
           "AND listing_id_marketplace = :listingIdMarketplace")
    Optional<MarketplaceListing> findByTenantIdAndMarketplaceAndListingId(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace,
        @Param("listingIdMarketplace") String listingIdMarketplace
    );

    /**
     * Find all listings for a tenant and marketplace
     */
    @Query("SELECT * FROM marketplace_listings " +
           "WHERE tenant_id = :tenantId " +
           "AND marketplace = CAST(:marketplace AS VARCHAR)")
    List<MarketplaceListing> findByTenantIdAndMarketplace(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace
    );

    /**
     * Find all listings for a specific product
     */
    @Query("SELECT * FROM marketplace_listings WHERE product_id = :productId")
    List<MarketplaceListing> findByProductId(@Param("productId") UUID productId);

    /**
     * Find listing for a specific variant
     */
    @Query("SELECT * FROM marketplace_listings WHERE variant_id = :variantId")
    Optional<MarketplaceListing> findByVariantId(@Param("variantId") UUID variantId);

    /**
     * Check if listing already exists
     */
    @Query("SELECT COUNT(*) > 0 FROM marketplace_listings " +
           "WHERE tenant_id = :tenantId " +
           "AND marketplace = CAST(:marketplace AS VARCHAR) " +
           "AND listing_id_marketplace = :listingIdMarketplace")
    boolean existsByTenantIdAndMarketplaceAndListingId(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace,
        @Param("listingIdMarketplace") String listingIdMarketplace
    );

    /**
     * Find active listings for sync
     */
    @Query("SELECT * FROM marketplace_listings " +
           "WHERE tenant_id = :tenantId " +
           "AND marketplace = CAST(:marketplace AS VARCHAR) " +
           "AND status = 'ACTIVE'")
    List<MarketplaceListing> findActiveByTenantIdAndMarketplace(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace
    );

    /**
     * Check if product is already published to marketplace
     * Story 5.3: Publish Products to Mercado Livre
     */
    @Query("SELECT COUNT(*) > 0 FROM marketplace_listings " +
           "WHERE tenant_id = :tenantId " +
           "AND product_id = :productId " +
           "AND marketplace = CAST(:marketplace AS VARCHAR)")
    boolean existsByTenantIdAndProductIdAndMarketplace(
        @Param("tenantId") UUID tenantId,
        @Param("productId") UUID productId,
        @Param("marketplace") String marketplace
    );
}
