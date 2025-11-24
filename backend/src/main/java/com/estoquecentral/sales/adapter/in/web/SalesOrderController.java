package com.estoquecentral.sales.adapter.in.web;

import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.adapter.out.variant.ProductVariantRepository;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.variant.ProductVariant;
import com.estoquecentral.inventory.adapter.out.LocationRepository;
import com.estoquecentral.inventory.application.StockAvailabilityService;
import com.estoquecentral.inventory.domain.Location;
import com.estoquecentral.sales.adapter.in.dto.*;
import com.estoquecentral.sales.adapter.out.CustomerRepository;
import com.estoquecentral.sales.application.SalesOrderService;
import com.estoquecentral.sales.domain.Customer;
import com.estoquecentral.sales.domain.SalesOrder;
import com.estoquecentral.sales.domain.SalesOrderItem;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SalesOrderController - REST API for B2B sales order management
 * Story 4.5: Sales Order B2B Interface
 *
 * Endpoints:
 * - POST /api/sales-orders - Create sales order (DRAFT)
 * - GET /api/sales-orders - List/search sales orders
 * - GET /api/sales-orders/{id} - Get sales order by ID
 * - PUT /api/sales-orders/{id} - Update sales order (only DRAFT)
 * - PUT /api/sales-orders/{id}/confirm - Confirm order (DRAFT → CONFIRMED)
 * - DELETE /api/sales-orders/{id} - Cancel order
 * - GET /api/stock/availability - Get stock availability
 */
