package com.estoquecentral.marketplace.application;

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

    public OrderPollingScheduledJob(
        MarketplaceConnectionRepository connectionRepository,
        MercadoLivreOrderImportService orderImportService
    ) {
        this.connectionRepository = connectionRepository;
        this.orderImportService = orderImportService;
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
            // Find all active ML connections
            List<MarketplaceConnection> connections = connectionRepository
                .findByMarketplaceAndStatus(
                    Marketplace.MERCADO_LIVRE.name(),
                    ConnectionStatus.CONNECTED.name()
                );

            log.info("Found {} active ML connections to poll", connections.size());

            for (MarketplaceConnection connection : connections) {
                try {
                    TenantContext.setTenantId(connection.getTenantId().toString());
                    pollOrdersForTenant(connection.getTenantId());
                } catch (Exception e) {
                    log.error("Error polling orders for tenant: {}", connection.getTenantId(), e);
                } finally {
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
