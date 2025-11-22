package com.estoquecentral.catalog.adapter.out;

import com.estoquecentral.catalog.domain.ProductComponent;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ProductComponentRepository - Data access for product components (BOM)
 *
 * <p>Provides methods to manage Bill of Materials relationships between products.
 */
@Repository
public interface ProductComponentRepository extends CrudRepository<ProductComponent, UUID> {

    /**
     * Finds all components for a composite product
     *
     * @param productId the composite product ID
     * @return list of components
     */
    @Query("SELECT * FROM product_components WHERE product_id = :productId ORDER BY created_at")
    List<ProductComponent> findByProductId(@Param("productId") UUID productId);

    /**
     * Finds specific component in a product
     *
     * @param productId product ID
     * @param componentProductId component product ID
     * @return optional component
     */
    @Query("SELECT * FROM product_components WHERE product_id = :productId AND component_product_id = :componentProductId")
    Optional<ProductComponent> findByProductIdAndComponentProductId(
            @Param("productId") UUID productId,
            @Param("componentProductId") UUID componentProductId);

    /**
     * Deletes specific component from a product
     *
     * @param productId product ID
     * @param componentProductId component product ID
     */
    @Query("DELETE FROM product_components WHERE product_id = :productId AND component_product_id = :componentProductId")
    void deleteByProductIdAndComponentProductId(
            @Param("productId") UUID productId,
            @Param("componentProductId") UUID componentProductId);

    /**
     * Deletes all components of a product
     *
     * @param productId product ID
     */
    @Query("DELETE FROM product_components WHERE product_id = :productId")
    void deleteByProductId(@Param("productId") UUID productId);

    /**
     * Counts how many components a product has
     *
     * @param productId product ID
     * @return count of components
     */
    @Query("SELECT COUNT(*) FROM product_components WHERE product_id = :productId")
    long countByProductId(@Param("productId") UUID productId);
}
