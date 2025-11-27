package com.estoquecentral.marketplace.application;

import com.estoquecentral.inventory.application.StockAdjustmentService;
import com.estoquecentral.inventory.application.StockReservationService;
import com.estoquecentral.inventory.domain.AdjustmentReasonCode;
import com.estoquecentral.inventory.domain.AdjustmentType;
import com.estoquecentral.marketplace.adapter.out.MarketplaceOrderRepository;
import com.estoquecentral.marketplace.domain.MarketplaceOrder;
import com.estoquecentral.marketplace.domain.OrderStatus;
import com.estoquecentral.sales.adapter.out.SaleItemRepository;
import com.estoquecentral.sales.adapter.out.SaleRepository;
import com.estoquecentral.sales.adapter.out.SalesOrderItemRepository;
import com.estoquecentral.sales.adapter.out.SalesOrderRepository;
import com.estoquecentral.sales.domain.Sale;
import com.estoquecentral.sales.domain.SaleItem;
import com.estoquecentral.sales.domain.SalesOrder;
import com.estoquecentral.sales.domain.SalesOrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for processing Mercado Livre order cancellations
 * Story 5.6: Process Mercado Livre Cancellations - AC2, AC3, AC4
 */
@Service
public class MercadoLivreCancellationService {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreCancellationService.class);

    private final MarketplaceOrderRepository orderRepository;
    private final StockAdjustmentService stockAdjustmentService;
    private final StockReservationService stockReservationService;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;

    public MercadoLivreCancellationService(
        MarketplaceOrderRepository orderRepository,
        StockAdjustmentService stockAdjustmentService,
        StockReservationService stockReservationService,
        SaleRepository saleRepository,
        SaleItemRepository saleItemRepository,
        SalesOrderRepository salesOrderRepository,
        SalesOrderItemRepository salesOrderItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.stockAdjustmentService = stockAdjustmentService;
        this.stockReservationService = stockReservationService;
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
    }

    /**
     * AC2: Process order cancellation
     * Called when webhook receives status=cancelled
     */
    @Transactional
    public void processCancellation(UUID tenantId, String mlOrderId) {
        log.info("Processing cancellation for ML order {} (tenant: {})", mlOrderId, tenantId);

        try {
            // Find the order
            Optional<MarketplaceOrder> orderOpt = orderRepository
                .findByTenantIdAndMarketplaceAndOrderId(
                    tenantId,
                    com.estoquecentral.marketplace.domain.Marketplace.MERCADO_LIVRE.name(),
                    mlOrderId
                );

            if (orderOpt.isEmpty()) {
                log.warn("Order {} not found for cancellation processing", mlOrderId);
                return;
            }

            MarketplaceOrder order = orderOpt.get();

            // Check if already cancelled
            if (order.getStatus() == OrderStatus.CANCELLED) {
                log.debug("Order {} already cancelled, skipping", mlOrderId);
                return;
            }

            // Process based on linked record type
            if (order.getSaleId() != null) {
                // AC3: Order was converted to sale (stock was deducted)
                processSaleCancellation(order);
            } else if (order.getSalesOrderId() != null) {
                // AC4: Order was pending (stock was reserved)
                processSalesOrderCancellation(order);
            } else {
                // Order was imported but not yet processed (no stock impact)
                log.info("Order {} has no linked sale/sales_order, no stock reversal needed", mlOrderId);
            }

            // Update order status
            order.setStatus(OrderStatus.CANCELLED);
            order.setLastSyncAt(LocalDateTime.now());
            order.setDataAtualizacao(LocalDateTime.now());
            orderRepository.save(order);

            log.info("Successfully processed cancellation for order {}", mlOrderId);

        } catch (Exception e) {
            log.error("Error processing cancellation for order {}", mlOrderId, e);
            throw new RuntimeException("Failed to process cancellation", e);
        }
    }

    /**
     * AC3: Process cancellation of completed sale (estorno de venda)
     * Stock was already deducted, need to create stock adjustment to reverse
     */
    private void processSaleCancellation(MarketplaceOrder order) {
        log.info("Processing sale cancellation for order {} (sale_id: {})",
            order.getOrderIdMarketplace(), order.getSaleId());

        try {
            // 1. Get sale details
            Optional<Sale> saleOpt = saleRepository.findById(order.getSaleId());
            if (saleOpt.isEmpty()) {
                log.error("Sale {} not found for cancellation", order.getSaleId());
                return;
            }

            Sale sale = saleOpt.get();

            // 2. Get all sale items
            List<SaleItem> items = saleItemRepository.findBySaleId(order.getSaleId());

            if (items.isEmpty()) {
                log.warn("No items found for sale {}", order.getSaleId());
                return;
            }

            // 3. For each sale item, create stock adjustment INCREASE (estorno)
            for (SaleItem item : items) {
                try {
                    String reasonDescription = String.format(
                        "Estorno venda ML cancelada - Order %s - Sale %s",
                        order.getOrderIdMarketplace(),
                        sale.getSaleNumber()
                    );

                    stockAdjustmentService.createAdjustment(
                        order.getTenantId(),
                        null, // userId - system action
                        item.getProductId(),
                        item.getVariantId(),
                        sale.getStockLocationId(),
                        AdjustmentType.INCREASE,
                        item.getQuantity(),
                        AdjustmentReasonCode.OTHER,
                        reasonDescription,
                        null // adjustmentDate - defaults to today
                    );

                    log.info("Created stock adjustment INCREASE for product {} variant {} quantity {}",
                        item.getProductId(), item.getVariantId(), item.getQuantity());

                } catch (Exception e) {
                    log.error("Error creating stock adjustment for sale item {}", item.getId(), e);
                    // Continue processing other items even if one fails
                }
            }

            // 4. TODO: Update sale.status = CANCELLED (if Sale entity supports status field)
            // Currently Sale doesn't have a status field in the entity
            // This would require a migration to add status to sales table
            log.info("Sale {} items reverted successfully. Note: Sale status update requires migration.",
                sale.getSaleNumber());

        } catch (Exception e) {
            log.error("Error processing sale cancellation for order {}", order.getOrderIdMarketplace(), e);
            throw new RuntimeException("Failed to process sale cancellation", e);
        }
    }

    /**
     * AC4: Process cancellation of pending order (liberação de reserva)
     * Stock was reserved, need to release reservation
     */
    private void processSalesOrderCancellation(MarketplaceOrder order) {
        log.info("Processing sales order cancellation for order {} (sales_order_id: {})",
            order.getOrderIdMarketplace(), order.getSalesOrderId());

        try {
            // 1. Get sales order details
            Optional<SalesOrder> salesOrderOpt = salesOrderRepository.findById(order.getSalesOrderId());
            if (salesOrderOpt.isEmpty()) {
                log.error("Sales order {} not found for cancellation", order.getSalesOrderId());
                return;
            }

            SalesOrder salesOrder = salesOrderOpt.get();

            // 2. Get all sales order items
            List<SalesOrderItem> items = salesOrderItemRepository.findBySalesOrderId(order.getSalesOrderId());

            if (items.isEmpty()) {
                log.warn("No items found for sales order {}", order.getSalesOrderId());
            } else {
                // 3. For each order item, release stock reservation
                for (SalesOrderItem item : items) {
                    try {
                        // Only release if there's a reservation
                        if (item.getQuantityReserved().compareTo(java.math.BigDecimal.ZERO) > 0) {
                            String releaseReason = String.format(
                                "Cancelamento pedido ML - Order %s - OV %s",
                                order.getOrderIdMarketplace(),
                                salesOrder.getOrderNumber()
                            );

                            stockReservationService.release(
                                order.getTenantId(),
                                item.getProductId(),
                                item.getVariantId(),
                                salesOrder.getStockLocationId(),
                                item.getQuantityReserved(),
                                salesOrder.getId(),
                                releaseReason,
                                null // userId - system action
                            );

                            log.info("Released stock reservation for product {} variant {} quantity {}",
                                item.getProductId(), item.getVariantId(), item.getQuantityReserved());
                        } else {
                            log.debug("No reservation to release for item {}", item.getId());
                        }

                    } catch (Exception e) {
                        log.error("Error releasing stock reservation for sales order item {}", item.getId(), e);
                        // Continue processing other items even if one fails
                    }
                }
            }

            // 4. Update sales_order.status = CANCELLED
            salesOrder.cancel();
            salesOrderRepository.save(salesOrder);

            log.info("Sales order {} cancelled successfully", salesOrder.getOrderNumber());

        } catch (Exception e) {
            log.error("Error processing sales order cancellation for order {}", order.getOrderIdMarketplace(), e);
            throw new RuntimeException("Failed to process sales order cancellation", e);
        }
    }

}
