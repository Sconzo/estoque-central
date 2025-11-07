package com.estoquecentral.reporting.adapter.in.web;

import com.estoquecentral.reporting.adapter.in.dto.*;
import com.estoquecentral.reporting.application.InventoryMovementReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Inventory Movement Report REST Controller
 * Endpoints for inventory movement reporting with advanced filters and CSV export
 */
@RestController
@RequestMapping("/api/reports/inventory-movements")
public class InventoryMovementReportController {

    private final InventoryMovementReportService service;

    public InventoryMovementReportController(InventoryMovementReportService service) {
        this.service = service;
    }

    /**
     * GET /api/reports/inventory-movements
     * Get detailed inventory movements with filters
     *
     * Query params:
     * - startDate: Start date (YYYY-MM-DD)
     * - endDate: End date (YYYY-MM-DD)
     * - productId: Filter by product UUID
     * - locationId: Filter by location UUID
     * - movementType: PURCHASE, SALE, ADJUSTMENT_IN, ADJUSTMENT_OUT, etc
     * - movementDirection: IN or OUT
     * - limit: Max results (default: 1000, max: 10000)
     *
     * Response:
     * [
     *   {
     *     "movementDate": "2025-11-07T10:30:00",
     *     "movementType": "PURCHASE",
     *     "movementDirection": "IN",
     *     "sku": "NOTE-001",
     *     "productName": "Notebook Dell",
     *     "locationName": "Armazém Principal",
     *     "quantity": 10,
     *     "unitCost": 3800.00,
     *     "totalValue": 38000.00
     *   }
     * ]
     */
    @GetMapping
    public ResponseEntity<List<InventoryMovementDetailDTO>> getMovements(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) String movementDirection,
            @RequestParam(defaultValue = "1000") Integer limit
    ) {
        InventoryMovementFilterDTO filter = new InventoryMovementFilterDTO(
                startDate,
                endDate,
                productId,
                locationId,
                movementType,
                movementDirection,
                limit
        );

        List<InventoryMovementDetailDTO> movements = service.getMovements(filter);
        return ResponseEntity.ok(movements);
    }

    /**
     * GET /api/reports/inventory-movements/complete
     * Get complete report with movements and totals
     *
     * Response:
     * {
     *   "movements": [ ... ],
     *   "totals": {
     *     "totalMovements": 150,
     *     "inCount": 80,
     *     "outCount": 70,
     *     "totalQuantityIn": 500,
     *     "totalQuantityOut": 320,
     *     "totalValueIn": 150000.00,
     *     "totalValueOut": 98000.00
     *   },
     *   "count": 150,
     *   "filter": { ... },
     *   "hasMoreResults": false
     * }
     */
    @GetMapping("/complete")
    public ResponseEntity<Map<String, Object>> getCompleteReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) String movementDirection,
            @RequestParam(defaultValue = "1000") Integer limit
    ) {
        InventoryMovementFilterDTO filter = new InventoryMovementFilterDTO(
                startDate,
                endDate,
                productId,
                locationId,
                movementType,
                movementDirection,
                limit
        );

        Map<String, Object> report = service.getCompleteReport(filter);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/reports/inventory-movements/recent
     * Get recent movements (last 30 days, max 1000)
     *
     * Response: Same as GET /movements
     */
    @GetMapping("/recent")
    public ResponseEntity<List<InventoryMovementDetailDTO>> getRecentMovements() {
        List<InventoryMovementDetailDTO> movements = service.getRecentMovements();
        return ResponseEntity.ok(movements);
    }

    /**
     * GET /api/reports/inventory-movements/summary/by-type
     * Get movement summary grouped by type
     *
     * Response:
     * [
     *   {
     *     "movementType": "PURCHASE",
     *     "movementDirection": "IN",
     *     "movementCount": 45,
     *     "totalQuantity": 500,
     *     "totalValue": 150000.00,
     *     "averageUnitCost": 300.00
     *   }
     * ]
     */
    @GetMapping("/summary/by-type")
    public ResponseEntity<List<InventoryMovementSummaryByTypeDTO>> getSummaryByType() {
        List<InventoryMovementSummaryByTypeDTO> summary = service.getSummaryByType();
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/reports/inventory-movements/summary/by-product
     * Get movement summary grouped by product
     *
     * Query params:
     * - productId: Filter by product UUID
     * - categoryName: Filter by category name (partial match)
     * - limit: Max results (default: 100)
     *
     * Response:
     * [
     *   {
     *     "sku": "NOTE-001",
     *     "productName": "Notebook Dell",
     *     "categoryName": "Informática",
     *     "totalMovements": 25,
     *     "inMovementsCount": 10,
     *     "totalQuantityIn": 50,
     *     "outMovementsCount": 15,
     *     "totalQuantityOut": 38,
     *     "netQuantityChange": 12,
     *     "totalValueMoved": 180000.00,
     *     "currentStock": 12
     *   }
     * ]
     */
    @GetMapping("/summary/by-product")
    public ResponseEntity<List<InventoryMovementSummaryByProductDTO>> getSummaryByProduct(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(defaultValue = "100") Integer limit
    ) {
        List<InventoryMovementSummaryByProductDTO> summary = service.getSummaryByProduct(
                productId,
                categoryName,
                limit
        );
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/reports/inventory-movements/count
     * Count movements with filters
     *
     * Response:
     * {
     *   "count": 150
     * }
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countMovements(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) String movementDirection,
            @RequestParam(defaultValue = "1000") Integer limit
    ) {
        InventoryMovementFilterDTO filter = new InventoryMovementFilterDTO(
                startDate,
                endDate,
                productId,
                locationId,
                movementType,
                movementDirection,
                limit
        );

        long count = service.countMovements(filter);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * GET /api/reports/inventory-movements/totals
     * Get movement totals with filters
     *
     * Response:
     * {
     *   "totalMovements": 150,
     *   "inCount": 80,
     *   "outCount": 70,
     *   "totalQuantityIn": 500,
     *   "totalQuantityOut": 320,
     *   "totalValueIn": 150000.00,
     *   "totalValueOut": 98000.00
     * }
     */
    @GetMapping("/totals")
    public ResponseEntity<?> getMovementTotals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) String movementDirection,
            @RequestParam(defaultValue = "1000") Integer limit
    ) {
        InventoryMovementFilterDTO filter = new InventoryMovementFilterDTO(
                startDate,
                endDate,
                productId,
                locationId,
                movementType,
                movementDirection,
                limit
        );

        return ResponseEntity.ok(service.getMovementTotals(filter));
    }

    /**
     * GET /api/reports/inventory-movements/export/csv
     * Export movements to CSV file
     *
     * Query params: Same as GET /movements
     *
     * Response: CSV file download
     * Content-Type: text/csv
     * Content-Disposition: attachment; filename="movimentacoes-estoque-YYYY-MM-DD.csv"
     */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportMovementsToCSV(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) String movementDirection,
            @RequestParam(defaultValue = "5000") Integer limit
    ) {
        InventoryMovementFilterDTO filter = new InventoryMovementFilterDTO(
                startDate,
                endDate,
                productId,
                locationId,
                movementType,
                movementDirection,
                limit
        );

        byte[] csvContent = service.exportMovementsToCSV(filter);

        String filename = String.format(
                "movimentacoes-estoque-%s.csv",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csvContent);
    }

    /**
     * GET /api/reports/inventory-movements/export/summary-by-product/csv
     * Export product summary to CSV file
     *
     * Response: CSV file download
     */
    @GetMapping("/export/summary-by-product/csv")
    public ResponseEntity<byte[]> exportSummaryByProductToCSV() {
        byte[] csvContent = service.exportSummaryByProductToCSV();

        String filename = String.format(
                "resumo-movimentacoes-produto-%s.csv",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csvContent);
    }

    /**
     * GET /api/reports/inventory-movements/export/summary-by-type/csv
     * Export type summary to CSV file
     *
     * Response: CSV file download
     */
    @GetMapping("/export/summary-by-type/csv")
    public ResponseEntity<byte[]> exportSummaryByTypeToCSV() {
        byte[] csvContent = service.exportSummaryByTypeToCSV();

        String filename = String.format(
                "resumo-movimentacoes-tipo-%s.csv",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csvContent);
    }
}
