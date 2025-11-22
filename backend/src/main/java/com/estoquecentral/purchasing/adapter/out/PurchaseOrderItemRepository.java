package com.estoquecentral.purchasing.adapter.out;

import com.estoquecentral.purchasing.domain.PurchaseOrderItem;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * PurchaseOrderItemRepository - Data access for purchase order items
 * Story 3.2: Purchase Order Creation
 */
@Repository
public interface PurchaseOrderItemRepository extends CrudRepository<PurchaseOrderItem, UUID> {

    /**
     * Find all items for a purchase order
     */
    @Query("""
        SELECT * FROM purchase_order_items
        WHERE purchase_order_id = :purchaseOrderId
        ORDER BY created_at
        """)
    List<PurchaseOrderItem> findByPurchaseOrderId(@Param("purchaseOrderId") UUID purchaseOrderId);

    /**
     * Find all items for a product across all purchase orders
     */
    @Query("""
        SELECT * FROM purchase_order_items
        WHERE tenant_id = :tenantId
          AND product_id = :productId
        ORDER BY created_at DESC
        """)
    List<PurchaseOrderItem> findByTenantIdAndProductId(
        @Param("tenantId") UUID tenantId,
        @Param("productId") UUID productId
    );

    /**
     * Find items by product variant
     */
    @Query("""
        SELECT * FROM purchase_order_items
        WHERE tenant_id = :tenantId
          AND product_variant_id = :variantId
        ORDER BY created_at DESC
        """)
    List<PurchaseOrderItem> findByTenantIdAndProductVariantId(
        @Param("tenantId") UUID tenantId,
        @Param("variantId") UUID variantId
    );

    /**
     * Delete all items for a purchase order
     */
    @Query("""
        DELETE FROM purchase_order_items
        WHERE purchase_order_id = :purchaseOrderId
        """)
    void deleteByPurchaseOrderId(@Param("purchaseOrderId") UUID purchaseOrderId);

    /**
     * Count items in a purchase order
     */
    @Query("""
        SELECT COUNT(*) FROM purchase_order_items
        WHERE purchase_order_id = :purchaseOrderId
        """)
    long countByPurchaseOrderId(@Param("purchaseOrderId") UUID purchaseOrderId);
}
