package com.estoquecentral.inventory.adapter.out;

import com.estoquecentral.inventory.domain.Inventory;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * InventoryRepository - Data access for Inventory entity
 *
 * <p>Provides CRUD operations and custom queries for inventory management.
 * Story 2.7: Multi-Warehouse Stock Control with variant support
 *
 * <p><strong>Key Queries:</strong>
 * <ul>
 *   <li>Find by product/variant and location</li>
 *   <li>Find products below minimum quantity</li>
 *   <li>Find products above maximum quantity</li>
 *   <li>Calculate total inventory value</li>
 *   <li>Aggregate inventory across locations</li>
 * </ul>
 *
 * @see Inventory
 */
@Repository
public interface InventoryRepository extends CrudRepository<Inventory, UUID> {

    /**
     * Finds inventory by product ID and location ID
     *
     * @param tenantId tenant ID
     * @param productId product ID
     * @param locationId location ID
     * @return optional inventory
     */
    @Query("""
        SELECT * FROM inventory
        WHERE tenant_id = :tenantId
          AND product_id = :productId
          AND location_id = :locationId
        """)
    Optional<Inventory> findByTenantIdAndProductIdAndLocationId(
        @Param("tenantId") UUID tenantId,
        @Param("productId") UUID productId,
        @Param("locationId") UUID locationId);

    /**
     * Finds inventory by variant ID and location ID
     *
     * @param tenantId tenant ID
     * @param variantId variant ID
     * @param locationId location ID
     * @return optional inventory
     */
    @Query("""
        SELECT * FROM inventory
        WHERE tenant_id = :tenantId
          AND variant_id = :variantId
          AND location_id = :locationId
        """)
    Optional<Inventory> findByTenantIdAndVariantIdAndLocationId(
        @Param("tenantId") UUID tenantId,
        @Param("variantId") UUID variantId,
        @Param("locationId") UUID locationId);

    /**
     * Finds all inventory records for a product (all locations)
     *
     * @param tenantId tenant ID
     * @param productId product ID
     * @return list of inventory records
     */
    @Query("""
        SELECT * FROM inventory
        WHERE tenant_id = :tenantId
          AND product_id = :productId
        ORDER BY location_id
        """)
    List<Inventory> findAllByTenantIdAndProductId(
        @Param("tenantId") UUID tenantId,
        @Param("productId") UUID productId);

    /**
     * Finds all inventory records for a variant (all locations)
     *
     * @param tenantId tenant ID
     * @param variantId variant ID
     * @return list of inventory records
     */
    @Query("""
        SELECT * FROM inventory
        WHERE tenant_id = :tenantId
          AND variant_id = :variantId
        ORDER BY location_id
        """)
    List<Inventory> findAllByTenantIdAndVariantId(
        @Param("tenantId") UUID tenantId,
        @Param("variantId") UUID variantId);

    /**
     * Finds inventory records below minimum quantity (Story 2.7 - AC6)
     *
     * @param tenantId tenant ID
     * @return list of inventory records where quantityForSale < minimumQuantity
     */
    @Query("""
        SELECT i.* FROM inventory i
        WHERE i.tenant_id = :tenantId
          AND i.minimum_quantity IS NOT NULL
          AND i.minimum_quantity > 0
          AND i.quantity_for_sale < i.minimum_quantity
        ORDER BY (i.quantity_for_sale * 100.0 / i.minimum_quantity) ASC
        """)
    List<Inventory> findBelowMinimum(@Param("tenantId") UUID tenantId);

    /**
     * Finds inventory records below minimum quantity for a specific location
     *
     * @param tenantId tenant ID
     * @param locationId location ID
     * @return list of inventory records below minimum at location
     */
    @Query("""
        SELECT i.* FROM inventory i
        WHERE i.tenant_id = :tenantId
          AND i.location_id = :locationId
          AND i.minimum_quantity IS NOT NULL
          AND i.minimum_quantity > 0
          AND i.quantity_for_sale < i.minimum_quantity
        ORDER BY (i.quantity_for_sale * 100.0 / i.minimum_quantity) ASC
        """)
    List<Inventory> findBelowMinimumByLocation(
        @Param("tenantId") UUID tenantId,
        @Param("locationId") UUID locationId);

    /**
     * Finds products with excess stock (quantityAvailable >= maximumQuantity)
     *
     * @param tenantId tenant ID
     * @return list of inventory records above maximum
     */
    @Query("""
        SELECT i.* FROM inventory i
        WHERE i.tenant_id = :tenantId
          AND i.maximum_quantity IS NOT NULL
          AND i.quantity_available >= i.maximum_quantity
        ORDER BY i.quantity_available DESC
        """)
    List<Inventory> findAboveMaximum(@Param("tenantId") UUID tenantId);

