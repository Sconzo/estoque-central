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
 *
 * <p><strong>Key Queries:</strong>
 * <ul>
 *   <li>Find by product and location</li>
 *   <li>Find products with low stock</li>
 *   <li>Find products with excess stock</li>
 *   <li>Calculate total inventory value</li>
 * </ul>
 *
 * @see Inventory
 */
@Repository
public interface InventoryRepository extends CrudRepository<Inventory, UUID> {

    /**
     * Finds inventory by product ID and location
     *
     * @param productId product ID
     * @param location location
     * @return optional inventory
     */
    @Query("""
        SELECT * FROM inventory
        WHERE product_id = :productId AND location = :location
        """)
    Optional<Inventory> findByProductIdAndLocation(@Param("productId") UUID productId,
                                                     @Param("location") String location);

    /**
     * Finds all inventory records for a product (all locations)
     *
     * @param productId product ID
     * @return list of inventory records
     */
    @Query("""
        SELECT * FROM inventory
        WHERE product_id = :productId
        ORDER BY location
        """)
    List<Inventory> findAllByProductId(@Param("productId") UUID productId);

    /**
     * Finds products with low stock (quantity <= min_quantity)
     *
     * @return list of inventory records below minimum
     */
    @Query("""
        SELECT i.* FROM inventory i
        WHERE i.min_quantity IS NOT NULL
          AND i.quantity <= i.min_quantity
        ORDER BY i.quantity ASC
        """)
    List<Inventory> findLowStockProducts();

    /**
     * Finds products with excess stock (quantity >= max_quantity)
     *
     * @return list of inventory records above maximum
     */
    @Query("""
        SELECT i.* FROM inventory i
        WHERE i.max_quantity IS NOT NULL
          AND i.quantity >= i.max_quantity
        ORDER BY i.quantity DESC
        """)
    List<Inventory> findExcessStockProducts();

    /**
     * Finds products with zero or negative available quantity
     *
     * @return list of inventory records out of stock
     */
    @Query("""
        SELECT * FROM inventory
        WHERE available_quantity <= 0
        ORDER BY quantity ASC
        """)
    List<Inventory> findOutOfStockProducts();

    /**
     * Finds inventory by location
     *
     * @param location location
     * @return list of inventory records
     */
    @Query("""
        SELECT * FROM inventory
        WHERE location = :location
        ORDER BY product_id
        """)
    List<Inventory> findByLocation(@Param("location") String location);

    /**
     * Finds all distinct locations
     *
     * @return list of location names
     */
    @Query("""
        SELECT DISTINCT location FROM inventory
        ORDER BY location
        """)
    List<String> findAllLocations();

    /**
     * Counts products with low stock
     *
     * @return count of products below minimum
     */
    @Query("""
        SELECT COUNT(*) FROM inventory
        WHERE min_quantity IS NOT NULL
          AND quantity <= min_quantity
        """)
    long countLowStockProducts();

    /**
     * Counts products out of stock
     *
     * @return count of products with zero available quantity
     */
    @Query("""
        SELECT COUNT(*) FROM inventory
        WHERE available_quantity <= 0
        """)
    long countOutOfStockProducts();

    /**
     * Gets total inventory value by location
     * Requires JOIN with products table to get cost
     *
     * @param location location
     * @return total inventory value
     */
    @Query("""
        SELECT COALESCE(SUM(i.quantity * p.cost), 0)
        FROM inventory i
        INNER JOIN products p ON i.product_id = p.id
        WHERE i.location = :location
          AND p.cost IS NOT NULL
        """)
    Double getTotalInventoryValueByLocation(@Param("location") String location);

    /**
     * Gets total inventory value (all locations)
     *
     * @return total inventory value
     */
    @Query("""
        SELECT COALESCE(SUM(i.quantity * p.cost), 0)
        FROM inventory i
        INNER JOIN products p ON i.product_id = p.id
        WHERE p.cost IS NOT NULL
        """)
    Double getTotalInventoryValue();

    /**
     * Gets inventory with product details (enriched view)
     *
     * @param productId product ID
     * @param location location
     * @return optional inventory with product details
     */
    @Query("""
        SELECT i.*
        FROM inventory i
        INNER JOIN products p ON i.product_id = p.id
        WHERE i.product_id = :productId
          AND i.location = :location
          AND p.ativo = true
        """)
    Optional<Inventory> findByProductIdAndLocationWithProduct(@Param("productId") UUID productId,
                                                                @Param("location") String location);

    /**
     * Checks if inventory exists for product and location
     *
     * @param productId product ID
     * @param location location
     * @return true if exists
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM inventory
            WHERE product_id = :productId AND location = :location
        )
        """)
    boolean existsByProductIdAndLocation(@Param("productId") UUID productId,
                                          @Param("location") String location);
}
