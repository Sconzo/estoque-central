package com.estoquecentral.inventory.adapter.in.web;

import com.estoquecentral.inventory.adapter.in.dto.BelowMinimumStockResponse;
import com.estoquecentral.inventory.adapter.in.dto.BomVirtualStockResponse;
import com.estoquecentral.inventory.adapter.in.dto.InitializeVariantStockRequest;
import com.estoquecentral.inventory.adapter.in.dto.SetMinimumQuantityRequest;
import com.estoquecentral.inventory.adapter.in.dto.StockByLocationResponse;
import com.estoquecentral.inventory.adapter.in.dto.StockResponse;
import com.estoquecentral.inventory.application.InventoryService;
import com.estoquecentral.inventory.application.StockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * StockController - REST API for stock queries and management
 * Story 2.7: Multi-Warehouse Stock Control
 *
 * Endpoints:
 * - GET /api/stock - List all stock with filters
 * - GET /api/stock/product/{productId} - Get stock by product (aggregated)
 * - GET /api/stock/product/{productId}/by-location - Get stock by product drill-down
 * - GET /api/stock/variant/{variantId} - Get stock by variant (aggregated)
 * - GET /api/stock/variant/{variantId}/by-location - Get stock by variant drill-down
 * - PUT /api/stock/product/{productId}/minimum - Set minimum quantity for product
 * - PUT /api/stock/variant/{variantId}/minimum - Set minimum quantity for variant
 * - GET /api/stock/below-minimum - Get products below minimum stock
 * - GET /api/stock/product/{productId}/bom-virtual - Calculate stock for virtual BOM (AC4)
 */
@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;
    private final InventoryService inventoryService;

    public StockController(StockService stockService, InventoryService inventoryService) {
        this.stockService = stockService;
        this.inventoryService = inventoryService;
    }

    /**
     * AC2: Get all stock with optional filters
     * GET /api/stock?productId=xxx&variantId=xxx&locationId=xxx&belowMinimum=true
     */
    @GetMapping
    public ResponseEntity<List<StockResponse>> getAllStock(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID variantId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) Boolean belowMinimum
    ) {
        List<StockResponse> stock = stockService.getAllStock(tenantId, productId, variantId, locationId, belowMinimum);
        return ResponseEntity.ok(stock);
    }

    /**
     * AC2: Get stock by product (aggregated across all locations)
     * GET /api/stock/product/{productId}
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<StockByLocationResponse> getStockByProduct(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID productId
    ) {
        StockByLocationResponse response = stockService.getStockByProduct(tenantId, productId);
        return ResponseEntity.ok(response);
    }

    /**
     * AC2: Get stock by product with location drill-down (same as above, kept for clarity)
     * GET /api/stock/product/{productId}/by-location
     */
    @GetMapping("/product/{productId}/by-location")
    public ResponseEntity<StockByLocationResponse> getStockByProductByLocation(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID productId
    ) {
        StockByLocationResponse response = stockService.getStockByProduct(tenantId, productId);
        return ResponseEntity.ok(response);
    }

    /**
     * AC2: Get stock by variant (aggregated across all locations)
     * GET /api/stock/variant/{variantId}
     */
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<StockByLocationResponse> getStockByVariant(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID variantId
    ) {
        StockByLocationResponse response = stockService.getStockByVariant(tenantId, variantId);
        return ResponseEntity.ok(response);
    }

    /**
     * AC2: Get stock by variant with location drill-down
     * GET /api/stock/variant/{variantId}/by-location
     */
    @GetMapping("/variant/{variantId}/by-location")
    public ResponseEntity<StockByLocationResponse> getStockByVariantByLocation(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID variantId
    ) {
        StockByLocationResponse response = stockService.getStockByVariant(tenantId, variantId);
        return ResponseEntity.ok(response);
    }

    /**
     * AC5: Set minimum quantity for product at a specific location
     * PUT /api/stock/product/{productId}/minimum
     */
    @PutMapping("/product/{productId}/minimum")
    public ResponseEntity<Void> setMinimumQuantityForProduct(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID productId,
            @Valid @RequestBody SetMinimumQuantityRequest request
    ) {
        stockService.setMinimumQuantity(tenantId, productId, null, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Initialize stock for a product variant at a location.
     * POST /api/stock/variant/{variantId}/initialize
     */
    @PostMapping("/variant/{variantId}/initialize")
    public ResponseEntity<Void> initializeVariantStock(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID variantId,
            @Valid @RequestBody InitializeVariantStockRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        inventoryService.createInventoryForVariant(
                variantId,
                request.getInitialQuantity() != null ? request.getInitialQuantity() : java.math.BigDecimal.ZERO,
                request.getLocationId(),
                request.getMinimumQuantity(),
                request.getMaximumQuantity(),
                userId
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * AC5: Set minimum quantity for variant at a specific location
     * PUT /api/stock/variant/{variantId}/minimum
     */
    @PutMapping("/variant/{variantId}/minimum")
    public ResponseEntity<Void> setMinimumQuantityForVariant(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID variantId,
            @Valid @RequestBody SetMinimumQuantityRequest request
    ) {
        stockService.setMinimumQuantity(tenantId, null, variantId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * AC6: Get products below minimum stock (stock rupture alert)
     * GET /api/stock/below-minimum?locationId=xxx
     */
    @GetMapping("/below-minimum")
    public ResponseEntity<BelowMinimumStockResponse> getProductsBelowMinimum(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(required = false) UUID locationId
    ) {
        BelowMinimumStockResponse response = stockService.getProductsBelowMinimum(tenantId, locationId);
        return ResponseEntity.ok(response);
    }

    /**
     * AC4: Calculate stock for composite products with virtual BOM
     * GET /api/stock/product/{productId}/bom-virtual?locationId=xxx
     *
     * Calculates how many units of a composite product (kit) can be assembled
     * based on component stock. Uses MIN(component_stock / quantity_required).
     *
     * If locationId is provided, calculates for that specific location.
     * If locationId is null, aggregates stock across all locations.
     */
    @GetMapping("/product/{productId}/bom-virtual")
    public ResponseEntity<BomVirtualStockResponse> calculateBomVirtualStock(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID productId,
            @RequestParam(required = false) UUID locationId
    ) {
        BomVirtualStockResponse response = stockService.calculateBomVirtualStock(tenantId, productId, locationId);
        return ResponseEntity.ok(response);
    }
}
