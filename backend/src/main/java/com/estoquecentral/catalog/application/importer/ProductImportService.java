package com.estoquecentral.catalog.application.importer;

import com.estoquecentral.catalog.adapter.in.dto.ImportConfirmResponse;
import com.estoquecentral.catalog.adapter.in.dto.ImportPreviewResponse;
import com.estoquecentral.catalog.adapter.in.dto.ProductCsvRow;
import com.estoquecentral.catalog.adapter.out.CategoryRepository;
import com.estoquecentral.catalog.adapter.out.ImportLogRepository;
import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.domain.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProductImportService - Orchestrates CSV product import with two-phase commit
 *
 * <p>Import workflow:
 * <ol>
 *   <li>preview() - Parse and validate CSV, create ImportLog with PREVIEW status</li>
 *   <li>User reviews errors in UI</li>
 *   <li>confirmImport() - Persist valid products, update ImportLog to COMPLETED</li>
 * </ol>
 */
@Service
public class ProductImportService {

    private final CsvParserService csvParser;
    private final ProductRepository productRepository;
    private final ImportLogRepository importLogRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProductImportService(CsvParserService csvParser,
                               ProductRepository productRepository,
                               ImportLogRepository importLogRepository,
                               CategoryRepository categoryRepository,
                               ObjectMapper objectMapper) {
        this.csvParser = csvParser;
        this.productRepository = productRepository;
        this.importLogRepository = importLogRepository;
        this.categoryRepository = categoryRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Phase 1: Parse and validate CSV, return preview
     *
     * @param file CSV file
     * @param tenantId tenant ID
     * @param userId user performing import
     * @return preview response with validation results
     */
    @Transactional
    public ImportPreviewResponse preview(MultipartFile file, UUID tenantId, UUID userId) throws IOException {
        String fileName = file.getOriginalFilename();
        String csvContent = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);

        // Parse and validate CSV
        List<ProductCsvRow> rows = csvParser.parseAndValidate(csvContent);

        // Resolve category names to IDs
        resolveCategoryNames(rows);

        // Validate SKU uniqueness (against DB and within CSV)
        validateUniqueness(rows, tenantId);

        // Create import log with PREVIEW status
        ImportLog importLog = new ImportLog(tenantId, userId, fileName, ImportStatus.PREVIEW);
        importLog.setTotalRows(rows.size());
        int validCount = (int) rows.stream().filter(ProductCsvRow::isValid).count();
        int errorCount = (int) rows.stream().filter(ProductCsvRow::hasErrors).count();
        importLog.setSuccessRows(validCount);
        importLog.setErrorRows(errorCount);

        // Store CSV content for the confirm phase
        importLog.setFileContent(csvContent);

        // Store error details as JSONB
        List<Map<String, Object>> errorDetails = rows.stream()
                .filter(ProductCsvRow::hasErrors)
                .map(row -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("row", row.getRowNumber());
                    detail.put("errors", row.getErrors());
                    return detail;
                })
                .collect(Collectors.toList());

        if (!errorDetails.isEmpty()) {
            try {
                importLog.setErrorDetails(objectMapper.writeValueAsString(errorDetails));
            } catch (JsonProcessingException e) {
                throw new IOException("Failed to serialize error details", e);
            }
        }

        importLog = importLogRepository.save(importLog);

