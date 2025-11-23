package com.estoquecentral.sales.adapter.out;

import com.estoquecentral.sales.domain.FiscalEvent;
import com.estoquecentral.sales.domain.FiscalEventType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Fiscal Event Repository
 * Story 4.3: NFCe Emission and Stock Decrease - NFR16
 */
@Repository
public interface FiscalEventRepository extends CrudRepository<FiscalEvent, UUID> {

    @Query("SELECT * FROM fiscal_events WHERE tenant_id = :tenantId AND sale_id = :saleId ORDER BY timestamp DESC")
    List<FiscalEvent> findByTenantIdAndSaleId(@Param("tenantId") UUID tenantId, @Param("saleId") UUID saleId);

    @Query("SELECT * FROM fiscal_events WHERE tenant_id = :tenantId AND event_type = :eventType::varchar ORDER BY timestamp DESC")
    List<FiscalEvent> findByTenantIdAndEventType(@Param("tenantId") UUID tenantId, @Param("eventType") String eventType);

    @Query("SELECT * FROM fiscal_events WHERE tenant_id = :tenantId AND nfce_key = :nfceKey ORDER BY timestamp DESC")
    List<FiscalEvent> findByTenantIdAndNfceKey(@Param("tenantId") UUID tenantId, @Param("nfceKey") String nfceKey);
}
