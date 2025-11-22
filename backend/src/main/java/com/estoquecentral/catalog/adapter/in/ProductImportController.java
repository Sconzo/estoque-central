package com.estoquecentral.catalog.adapter.in;

import com.estoquecentral.catalog.adapter.in.dto.ImportConfirmResponse;
import com.estoquecentral.catalog.adapter.in.dto.ImportPreviewResponse;
import com.estoquecentral.catalog.adapter.out.ImportLogRepository;
import com.estoquecentral.catalog.application.importer.ProductImportService;
import com.estoquecentral.catalog.domain.ImportLog;
import com.estoquecentral.catalog.domain.ProductType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * ProductImportController - REST API for CSV product import
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/products/import/preview - Upload and validate CSV</li>
 *   <li>POST /api/products/import/confirm - Confirm and persist products</li>
 *   <li>GET /api/products/import/template - Download CSV template</li>
 *   <li>GET /api/products/import-logs/{id} - Get import log details</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Import", description = "CSV import for bulk product creation")
public class ProductImportController {

    private final ProductImportService importService;
    private final ImportLogRepository importLogRepository;

    @Autowired
    public ProductImportController(ProductImportService importService,
                                   ImportLogRepository importLogRepository) {
        this.importService = importService;
        this.importLogRepository = importLogRepository;
    }

    /**
     * Phase 1: Upload CSV and get preview with validation
     *
     * POST /api/products/import/preview
     */
    @PostMapping(value = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Preview CSV import",
               description = "Uploads CSV file, validates rows, and returns preview with errors")
    public ResponseEntity<ImportPreviewResponse> preview(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tenantId") UUID tenantId,
            Authentication authentication) throws IOException {

        UUID userId = UUID.fromString(authentication.getName());

        ImportPreviewResponse preview = importService.preview(file, tenantId, userId);

        return ResponseEntity.ok(preview);
    }

    /**
     * Phase 2: Confirm import and persist valid products
     *
     * POST /api/products/import/confirm
     */
    @PostMapping("/import/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Confirm CSV import",
               description = "Persists valid products from previously previewed import")
    public ResponseEntity<ImportConfirmResponse> confirmImport(
            @RequestParam("importLogId") UUID importLogId,
            @RequestParam("tenantId") UUID tenantId,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        ImportConfirmResponse response = importService.confirmImport(importLogId, tenantId, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Download CSV template for specific product type
     *
     * GET /api/products/import/template?type={type}
     */
    @GetMapping("/import/template")
    @Operation(summary = "Download CSV template",
               description = "Returns CSV template with example data for specified product type")
    public ResponseEntity<String> downloadTemplate(
            @RequestParam(value = "type", defaultValue = "SIMPLE") ProductType type) {

        String csvTemplate = importService.generateTemplate(type);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "product_import_template_" + type + ".csv");

        return new ResponseEntity<>(csvTemplate, headers, HttpStatus.OK);
    }

    /**
     * Get import log details
     *
     * GET /api/products/import-logs/{id}
     */
    @GetMapping("/import-logs/{id}")
    @Operation(summary = "Get import log",
               description = "Returns details of a specific import operation")
    public ResponseEntity<ImportLog> getImportLog(@PathVariable UUID id) {

        ImportLog importLog = importLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Import log not found: " + id));

        return ResponseEntity.ok(importLog);
    }
}