        return new ImportPreviewResponse(importLog.getId(), fileName, rows);
    }

    /**
     * Phase 2: Confirm import and persist valid products
     *
     * @param importLogId import log ID from preview
     * @param tenantId tenant ID
     * @param userId user performing import
     * @return confirm response with success/error counts
     */
    @Transactional
    public ImportConfirmResponse confirmImport(UUID importLogId, UUID tenantId, UUID userId) {
        // Load import log
        ImportLog importLog = importLogRepository.findById(importLogId)
                .orElseThrow(() -> new IllegalArgumentException("Import log not found: " + importLogId));

        // Validate status
        if (importLog.getStatus() != ImportStatus.PREVIEW) {
            throw new IllegalStateException("Import already processed. Status: " + importLog.getStatus());
        }

        // Validate file content exists
        if (importLog.getFileContent() == null) {
            throw new IllegalStateException("CSV content not found for import: " + importLogId);
        }

        int successCount = 0;
        int errorCount = 0;

        // Re-parse CSV from stored content
        List<ProductCsvRow> rows;
        try {
            rows = csvParser.parseAndValidate(importLog.getFileContent());
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao re-processar CSV armazenado: " + e.getMessage(), e);
        }

        resolveCategoryNames(rows);
        validateUniqueness(rows, tenantId);

        // Create products directly (avoid ProductService @Transactional proxy issues)
        for (ProductCsvRow row : rows) {
            if (row.isValid()) {
                Product product = buildProduct(row, tenantId, userId);
                productRepository.save(product);
                successCount++;
            } else {
                errorCount++;
            }
        }

        // Update import log to COMPLETED and clear stored CSV
        importLog.setStatus(ImportStatus.COMPLETED);
        importLog.setSuccessRows(successCount);
        importLog.setErrorRows(errorCount);
        importLog.setFileContent(null);
        importLogRepository.save(importLog);

        String message = String.format("Importação concluída: %d produtos criados, %d erros",
                successCount, errorCount);

        return new ImportConfirmResponse(
                importLog.getId(),
                importLog.getTotalRows(),
                successCount,
                errorCount,
                "COMPLETED",
                message
        );
    }

    /**
     * Builds a Product entity from CSV row (without going through ProductService proxy)
     */
    private Product buildProduct(ProductCsvRow row, UUID tenantId, UUID userId) {
        Product product = new Product(
                tenantId,
                ProductType.SIMPLE,
                row.getName(),
                row.getSku(),
                row.getBarcode(),
                row.getDescription(),
                row.getResolvedCategoryId(),
                row.getPrice(),
                row.getCost(),
                row.getUnit(),
                row.getControlsInventory(),
                ProductStatus.ACTIVE
        );
        product.setCreatedBy(userId);
        return product;
    }

    /**
     * Validates SKU and barcode uniqueness against DB and within the CSV itself.
     */
    private void validateUniqueness(List<ProductCsvRow> rows, UUID tenantId) {
        // Check for duplicate SKUs within CSV
        Map<String, List<ProductCsvRow>> skuGroups = rows.stream()
                .filter(r -> r.getSku() != null)
                .collect(Collectors.groupingBy(r -> r.getSku().toLowerCase()));

        for (Map.Entry<String, List<ProductCsvRow>> entry : skuGroups.entrySet()) {
            if (entry.getValue().size() > 1) {
                entry.getValue().forEach(row ->
                    row.addError("SKU duplicado no CSV: " + row.getSku())
                );
            }
        }

        // Check for duplicate barcodes within CSV
        Map<String, List<ProductCsvRow>> barcodeGroups = rows.stream()
                .filter(r -> r.getBarcode() != null && !r.getBarcode().isBlank())
                .collect(Collectors.groupingBy(r -> r.getBarcode().toLowerCase()));

        for (Map.Entry<String, List<ProductCsvRow>> entry : barcodeGroups.entrySet()) {
            if (entry.getValue().size() > 1) {
                entry.getValue().forEach(row ->
                    row.addError("Código de barras duplicado no CSV: " + row.getBarcode())
                );
            }
        }

        // Check against existing products in DB
        for (ProductCsvRow row : rows) {
            if (row.getSku() != null && !row.hasErrors()) {
                if (productRepository.findByTenantIdAndSku(tenantId, row.getSku()).isPresent()) {
                    row.addError("SKU já existe no sistema: " + row.getSku());
                }
            }
            if (row.getBarcode() != null && !row.getBarcode().isBlank() && !row.hasErrors()) {
                if (productRepository.findByTenantIdAndBarcode(tenantId, row.getBarcode()).isPresent()) {
                    row.addError("Código de barras já existe no sistema: " + row.getBarcode());
                }
            }
        }
    }

    /**
     * Resolves category names to UUIDs by looking up in the database.
     * Adds validation errors if category not found or ambiguous.
     */
    private void resolveCategoryNames(List<ProductCsvRow> rows) {
        // Collect unique category names
        Set<String> categoryNames = rows.stream()
                .map(ProductCsvRow::getCategory)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toSet());

        // Resolve each name once
        Map<String, List<Category>> resolvedMap = new HashMap<>();
        for (String name : categoryNames) {
            resolvedMap.put(name.toLowerCase(), categoryRepository.findByNameIgnoreCase(name));
        }

        // Apply resolution to each row
        for (ProductCsvRow row : rows) {
            String categoryName = row.getCategory();
            if (categoryName == null || categoryName.isBlank()) {
                continue; // Already flagged by basic validation
            }

            List<Category> matches = resolvedMap.getOrDefault(categoryName.toLowerCase(), List.of());
            if (matches.isEmpty()) {
                row.addError("Categoria não encontrada: " + categoryName);
            } else if (matches.size() > 1) {
                row.addError("Múltiplas categorias com o nome '" + categoryName + "'. Verifique as categorias cadastradas.");
            } else {
                row.setResolvedCategoryId(matches.get(0).getId());
            }
        }
    }

    /**
     * Generates CSV template with example rows
     *
     * @return CSV template string
     */
    public String generateTemplate() {
        StringBuilder csv = new StringBuilder();

        csv.append("name,sku,barcode,description,category,price,cost,unit,controlsInventory\n");
        csv.append("Notebook Dell Inspiron,NOTE-001,7891234567890,Notebook 15 polegadas 8GB RAM,Eletrônicos,3499.90,2500.00,UN,true\n");
        csv.append("Mouse Logitech MX,MOUSE-001,7891234567891,Mouse sem fio ergonômico,Periféricos,299.90,150.00,UN,true\n");

        return csv.toString();
    }
}
