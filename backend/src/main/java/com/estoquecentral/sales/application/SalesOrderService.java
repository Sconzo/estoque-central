package com.estoquecentral.sales.application;

import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.adapter.out.variant.ProductVariantRepository;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.variant.ProductVariant;
import com.estoquecentral.inventory.adapter.out.LocationRepository;
import com.estoquecentral.inventory.application.StockAvailabilityService;
import com.estoquecentral.inventory.domain.Location;
import com.estoquecentral.sales.adapter.out.CustomerRepository;
import com.estoquecentral.sales.adapter.out.SalesOrderItemRepository;
import com.estoquecentral.sales.adapter.out.SalesOrderRepository;
import com.estoquecentral.sales.domain.Customer;
import com.estoquecentral.sales.domain.SalesOrder;
import com.estoquecentral.sales.domain.SalesOrderItem;
import com.estoquecentral.sales.domain.SalesOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SalesOrderService - Business logic for B2B sales order management
 * Story 4.5: Sales Order B2B Interface
 *
 * Manages sales order lifecycle: create, update, confirm, cancel
 * Stock validation occurs on confirm (actual reservation is Story 4.6)
 */
@Service
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final CustomerRepository customerRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final SalesOrderNumberGenerator numberGenerator;
    private final StockAvailabilityService stockAvailabilityService;

    public SalesOrderService(
            SalesOrderRepository salesOrderRepository,
            SalesOrderItemRepository salesOrderItemRepository,
            CustomerRepository customerRepository,
            LocationRepository locationRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            SalesOrderNumberGenerator numberGenerator,
            StockAvailabilityService stockAvailabilityService) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.customerRepository = customerRepository;
        this.locationRepository = locationRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.numberGenerator = numberGenerator;
        this.stockAvailabilityService = stockAvailabilityService;
    }

    /**
     * Create a new sales order in DRAFT status
     */
    @Transactional
    public SalesOrder createOrder(
            UUID tenantId,
            UUID customerId,
            UUID stockLocationId,
            LocalDate orderDate,
            LocalDate deliveryDateExpected,
            String paymentTerms,
            String notes,
            List<SalesOrderItemData> items,
            UUID userId) {

        // Validate at least one item
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Sales order must have at least one item");
        }

        // Validate customer exists and is active
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        if (!customer.getAtivo()) {
            throw new IllegalArgumentException("Customer is not active: " + customerId);
        }

        // Validate location exists
        Location location = locationRepository.findById(stockLocationId)
            .orElseThrow(() -> new IllegalArgumentException("Location not found: " + stockLocationId));

        // Create sales order
        SalesOrder order = new SalesOrder();
        order.setTenantId(tenantId);
        order.setCustomerId(customerId);
        order.setStockLocationId(stockLocationId);
        order.setOrderDate(orderDate != null ? orderDate : LocalDate.now());
        order.setDeliveryDateExpected(deliveryDateExpected);

        if (paymentTerms != null) {
            order.setPaymentTerms(com.estoquecentral.sales.domain.PaymentTerms.valueOf(paymentTerms));
        }

        order.setNotes(notes);
        order.setStatus(SalesOrderStatus.DRAFT);
        order.setCreatedByUserId(userId);
        order.setDataCriacao(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Generate order number
        String orderNumber = numberGenerator.generateOrderNumber(tenantId);
        order.setOrderNumber(orderNumber);

        // Save order (to get ID)
        SalesOrder savedOrder = salesOrderRepository.save(order);

        // Create and save items
        BigDecimal total = BigDecimal.ZERO;
        for (SalesOrderItemData itemData : items) {
            SalesOrderItem item = new SalesOrderItem();
            item.setSalesOrderId(savedOrder.getId());
            item.setProductId(itemData.productId());
            item.setVariantId(itemData.variantId());
            item.setQuantityOrdered(itemData.quantity());
            item.setQuantityReserved(BigDecimal.ZERO); // Reserved in Story 4.6
            item.setUnitPrice(itemData.unitPrice());
            item.calculateTotal();

            salesOrderItemRepository.save(item);

            total = total.add(item.getTotalPrice());
        }

        // Update order total
        savedOrder.setTotalAmount(total);
        salesOrderRepository.save(savedOrder);

        return savedOrder;
    }

    /**
     * Update a sales order (only if DRAFT)
     */
    @Transactional
    public SalesOrder updateOrder(
            UUID tenantId,
            UUID orderId,
            UUID customerId,
            UUID stockLocationId,
            LocalDate orderDate,
            LocalDate deliveryDateExpected,
            String paymentTerms,
            String notes,
            List<SalesOrderItemData> items,
            UUID userId) {

        SalesOrder order = salesOrderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + orderId));

        // Verify tenant
        if (!order.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Sales order does not belong to this tenant");
        }

        // Can only update DRAFT orders
        if (!order.isDraft()) {
            throw new IllegalStateException("Can only update DRAFT orders. Current status: " + order.getStatus());
        }

        // Update order fields
        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
            if (!customer.getAtivo()) {
                throw new IllegalArgumentException("Customer is not active: " + customerId);
            }
            order.setCustomerId(customerId);
        }

        if (stockLocationId != null) {
            locationRepository.findById(stockLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + stockLocationId));
            order.setStockLocationId(stockLocationId);
        }

        if (orderDate != null) {
            order.setOrderDate(orderDate);
        }

        order.setDeliveryDateExpected(deliveryDateExpected);

        if (paymentTerms != null) {
            order.setPaymentTerms(com.estoquecentral.sales.domain.PaymentTerms.valueOf(paymentTerms));
        }

        order.setNotes(notes);
        order.setUpdatedBy(userId);
        order.setUpdatedAt(LocalDateTime.now());

        // Update items if provided
        if (items != null && !items.isEmpty()) {
            // Delete existing items
            salesOrderItemRepository.deleteBySalesOrderId(orderId);

            // Create new items
            BigDecimal total = BigDecimal.ZERO;
            for (SalesOrderItemData itemData : items) {
                SalesOrderItem item = new SalesOrderItem();
                item.setSalesOrderId(order.getId());
                item.setProductId(itemData.productId());
                item.setVariantId(itemData.variantId());
                item.setQuantityOrdered(itemData.quantity());
                item.setQuantityReserved(BigDecimal.ZERO);
                item.setUnitPrice(itemData.unitPrice());
                item.calculateTotal();

                salesOrderItemRepository.save(item);

                total = total.add(item.getTotalPrice());
            }

            // Update total
            order.setTotalAmount(total);
        }

        return salesOrderRepository.save(order);
    }

    /**
     * Confirm sales order - validates stock availability
     * DRAFT → CONFIRMED
     * Note: Actual stock reservation happens in Story 4.6
     */
    @Transactional
    public SalesOrder confirmOrder(UUID tenantId, UUID orderId, UUID userId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + orderId));

        // Verify tenant
        if (!order.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Sales order does not belong to this tenant");
        }

        // Can only confirm DRAFT orders
        if (!order.canBeConfirmed()) {
            throw new IllegalStateException("Cannot confirm order in status: " + order.getStatus());
        }

        // Validate stock availability for all items
        List<SalesOrderItem> items = salesOrderItemRepository.findBySalesOrderId(orderId);

        for (SalesOrderItem item : items) {
            boolean available = stockAvailabilityService.isAvailable(
                tenantId,
                item.getProductId(),
                item.getVariantId(),
                order.getStockLocationId(),
                item.getQuantityOrdered()
            );

            if (!available) {
                String itemName = getItemName(item);
                throw new IllegalStateException(
                    "Insufficient stock for item: " + itemName +
                    ". Required: " + item.getQuantityOrdered()
                );
            }
        }

        // Update status to CONFIRMED
        order.confirm();
        order.setUpdatedBy(userId);

        return salesOrderRepository.save(order);
    }

    /**
     * Cancel sales order
     */
    @Transactional
    public SalesOrder cancelOrder(UUID tenantId, UUID orderId, UUID userId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + orderId));

        // Verify tenant
        if (!order.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Sales order does not belong to this tenant");
        }

        // Can cancel DRAFT or CONFIRMED orders
        if (!order.canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        order.cancel();
        order.setUpdatedBy(userId);

        // Note: If order was CONFIRMED and stock was reserved (Story 4.6),
        // we would unreserve stock here

        return salesOrderRepository.save(order);
    }

    /**
     * Get sales order by ID with items
     */
    @Transactional(readOnly = true)
    public Optional<SalesOrderWithItems> getById(UUID tenantId, UUID orderId) {
        Optional<SalesOrder> orderOpt = salesOrderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        SalesOrder order = orderOpt.get();

        // Verify tenant
        if (!order.getTenantId().equals(tenantId)) {
            return Optional.empty();
        }

        List<SalesOrderItem> items = salesOrderItemRepository.findBySalesOrderId(orderId);

        return Optional.of(new SalesOrderWithItems(order, items));
    }

    /**
     * Search sales orders with filters and pagination
     */
    @Transactional(readOnly = true)
    public Page<SalesOrder> search(
            UUID tenantId,
            UUID customerId,
            String status,
            LocalDate orderDateFrom,
            LocalDate orderDateTo,
            String orderNumber,
            Pageable pageable) {

        return salesOrderRepository.search(
            tenantId,
            customerId,
            status,
            orderDateFrom,
            orderDateTo,
            orderNumber,
            pageable
        );
    }

    /**
     * Get all sales orders for a tenant
     */
    @Transactional(readOnly = true)
    public List<SalesOrder> getAllOrders(UUID tenantId) {
        return salesOrderRepository.findByTenantId(tenantId);
    }

    /**
     * Get sales orders by status
     */
    @Transactional(readOnly = true)
    public List<SalesOrder> getOrdersByStatus(UUID tenantId, SalesOrderStatus status) {
        return salesOrderRepository.findByTenantIdAndStatus(tenantId, status.name());
    }

    /**
     * Get overdue sales orders
     */
    @Transactional(readOnly = true)
    public List<SalesOrder> getOverdueOrders(UUID tenantId) {
        return salesOrderRepository.findOverdueSalesOrders(tenantId);
    }

    /**
     * Helper: Get item name for error messages
     */
    private String getItemName(SalesOrderItem item) {
        if (item.hasProduct()) {
            Optional<Product> product = productRepository.findById(item.getProductId());
            return product.map(Product::getName).orElse("Unknown Product");
        } else if (item.hasVariant()) {
            Optional<ProductVariant> variant = variantRepository.findById(item.getVariantId());
            return variant.map(ProductVariant::getSku).orElse("Unknown Variant");
        }
        return "Unknown Item";
    }

    /**
     * Data class for creating/updating sales order items
     */
    public record SalesOrderItemData(
        UUID productId,
        UUID variantId,
        BigDecimal quantity,
        BigDecimal unitPrice
    ) {}

    /**
     * Data class for sales order with items
     */
    public record SalesOrderWithItems(
        SalesOrder order,
        List<SalesOrderItem> items
    ) {}
}
