package com.estoquecentral.inventory.adapter.in.web;

import com.estoquecentral.inventory.adapter.in.dto.CreateStockTransferRequest;
import com.estoquecentral.inventory.adapter.in.dto.StockTransferResponse;
import com.estoquecentral.inventory.application.StockTransferService;
import com.estoquecentral.inventory.application.StockTransferService.StockTransferFilters;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * StockTransferController - REST API for stock transfers
 * Story 2.9: Stock Transfer Between Locations
 *
 * Endpoints:
 * - POST /api/stock-transfers - Create transfer
 * - GET /api/stock-transfers - Get transfer history with filters
 * - GET /api/stock-transfers/product/{productId} - Get transfers for product
 * - GET /api/stock-transfers/from/{locationId} - Get transfers from location
 * - GET /api/stock-transfers/to/{locationId} - Get transfers to location
 */
@RestController
@RequestMapping("/api/stock-transfers")
public class StockTransferController {

    private final StockTransferService stockTransferService;

    public StockTransferController(StockTransferService stockTransferService) {
        this.stockTransferService = stockTransferService;
    }

    // ============================================================
    // AC2: Create Stock Transfer
    // ============================================================

    /**
     * Create a stock transfer between locations
     * POST /api/stock-transfers
     *
     * Validates:
     * - Stock availability at origin
     * - Origin != Destination
     * - Quantity > 0
     *
     * Creates:
     * - Transfer record
     * - Updates inventory (origin and destination)
     * - Two stock movements (TRANSFER_OUT, TRANSFER_IN)
     */
    @PostMapping
    public ResponseEntity<StockTransferResponse> createTransfer(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId,  // TODO: Get from security context
            @Valid @RequestBody CreateStockTransferRequest request
    ) {
        try {
            StockTransferResponse response = stockTransferService.createTransfer(tenantId, request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Validation errors (400 Bad Request)
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================================
    // AC5: Get Transfer History with Filters
    // ============================================================

    /**
     * Get transfer history with optional filters
     * GET /api/stock-transfers?productId=xxx&originLocationId=xxx&destinationLocationId=xxx&startDate=xxx&endDate=xxx
     *
     * Filters:
     * - productId: Filter by product
     * - variantId: Filter by variant
     * - originLocationId: Filter by origin location
     * - destinationLocationId: Filter by destination location
     * - startDate: Filter by date range start
     * - endDate: Filter by date range end
     * - userId: Filter by user who performed transfer
     */
    @GetMapping
    public ResponseEntity<List<StockTransferResponse>> getTransferHistory(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID variantId,
            @RequestParam(required = false) UUID originLocationId,
            @RequestParam(required = false) UUID destinationLocationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) UUID userId
    ) {
        StockTransferFilters filters = new StockTransferFilters();
        filters.setProductId(productId);
        filters.setVariantId(variantId);
        filters.setOriginLocationId(originLocationId);
        filters.setDestinationLocationId(destinationLocationId);
        filters.setStartDate(startDate);
        filters.setEndDate(endDate);
        filters.setUserId(userId);

        List<StockTransferResponse> transfers = stockTransferService.getTransferHistory(tenantId, filters);
        return ResponseEntity.ok(transfers);
    }

    // ============================================================
    // Convenience Endpoints
    // ============================================================

    /**
     * Get all transfers for a specific product
     * GET /api/stock-transfers/product/{productId}
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockTransferResponse>> getTransfersForProduct(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID productId
    ) {
        List<StockTransferResponse> transfers = stockTransferService.getTransfersForProduct(tenantId, productId);
        return ResponseEntity.ok(transfers);
    }

    /**
     * Get all transfers FROM a specific location
     * GET /api/stock-transfers/from/{locationId}
     */
    @GetMapping("/from/{locationId}")
    public ResponseEntity<List<StockTransferResponse>> getTransfersFromLocation(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID locationId
    ) {
        List<StockTransferResponse> transfers = stockTransferService.getTransfersFromLocation(tenantId, locationId);
        return ResponseEntity.ok(transfers);
    }

    /**
     * Get all transfers TO a specific location
     * GET /api/stock-transfers/to/{locationId}
     */
    @GetMapping("/to/{locationId}")
    public ResponseEntity<List<StockTransferResponse>> getTransfersToLocation(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID locationId
    ) {
        List<StockTransferResponse> transfers = stockTransferService.getTransfersToLocation(tenantId, locationId);
        return ResponseEntity.ok(transfers);
    }
}
