package com.estoquecentral.marketplace.application;

import com.estoquecentral.inventory.application.StockAdjustmentService;
import com.estoquecentral.inventory.application.StockReservationService;
import com.estoquecentral.inventory.domain.AdjustmentReasonCode;
import com.estoquecentral.inventory.domain.AdjustmentType;
import com.estoquecentral.marketplace.adapter.out.MarketplaceOrderRepository;
import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.MarketplaceOrder;
import com.estoquecentral.marketplace.domain.OrderStatus;
import com.estoquecentral.sales.adapter.out.SaleItemRepository;
import com.estoquecentral.sales.adapter.out.SaleRepository;
import com.estoquecentral.sales.adapter.out.SalesOrderItemRepository;
import com.estoquecentral.sales.adapter.out.SalesOrderRepository;
import com.estoquecentral.sales.application.NotificationService;
import com.estoquecentral.sales.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MercadoLivreCancellationService
 * Story 5.6: Process Mercado Livre Cancellations
 */
@ExtendWith(MockitoExtension.class)
class MercadoLivreCancellationServiceTest {

    @Mock
    private MarketplaceOrderRepository orderRepository;
    @Mock
    private StockAdjustmentService stockAdjustmentService;
    @Mock
    private StockReservationService stockReservationService;
    @Mock
    private SaleRepository saleRepository;
    @Mock
    private SaleItemRepository saleItemRepository;
    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private SalesOrderItemRepository salesOrderItemRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MarketplaceStockSyncService marketplaceStockSyncService;

    private MercadoLivreCancellationService service;

    private UUID tenantId;
    private String mlOrderId;
    private MarketplaceOrder marketplaceOrder;

    @BeforeEach
    void setUp() {
        service = new MercadoLivreCancellationService(
            orderRepository,
            stockAdjustmentService,
            stockReservationService,
            saleRepository,
            saleItemRepository,
            salesOrderRepository,
            salesOrderItemRepository,
            notificationService,
            marketplaceStockSyncService
        );

        tenantId = UUID.randomUUID();
        mlOrderId = "ML-123456789";

        marketplaceOrder = new MarketplaceOrder();
        marketplaceOrder.setId(UUID.randomUUID());
        marketplaceOrder.setTenantId(tenantId);
        marketplaceOrder.setOrderIdMarketplace(mlOrderId);
        marketplaceOrder.setMarketplace(Marketplace.MERCADO_LIVRE);
        marketplaceOrder.setStatus(OrderStatus.PENDING);
    }

    @Test
    void processCancellation_withSale_shouldRevertStockAndCancelSale() {
        // Arrange
        UUID saleId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        marketplaceOrder.setSaleId(saleId);

        Sale sale = new Sale();
        sale.setId(saleId);
        sale.setTenantId(tenantId);
        sale.setSaleNumber("SALE-001");
        sale.setStockLocationId(locationId);
        sale.setStatus(SaleStatus.ACTIVE);

        SaleItem item = new SaleItem();
        item.setId(UUID.randomUUID());
        item.setSaleId(saleId);
        item.setProductId(productId);
        item.setVariantId(null);
        item.setQuantity(new BigDecimal("5"));

        when(orderRepository.findByTenantIdAndMarketplaceAndOrderId(eq(tenantId), anyString(), eq(mlOrderId)))
            .thenReturn(Optional.of(marketplaceOrder));
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleItemRepository.findBySaleId(saleId)).thenReturn(List.of(item));

        // Act
        service.processCancellation(tenantId, mlOrderId);

        // Assert
        verify(stockAdjustmentService).createAdjustment(
            eq(tenantId),
            isNull(),
            eq(productId),
            isNull(),
            eq(locationId),
            eq(AdjustmentType.INCREASE),
            eq(new BigDecimal("5")),
            eq(AdjustmentReasonCode.OTHER),
            contains("Estorno venda ML cancelada"),
            isNull()
        );

        verify(saleRepository).save(argThat(s ->
            s.getStatus() == SaleStatus.CANCELLED
        ));

        verify(notificationService).notifyCancellation(
            eq(tenantId),
            eq(mlOrderId),
            eq("SALE"),
            eq("SALE-001"),
            anyString()
        );

