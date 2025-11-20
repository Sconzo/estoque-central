package com.estoquecentral.catalog.adapter.out;

import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductStatus;
import com.estoquecentral.catalog.domain.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ProductRepository - Data access for Product entity
 *
 * <p>Provides CRUD operations and custom queries for product management.
 * Uses Spring Data JDBC with pagination support.
 *
 * <p><strong>Key Features:</strong>
 * <ul>
 *   <li>Pagination support for large product lists</li>
 *   <li>Full-text search by name, SKU, barcode</li>
 *   <li>Filtering by category (including subcategories)</li>
 *   <li>SKU and barcode uniqueness validation</li>
 * </ul>
 *
 * @see Product
 */
@Repository
public interface ProductRepository extends CrudRepository<Product, UUID>,
        PagingAndSortingRepository<Product, UUID> {

    /**
     * Finds all active products with pagination
     *
     * @param pageable pagination parameters
     * @return page of active products
     */
    Page<Product> findByAtivoTrue(Pageable pageable);

    /**
     * Finds product by ID and active status
     *
     * @param id product ID
     * @return optional product
     */
    @Query("""
        SELECT * FROM products
        WHERE id = :id AND ativo = true
        """)
    Optional<Product> findByIdAndActive(@Param("id") UUID id);

    /**
     * Finds product by SKU (tenant-scoped)
     *
     * @param tenantId tenant ID
     * @param sku product SKU
     * @return optional product
     */
    @Query("""
        SELECT * FROM products
        WHERE tenant_id = :tenantId AND sku = :sku AND ativo = true
        """)
    Optional<Product> findByTenantIdAndSku(@Param("tenantId") UUID tenantId,
                                            @Param("sku") String sku);

    /**
     * Finds product by barcode (tenant-scoped)
     *
     * @param tenantId tenant ID
     * @param barcode product barcode
     * @return optional product
     */
    @Query("""
        SELECT * FROM products
        WHERE tenant_id = :tenantId AND barcode = :barcode AND ativo = true
        """)
    Optional<Product> findByTenantIdAndBarcode(@Param("tenantId") UUID tenantId,
                                                @Param("barcode") String barcode);

    /**
     * Searches products by name, SKU, or barcode (case-insensitive)
     * Returns List instead of Page due to Spring Data JDBC limitations
     *
     * @param query search query
     * @return list of matching products
     */
    @Query("""
        SELECT * FROM products
        WHERE ativo = true
          AND (
            LOWER(name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(sku) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(barcode) LIKE LOWER(CONCAT('%', :query, '%'))
          )
        ORDER BY name
        """)
    List<Product> search(@Param("query") String query);

    /**
     * Finds products by category ID with pagination
     *
     * @param categoryId category ID
     * @param pageable pagination parameters
     * @return page of products in category
     */
    Page<Product> findByCategoryIdAndAtivoTrue(UUID categoryId, Pageable pageable);

    /**
     * Finds products by category ID and all its descendants
     * Uses recursive CTE to traverse category tree
     * Returns List instead of Page due to Spring Data JDBC limitations
     *
     * @param categoryId root category ID
     * @return list of products in category and subcategories
     */
    @Query("""
        WITH RECURSIVE category_tree AS (
            SELECT id FROM categories WHERE id = :categoryId
            UNION ALL
            SELECT c.id FROM categories c
            INNER JOIN category_tree ct ON c.parent_id = ct.id
            WHERE c.ativo = true
        )
        SELECT p.* FROM products p
        WHERE p.category_id IN (SELECT id FROM category_tree)
          AND p.ativo = true
        ORDER BY p.name
        """)
    List<Product> findByCategoryIdIncludingDescendants(@Param("categoryId") UUID categoryId);

    /**
     * Finds products by status with pagination
     *
     * @param status product status
     * @param pageable pagination parameters
     * @return page of products with status
     */
    Page<Product> findByStatusAndAtivoTrue(ProductStatus status, Pageable pageable);

    /**
     * Finds products by type with pagination
     *
     * @param type product type
     * @param pageable pagination parameters
     * @return page of products with type
     */
    Page<Product> findByTypeAndAtivoTrue(ProductType type, Pageable pageable);

    /**
     * Checks if SKU exists for another product (used for validation on update)
     *
     * @param tenantId tenant ID
     * @param sku SKU to check
     * @param excludeId product ID to exclude from check
     * @return true if SKU exists for another product
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM products
            WHERE tenant_id = :tenantId
              AND sku = :sku
              AND id != :excludeId
              AND ativo = true
        )
        """)
    boolean existsByTenantIdAndSkuExcludingId(@Param("tenantId") UUID tenantId,
                                                @Param("sku") String sku,
                                                @Param("excludeId") UUID excludeId);

    /**
     * Checks if barcode exists for another product (used for validation on update)
     *
     * @param tenantId tenant ID
     * @param barcode barcode to check
     * @param excludeId product ID to exclude from check
     * @return true if barcode exists for another product
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM products
            WHERE tenant_id = :tenantId
              AND barcode = :barcode
              AND id != :excludeId
              AND ativo = true
        )
        """)
    boolean existsByTenantIdAndBarcodeExcludingId(@Param("tenantId") UUID tenantId,
                                                    @Param("barcode") String barcode,
                                                    @Param("excludeId") UUID excludeId);

    /**
     * Counts active products
     *
     * @return count of active products
     */
    @Query("""
        SELECT COUNT(*) FROM products WHERE ativo = true
        """)
    long countActive();

    /**
     * Counts products by category (direct children only)
     *
     * @param categoryId category ID
     * @return count of products in category
     */
    @Query("""
        SELECT COUNT(*) FROM products
        WHERE category_id = :categoryId AND ativo = true
        """)
    long countByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * Counts products by status
     *
     * @param status product status
     * @return count of products with status
     */
    @Query("""
        SELECT COUNT(*) FROM products
        WHERE status = :status AND ativo = true
        """)
    long countByStatus(@Param("status") String status);
}
