package com.estoquecentral.sales.adapter.out;

import com.estoquecentral.sales.domain.SaleItem;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Sale Item Repository
 * Story 4.3: NFCe Emission and Stock Decrease
 */
@Repository
public interface SaleItemRepository extends CrudRepository<SaleItem, UUID> {

    @Query("SELECT * FROM sale_items WHERE sale_id = :saleId")
    List<SaleItem> findBySaleId(@Param("saleId") UUID saleId);

    @Query("SELECT * FROM sale_items WHERE product_id = :productId")
    List<SaleItem> findByProductId(@Param("productId") UUID productId);

    @Query("DELETE FROM sale_items WHERE sale_id = :saleId")
    void deleteBySaleId(@Param("saleId") UUID saleId);
}