        verify(marketplaceStockSyncService).enqueueStockSync(
            eq(tenantId),
            eq(productId),
            isNull(),
            eq(Marketplace.MERCADO_LIVRE)
        );

        verify(orderRepository).save(argThat(o ->
            o.getStatus() == OrderStatus.CANCELLED
        ));
    }

    @Test
    void processCancellation_withSalesOrder_shouldReleaseReservation() {
        // Arrange
        UUID salesOrderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        marketplaceOrder.setSalesOrderId(salesOrderId);

        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setId(salesOrderId);
        salesOrder.setTenantId(tenantId);
        salesOrder.setOrderNumber("OV-001");
        salesOrder.setStockLocationId(locationId);
        salesOrder.setStatus(SalesOrderStatus.CONFIRMED);

        SalesOrderItem item = new SalesOrderItem();
        item.setId(UUID.randomUUID());
        item.setSalesOrderId(salesOrderId);
        item.setProductId(productId);
        item.setVariantId(null);
        item.setQuantityReserved(new BigDecimal("10"));

        when(orderRepository.findByTenantIdAndMarketplaceAndOrderId(eq(tenantId), anyString(), eq(mlOrderId)))
            .thenReturn(Optional.of(marketplaceOrder));
        when(salesOrderRepository.findById(salesOrderId)).thenReturn(Optional.of(salesOrder));
        when(salesOrderItemRepository.findBySalesOrderId(salesOrderId)).thenReturn(List.of(item));

        // Act
        service.processCancellation(tenantId, mlOrderId);

        // Assert
        verify(stockReservationService).release(
            eq(tenantId),
            eq(productId),
            isNull(),
            eq(locationId),
            eq(new BigDecimal("10")),
            eq(salesOrderId),
            contains("Cancelamento pedido ML"),
            isNull()
        );

        verify(salesOrderRepository).save(argThat(so ->
            so.getStatus() == SalesOrderStatus.CANCELLED
        ));

        verify(notificationService).notifyCancellation(
            eq(tenantId),
            eq(mlOrderId),
            eq("SALES_ORDER"),
            eq("OV-001"),
            anyString()
        );

        verify(marketplaceStockSyncService).enqueueStockSync(
            eq(tenantId),
            eq(productId),
            isNull(),
            eq(Marketplace.MERCADO_LIVRE)
        );

        verify(orderRepository).save(argThat(o ->
            o.getStatus() == OrderStatus.CANCELLED
        ));
    }

    @Test
    void processCancellation_orderNotFound_shouldLogWarning() {
        // Arrange
        when(orderRepository.findByTenantIdAndMarketplaceAndOrderId(tenantId, Marketplace.MERCADO_LIVRE.name(), mlOrderId))
            .thenReturn(Optional.empty());

        // Act
        service.processCancellation(tenantId, mlOrderId);

        // Assert
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(stockAdjustmentService);
        verifyNoInteractions(stockReservationService);
        verifyNoInteractions(notificationService);
    }

    @Test
    void processCancellation_alreadyCancelled_shouldSkip() {
        // Arrange
        marketplaceOrder.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findByTenantIdAndMarketplaceAndOrderId(eq(tenantId), anyString(), eq(mlOrderId)))
            .thenReturn(Optional.of(marketplaceOrder));

        // Act
        service.processCancellation(tenantId, mlOrderId);

        // Assert
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(stockAdjustmentService);
        verifyNoInteractions(stockReservationService);
        verifyNoInteractions(notificationService);
    }

    @Test
    void processCancellation_noLinkedRecord_shouldOnlyUpdateOrderStatus() {
        // Arrange
        marketplaceOrder.setSaleId(null);
        marketplaceOrder.setSalesOrderId(null);

        when(orderRepository.findByTenantIdAndMarketplaceAndOrderId(eq(tenantId), anyString(), eq(mlOrderId)))
            .thenReturn(Optional.of(marketplaceOrder));

        // Act
        service.processCancellation(tenantId, mlOrderId);

        // Assert
        verify(orderRepository).save(argThat(o ->
            o.getStatus() == OrderStatus.CANCELLED
        ));
        verifyNoInteractions(stockAdjustmentService);
        verifyNoInteractions(stockReservationService);
        verifyNoInteractions(notificationService);
        verifyNoInteractions(marketplaceStockSyncService);
    }
}
