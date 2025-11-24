package com.estoquecentral.sales.adapter.out;

import com.estoquecentral.sales.domain.SaleItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * SaleItemRepository - Repository for SaleItem entity
 * Story 4.3: NFCe Emission and Stock Decrease
 */
@Repository
public interface SaleItemRepository extends CrudRepository<SaleItem, UUID> {
    List<SaleItem> findBySaleId(UUID saleId);
}
