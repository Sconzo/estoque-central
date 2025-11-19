package com.estoquecentral.catalog.adapter.in;

import com.estoquecentral.catalog.adapter.in.dto.ProductCreateRequest;
import com.estoquecentral.catalog.adapter.in.dto.ProductDTO;
import com.estoquecentral.catalog.adapter.in.dto.ProductUpdateRequest;
import com.estoquecentral.catalog.application.ProductService;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductStatus;
import com.estoquecentral.shared.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * ProductController - REST API for product management
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>GET /api/products - List all products (paginated)</li>
 *   <li>GET /api/products/search - Search products by query</li>
 *   <li>GET /api/products/category/{categoryId} - List products by category</li>
 *   <li>GET /api/products/sku/{sku} - Get product by SKU</li>
 *   <li>GET /api/products/barcode/{barcode} - Get product by barcode</li>
 *   <li>GET /api/products/{id} - Get product by ID</li>
 *   <li>POST /api/products - Create product</li>
 *   <li>PUT /api/products/{id} - Update product</li>
 *   <li>PATCH /api/products/{id}/status - Update product status</li>
 *   <li>DELETE /api/products/{id} - Delete product (soft delete)</li>
 *   <li>PUT /api/products/{id}/activate - Activate product</li>
 * </ul>
 *
 * <p><strong>Security:</strong> Requires authentication. ADMIN or GERENTE
 * roles required for write operations.
 *
 * @see ProductService
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Lists all active products with pagination
     *
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @return page of products
     */
    @GetMapping
    @Operation(summary = "List all products", description = "Returns paginated list of all active products")
    public ResponseEntity<Page<ProductDTO>> listAll(
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Page size") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.listAll(pageable);

        Page<ProductDTO> dtos = products.map(ProductDTO::fromEntity);

        return ResponseEntity.ok(dtos);
    }

    /**
     * Gets product by ID
     *
     * @param id product ID
     * @return product
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Returns product details")
    public ResponseEntity<ProductDTO> getById(@PathVariable UUID id) {
        Product product = productService.getById(id);
        return ResponseEntity.ok(ProductDTO.fromEntity(product));
    }

    /**
     * Gets product by SKU
     *
     * @param sku product SKU
     * @return product
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Returns product by SKU (tenant-scoped)")
    public ResponseEntity<ProductDTO> getBySku(@PathVariable String sku) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        Product product = productService.getBySku(tenantId, sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));
        return ResponseEntity.ok(ProductDTO.fromEntity(product));
    }

    /**
     * Gets product by barcode
     *
     * @param barcode product barcode
     * @return product
     */
    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get product by barcode", description = "Returns product by barcode (tenant-scoped)")
    public ResponseEntity<ProductDTO> getByBarcode(@PathVariable String barcode) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        Product product = productService.getByBarcode(tenantId, barcode)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with barcode: " + barcode));
        return ResponseEntity.ok(ProductDTO.fromEntity(product));
    }

    /**
     * Searches products by query (name, SKU, barcode)
     *
     * @param q search query
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @return page of matching products
     */
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Searches products by name, SKU, or barcode (case-insensitive)")
    public ResponseEntity<Page<ProductDTO>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.search(q, pageable);

        Page<ProductDTO> dtos = products.map(ProductDTO::fromEntity);

        return ResponseEntity.ok(dtos);
    }

    /**
     * Lists products by category
     *
     * @param categoryId category ID
     * @param includeSubcategories if true, includes products from subcategories (default false)
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @return page of products in category
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Returns products in category (optionally including subcategories)")
    public ResponseEntity<Page<ProductDTO>> getByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "false") boolean includeSubcategories,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.findByCategory(categoryId, includeSubcategories, pageable);

        Page<ProductDTO> dtos = products.map(ProductDTO::fromEntity);

        return ResponseEntity.ok(dtos);
    }

    /**
     * Lists products by status
     *
     * @param status product status (ACTIVE, INACTIVE, DISCONTINUED)
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @return page of products with status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get products by status", description = "Returns products with specific status")
    public ResponseEntity<Page<ProductDTO>> getByStatus(
            @PathVariable ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.findByStatus(status, pageable);

        Page<ProductDTO> dtos = products.map(ProductDTO::fromEntity);

        return ResponseEntity.ok(dtos);
    }

    /**
     * Creates new product
     *
     * @param request product creation request
     * @param authentication current user
     * @return created product
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Create product", description = "Creates new product (requires ADMIN or GERENTE role)")
    public ResponseEntity<ProductDTO> create(
            @Valid @RequestBody ProductCreateRequest request,
            Authentication authentication) {

        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        UUID userId = UUID.fromString(authentication.getName());

        Product product = productService.create(
                tenantId,
                request.getName(),
                request.getSku(),
                request.getBarcode(),
                request.getDescription(),
                request.getCategoryId(),
                request.getPrice(),
                request.getCost(),
                request.getUnit(),
                request.getControlsInventory(),
                userId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductDTO.fromEntity(product));
    }

    /**
     * Updates product
     *
     * @param id product ID
     * @param request update request
     * @param authentication current user
     * @return updated product
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Update product", description = "Updates product (requires ADMIN or GERENTE role)")
    public ResponseEntity<ProductDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        Product product = productService.update(
                id,
                request.getName(),
                request.getDescription(),
                request.getCategoryId(),
                request.getPrice(),
                request.getCost(),
                request.getUnit(),
                request.getControlsInventory(),
                userId
        );

        return ResponseEntity.ok(ProductDTO.fromEntity(product));
    }

    /**
     * Updates product status
     *
     * @param id product ID
     * @param status new status
     * @param authentication current user
     * @return updated product
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Update product status", description = "Updates product status (requires ADMIN or GERENTE role)")
    public ResponseEntity<ProductDTO> updateStatus(
            @PathVariable UUID id,
            @RequestParam ProductStatus status,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        Product product = productService.updateStatus(id, status, userId);

        return ResponseEntity.ok(ProductDTO.fromEntity(product));
    }

    /**
     * Deletes product (soft delete)
     *
     * @param id product ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Delete product", description = "Soft deletes product (requires ADMIN or GERENTE role)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activates previously deactivated product
     *
     * @param id product ID
     * @return activated product
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate product", description = "Activates deactivated product (requires ADMIN role)")
    public ResponseEntity<ProductDTO> activate(@PathVariable UUID id) {
        Product product = productService.activate(id);
        return ResponseEntity.ok(ProductDTO.fromEntity(product));
    }

    /**
     * Gets product statistics
     *
     * @return statistics object
     */
    @GetMapping("/stats")
    @Operation(summary = "Get product statistics", description = "Returns product count statistics")
    public ResponseEntity<ProductStatsDTO> getStats() {
        long totalActive = productService.countActive();
        long totalActiveStatus = productService.countByStatus(ProductStatus.ACTIVE);
        long totalInactive = productService.countByStatus(ProductStatus.INACTIVE);
        long totalDiscontinued = productService.countByStatus(ProductStatus.DISCONTINUED);

        ProductStatsDTO stats = new ProductStatsDTO(
                totalActive,
                totalActiveStatus,
                totalInactive,
                totalDiscontinued
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Inner class for statistics response
     */
    public static class ProductStatsDTO {
        private long totalActive;
        private long statusActive;
        private long statusInactive;
        private long statusDiscontinued;

        public ProductStatsDTO(long totalActive, long statusActive, long statusInactive,
                               long statusDiscontinued) {
            this.totalActive = totalActive;
            this.statusActive = statusActive;
            this.statusInactive = statusInactive;
            this.statusDiscontinued = statusDiscontinued;
        }

        public long getTotalActive() {
            return totalActive;
        }

        public long getStatusActive() {
            return statusActive;
        }

        public long getStatusInactive() {
            return statusInactive;
        }

        public long getStatusDiscontinued() {
            return statusDiscontinued;
        }
    }
}
