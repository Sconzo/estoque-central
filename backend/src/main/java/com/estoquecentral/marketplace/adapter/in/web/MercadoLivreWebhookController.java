package com.estoquecentral.marketplace.adapter.in.web;

import com.estoquecentral.marketplace.application.MercadoLivreOrderImportService;
import com.estoquecentral.marketplace.application.dto.ml.MLWebhookNotification;
import com.estoquecentral.shared.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Webhook endpoint for Mercado Livre notifications
 * Story 5.5: Import and Process Orders from Mercado Livre - AC2
 *
 * ML sends POST requests to this endpoint when orders are created/updated
 */
@RestController
@RequestMapping("/api/webhooks/mercadolivre")
public class MercadoLivreWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreWebhookController.class);

    private final MercadoLivreOrderImportService orderImportService;

    public MercadoLivreWebhookController(MercadoLivreOrderImportService orderImportService) {
        this.orderImportService = orderImportService;
    }

    /**
     * AC2: Webhook endpoint for ML order notifications
     * POST /api/webhooks/mercadolivre/orders
     *
     * ML sends notifications when orders are created or updated.
     * We must respond quickly (< 3 seconds) or ML will retry.
     * Processing is done asynchronously.
     */
    @PostMapping("/orders")
    public ResponseEntity<Void> handleOrderNotification(@RequestBody MLWebhookNotification notification) {
        log.info("Received webhook notification: topic={}, resource={}",
            notification.getTopic(), notification.getResource());

        try {
            // Validate notification
            if (!notification.isOrderNotification()) {
                log.warn("Received non-order notification: topic={}", notification.getTopic());
                return ResponseEntity.ok().build();  // Return 200 to avoid retries
            }

            String orderId = notification.extractOrderId();
            if (orderId == null) {
                log.warn("Could not extract order ID from resource: {}", notification.getResource());
                return ResponseEntity.ok().build();
            }

            // Process asynchronously (must return quickly for ML)
            processOrderAsync(orderId);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error handling webhook notification", e);
            // Still return 200 to avoid infinite retries from ML
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Process order import asynchronously
     * Runs in separate thread to avoid blocking webhook response
     */
    @Async
    protected void processOrderAsync(String mlOrderId) {
        log.info("Processing order asynchronously: {}", mlOrderId);

        try {
            // Note: Webhook doesn't have tenant context in URL
            // In production, you would need to:
            // 1. Store user_id -> tenant_id mapping when OAuth is completed
            // 2. Look up tenant from notification.getUserId()
            // For now, we'll skip tenant resolution as this requires additional implementation

            log.warn("Async order processing for {} - tenant resolution not implemented. " +
                     "Order will be imported during polling job.", mlOrderId);

            // TODO: Implement tenant resolution from user_id
            // UUID tenantId = resolveTenantFromUserId(notification.getUserId());
            // TenantContext.setTenantId(tenantId.toString());
            // orderImportService.importOrder(tenantId, mlOrderId);

        } catch (Exception e) {
            log.error("Error processing order {} asynchronously", mlOrderId, e);
        } finally {
            TenantContext.clear();
        }
    }
}
