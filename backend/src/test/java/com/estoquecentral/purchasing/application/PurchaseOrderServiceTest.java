package com.estoquecentral.purchasing.application;

import com.estoquecentral.purchasing.adapter.out.PurchaseOrderItemRepository;
import com.estoquecentral.purchasing.adapter.out.PurchaseOrderRepository;
import com.estoquecentral.purchasing.adapter.out.SupplierRepository;
import com.estoquecentral.purchasing.domain.PurchaseOrder;
import com.estoquecentral.purchasing.domain.PurchaseOrderItem;
import com.estoquecentral.purchasing.domain.PurchaseOrderStatus;
import com.estoquecentral.purchasing.domain.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PurchaseOrderService
 * Story 3.2: Purchase Order Creation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseOrderService Unit Tests")
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private UUID tenantId;
    private UUID supplierId;
    private UUID locationId;
    private UUID userId;
    private UUID productId;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        supplierId = UUID.randomUUID();
        locationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        supplier = new Supplier();
        supplier.setId(supplierId);
        supplier.setTenantId(tenantId);
        supplier.setAtivo(true);
        supplier.setStatus(com.estoquecentral.purchasing.domain.SupplierStatus.ACTIVE);
        supplier.setCompanyName("Test Supplier");
    }

    @Test
    @DisplayName("Should create purchase order with items successfully")
    void shouldCreatePurchaseOrderWithItems() {
        // Given
        List<PurchaseOrderService.PurchaseOrderItemData> items = List.of(
            new PurchaseOrderService.PurchaseOrderItemData(
                productId,
                null,
                "PROD-001",
                "Test Product",
                new BigDecimal("10.00"),
                new BigDecimal("15.50"),
                "Test notes"
            )
        );

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(orderNumberGenerator.generateOrderNumber(tenantId)).thenReturn("PO-202511-0001");
        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
            .thenAnswer(invocation -> {
                PurchaseOrder po = invocation.getArgument(0);
                po.setId(UUID.randomUUID());
                return po;
            });
        when(purchaseOrderItemRepository.save(any(PurchaseOrderItem.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PurchaseOrder result = purchaseOrderService.createPurchaseOrder(
            tenantId,
            supplierId,
            locationId,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "Test PO",
            items,
            userId
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPoNumber()).isEqualTo("PO-202511-0001");
        assertThat(result.getStatus()).isEqualTo(PurchaseOrderStatus.DRAFT);
        assertThat(result.getSupplierId()).isEqualTo(supplierId);
        verify(purchaseOrderRepository, times(2)).save(any(PurchaseOrder.class)); // Once for PO, once for updating totals
        verify(purchaseOrderItemRepository, times(1)).save(any(PurchaseOrderItem.class));
    }

    @Test
    @DisplayName("Should throw exception when creating PO without items")
    void shouldThrowExceptionWhenCreatingPOWithoutItems() {
        // Given
        List<PurchaseOrderService.PurchaseOrderItemData> items = List.of();

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(
            tenantId,
            supplierId,
            locationId,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "Test PO",
            items,
            userId
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("at least one item");
    }

    @Test
    @DisplayName("Should throw exception when supplier is not active")
    void shouldThrowExceptionWhenSupplierNotActive() {
        // Given
        supplier.setStatus(com.estoquecentral.purchasing.domain.SupplierStatus.INACTIVE);
        List<PurchaseOrderService.PurchaseOrderItemData> items = List.of(
            new PurchaseOrderService.PurchaseOrderItemData(
                productId, null, "PROD-001", "Test Product",
                new BigDecimal("10.00"), new BigDecimal("15.50"), null
            )
        );

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(
            tenantId,
            supplierId,
            locationId,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "Test PO",
            items,
            userId
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("Should throw exception when supplier not found")
    void shouldThrowExceptionWhenSupplierNotFound() {
        // Given
        List<PurchaseOrderService.PurchaseOrderItemData> items = List.of(
            new PurchaseOrderService.PurchaseOrderItemData(
                productId, null, "PROD-001", "Test Product",
                new BigDecimal("10.00"), new BigDecimal("15.50"), null
            )
        );

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(
            tenantId,
            supplierId,
            locationId,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "Test PO",
            items,
            userId
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should update status from DRAFT to SENT_TO_SUPPLIER")
    void shouldUpdateStatusFromDraftToSent() {
        // Given
        UUID poId = UUID.randomUUID();
        PurchaseOrder po = new PurchaseOrder();
        po.setId(poId);
        po.setTenantId(tenantId);
        po.setStatus(PurchaseOrderStatus.DRAFT);

        when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        purchaseOrderService.updateStatus(tenantId, poId, PurchaseOrderStatus.SENT_TO_SUPPLIER, userId);

        // Then
        verify(purchaseOrderRepository).save(argThat(savedPo ->
            savedPo.getStatus() == PurchaseOrderStatus.SENT_TO_SUPPLIER &&
            savedPo.getSentToSupplierAt() != null
        ));
    }

    @Test
    @DisplayName("Should throw exception for invalid status transition")
    void shouldThrowExceptionForInvalidStatusTransition() {
        // Given
        UUID poId = UUID.randomUUID();
        PurchaseOrder po = new PurchaseOrder();
        po.setId(poId);
        po.setTenantId(tenantId);
        po.setStatus(PurchaseOrderStatus.CANCELLED);

        when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(po));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.updateStatus(
            tenantId,
            poId,
            PurchaseOrderStatus.DRAFT,
            userId
        ))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("not allowed");
    }

    @Test
    @DisplayName("Should cancel purchase order when in DRAFT status")
    void shouldCancelPurchaseOrderWhenDraft() {
        // Given
        UUID poId = UUID.randomUUID();
        PurchaseOrder po = new PurchaseOrder();
        po.setId(poId);
        po.setTenantId(tenantId);
        po.setStatus(PurchaseOrderStatus.DRAFT);

        when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        purchaseOrderService.cancelPurchaseOrder(tenantId, poId, userId);

        // Then
        verify(purchaseOrderRepository).save(argThat(savedPo ->
            savedPo.getStatus() == PurchaseOrderStatus.CANCELLED &&
            savedPo.getCancelledAt() != null
        ));
    }

    @Test
    @DisplayName("Should delete purchase order when in DRAFT status")
    void shouldDeletePurchaseOrderWhenDraft() {
        // Given
        UUID poId = UUID.randomUUID();
        PurchaseOrder po = new PurchaseOrder();
        po.setId(poId);
        po.setTenantId(tenantId);
        po.setStatus(PurchaseOrderStatus.DRAFT);

        when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(po));

        // When
        purchaseOrderService.deletePurchaseOrder(tenantId, poId);

        // Then
        verify(purchaseOrderItemRepository).deleteByPurchaseOrderId(poId);
        verify(purchaseOrderRepository).deleteById(poId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-DRAFT purchase order")
    void shouldThrowExceptionWhenDeletingNonDraftPO() {
        // Given
        UUID poId = UUID.randomUUID();
        PurchaseOrder po = new PurchaseOrder();
        po.setId(poId);
        po.setTenantId(tenantId);
        po.setStatus(PurchaseOrderStatus.SENT_TO_SUPPLIER);

        when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(po));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.deletePurchaseOrder(tenantId, poId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("DRAFT");
    }

    @Test
    @DisplayName("Should get purchase order by ID with items")
    void shouldGetPurchaseOrderByIdWithItems() {
        // Given
        UUID poId = UUID.randomUUID();
        PurchaseOrder po = new PurchaseOrder();
        po.setId(poId);
        po.setTenantId(tenantId);
        po.setPoNumber("PO-202511-0001");

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setId(UUID.randomUUID());
        item.setPurchaseOrderId(poId);
        item.setProductId(productId);

        when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(po));
        when(purchaseOrderItemRepository.findByPurchaseOrderId(poId)).thenReturn(List.of(item));

        // When
        Optional<PurchaseOrderService.PurchaseOrderWithItems> result =
            purchaseOrderService.getPurchaseOrderById(tenantId, poId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().purchaseOrder()).isEqualTo(po);
        assertThat(result.get().items()).hasSize(1);
    }

    @Test
    @DisplayName("Should return empty when purchase order not found")
    void shouldReturnEmptyWhenPurchaseOrderNotFound() {
        // Given
        UUID poId = UUID.randomUUID();
        when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.empty());

        // When
        Optional<PurchaseOrderService.PurchaseOrderWithItems> result =
            purchaseOrderService.getPurchaseOrderById(tenantId, poId);

        // Then
        assertThat(result).isEmpty();
    }
}
