package com.estoquecentral.catalog.adapter.in;

import com.estoquecentral.catalog.adapter.in.dto.ProductAttributeDTO;
import com.estoquecentral.catalog.adapter.in.dto.ProductCreateRequest;
import com.estoquecentral.catalog.adapter.in.dto.ProductDTO;
import com.estoquecentral.catalog.adapter.in.dto.ProductUpdateRequest;
import com.estoquecentral.catalog.adapter.out.CategoryRepository;
import com.estoquecentral.catalog.adapter.out.ProductAttributeRepository;
import com.estoquecentral.catalog.application.ProductService;
import com.estoquecentral.catalog.domain.BomType;
import com.estoquecentral.catalog.domain.Category;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductAttribute;
import com.estoquecentral.catalog.domain.ProductStatus;
import com.estoquecentral.catalog.domain.ProductType;
import com.estoquecentral.inventory.application.InventoryService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    private final CategoryRepository categoryRepository;
    private final InventoryService inventoryService;
    private final ProductAttributeRepository productAttributeRepository;

    @Autowired
    public ProductController(ProductService productService, CategoryRepository categoryRepository,
                             InventoryService inventoryService, ProductAttributeRepository productAttributeRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.inventoryService = inventoryService;
        this.productAttributeRepository = productAttributeRepository;
    }

    /**
     * Converts Product to ProductDTO with category name
     */
    private ProductDTO toDTO(Product product, Map<UUID, String> categoryNames) {
        ProductDTO dto = ProductDTO.fromEntity(product);
        if (product.getCategoryId() != null && categoryNames.containsKey(product.getCategoryId())) {
            dto.setCategoryName(categoryNames.get(product.getCategoryId()));
        }
        return dto;
    }

    /**
     * Builds a map of category ID to category name
     */
    private Map<UUID, String> getCategoryNamesMap() {
        return StreamSupport.stream(categoryRepository.findAll().spliterator(), false)
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    /**
     * Lists all active products with pagination
     *
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @return page of products
     */
    @GetMapping
    @Operation(summary = "List all products", description = "Returns paginated list of products optionally filtered by status")
    public ResponseEntity<Page<ProductDTO>> listAll(
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Page size") int size,
            @RequestParam(required = false) @Parameter(description = "Filter by status (ACTIVE, INACTIVE, DISCONTINUED)") ProductStatus status) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.listAll(pageable, status);

        Map<UUID, String> categoryNames = getCategoryNamesMap();
        Page<ProductDTO> dtos = products.map(product -> toDTO(product, categoryNames));

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
        ProductDTO dto = ProductDTO.fromEntity(product);
        if (product.getCategoryId() != null) {
            categoryRepository.findById(product.getCategoryId())
                    .ifPresent(category -> dto.setCategoryName(category.getName()));
        }
        return ResponseEntity.ok(dto);
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
        ProductDTO dto = ProductDTO.fromEntity(product);
        if (product.getCategoryId() != null) {
            categoryRepository.findById(product.getCategoryId())
                    .ifPresent(category -> dto.setCategoryName(category.getName()));
        }
        return ResponseEntity.ok(dto);
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
        ProductDTO dto = ProductDTO.fromEntity(product);
        if (product.getCategoryId() != null) {
            categoryRepository.findById(product.getCategoryId())
                    .ifPresent(category -> dto.setCategoryName(category.getName()));
        }
        return ResponseEntity.ok(dto);
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

        Map<UUID, String> categoryNames = getCategoryNamesMap();
        Page<ProductDTO> dtos = products.map(product -> toDTO(product, categoryNames));

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

        Map<UUID, String> categoryNames = getCategoryNamesMap();
        Page<ProductDTO> dtos = products.map(product -> toDTO(product, categoryNames));

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

        Map<UUID, String> categoryNames = getCategoryNamesMap();
        Page<ProductDTO> dtos = products.map(product -> toDTO(product, categoryNames));

        return ResponseEntity.ok(dtos);
    }

    /**
     * Creates new product with optional inventory and descriptive attributes (atomic transaction)
     *
     * @param request product creation request
     * @param authentication current user
     * @return created product
     */
    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Create product", description = "Creates new product with optional inventory and attributes (requires ADMIN or GERENTE role)")
    public ResponseEntity<ProductDTO> create(
            @Valid @RequestBody ProductCreateRequest request,
            Authentication authentication) {

        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        UUID userId = UUID.fromString(authentication.getName());

        // Use type from request, default to SIMPLE if not provided
        ProductType type = request.getType() != null ? request.getType() : ProductType.SIMPLE;
        BomType bomType = request.getBomType();

        Product product = productService.create(
                tenantId,
                type,
                bomType,
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

        // Create inventory if controlsInventory=true and locationId provided
        Boolean controlsInventory = request.getControlsInventory() != null ? request.getControlsInventory() : false;
        if (controlsInventory && request.getLocationId() != null) {
            BigDecimal initialQty = request.getInitialQuantity() != null ? request.getInitialQuantity() : BigDecimal.ZERO;
            inventoryService.createInventory(
                    product.getId(),
                    initialQty,
                    request.getLocationId(),
                    request.getMinimumQuantity(),
                    request.getMaximumQuantity(),
                    userId
            );
        }

        // Save descriptive attributes
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            int sortOrder = 0;
            for (ProductAttributeDTO attrDto : request.getAttributes()) {
                if (attrDto.key() != null && !attrDto.key().isBlank()
                        && attrDto.value() != null && !attrDto.value().isBlank()) {
                    ProductAttribute attr = new ProductAttribute(
                            tenantId, product.getId(), attrDto.key().trim(), attrDto.value().trim(), sortOrder++
                    );
                    productAttributeRepository.save(attr);
                }
            }
        }

        ProductDTO dto = ProductDTO.fromEntity(product);
        if (product.getCategoryId() != null) {
            categoryRepository.findById(product.getCategoryId())
                    .ifPresent(category -> dto.setCategoryName(category.getName()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
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
                request.getStatus(),
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

    // ==================== Product Attributes Endpoints ====================

    /**
     * Gets descriptive attributes for a product
     *
     * @param id product ID
     * @return list of attributes
     */
    @GetMapping("/{id}/attributes")
    @Operation(summary = "Get product attributes", description = "Returns descriptive attributes for a product")
    public ResponseEntity<List<ProductAttributeDTO>> getAttributes(@PathVariable UUID id) {
        List<ProductAttribute> attributes = productAttributeRepository.findByProductId(id);
        List<ProductAttributeDTO> dtos = attributes.stream()
                .map(attr -> new ProductAttributeDTO(attr.getAttributeKey(), attr.getAttributeValue()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Replaces all descriptive attributes for a product
     *
     * @param id product ID
     * @param attributes new attributes
     * @return saved attributes
     */
    @PutMapping("/{id}/attributes")
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Update product attributes", description = "Replaces all descriptive attributes (requires ADMIN or GERENTE role)")
    public ResponseEntity<List<ProductAttributeDTO>> updateAttributes(
            @PathVariable UUID id,
            @RequestBody List<ProductAttributeDTO> attributes) {

        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        // Delete existing attributes
        productAttributeRepository.deleteByProductId(id);

        // Save new attributes
        int sortOrder = 0;
        for (ProductAttributeDTO attrDto : attributes) {
            if (attrDto.key() != null && !attrDto.key().isBlank()
                    && attrDto.value() != null && !attrDto.value().isBlank()) {
                ProductAttribute attr = new ProductAttribute(
                        tenantId, id, attrDto.key().trim(), attrDto.value().trim(), sortOrder++
                );
                productAttributeRepository.save(attr);
            }
        }

        // Return saved attributes
        List<ProductAttribute> saved = productAttributeRepository.findByProductId(id);
        List<ProductAttributeDTO> dtos = saved.stream()
                .map(attr -> new ProductAttributeDTO(attr.getAttributeKey(), attr.getAttributeValue()))
                .toList();
        return ResponseEntity.ok(dtos);
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
