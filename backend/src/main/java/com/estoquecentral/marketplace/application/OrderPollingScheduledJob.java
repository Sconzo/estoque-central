package com.estoquecentral.marketplace.application;

import com.estoquecentral.auth.adapter.out.TenantRepository;
import com.estoquecentral.auth.domain.Tenant;
import com.estoquecentral.marketplace.adapter.out.MarketplaceConnectionRepository;
import com.estoquecentral.marketplace.domain.ConnectionStatus;
import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.MarketplaceConnection;
import com.estoquecentral.shared.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled job for polling Mercado Livre orders
 * Story 5.5: Import and Process Orders from Mercado Livre - AC3
 *
 * Alternative to webhooks for fetching new orders
 * Runs periodically to check for new orders from ML
 */
@Component
public class OrderPollingScheduledJob {

    private static final Logger log = LoggerFactory.getLogger(OrderPollingScheduledJob.class);

    private final MarketplaceConnectionRepository connectionRepository;
    private final MercadoLivreOrderImportService orderImportService;
    private final TenantRepository tenantRepository;

    public OrderPollingScheduledJob(
        MarketplaceConnectionRepository connectionRepository,
        MercadoLivreOrderImportService orderImportService,
        TenantRepository tenantRepository
    ) {
        this.connectionRepository = connectionRepository;
        this.orderImportService = orderImportService;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Poll for new orders every 10 minutes
     * Note: In production, webhooks are preferred over polling
     * This is a backup mechanism
     */
    @Scheduled(fixedDelay = 600000, initialDelay = 60000)  // 10 minutes, start after 1 minute
    public void pollForNewOrders() {
        log.debug("Starting order polling job");

        try {
            // Get all active tenants
            List<Tenant> activeTenants = tenantRepository.findAllActive();

            if (activeTenants.isEmpty()) {
                log.debug("No active tenants found");
                return;
            }

            log.debug("Polling orders for {} active tenants", activeTenants.size());

            // Process each tenant
            for (Tenant tenant : activeTenants) {
                try {
                    // Set tenant context
                    TenantContext.setTenantId(tenant.getId().toString());

                    // Find active ML connections for this tenant
                    List<MarketplaceConnection> connections = connectionRepository
                        .findByTenantAndMarketplaceAndStatus(
                            tenant.getId(),
                            Marketplace.MERCADO_LIVRE.name(),
                            ConnectionStatus.CONNECTED.name()
                        );

                    if (connections.isEmpty()) {
                        continue;
                    }

                    log.info("Found {} active ML connections to poll for tenant {}",
                        connections.size(), tenant.getId());

                    // Poll orders for this tenant
                    pollOrdersForTenant(tenant.getId());

                } catch (Exception e) {
                    log.error("Error polling orders for tenant: {}", tenant.getId(), e);
                } finally {
                    // Always clear tenant context
                    TenantContext.clear();
                }
            }

            log.debug("Order polling job completed");

        } catch (Exception e) {
            log.error("Error in order polling job", e);
        }
    }

    /**
     * Poll orders for a single tenant
     * In a full implementation, this would:
     * 1. Call /orders/search to find recent orders
     * 2. Import each order that isn't already imported
     *
     * For now, this is a placeholder
     */
    private void pollOrdersForTenant(java.util.UUID tenantId) {
        log.debug("Polling orders for tenant: {}", tenantId);

        // TODO: Implement actual polling logic
        // This would call:
        // 1. mlApiClient.get("/orders/search?seller={seller_id}&order.date_created.from={lastPollTime}", ...)
        // 2. For each order ID: orderImportService.importOrder(tenantId, orderId)

        // For now, just log that polling would happen
        log.debug("Order polling for tenant {} - not yet fully implemented", tenantId);
    }
}
