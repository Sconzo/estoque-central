package com.estoquecentral.sales.application;

import com.estoquecentral.auth.adapter.out.TenantRepository;
import com.estoquecentral.auth.domain.Tenant;
import com.estoquecentral.inventory.application.StockReservationService;
import com.estoquecentral.sales.adapter.out.SalesOrderItemRepository;
import com.estoquecentral.sales.adapter.out.SalesOrderRepository;
import com.estoquecentral.sales.domain.SalesOrder;
import com.estoquecentral.sales.domain.SalesOrderItem;
import com.estoquecentral.sales.domain.SalesOrderStatus;
import com.estoquecentral.tenant.application.TenantSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AutoReleaseScheduledJob - Scheduled job to automatically release expired sales order reservations
 * Story 4.6: Stock Reservation and Automatic Release - AC4
 *
 * <p>Runs daily at 2:00 AM to find CONFIRMED orders older than configured days
 * and automatically releases their stock reservations, updating order status to EXPIRED.
 *
 * <p><strong>Schedule:</strong> Daily at 02:00 AM
 * <p><strong>Process:</strong>
 * <ul>
 *   <li>Iterates through all tenants</li>
 *   <li>Gets tenant-specific auto-release configuration (default: 7 days)</li>
 *   <li>Finds CONFIRMED orders older than N days</li>
 *   <li>Releases stock reservations via StockReservationService</li>
 *   <li>Updates order status to EXPIRED</li>
 *   <li>Logs/notifies stakeholders</li>
 * </ul>
 */
@Component
public class AutoReleaseScheduledJob {

    private static final Logger logger = LoggerFactory.getLogger(AutoReleaseScheduledJob.class);

    private final TenantRepository tenantRepository;
    private final TenantSettingsService tenantSettingsService;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final StockReservationService stockReservationService;

    public AutoReleaseScheduledJob(
            TenantRepository tenantRepository,
            TenantSettingsService tenantSettingsService,
            SalesOrderRepository salesOrderRepository,
            SalesOrderItemRepository salesOrderItemRepository,
            StockReservationService stockReservationService) {
        this.tenantRepository = tenantRepository;
        this.tenantSettingsService = tenantSettingsService;
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.stockReservationService = stockReservationService;
    }

    /**
     * Scheduled job that runs daily at 2:00 AM to release expired order reservations
     */
    @Scheduled(cron = "0 0 2 * * ?") // 02:00 AM daily
    public void releaseExpiredOrders() {
        logger.info("Starting auto-release job for expired sales order reservations");

        try {
            // Get all active tenants
            List<Tenant> tenants = (List<Tenant>) tenantRepository.findAll();

            int totalOrdersProcessed = 0;
            int totalTenants = 0;

            for (Tenant tenant : tenants) {
                try {
                    int ordersReleased = releaseExpiredOrdersForTenant(tenant.getId());
                    if (ordersReleased > 0) {
                        totalOrdersProcessed += ordersReleased;
                        totalTenants++;
                    }
                } catch (Exception e) {
                    logger.error("Error processing auto-release for tenant {}: {}",
                        tenant.getId(), e.getMessage(), e);
                }
            }

            logger.info("Auto-release job completed. Processed {} orders across {} tenants",
                totalOrdersProcessed, totalTenants);

        } catch (Exception e) {
            logger.error("Fatal error in auto-release job: {}", e.getMessage(), e);
        }
    }

    /**
     * Releases expired orders for a specific tenant
     *
     * @param tenantId tenant ID
     * @return number of orders released
     */
    @Transactional
    public int releaseExpiredOrdersForTenant(UUID tenantId) {
        logger.debug("Processing auto-release for tenant {}", tenantId);

        // Get tenant-specific auto-release configuration
        int autoReleaseDays = tenantSettingsService.getAutoReleaseDays(tenantId);

        // Calculate expiration date
        LocalDate expirationDate = LocalDate.now().minusDays(autoReleaseDays);

        logger.debug("Using auto-release period of {} days (expiration date: {})",
            autoReleaseDays, expirationDate);

        // Find expired confirmed orders
        List<SalesOrder> expiredOrders = salesOrderRepository.findExpiredConfirmedOrders(
            tenantId,
            expirationDate
        );

        if (expiredOrders.isEmpty()) {
            logger.debug("No expired orders found for tenant {}", tenantId);
            return 0;
        }

        logger.info("Found {} expired orders for tenant {} (older than {} days)",
            expiredOrders.size(), tenantId, autoReleaseDays);

        int ordersProcessed = 0;

        for (SalesOrder order : expiredOrders) {
            try {
                releaseOrderReservations(order, autoReleaseDays, tenantId);
                ordersProcessed++;
            } catch (Exception e) {
                logger.error("Error releasing order {}: {}", order.getId(), e.getMessage(), e);
            }
        }

        logger.info("Released {} expired orders for tenant {}", ordersProcessed, tenantId);

        return ordersProcessed;
    }

    /**
     * Releases reservations for a single order
     *
     * @param order sales order to release
     * @param autoReleaseDays number of days used for auto-release
     * @param tenantId tenant ID
     */
    @Transactional
    protected void releaseOrderReservations(SalesOrder order, int autoReleaseDays, UUID tenantId) {
        logger.debug("Releasing reservations for order {} ({})",
            order.getId(), order.getOrderNumber());

        // Get order items
        List<SalesOrderItem> items = salesOrderItemRepository.findBySalesOrderId(order.getId());

        // Release reservation for each item
        for (SalesOrderItem item : items) {
            if (item.getQuantityReserved().compareTo(java.math.BigDecimal.ZERO) > 0) {
                try {
                    String reason = String.format(
                        "Liberação automática OV %s - Ordem expirada após %d dias",
                        order.getOrderNumber(),
                        autoReleaseDays
                    );

                    stockReservationService.release(
                        tenantId,
                        item.getProductId(),
                        item.getVariantId(),
                        order.getStockLocationId(),
                        item.getQuantityReserved(),
                        order.getId(),
                        reason,
                        null // System user - could be replaced with a system UUID
                    );

                    // Update item reserved quantity to zero
                    item.setQuantityReserved(java.math.BigDecimal.ZERO);
                    salesOrderItemRepository.save(item);

                } catch (Exception e) {
                    logger.error("Error releasing item {} from order {}: {}",
                        item.getId(), order.getId(), e.getMessage(), e);
                    throw e; // Re-throw to rollback transaction
                }
            }
        }

        // Update order status to EXPIRED
        order.setStatus(SalesOrderStatus.EXPIRED);
        order.setUpdatedAt(LocalDateTime.now());
        salesOrderRepository.save(order);

        logger.info("Successfully released order {} ({}). Status updated to EXPIRED",
            order.getId(), order.getOrderNumber());

        // TODO Story 4.6 AC4: Send notification to salesperson
        // This could be implemented via email/push notification service
        // notificationService.notifyOrderExpired(order);
    }
}