    /**
     * Finds products with zero or negative quantity for sale
     *
     * @param tenantId tenant ID
     * @return list of inventory records out of stock
     */
    @Query("""
        SELECT * FROM inventory
        WHERE tenant_id = :tenantId
          AND quantity_for_sale <= 0
        ORDER BY quantity_available ASC
        """)
    List<Inventory> findOutOfStock(@Param("tenantId") UUID tenantId);

    /**
     * Finds inventory by location
     *
     * @param tenantId tenant ID
     * @param locationId location ID
     * @return list of inventory records
     */
    @Query("""
        SELECT * FROM inventory
        WHERE tenant_id = :tenantId
          AND location_id = :locationId
        ORDER BY product_id, variant_id
        """)
    List<Inventory> findByTenantIdAndLocationId(
        @Param("tenantId") UUID tenantId,
        @Param("locationId") UUID locationId);

    /**
     * Finds all inventory for a tenant (all products, all locations)
     *
     * @param tenantId tenant ID
     * @return list of inventory records
     */
    @Query("""
        SELECT * FROM inventory
        WHERE tenant_id = :tenantId
        ORDER BY location_id, product_id, variant_id
        """)
    List<Inventory> findAllByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Counts inventory records below minimum quantity
     *
     * @param tenantId tenant ID
     * @return count of products below minimum
     */
    @Query("""
        SELECT COUNT(*) FROM inventory
        WHERE tenant_id = :tenantId
          AND minimum_quantity IS NOT NULL
          AND minimum_quantity > 0
          AND quantity_for_sale < minimum_quantity
        """)
    long countBelowMinimum(@Param("tenantId") UUID tenantId);

    /**
     * Counts products out of stock
     *
     * @param tenantId tenant ID
     * @return count of products with zero quantity for sale
     */
    @Query("""
        SELECT COUNT(*) FROM inventory
        WHERE tenant_id = :tenantId
          AND quantity_for_sale <= 0
        """)
    long countOutOfStock(@Param("tenantId") UUID tenantId);

    /**
     * Counts inventory records with available stock at a location
     *
     * @param locationId location ID
     * @return count of inventory with quantity_available > 0
     */
    @Query("""
        SELECT COUNT(*) FROM inventory
        WHERE location_id = :locationId
          AND quantity_available > 0
        """)
    long countByLocationIdWithAvailableStock(@Param("locationId") UUID locationId);

    /**
     * Gets total inventory value by location
     * Calculates value for both simple products and variants
     *
     * @param tenantId tenant ID
     * @param locationId location ID
     * @return total inventory value
     */
    @Query("""
        SELECT COALESCE(SUM(i.quantity_available * COALESCE(pv.cost, p.cost)), 0)
        FROM inventory i
        LEFT JOIN products p ON i.product_id = p.id
        LEFT JOIN product_variants pv ON i.variant_id = pv.id
        WHERE i.tenant_id = :tenantId
          AND i.location_id = :locationId
          AND (p.cost IS NOT NULL OR pv.cost IS NOT NULL)
        """)
    Double getTotalInventoryValueByLocation(
        @Param("tenantId") UUID tenantId,
        @Param("locationId") UUID locationId);

    /**
     * Gets total inventory value for tenant (all locations)
     *
     * @param tenantId tenant ID
     * @return total inventory value
     */
    @Query("""
        SELECT COALESCE(SUM(i.quantity_available * COALESCE(pv.cost, p.cost)), 0)
        FROM inventory i
        LEFT JOIN products p ON i.product_id = p.id
        LEFT JOIN product_variants pv ON i.variant_id = pv.id
        WHERE i.tenant_id = :tenantId
          AND (p.cost IS NOT NULL OR pv.cost IS NOT NULL)
        """)
    Double getTotalInventoryValue(@Param("tenantId") UUID tenantId);

    /**
     * Checks if inventory exists for product and location
     *
     * @param tenantId tenant ID
     * @param productId product ID
     * @param locationId location ID
     * @return true if exists
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM inventory
            WHERE tenant_id = :tenantId
              AND product_id = :productId
              AND location_id = :locationId
        )
        """)
    boolean existsByTenantIdAndProductIdAndLocationId(
        @Param("tenantId") UUID tenantId,
        @Param("productId") UUID productId,
        @Param("locationId") UUID locationId);

    /**
     * Checks if inventory exists for variant and location
     *
     * @param tenantId tenant ID
     * @param variantId variant ID
     * @param locationId location ID
     * @return true if exists
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM inventory
            WHERE tenant_id = :tenantId
              AND variant_id = :variantId
              AND location_id = :locationId
        )
        """)
    boolean existsByTenantIdAndVariantIdAndLocationId(
        @Param("tenantId") UUID tenantId,
        @Param("variantId") UUID variantId,
        @Param("locationId") UUID locationId);
}
