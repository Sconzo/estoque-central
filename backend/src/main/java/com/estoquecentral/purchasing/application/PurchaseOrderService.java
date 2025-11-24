package com.estoquecentral.purchasing.application;

import com.estoquecentral.purchasing.adapter.in.dto.ReceivingItemDTO;
import com.estoquecentral.purchasing.adapter.in.dto.ReceivingOrderDetailDTO;
import com.estoquecentral.purchasing.adapter.in.dto.ReceivingOrderSummaryDTO;
import com.estoquecentral.purchasing.adapter.out.PurchaseOrderItemRepository;
import com.estoquecentral.purchasing.adapter.out.PurchaseOrderRepository;
import com.estoquecentral.purchasing.adapter.out.SupplierRepository;
import com.estoquecentral.purchasing.domain.PurchaseOrder;
import com.estoquecentral.purchasing.domain.PurchaseOrderItem;
import com.estoquecentral.purchasing.domain.PurchaseOrderStatus;
import com.estoquecentral.purchasing.domain.Supplier;
import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.inventory.adapter.out.LocationRepository;
import com.estoquecentral.inventory.domain.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PurchaseOrderService - Business logic for purchase order management
 * Story 3.2: Purchase Order Creation
 */
@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    // Allowed status transitions
    private static final Map<PurchaseOrderStatus, Set<PurchaseOrderStatus>> ALLOWED_TRANSITIONS = Map.of(
        PurchaseOrderStatus.DRAFT, Set.of(PurchaseOrderStatus.PENDING_APPROVAL, PurchaseOrderStatus.SENT_TO_SUPPLIER, PurchaseOrderStatus.CANCELLED),
        PurchaseOrderStatus.PENDING_APPROVAL, Set.of(PurchaseOrderStatus.APPROVED, PurchaseOrderStatus.CANCELLED),
        PurchaseOrderStatus.APPROVED, Set.of(PurchaseOrderStatus.SENT_TO_SUPPLIER, PurchaseOrderStatus.CANCELLED),
        PurchaseOrderStatus.SENT_TO_SUPPLIER, Set.of(PurchaseOrderStatus.PARTIALLY_RECEIVED, PurchaseOrderStatus.RECEIVED, PurchaseOrderStatus.CANCELLED),
        PurchaseOrderStatus.PARTIALLY_RECEIVED, Set.of(PurchaseOrderStatus.RECEIVED),
        PurchaseOrderStatus.RECEIVED, Set.of(PurchaseOrderStatus.CLOSED),
        PurchaseOrderStatus.CANCELLED, Set.of(),
        PurchaseOrderStatus.CLOSED, Set.of()
    );

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            LocationRepository locationRepository,
            OrderNumberGenerator orderNumberGenerator) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
        this.orderNumberGenerator = orderNumberGenerator;
    }

    /**
     * Create a new purchase order with items
     */
    @Transactional
    public PurchaseOrder createPurchaseOrder(
            UUID tenantId,
            UUID supplierId,
            UUID locationId,
            LocalDate orderDate,
            LocalDate expectedDeliveryDate,
            String notes,
            List<PurchaseOrderItemData> items,
            UUID userId) {

        // Validate at least one item
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Purchase order must have at least one item");
        }

        // Validate supplier exists and is active
        Supplier supplier = supplierRepository.findById(supplierId)
            .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierId));

        if (!supplier.isActive()) {
            throw new IllegalArgumentException("Supplier is not active: " + supplierId);
        }

        // Create purchase order
        PurchaseOrder po = new PurchaseOrder();
        po.setTenantId(tenantId);
        po.setSupplierId(supplierId);
        po.setLocationId(locationId);
        po.setOrderDate(orderDate != null ? orderDate : LocalDate.now());
        po.setExpectedDeliveryDate(expectedDeliveryDate);
        po.setNotes(notes);
        po.setStatus(PurchaseOrderStatus.DRAFT);
        po.setCreatedBy(userId);
        po.setUpdatedBy(userId);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());

        // Generate PO number
        String poNumber = orderNumberGenerator.generateOrderNumber(tenantId);
        po.setPoNumber(poNumber);

        // Save PO (to get ID)
        PurchaseOrder savedPo = purchaseOrderRepository.save(po);

        // Create and save items
        BigDecimal subtotal = BigDecimal.ZERO;
        for (PurchaseOrderItemData itemData : items) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setTenantId(tenantId);
            item.setPurchaseOrderId(savedPo.getId());
            item.setProductId(itemData.productId());
            item.setProductVariantId(itemData.variantId());
            item.setProductSku(itemData.productSku() != null ? itemData.productSku() : "");
            item.setProductName(itemData.productName() != null ? itemData.productName() : "");
            item.setQuantityOrdered(itemData.quantityOrdered());
            item.setUnitCost(itemData.unitCost());
            item.setNotes(itemData.notes());
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());

            // Calculate item totals
            item.calculateTotals();

            purchaseOrderItemRepository.save(item);

            subtotal = subtotal.add(item.getTotal());
        }

        // Update PO totals
        savedPo.setSubtotal(subtotal);
        savedPo.calculateTotals();
        purchaseOrderRepository.save(savedPo);

        return savedPo;
    }

    /**
     * Get purchase order by ID with items
     */
    @Transactional(readOnly = true)
    public Optional<PurchaseOrderWithItems> getPurchaseOrderById(UUID tenantId, UUID id) {
        Optional<PurchaseOrder> poOpt = purchaseOrderRepository.findById(id);

        if (poOpt.isEmpty()) {
            return Optional.empty();
        }

        PurchaseOrder po = poOpt.get();

        // Verify tenant
        if (!po.getTenantId().equals(tenantId)) {
            return Optional.empty();
        }

        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(id);

        return Optional.of(new PurchaseOrderWithItems(po, items));
    }

    /**
     * Search purchase orders with filters
     */
    @Transactional(readOnly = true)
    public Page<PurchaseOrder> searchPurchaseOrders(
            UUID tenantId,
            UUID supplierId,
            String status,
            LocalDate orderDateFrom,
            LocalDate orderDateTo,
            String poNumber,
            Pageable pageable) {

        return purchaseOrderRepository.search(
            tenantId,
            supplierId,
            status,
            orderDateFrom,
            orderDateTo,
            poNumber,
            pageable
        );
    }

    /**
     * Update purchase order status
     */
    @Transactional
    public void updateStatus(UUID tenantId, UUID poId, PurchaseOrderStatus newStatus, UUID userId) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + poId));

        // Verify tenant
        if (!po.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Purchase order does not belong to this tenant");
        }

        // Validate transition
        if (!isTransitionAllowed(po.getStatus(), newStatus)) {
            throw new IllegalStateException(
                String.format("Transition from %s to %s is not allowed", po.getStatus(), newStatus)
            );
        }

        // Update status
        po.setStatus(newStatus);
        po.setUpdatedBy(userId);
        po.setUpdatedAt(LocalDateTime.now());

        // Update specific timestamps
        if (newStatus == PurchaseOrderStatus.APPROVED) {
            po.setApprovedBy(userId);
            po.setApprovedAt(LocalDateTime.now());
        } else if (newStatus == PurchaseOrderStatus.SENT_TO_SUPPLIER) {
            po.setSentToSupplierAt(LocalDateTime.now());
        } else if (newStatus == PurchaseOrderStatus.CANCELLED) {
            po.setCancelledAt(LocalDateTime.now());
        }

        purchaseOrderRepository.save(po);
    }

    /**
     * Cancel purchase order (only if DRAFT or SENT_TO_SUPPLIER)
     */
    @Transactional
    public void cancelPurchaseOrder(UUID tenantId, UUID poId, UUID userId) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + poId));

        // Verify tenant
        if (!po.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Purchase order does not belong to this tenant");
        }

        // Can only cancel if DRAFT, PENDING_APPROVAL, APPROVED, or SENT_TO_SUPPLIER
        if (!po.canBeCancelled()) {
            throw new IllegalStateException(
                "Purchase order cannot be cancelled in status: " + po.getStatus()
            );
        }

        po.cancel();
        po.setUpdatedBy(userId);
        purchaseOrderRepository.save(po);
    }

    /**
     * Delete purchase order (only if DRAFT)
     */
    @Transactional
    public void deletePurchaseOrder(UUID tenantId, UUID poId) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + poId));

        // Verify tenant
        if (!po.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Purchase order does not belong to this tenant");
        }

        // Can only delete if DRAFT
        if (!po.isDraft()) {
            throw new IllegalStateException(
                "Only DRAFT purchase orders can be deleted. Current status: " + po.getStatus()
            );
        }

        // Delete items first (CASCADE should handle this, but being explicit)
        purchaseOrderItemRepository.deleteByPurchaseOrderId(poId);

        // Delete PO
        purchaseOrderRepository.deleteById(poId);
    }

    /**
     * Get all purchase orders for a tenant
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrder> getAllPurchaseOrders(UUID tenantId) {
        return purchaseOrderRepository.findByTenantId(tenantId);
    }

    /**
     * Get purchase orders by status
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrder> getPurchaseOrdersByStatus(UUID tenantId, PurchaseOrderStatus status) {
        return purchaseOrderRepository.findByTenantIdAndStatus(tenantId, status.name());
    }

    /**
     * Get overdue purchase orders
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOverduePurchaseOrders(UUID tenantId) {
        return purchaseOrderRepository.findOverduePurchaseOrders(tenantId);
    }

    /**
     * Check if status transition is allowed
     */
    private boolean isTransitionAllowed(PurchaseOrderStatus from, PurchaseOrderStatus to) {
        Set<PurchaseOrderStatus> allowedStatuses = ALLOWED_TRANSITIONS.get(from);
        return allowedStatuses != null && allowedStatuses.contains(to);
    }

    /**
     * Data class for creating purchase order items
     */
    public record PurchaseOrderItemData(
        UUID productId,
        UUID variantId,
        String productSku,
        String productName,
        BigDecimal quantityOrdered,
        BigDecimal unitCost,
        String notes
    ) {}

    /**
     * Data class for purchase order with items
     */
    public record PurchaseOrderWithItems(
        PurchaseOrder purchaseOrder,
        List<PurchaseOrderItem> items
    ) {}

    /**
     * Get purchase orders pending receipt (Story 3.3 AC3)
     * Returns orders with status SENT_TO_SUPPLIER or PARTIALLY_RECEIVED
     *
     * @param tenantId Tenant ID
     * @param supplierId Optional supplier filter
     * @return List of purchase orders pending receipt ordered by order date descending
     */
    @Transactional(readOnly = true)
    public List<ReceivingOrderSummaryDTO> getPendingReceiptOrders(UUID tenantId, UUID supplierId) {
        List<PurchaseOrder> orders;

        if (supplierId != null) {
            // Filter by supplier
            orders = purchaseOrderRepository.findByTenantIdAndSupplierIdAndStatusIn(
                tenantId,
                supplierId,
                List.of("SENT_TO_SUPPLIER", "PARTIALLY_RECEIVED"),
                Sort.by(Sort.Direction.DESC, "orderDate")
            );
        } else {
            // All pending orders
            orders = purchaseOrderRepository.findByTenantIdAndStatusIn(
                tenantId,
                List.of("SENT_TO_SUPPLIER", "PARTIALLY_RECEIVED"),
                Sort.by(Sort.Direction.DESC, "orderDate")
            );
        }

        // Map to DTO with items summary
        return orders.stream()
            .map(this::mapToReceivingOrderSummary)
            .collect(Collectors.toList());
    }

    /**
     * Get receiving details for a specific purchase order (Story 3.3 AC4)
     *
     * @param tenantId Tenant ID
     * @param orderId Purchase order ID
     * @return Receiving order details or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<ReceivingOrderDetailDTO> getReceivingDetails(UUID tenantId, UUID orderId) {
        Optional<PurchaseOrder> poOpt = purchaseOrderRepository.findById(orderId);

        if (poOpt.isEmpty() || !poOpt.get().getTenantId().equals(tenantId)) {
            return Optional.empty();
        }

        PurchaseOrder po = poOpt.get();
        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(orderId);

        // Fetch supplier name
        Optional<Supplier> supplierOpt = supplierRepository.findById(po.getSupplierId());
        String supplierName = supplierOpt.map(Supplier::getCompanyName).orElse("Unknown");

        // Fetch stock location name
        Optional<Location> locationOpt = locationRepository.findById(po.getLocationId());
        String locationName = locationOpt.map(Location::getName).orElse("Unknown");

        // Map to DTO
        ReceivingOrderDetailDTO dto = new ReceivingOrderDetailDTO();
        dto.setId(po.getId());
        dto.setOrderNumber(po.getPoNumber());
        dto.setSupplierName(supplierName);
        dto.setStockLocationName(locationName);

        // Map items with barcode from product
        List<ReceivingItemDTO> itemDTOs = items.stream()
            .map(item -> mapToReceivingItem(item, tenantId))
            .collect(Collectors.toList());

        dto.setItems(itemDTOs);

        return Optional.of(dto);
    }

    /**
     * Map PurchaseOrder to ReceivingOrderSummaryDTO
     */
    private ReceivingOrderSummaryDTO mapToReceivingOrderSummary(PurchaseOrder po) {
        ReceivingOrderSummaryDTO dto = new ReceivingOrderSummaryDTO();
        dto.setId(po.getId());
        dto.setOrderNumber(po.getPoNumber());
        dto.setOrderDate(po.getOrderDate());
        dto.setStatus(po.getStatus());

        // Fetch supplier name
        Optional<Supplier> supplierOpt = supplierRepository.findById(po.getSupplierId());
        dto.setSupplierName(supplierOpt.map(Supplier::getCompanyName).orElse("Unknown"));

        // Get items to calculate summary
        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(po.getId());

        int totalItems = items.size();
        int totalReceived = (int) items.stream()
            .filter(item -> item.getQuantityReceived() != null &&
                          item.getQuantityReceived().compareTo(BigDecimal.ZERO) > 0)
            .count();
        int totalPending = totalItems - totalReceived;

        ReceivingOrderSummaryDTO.ItemsSummary itemsSummary =
            new ReceivingOrderSummaryDTO.ItemsSummary(
                totalItems,
                totalReceived,
                totalPending,
                po.getTotal()
            );

        dto.setItemsSummary(itemsSummary);

        return dto;
    }

    /**
     * Map PurchaseOrderItem to ReceivingItemDTO with barcode from product
     */
    private ReceivingItemDTO mapToReceivingItem(PurchaseOrderItem item, UUID tenantId) {
        ReceivingItemDTO dto = new ReceivingItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setSku(item.getProductSku());
        dto.setQuantityOrdered(item.getQuantityOrdered());
        dto.setQuantityReceived(item.getQuantityReceived() != null ? item.getQuantityReceived() : BigDecimal.ZERO);
        dto.setQuantityPending(item.getQuantityOrdered().subtract(dto.getQuantityReceived()));
        dto.setUnitCost(item.getUnitCost());

        // Fetch barcode from product
        Optional<Product> productOpt = productRepository.findById(item.getProductId());
        String barcode = productOpt.map(Product::getBarcode).orElse("");
        dto.setBarcode(barcode);

        return dto;
    }
}
