package com.estoquecentral.sales.adapter.out;

import com.estoquecentral.sales.domain.SalesOrderItem;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * SalesOrderItemRepository - Data access for sales order items
 * Story 4.5: Sales Order B2B Interface
 */
@Repository
public interface SalesOrderItemRepository extends CrudRepository<SalesOrderItem, UUID> {

    /**
     * Find all items for a sales order
     */
    @Query("""
        SELECT * FROM sales_order_items
        WHERE sales_order_id = :salesOrderId
        ORDER BY created_at
        """)
    List<SalesOrderItem> findBySalesOrderId(@Param("salesOrderId") UUID salesOrderId);

    /**
     * Find items by product
     */
    @Query("""
        SELECT * FROM sales_order_items
        WHERE product_id = :productId
        ORDER BY created_at DESC
        """)
    List<SalesOrderItem> findByProductId(@Param("productId") UUID productId);

    /**
     * Find items by variant
     */
    @Query("""
        SELECT * FROM sales_order_items
        WHERE variant_id = :variantId
        ORDER BY created_at DESC
        """)
    List<SalesOrderItem> findByVariantId(@Param("variantId") UUID variantId);

    /**
     * Delete all items for a sales order
     */
    @Modifying
    @Query("""
        DELETE FROM sales_order_items
        WHERE sales_order_id = :salesOrderId
        """)
    void deleteBySalesOrderId(@Param("salesOrderId") UUID salesOrderId);

    /**
     * Count items in a sales order
     */
    @Query("""
        SELECT COUNT(*) FROM sales_order_items
        WHERE sales_order_id = :salesOrderId
        """)
    long countBySalesOrderId(@Param("salesOrderId") UUID salesOrderId);
}
