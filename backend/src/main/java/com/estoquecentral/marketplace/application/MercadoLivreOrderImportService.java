package com.estoquecentral.marketplace.application;

import com.estoquecentral.marketplace.adapter.out.MarketplaceListingRepository;
import com.estoquecentral.marketplace.adapter.out.MarketplaceOrderRepository;
import com.estoquecentral.marketplace.application.dto.OrderPreviewResponse;
import com.estoquecentral.marketplace.application.dto.ml.MLOrderResponse;
import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.MarketplaceListing;
import com.estoquecentral.marketplace.domain.MarketplaceOrder;
import com.estoquecentral.marketplace.domain.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for importing orders from Mercado Livre
 * Story 5.5: Import and Process Orders from Mercado Livre - AC3, AC4
 */
@Service
public class MercadoLivreOrderImportService {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreOrderImportService.class);

    private final MercadoLivreApiClient mlApiClient;
    private final MarketplaceOrderRepository orderRepository;
    private final MarketplaceListingRepository listingRepository;
    private final ObjectMapper objectMapper;
    private final MercadoLivreCancellationService cancellationService;

    public MercadoLivreOrderImportService(
        MercadoLivreApiClient mlApiClient,
        MarketplaceOrderRepository orderRepository,
        MarketplaceListingRepository listingRepository,
        ObjectMapper objectMapper,
        MercadoLivreCancellationService cancellationService
    ) {
        this.mlApiClient = mlApiClient;
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
        this.objectMapper = objectMapper;
        this.cancellationService = cancellationService;
    }

    /**
     * AC3: Import single order from Mercado Livre
     * Called by webhook handler or polling job
     */
    @Transactional
    public void importOrder(UUID tenantId, String mlOrderId) {
        log.info("Importing order {} for tenant {}", mlOrderId, tenantId);

        try {
            // Check if already imported
            if (orderRepository.existsByTenantIdAndMarketplaceAndOrderId(
                    tenantId, Marketplace.MERCADO_LIVRE.name(), mlOrderId)) {
                log.debug("Order {} already imported, updating status", mlOrderId);
                updateExistingOrder(tenantId, mlOrderId);
                return;
            }

            // Fetch order details from ML
            MLOrderResponse mlOrder = mlApiClient.get(
                "/orders/" + mlOrderId,
                MLOrderResponse.class,
                tenantId
            );

            // Create marketplace order record
            MarketplaceOrder order = createMarketplaceOrder(tenantId, mlOrder);
            orderRepository.save(order);

            log.info("Order {} imported successfully", mlOrderId);

            // TODO: Process order items and create sale/sales_order
            // This will be implemented when integrating with SaleService
            // For now, we just store the order

        } catch (Exception e) {
            log.error("Error importing order {} for tenant {}", mlOrderId, tenantId, e);
            throw new RuntimeException("Failed to import order from Mercado Livre", e);
        }
    }

    /**
     * Update existing order status from ML
     */
    @Transactional
    public void updateExistingOrder(UUID tenantId, String mlOrderId) {
        log.debug("Updating order {} for tenant {}", mlOrderId, tenantId);

        try {
            // Fetch current order from ML
            MLOrderResponse mlOrder = mlApiClient.get(
                "/orders/" + mlOrderId,
                MLOrderResponse.class,
                tenantId
            );

            // Find existing order
            Optional<MarketplaceOrder> existingOrder = orderRepository
                .findByTenantIdAndMarketplaceAndOrderId(
                    tenantId,
                    Marketplace.MERCADO_LIVRE.name(),
                    mlOrderId
                );

            if (existingOrder.isPresent()) {
                MarketplaceOrder order = existingOrder.get();
                OrderStatus previousStatus = order.getStatus();
                OrderStatus newStatus = mapMLStatusToOrderStatus(mlOrder.getStatus(), mlOrder.isPaymentApproved());

                // Update status
                order.setStatus(newStatus);
                order.setPaymentStatus(getPaymentStatus(mlOrder));
                order.setShippingStatus(getShippingStatus(mlOrder));
                order.setLastSyncAt(LocalDateTime.now());
                order.setDataAtualizacao(LocalDateTime.now());

                // Update raw data
                try {
                    order.setMlRawData(objectMapper.writeValueAsString(mlOrder));
                } catch (JsonProcessingException e) {
                    log.warn("Failed to serialize ML order data", e);
                }

                orderRepository.save(order);
                log.debug("Order {} updated successfully", mlOrderId);

                // Story 5.6: AC1 & AC2 - Process cancellation if status changed to CANCELLED
                if (newStatus == OrderStatus.CANCELLED && previousStatus != OrderStatus.CANCELLED) {
                    log.info("Order {} status changed to CANCELLED, triggering cancellation processing", mlOrderId);
                    cancellationService.processCancellation(tenantId, mlOrderId);
                }
            }

        } catch (Exception e) {
            log.error("Error updating order {} for tenant {}", mlOrderId, tenantId, e);
        }
    }

    /**
     * AC4: Identify products/variants sold in order
     * Returns list of (productId, variantId, quantity) tuples
     */
    public List<OrderItemIdentification> identifyOrderItems(UUID tenantId, MLOrderResponse mlOrder) {
        List<OrderItemIdentification> identifications = new ArrayList<>();

        if (mlOrder.getOrderItems() == null) {
            return identifications;
        }

        for (MLOrderResponse.MLOrderItem mlItem : mlOrder.getOrderItems()) {
            String listingId = mlItem.getItem().getId();
            Long variationId = mlItem.getItem().getVariationId();

            log.debug("Identifying order item: listingId={}, variationId={}", listingId, variationId);

            // Search for marketplace listing
            String fullListingId = variationId != null
                ? listingId + "-" + variationId
                : listingId;

            Optional<MarketplaceListing> listing = listingRepository
                .findByTenantIdAndMarketplaceAndListingId(
                    tenantId,
                    Marketplace.MERCADO_LIVRE.name(),
                    fullListingId
                );

            if (listing.isPresent()) {
                OrderItemIdentification identification = new OrderItemIdentification(
                    listing.get().getProductId(),
                    listing.get().getVariantId(),
                    mlItem.getQuantity(),
                    mlItem.getUnitPrice()
                );
                identifications.add(identification);
                log.debug("Item identified: productId={}, variantId={}, quantity={}",
                    identification.productId, identification.variantId, identification.quantity);
            } else {
                log.warn("Listing not found for listingId={}, variationId={}. " +
                         "Order item cannot be processed.", listingId, variationId);
            }
        }

        return identifications;
    }

    /**
     * Get orders list for frontend (AC6)
     */
    public List<OrderPreviewResponse> getOrdersPreview(UUID tenantId) {
        log.info("Fetching orders preview for tenant {}", tenantId);

        List<MarketplaceOrder> orders = orderRepository.findByTenantIdAndMarketplace(
            tenantId,
            Marketplace.MERCADO_LIVRE.name()
        );

        return orders.stream()
            .map(this::mapToOrderPreviewResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get orders by date range
     */
    public List<OrderPreviewResponse> getOrdersByDateRange(
            UUID tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching orders for tenant {} between {} and {}", tenantId, startDate, endDate);

        List<MarketplaceOrder> orders = orderRepository.findOrdersByDateRange(
            tenantId, startDate, endDate
        );

        return orders.stream()
            .map(this::mapToOrderPreviewResponse)
            .collect(Collectors.toList());
    }

    // Helper Methods

    private MarketplaceOrder createMarketplaceOrder(UUID tenantId, MLOrderResponse mlOrder) {
        MarketplaceOrder order = new MarketplaceOrder();
        order.setTenantId(tenantId);
        order.setMarketplace(Marketplace.MERCADO_LIVRE);
        order.setOrderIdMarketplace(mlOrder.getId().toString());

        // Customer info
        if (mlOrder.getBuyer() != null) {
            order.setCustomerName(mlOrder.getBuyer().getFullName());
            order.setCustomerEmail(mlOrder.getBuyer().getEmail());
            if (mlOrder.getBuyer().getPhone() != null) {
                order.setCustomerPhone(mlOrder.getBuyer().getPhone().getFullPhone());
            }
        }

        order.setTotalAmount(mlOrder.getTotalAmount());
        order.setStatus(mapMLStatusToOrderStatus(mlOrder.getStatus(), mlOrder.isPaymentApproved()));
        order.setPaymentStatus(getPaymentStatus(mlOrder));
        order.setShippingStatus(getShippingStatus(mlOrder));
        order.setImportedAt(LocalDateTime.now());
        order.setDataCriacao(LocalDateTime.now());
        order.setDataAtualizacao(LocalDateTime.now());

        // Store raw ML data for debugging/audit
        try {
            order.setMlRawData(objectMapper.writeValueAsString(mlOrder));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize ML order data", e);
        }

        return order;
    }

    private OrderStatus mapMLStatusToOrderStatus(String mlStatus, boolean paymentApproved) {
        if (mlStatus == null) {
            return OrderStatus.PENDING;
        }

        // ML order status: confirmed, payment_required, payment_in_process, paid, cancelled, etc
        switch (mlStatus.toLowerCase()) {
            case "paid":
                return OrderStatus.PAID;
            case "cancelled":
                return OrderStatus.CANCELLED;
            case "confirmed":
                return paymentApproved ? OrderStatus.PAID : OrderStatus.PENDING;
            default:
                return OrderStatus.PENDING;
        }
    }

    private String getPaymentStatus(MLOrderResponse mlOrder) {
        if (mlOrder.getPayments() != null && !mlOrder.getPayments().isEmpty()) {
            return mlOrder.getPayments().get(0).getStatus();
        }
        return null;
    }

    private String getShippingStatus(MLOrderResponse mlOrder) {
        if (mlOrder.getShipping() != null) {
            return mlOrder.getShipping().getStatus();
        }
        return null;
    }

    private OrderPreviewResponse mapToOrderPreviewResponse(MarketplaceOrder order) {
        return new OrderPreviewResponse(
            order.getId(),
            order.getOrderIdMarketplace(),
            order.getMarketplace().name(),
            order.getCustomerName(),
            order.getCustomerEmail(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getPaymentStatus(),
            order.getShippingStatus(),
            order.getImportedAt(),
            order.getSaleId(),
            order.getSalesOrderId()
        );
    }

    /**
     * Helper class to hold identified order item information
     */
    public static class OrderItemIdentification {
        public final UUID productId;
        public final UUID variantId;
        public final Integer quantity;
        public final java.math.BigDecimal unitPrice;

        public OrderItemIdentification(UUID productId, UUID variantId, Integer quantity, java.math.BigDecimal unitPrice) {
            this.productId = productId;
            this.variantId = variantId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
}
