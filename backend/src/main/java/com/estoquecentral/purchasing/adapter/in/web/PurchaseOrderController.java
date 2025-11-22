package com.estoquecentral.purchasing.adapter.in.web;

import com.estoquecentral.purchasing.adapter.in.dto.*;
import com.estoquecentral.purchasing.application.PurchaseOrderService;
import com.estoquecentral.purchasing.domain.PurchaseOrder;
import com.estoquecentral.purchasing.domain.PurchaseOrderItem;
import com.estoquecentral.purchasing.domain.PurchaseOrderStatus;
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
 * PurchaseOrderController - REST API for purchase order management
 * Story 3.2: Purchase Order Creation
 *
 * Endpoints:
 * - POST /api/purchase-orders - Create purchase order
 * - GET /api/purchase-orders - List purchase orders with pagination and filters
 * - GET /api/purchase-orders/{id} - Get purchase order by ID with items
 * - PUT /api/purchase-orders/{id}/status - Update status
 * - DELETE /api/purchase-orders/{id} - Delete (only DRAFT)
 */
@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    /**
     * Create a new purchase order
     * POST /api/purchase-orders
     */
    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId,
            @Valid @RequestBody CreatePurchaseOrderRequest request
    ) {
        try {
            // Convert request items to service data
            List<PurchaseOrderService.PurchaseOrderItemData> itemsData = request.getItems().stream()
                .map(item -> new PurchaseOrderService.PurchaseOrderItemData(
                    item.getProductId(),
                    item.getVariantId(),
                    null, // productSku will be fetched from product
                    null, // productName will be fetched from product
                    item.getQuantityOrdered(),
                    item.getUnitCost(),
                    item.getNotes()
                ))
                .collect(Collectors.toList());

            // Create purchase order
            PurchaseOrder po = purchaseOrderService.createPurchaseOrder(
                tenantId,
                request.getSupplierId(),
                request.getStockLocationId(),
                request.getOrderDate(),
                request.getExpectedDeliveryDate(),
                request.getNotes(),
                itemsData,
                userId
            );

            // Get full details with items
            Optional<PurchaseOrderService.PurchaseOrderWithItems> poWithItems =
                purchaseOrderService.getPurchaseOrderById(tenantId, po.getId());

            if (poWithItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            PurchaseOrderResponse response = mapToResponse(poWithItems.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get purchase order by ID
     * GET /api/purchase-orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderById(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID id
    ) {
        Optional<PurchaseOrderService.PurchaseOrderWithItems> poWithItems =
            purchaseOrderService.getPurchaseOrderById(tenantId, id);

        if (poWithItems.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PurchaseOrderResponse response = mapToResponse(poWithItems.get());
        return ResponseEntity.ok(response);
    }

    /**
     * Search purchase orders with filters and pagination
     * GET /api/purchase-orders
     *
     * Query params:
     * - supplier_id (optional)
     * - status (optional)
     * - order_date_from (optional, format: yyyy-MM-dd)
     * - order_date_to (optional, format: yyyy-MM-dd)
     * - order_number (optional)
     * - page (default: 0)
     * - size (default: 20)
     * - sort (default: createdAt,desc)
     */
    @GetMapping
    public ResponseEntity<Page<PurchaseOrderResponse>> searchPurchaseOrders(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(required = false, name = "supplier_id") UUID supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "order_date_from") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate orderDateFrom,
            @RequestParam(required = false, name = "order_date_to") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate orderDateTo,
            @RequestParam(required = false, name = "order_number") String orderNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        // Parse sort
        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        String sortField = sort.length > 0 ? sort[0] : "createdAt";

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<PurchaseOrder> poPage = purchaseOrderService.searchPurchaseOrders(
            tenantId,
            supplierId,
            status,
            orderDateFrom,
            orderDateTo,
            orderNumber,
            pageable
        );

        // Map to response (without items for list view)
        Page<PurchaseOrderResponse> responsePage = poPage.map(this::mapToResponseSummary);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Update purchase order status
     * PUT /api/purchase-orders/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        try {
            purchaseOrderService.updateStatus(tenantId, id, request.getStatus(), userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            // Invalid status transition
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete purchase order (only DRAFT)
     * DELETE /api/purchase-orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID id
    ) {
        try {
            purchaseOrderService.deletePurchaseOrder(tenantId, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            // Can only delete DRAFT
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods to map entities to DTOs

    /**
     * Map PurchaseOrder with items to full response
     */
    private PurchaseOrderResponse mapToResponse(PurchaseOrderService.PurchaseOrderWithItems poWithItems) {
        PurchaseOrder po = poWithItems.purchaseOrder();
        List<PurchaseOrderItem> items = poWithItems.items();

        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.setId(po.getId());
        response.setPoNumber(po.getPoNumber());
        response.setStatus(po.getStatus());
        response.setOrderDate(po.getOrderDate());
        response.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        response.setTotalAmount(po.getTotal());
        response.setNotes(po.getNotes());
        response.setCreatedAt(po.getCreatedAt());
        response.setUpdatedAt(po.getUpdatedAt());

        // Map items
        List<PurchaseOrderItemResponse> itemResponses = items.stream()
            .map(this::mapItemToResponse)
            .collect(Collectors.toList());
        response.setItems(itemResponses);

        // Note: Supplier and Location summaries would require additional repository calls
        // For now, using minimal data. Can be enhanced later.

        return response;
    }

    /**
     * Map PurchaseOrder to summary response (without items)
     */
    private PurchaseOrderResponse mapToResponseSummary(PurchaseOrder po) {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.setId(po.getId());
        response.setPoNumber(po.getPoNumber());
        response.setStatus(po.getStatus());
        response.setOrderDate(po.getOrderDate());
        response.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        response.setTotalAmount(po.getTotal());
        response.setNotes(po.getNotes());
        response.setCreatedAt(po.getCreatedAt());
        response.setUpdatedAt(po.getUpdatedAt());

        return response;
    }

    /**
     * Map PurchaseOrderItem to response
     */
    private PurchaseOrderItemResponse mapItemToResponse(PurchaseOrderItem item) {
        PurchaseOrderItemResponse response = new PurchaseOrderItemResponse();
        response.setId(item.getId());
        response.setQuantityOrdered(item.getQuantityOrdered());
        response.setQuantityReceived(item.getQuantityReceived());
        response.setUnitCost(item.getUnitCost());
        response.setTotalCost(item.getTotal());
        response.setNotes(item.getNotes());

        // Product summary from snapshot data in item
        PurchaseOrderItemResponse.ProductSummary productSummary =
            new PurchaseOrderItemResponse.ProductSummary(
                item.getProductId(),
                item.getProductSku(),
                item.getProductName()
            );
        response.setProduct(productSummary);

        return response;
    }
}
