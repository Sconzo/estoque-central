package com.estoquecentral.sales.adapter.out;

import com.estoquecentral.sales.domain.FiscalEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * FiscalEventRepository - Repository for FiscalEvent entity
 * Story 4.3: NFCe Emission and Stock Decrease
 * NFR16: 5-year retention for fiscal compliance
 */
@Repository
public interface FiscalEventRepository extends CrudRepository<FiscalEvent, UUID> {
    List<FiscalEvent> findBySaleIdOrderByTimestampDesc(UUID saleId);
    List<FiscalEvent> findByTenantIdOrderByTimestampDesc(UUID tenantId);
}
