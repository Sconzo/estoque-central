package com.estoquecentral.purchasing.adapter.out;

import com.estoquecentral.purchasing.domain.ReceivingItem;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ReceivingItemRepository - Data access for receiving items
 * Story 3.4: Receiving Processing and Weighted Average Cost Update
 */
@Repository
public interface ReceivingItemRepository extends CrudRepository<ReceivingItem, UUID> {

    /**
     * Find all items for a receiving
     */
    @Query("""
        SELECT * FROM receiving_items
        WHERE receiving_id = :receivingId
        ORDER BY id
        """)
    List<ReceivingItem> findByReceivingId(@Param("receivingId") UUID receivingId);

    /**
     * Find items by purchase order item
     */
    @Query("""
        SELECT * FROM receiving_items
        WHERE purchase_order_item_id = :purchaseOrderItemId
        ORDER BY id
        """)
    List<ReceivingItem> findByPurchaseOrderItemId(@Param("purchaseOrderItemId") UUID purchaseOrderItemId);
}
