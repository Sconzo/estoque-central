package com.estoquecentral.inventory.adapter.in.web;

import com.estoquecentral.inventory.adapter.in.dto.CreateStockMovementRequest;
import com.estoquecentral.inventory.adapter.in.dto.StockMovementFilters;
import com.estoquecentral.inventory.adapter.in.dto.StockMovementResponse;
import com.estoquecentral.inventory.application.StockMovementService;
import com.estoquecentral.inventory.domain.MovementType;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * StockMovementController - REST API for stock movement history
 * Story 2.8: Stock Movement History
 *
 * Endpoints:
 * - POST /api/stock-movements - Create manual movement (ENTRY, EXIT, ADJUSTMENT)
 * - GET /api/stock-movements - Query movements with filters
 * - GET /api/stock-movements/timeline - Get movement timeline for product/variant
 * - GET /api/stock-movements/validate-balance - Validate balance consistency
 */
@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    // ============================================================
    // AC1: Create Stock Movement
    // ============================================================

    /**
     * Create a manual stock movement
     * POST /api/stock-movements
     *
     * Supports: ENTRY, EXIT, ADJUSTMENT, TRANSFER_IN, TRANSFER_OUT
     * Automatically updates inventory and creates audit trail
     *
     * NOTE: Movements like SALE, PURCHASE are created automatically by their respective services
     */
    @PostMapping
    public ResponseEntity<StockMovementResponse> createMovement(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId,  // TODO: Get from security context in real app
            @Valid @RequestBody CreateStockMovementRequest request
    ) {
        StockMovementResponse response = stockMovementService.createMovement(tenantId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // AC2: Query Movements with Filters
    // ============================================================

    /**
     * Get stock movements with flexible filtering
     * GET /api/stock-movements?productId=xxx&locationId=xxx&type=ENTRY&startDate=xxx&endDate=xxx
     *
     * Supports multiple optional filters:
     * - productId: Filter by product
     * - variantId: Filter by variant
     * - locationId: Filter by location
     * - type: Filter by movement type (ENTRY, EXIT, etc.)
     * - startDate: Filter by date range start
     * - endDate: Filter by date range end
     * - documentType: Filter by source document type
     * - documentId: Filter by source document ID
     * - userId: Filter by user who performed the movement
     */
    @GetMapping
    public ResponseEntity<List<StockMovementResponse>> getMovements(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID variantId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) UUID documentId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "50") Integer size
    ) {
        StockMovementFilters filters = new StockMovementFilters();
        filters.setProductId(productId);
        filters.setVariantId(variantId);
        filters.setLocationId(locationId);
        filters.setType(type);
        filters.setStartDate(startDate);
        filters.setEndDate(endDate);
        filters.setDocumentType(documentType);
        filters.setDocumentId(documentId);
        filters.setUserId(userId);
        filters.setPage(page);
        filters.setSize(size);

        List<StockMovementResponse> movements = stockMovementService.getMovements(tenantId, filters);
        return ResponseEntity.ok(movements);
    }

    // ============================================================
    // AC3: Movement Timeline (Audit Trail)
    // ============================================================

    /**
     * Get complete movement timeline for a product or variant
     * GET /api/stock-movements/timeline?productId=xxx&locationId=xxx
     *
     * Returns chronological list of all movements for traceability
     * Useful for auditing and understanding stock history
     */
    @GetMapping("/timeline")
    public ResponseEntity<List<StockMovementResponse>> getMovementTimeline(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID variantId,
            @RequestParam(required = false) UUID locationId
    ) {
        if (productId == null && variantId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<StockMovementResponse> timeline = stockMovementService.getMovementTimeline(
                tenantId, productId, variantId, locationId
        );
        return ResponseEntity.ok(timeline);
    }

    // ============================================================
    // AC4: Validate Balance Consistency
    // ============================================================

    /**
     * Validate that movement history balance matches current inventory
     * GET /api/stock-movements/validate-balance?productId=xxx&locationId=xxx
     *
     * Returns 200 OK if balance is consistent
     * Returns 409 CONFLICT if balance is inconsistent (data integrity issue)
     */
    @GetMapping("/validate-balance")
    public ResponseEntity<Void> validateBalance(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID variantId,
            @RequestParam UUID locationId
    ) {
        if (productId == null && variantId == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean isValid = stockMovementService.validateBalance(tenantId, productId, variantId, locationId);

        if (isValid) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // ============================================================
    // Convenience Endpoints
    // ============================================================

    /**
     * Get recent movements (last 50) for quick overview
     * GET /api/stock-movements/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<StockMovementResponse>> getRecentMovements(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(defaultValue = "50") Integer limit
    ) {
        StockMovementFilters filters = new StockMovementFilters();
        filters.setSize(limit);

        List<StockMovementResponse> movements = stockMovementService.getMovements(tenantId, filters);
        return ResponseEntity.ok(movements);
    }

    /**
     * Get movements by document (e.g., all movements from a sale)
     * GET /api/stock-movements/by-document?documentType=SALE&documentId=xxx
     */
    @GetMapping("/by-document")
    public ResponseEntity<List<StockMovementResponse>> getMovementsByDocument(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam String documentType,
            @RequestParam UUID documentId
    ) {
        StockMovementFilters filters = new StockMovementFilters();
        filters.setDocumentType(documentType);
        filters.setDocumentId(documentId);

        List<StockMovementResponse> movements = stockMovementService.getMovements(tenantId, filters);
        return ResponseEntity.ok(movements);
    }
}