@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final CustomerRepository customerRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final StockAvailabilityService stockAvailabilityService;

    public SalesOrderController(
            SalesOrderService salesOrderService,
            CustomerRepository customerRepository,
            LocationRepository locationRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            StockAvailabilityService stockAvailabilityService) {
        this.salesOrderService = salesOrderService;
        this.customerRepository = customerRepository;
        this.locationRepository = locationRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.stockAvailabilityService = stockAvailabilityService;
    }

    /**
     * Create a new sales order
     * POST /api/sales-orders
     */
    @PostMapping
    public ResponseEntity<SalesOrderResponseDTO> createSalesOrder(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId,
            @Valid @RequestBody SalesOrderRequestDTO request
    ) {
        try {
            // Convert request items to service data
            List<SalesOrderService.SalesOrderItemData> itemsData = request.getItems().stream()
                .map(item -> new SalesOrderService.SalesOrderItemData(
                    item.getProductId(),
                    item.getVariantId(),
                    item.getQuantity(),
                    item.getUnitPrice()
                ))
                .collect(Collectors.toList());

            // Create sales order
            SalesOrder order = salesOrderService.createOrder(
                tenantId,
                request.getCustomerId(),
                request.getStockLocationId(),
                request.getOrderDate(),
                request.getDeliveryDateExpected(),
                request.getPaymentTerms() != null ? request.getPaymentTerms().name() : null,
                request.getNotes(),
                itemsData,
                userId
            );

            // Get full details with items
            Optional<SalesOrderService.SalesOrderWithItems> orderWithItems =
                salesOrderService.getById(tenantId, order.getId());

            if (orderWithItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            SalesOrderResponseDTO response = mapToResponse(tenantId, orderWithItems.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get sales order by ID
     * GET /api/sales-orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SalesOrderResponseDTO> getSalesOrderById(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID id
    ) {
        Optional<SalesOrderService.SalesOrderWithItems> orderWithItems =
            salesOrderService.getById(tenantId, id);

        if (orderWithItems.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SalesOrderResponseDTO response = mapToResponse(tenantId, orderWithItems.get());
        return ResponseEntity.ok(response);
    }

    /**
     * Search sales orders with filters and pagination
     * GET /api/sales-orders
     *
     * Query params:
     * - customer_id (optional)
     * - status (optional)
     * - order_date_from (optional, format: yyyy-MM-dd)
     * - order_date_to (optional, format: yyyy-MM-dd)
     * - order_number (optional)
     * - page (default: 0)
     * - size (default: 20)
     * - sort (default: dataCriacao,desc)
     */
    @GetMapping
    public ResponseEntity<Page<SalesOrderResponseDTO>> searchSalesOrders(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(required = false, name = "customer_id") UUID customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "order_date_from") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate orderDateFrom,
            @RequestParam(required = false, name = "order_date_to") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate orderDateTo,
            @RequestParam(required = false, name = "order_number") String orderNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dataCriacao,desc") String[] sort
    ) {
        // Parse sort
        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        String sortField = sort.length > 0 ? sort[0] : "dataCriacao";

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<SalesOrder> orderPage = salesOrderService.search(
            tenantId,
            customerId,
            status,
            orderDateFrom,
            orderDateTo,
            orderNumber,
            pageable
        );

        // Map to response (without items for list view)
        Page<SalesOrderResponseDTO> responsePage = orderPage.map(order -> mapToResponseSummary(tenantId, order));

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Update sales order (only DRAFT)
     * PUT /api/sales-orders/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SalesOrderResponseDTO> updateSalesOrder(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody SalesOrderRequestDTO request
    ) {
        try {
            // Convert request items to service data
            List<SalesOrderService.SalesOrderItemData> itemsData = request.getItems().stream()
                .map(item -> new SalesOrderService.SalesOrderItemData(
                    item.getProductId(),
                    item.getVariantId(),
                    item.getQuantity(),
                    item.getUnitPrice()
                ))
                .collect(Collectors.toList());

            // Update sales order
            SalesOrder order = salesOrderService.updateOrder(
                tenantId,
                id,
                request.getCustomerId(),
                request.getStockLocationId(),
                request.getOrderDate(),
                request.getDeliveryDateExpected(),
                request.getPaymentTerms() != null ? request.getPaymentTerms().name() : null,
                request.getNotes(),
                itemsData,
                userId
            );

            // Get full details with items
            Optional<SalesOrderService.SalesOrderWithItems> orderWithItems =
                salesOrderService.getById(tenantId, order.getId());

            if (orderWithItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            SalesOrderResponseDTO response = mapToResponse(tenantId, orderWithItems.get());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Confirm sales order (DRAFT → CONFIRMED)
     * PUT /api/sales-orders/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<SalesOrderResponseDTO> confirmSalesOrder(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId,
            @PathVariable UUID id
    ) {
        try {
            SalesOrder order = salesOrderService.confirmOrder(tenantId, id, userId);

            Optional<SalesOrderService.SalesOrderWithItems> orderWithItems =
                salesOrderService.getById(tenantId, order.getId());

            if (orderWithItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            SalesOrderResponseDTO response = mapToResponse(tenantId, orderWithItems.get());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            // Insufficient stock or invalid status
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cancel sales order
     * DELETE /api/sales-orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelSalesOrder(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId,
            @PathVariable UUID id
    ) {
        try {
            salesOrderService.cancelOrder(tenantId, id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get stock availability
     * GET /api/stock/availability?productId=X&variantId=Y&locationId=Z
     */
    @GetMapping("/stock/availability")
    public ResponseEntity<StockAvailabilityDTO> getStockAvailability(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(required = false, name = "productId") UUID productId,
            @RequestParam(required = false, name = "variantId") UUID variantId,
            @RequestParam(name = "locationId") UUID locationId
    ) {
        try {
            StockAvailabilityService.StockAvailabilityDTO availability =
                stockAvailabilityService.getAvailability(tenantId, productId, variantId, locationId);

            StockAvailabilityDTO response = new StockAvailabilityDTO(
                availability.available(),
                availability.reserved(),
                availability.forSale(),
                availability.inStock()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods to map entities to DTOs

    /**
     * Map SalesOrder with items to full response
     */
    private SalesOrderResponseDTO mapToResponse(UUID tenantId, SalesOrderService.SalesOrderWithItems orderWithItems) {
        SalesOrder order = orderWithItems.order();
        List<SalesOrderItem> items = orderWithItems.items();

        SalesOrderResponseDTO response = new SalesOrderResponseDTO();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus());
        response.setOrderDate(order.getOrderDate());
        response.setDeliveryDateExpected(order.getDeliveryDateExpected());
        response.setPaymentTerms(order.getPaymentTerms());
        response.setTotalAmount(order.getTotalAmount());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getDataCriacao());
        response.setUpdatedAt(order.getUpdatedAt());

        // Fetch customer summary
        Optional<Customer> customerOpt = customerRepository.findById(order.getCustomerId());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            response.setCustomer(new SalesOrderResponseDTO.CustomerSummary(
                customer.getId(),
                customer.getFullName(),
                customer.isBusiness() ? customer.getCnpj() : customer.getCpf()
            ));
        }

        // Fetch location summary
        Optional<Location> locationOpt = locationRepository.findById(order.getStockLocationId());
        if (locationOpt.isPresent()) {
            Location location = locationOpt.get();
            response.setLocation(new SalesOrderResponseDTO.LocationSummary(
                location.getId(),
                location.getName()
            ));
        }

        // Map items with stock info
        List<SalesOrderItemResponseDTO> itemResponses = items.stream()
            .map(item -> mapItemToResponse(tenantId, item, order.getStockLocationId()))
            .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    /**
     * Map SalesOrder to summary response (without items)
     */
    private SalesOrderResponseDTO mapToResponseSummary(UUID tenantId, SalesOrder order) {
        SalesOrderResponseDTO response = new SalesOrderResponseDTO();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus());
        response.setOrderDate(order.getOrderDate());
        response.setDeliveryDateExpected(order.getDeliveryDateExpected());
        response.setPaymentTerms(order.getPaymentTerms());
        response.setTotalAmount(order.getTotalAmount());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getDataCriacao());
        response.setUpdatedAt(order.getUpdatedAt());

        // Fetch customer summary
        Optional<Customer> customerOpt = customerRepository.findById(order.getCustomerId());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            response.setCustomer(new SalesOrderResponseDTO.CustomerSummary(
                customer.getId(),
                customer.getFullName(),
                customer.isBusiness() ? customer.getCnpj() : customer.getCpf()
            ));
        }

        // Fetch location summary
        Optional<Location> locationOpt = locationRepository.findById(order.getStockLocationId());
        if (locationOpt.isPresent()) {
            Location location = locationOpt.get();
            response.setLocation(new SalesOrderResponseDTO.LocationSummary(
                location.getId(),
                location.getName()
            ));
        }

        return response;
    }

    /**
     * Map SalesOrderItem to response with stock availability
     */
    private SalesOrderItemResponseDTO mapItemToResponse(UUID tenantId, SalesOrderItem item, UUID locationId) {
        SalesOrderItemResponseDTO response = new SalesOrderItemResponseDTO();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setVariantId(item.getVariantId());
        response.setQuantityOrdered(item.getQuantityOrdered());
        response.setQuantityReserved(item.getQuantityReserved());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getTotalPrice());

        // Fetch product/variant name and SKU
        if (item.hasProduct()) {
            Optional<Product> product = productRepository.findById(item.getProductId());
            if (product.isPresent()) {
                response.setProductName(product.get().getName());
                response.setProductSku(product.get().getSku());
            }
        } else if (item.hasVariant()) {
            Optional<ProductVariant> variant = variantRepository.findById(item.getVariantId());
            if (variant.isPresent()) {
                response.setProductName(variant.get().getName());
                response.setProductSku(variant.get().getSku());
            }
        }

        // Fetch stock availability
        try {
            StockAvailabilityService.StockAvailabilityDTO availability =
                stockAvailabilityService.getAvailability(
                    tenantId,
                    item.getProductId(),
                    item.getVariantId(),
                    locationId
                );

            response.setStockInfo(new SalesOrderItemResponseDTO.StockInfo(
                availability.available(),
                availability.reserved(),
                availability.forSale()
            ));
        } catch (Exception e) {
            // If stock info not available, set zeros
            response.setStockInfo(new SalesOrderItemResponseDTO.StockInfo(
                java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO
            ));
        }

        return response;
    }
}
