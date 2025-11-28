package com.estoquecentral.marketplace.adapter.out;

import com.estoquecentral.marketplace.domain.SafetyMarginRule;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SafetyMarginRule
 * Story 5.7: Configurable Safety Stock Margin
 */
@Repository
public interface SafetyMarginRuleRepository extends CrudRepository<SafetyMarginRule, UUID> {

    /**
     * Find all rules for a tenant and marketplace, ordered by priority (highest first)
     */
    @Query("SELECT * FROM safety_margin_rules WHERE tenant_id = :tenantId " +
           "AND marketplace = :marketplace ORDER BY priority ASC")
    List<SafetyMarginRule> findByTenantIdAndMarketplace(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace
    );

    /**
     * Find product-specific rule (priority=1)
     */
    @Query("SELECT * FROM safety_margin_rules WHERE tenant_id = :tenantId " +
           "AND marketplace = :marketplace AND product_id = :productId AND priority = 1")
    Optional<SafetyMarginRule> findProductRule(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace,
        @Param("productId") UUID productId
    );

    /**
     * Find category-specific rule (priority=2)
     */
    @Query("SELECT * FROM safety_margin_rules WHERE tenant_id = :tenantId " +
           "AND marketplace = :marketplace AND category_id = :categoryId AND priority = 2")
    Optional<SafetyMarginRule> findCategoryRule(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace,
        @Param("categoryId") UUID categoryId
    );

    /**
     * Find global marketplace rule (priority=3)
     */
    @Query("SELECT * FROM safety_margin_rules WHERE tenant_id = :tenantId " +
           "AND marketplace = :marketplace AND priority = 3")
    Optional<SafetyMarginRule> findGlobalRule(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace
    );

    /**
     * Find all rules for a tenant (for listing in UI)
     */
    @Query("SELECT * FROM safety_margin_rules WHERE tenant_id = :tenantId " +
           "ORDER BY priority ASC, data_criacao DESC")
    List<SafetyMarginRule> findAllByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Find rules by marketplace filter (for UI filtering)
     */
    @Query("SELECT * FROM safety_margin_rules WHERE tenant_id = :tenantId " +
           "AND marketplace = :marketplace ORDER BY priority ASC, data_criacao DESC")
    List<SafetyMarginRule> findByTenantIdAndMarketplaceForListing(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace
    );

    /**
     * Check if a rule exists for specific scope
     */
    @Query("SELECT EXISTS(SELECT 1 FROM safety_margin_rules WHERE tenant_id = :tenantId " +
           "AND marketplace = :marketplace AND product_id = :productId)")
    boolean existsProductRule(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace,
        @Param("productId") UUID productId
    );

    /**
     * Check if a category rule exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM safety_margin_rules WHERE tenant_id = :tenantId " +
           "AND marketplace = :marketplace AND category_id = :categoryId)")
    boolean existsCategoryRule(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace,
        @Param("categoryId") UUID categoryId
    );

    /**
     * Check if a global rule exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM safety_margin_rules WHERE tenant_id = :tenantId " +
           "AND marketplace = :marketplace AND product_id IS NULL AND category_id IS NULL)")
    boolean existsGlobalRule(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace
    );

    /**
     * Find all products affected by a rule deletion (for re-sync trigger)
     * Returns product IDs that would be affected when a rule is deleted
     */
    @Query("SELECT DISTINCT ml.product_id FROM marketplace_listings ml " +
           "WHERE ml.tenant_id = :tenantId AND ml.marketplace = :marketplace " +
           "AND (:categoryId IS NULL OR ml.product_id IN (" +
           "    SELECT p.id FROM products p WHERE p.category_id = :categoryId" +
           "))")
    List<UUID> findAffectedProducts(
        @Param("tenantId") UUID tenantId,
        @Param("marketplace") String marketplace,
        @Param("categoryId") UUID categoryId
    );
}
