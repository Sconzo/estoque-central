package com.estoquecentral.marketplace.application;

import com.estoquecentral.auth.adapter.out.TenantRepository;
import com.estoquecentral.auth.domain.Tenant;
import com.estoquecentral.shared.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled worker for processing marketplace stock sync queue
 * Story 5.4: Stock Synchronization to Mercado Livre - AC2
 * Runs every 1 minute to ensure < 5min latency (NFR4)
 */
@Component
public class StockSyncScheduledWorker {

    private static final Logger log = LoggerFactory.getLogger(StockSyncScheduledWorker.class);

    private final MarketplaceStockSyncService syncService;
    private final TenantRepository tenantRepository;

    public StockSyncScheduledWorker(
        MarketplaceStockSyncService syncService,
        TenantRepository tenantRepository
    ) {
        this.syncService = syncService;
        this.tenantRepository = tenantRepository;
    }

    /**
     * AC2: Worker roda a cada 1 minuto
     * Ensures latência < 5min in 95% of cases (NFR4)
     */
    @Scheduled(fixedDelay = 60000) // 1 minute
    public void processStockSyncQueue() {
        try {
            log.debug("Starting stock sync queue processing");

            // Get all active tenants
            List<Tenant> activeTenants = tenantRepository.findAllActive();

            if (activeTenants.isEmpty()) {
                log.debug("No active tenants found");
                return;
            }

            log.debug("Processing sync queue for {} active tenants", activeTenants.size());

            // Process queue for each tenant
            for (Tenant tenant : activeTenants) {
                try {
                    // Set tenant context
                    TenantContext.setTenantId(tenant.getId().toString());

                    // Process sync queue for this tenant
                    syncService.processSyncQueueForTenant(tenant.getId());

                } catch (Exception e) {
                    log.error("Error processing sync queue for tenant: {}", tenant.getId(), e);
                } finally {
                    // Always clear tenant context
                    TenantContext.clear();
                }
            }

            log.debug("Finished stock sync queue processing");
        } catch (Exception e) {
            log.error("Error processing stock sync queue", e);
        }
    }
}
