package com.estoquecentral.catalog.application;

import com.estoquecentral.catalog.adapter.out.CategoryRepository;
import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.domain.BomType;
import com.estoquecentral.catalog.domain.Category;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductStatus;
import com.estoquecentral.catalog.domain.ProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ProductService - Business logic for product management
 *
 * <p>Handles product operations with comprehensive validation:
 * <ul>
 *   <li>SKU uniqueness per tenant</li>
 *   <li>Barcode uniqueness per tenant</li>
 *   <li>Category existence and active status</li>
 *   <li>Price validation (>= 0)</li>
 *   <li>Cost validation (>= 0 if provided)</li>
 * </ul>
 *
 * <p><strong>Story 2.2 scope:</strong> Only SIMPLE products supported.
 *
 * @see Product
 * @see ProductRepository
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Lists all products with pagination, optionally filtered by status
     *
     * @param pageable pagination parameters
     * @param status optional status filter (null = show only active)
     * @return page of products
     */
    @Transactional(readOnly = true)
    public Page<Product> listAll(Pageable pageable, ProductStatus status) {
        List<Product> content;
        long total;

        if (status != null) {
            // Filter by specific status
            content = productRepository.findByStatus(status, pageable.getPageSize(), pageable.getOffset());
            total = productRepository.countByStatus(status);
        } else {
            // Default: show only active products
            content = productRepository.findByAtivoTrue(pageable.getPageSize(), pageable.getOffset());
            total = productRepository.countByAtivoTrue();
        }

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Gets product by ID
     *
     * @param id product ID
     * @return product
     * @throws IllegalArgumentException if product not found
     */
    @Transactional(readOnly = true)
    public Product getById(UUID id) {
        return productRepository.findByIdAndActive(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
    }

    /**
     * Gets product by SKU
     *
     * @param tenantId tenant ID
     * @param sku product SKU
     * @return optional product
     */
    @Transactional(readOnly = true)
    public Optional<Product> getBySku(UUID tenantId, String sku) {
        return productRepository.findByTenantIdAndSku(tenantId, sku);
    }

    /**
     * Gets product by barcode
     *
     * @param tenantId tenant ID
     * @param barcode product barcode
     * @return optional product
     */
    @Transactional(readOnly = true)
    public Optional<Product> getByBarcode(UUID tenantId, String barcode) {
        return productRepository.findByTenantIdAndBarcode(tenantId, barcode);
    }

    /**
     * Searches products by query (name, SKU, barcode)
     * Note: Returns all results without pagination due to Spring Data JDBC limitations
     *
     * @param query search query
     * @param pageable pagination parameters (ignored for now)
     * @return page of matching products
     */
    @Transactional(readOnly = true)
    public Page<Product> search(String query, Pageable pageable) {
        List<Product> results = productRepository.search(query);
        return new PageImpl<>(results, pageable, results.size());
    }

    /**
     * Finds products by category
     *
     * @param categoryId category ID
     * @param includeSubcategories if true, includes products from subcategories
     * @param pageable pagination parameters
     * @return page of products
     */
    @Transactional(readOnly = true)
    public Page<Product> findByCategory(UUID categoryId, boolean includeSubcategories,
                                         Pageable pageable) {
        if (includeSubcategories) {
            List<Product> results = productRepository.findByCategoryIdIncludingDescendants(categoryId);
            return new PageImpl<>(results, pageable, results.size());
        } else {
            List<Product> content = productRepository.findByCategoryIdAndAtivoTrue(
                categoryId,
                pageable.getPageSize(),
                pageable.getOffset()
            );
            long total = productRepository.countByCategoryIdAndAtivoTrue(categoryId);
            return new PageImpl<>(content, pageable, total);
        }
    }

    /**
     * Finds products by status
     *
     * @param status product status
     * @param pageable pagination parameters
     * @return page of products
     */
    @Transactional(readOnly = true)
    public Page<Product> findByStatus(ProductStatus status, Pageable pageable) {
        List<Product> content = productRepository.findByStatusAndAtivoTrue(
            status.name(),
            pageable.getPageSize(),
            pageable.getOffset()
        );
        long total = productRepository.countByStatusAndAtivoTrue(status.name());
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Creates new product
     *
     * @param tenantId tenant ID
     * @param name product name
     * @param sku product SKU
     * @param barcode product barcode (optional)
     * @param description product description
     * @param categoryId category ID
     * @param price product price
     * @param cost product cost (optional)
     * @param unit unit of measure
     * @param controlsInventory inventory control flag
     * @param createdBy user creating the product
     * @return created product
     * @throws IllegalArgumentException if validation fails
     */
    public Product create(UUID tenantId, String name, String sku, String barcode,
                          String description, UUID categoryId, BigDecimal price,
                          BigDecimal cost, String unit, Boolean controlsInventory,
                          UUID createdBy) {
        return create(tenantId, ProductType.SIMPLE, name, sku, barcode, description,
                categoryId, price, cost, unit, controlsInventory, createdBy);
    }

    /**
     * Creates new product with specific type
     *
     * @param tenantId tenant ID
     * @param type product type
     * @param name product name
     * @param sku product SKU
     * @param barcode product barcode (optional)
     * @param description product description
     * @param categoryId category ID
     * @param price product price
     * @param cost product cost (optional)
     * @param unit unit of measure
     * @param controlsInventory inventory control flag
     * @param createdBy user creating the product
     * @return created product
     * @throws IllegalArgumentException if validation fails
     */
    public Product create(UUID tenantId, ProductType type, String name, String sku, String barcode,
                          String description, UUID categoryId, BigDecimal price,
                          BigDecimal cost, String unit, Boolean controlsInventory,
                          UUID createdBy) {
        return create(tenantId, type, null, name, sku, barcode, description,
                categoryId, price, cost, unit, controlsInventory, createdBy);
    }

    /**
     * Creates new product with BOM type (for COMPOSITE products)
     *
     * @param tenantId tenant ID
     * @param type product type
     * @param bomType BOM type (VIRTUAL or PHYSICAL, required for COMPOSITE)
     * @param name product name
     * @param sku product SKU
     * @param barcode product barcode (optional)
     * @param description product description
     * @param categoryId category ID
     * @param price product price
     * @param cost product cost (optional)
     * @param unit unit of measure
     * @param controlsInventory inventory control flag
     * @param createdBy user creating the product
     * @return created product
     * @throws IllegalArgumentException if validation fails
     */
    public Product create(UUID tenantId, ProductType type, BomType bomType, String name, String sku, String barcode,
                          String description, UUID categoryId, BigDecimal price,
                          BigDecimal cost, String unit, Boolean controlsInventory,
                          UUID createdBy) {
        // Validation
        validateProductName(name);
        validateSku(sku);
        validateSkuUniqueness(tenantId, sku);
        if (barcode != null && !barcode.trim().isEmpty()) {
            validateBarcodeUniqueness(tenantId, barcode);
        }
        validateCategory(categoryId);
        validatePrice(price);
        validateCost(cost);

        // Validate BOM type for COMPOSITE products
        if (type == ProductType.COMPOSITE && bomType == null) {
            throw new IllegalArgumentException("BOM type is required for COMPOSITE products");
        }
        if (type != ProductType.COMPOSITE && bomType != null) {
            throw new IllegalArgumentException("BOM type can only be set for COMPOSITE products");
        }

        // Create product
        Product product = new Product(
                tenantId,
                type,
                bomType,
                name,
                sku,
                barcode,
                description,
                categoryId,
                price,
                cost,
                unit,
                controlsInventory,
                ProductStatus.ACTIVE
        );
        product.setCreatedBy(createdBy);

        return productRepository.save(product);
    }

    /**
     * Updates product
     *
     * @param id product ID
     * @param name new name
     * @param description new description
     * @param categoryId new category ID
     * @param price new price
     * @param cost new cost
     * @param unit new unit
     * @param controlsInventory new inventory control flag
     * @param status new status
     * @param updatedBy user making the update
     * @return updated product
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public Product update(UUID id, String name, String description, UUID categoryId,
                          BigDecimal price, BigDecimal cost, String unit,
                          Boolean controlsInventory, ProductStatus status, UUID updatedBy) {
        // Find product directly (avoid readOnly transaction from getById)
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        // Validation
        validateProductName(name);
        validateCategory(categoryId);
        validatePrice(price);
        validateCost(cost);

        // Update
        product.update(name, description, categoryId, price, cost, unit,
                controlsInventory, status, updatedBy);

        return productRepository.save(product);
    }

    /**
     * Updates product status
     *
     * @param id product ID
     * @param status new status
     * @param updatedBy user making the update
     * @return updated product
     */
    @Transactional
    public Product updateStatus(UUID id, ProductStatus status, UUID updatedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        product.updateStatus(status, updatedBy);
        return productRepository.save(product);
    }

    /**
     * Deletes product (soft delete)
     *
     * @param id product ID
     * @throws IllegalArgumentException if product not found
     */
    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        product.deactivate();
        productRepository.save(product);
    }

    /**
     * Activates previously deactivated product
     *
     * @param id product ID
     * @return activated product
     */
    public Product activate(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        product.activate();
        return productRepository.save(product);
    }

    /**
     * Counts active products
     *
     * @return count of active products
     */
    @Transactional(readOnly = true)
    public long countActive() {
        return productRepository.countActive();
    }

    /**
     * Counts products by category
     *
     * @param categoryId category ID
     * @return count of products in category
     */
    @Transactional(readOnly = true)
    public long countByCategory(UUID categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    /**
     * Counts products by status
     *
     * @param status product status
     * @return count of products with status
     */
    @Transactional(readOnly = true)
    public long countByStatus(ProductStatus status) {
        return productRepository.countByStatus(status.name());
    }

    // ==================== Validation Methods ====================

    private void validateProductName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("Product name cannot exceed 200 characters");
        }
    }

    private void validateSku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be empty");
        }
        if (sku.length() > 100) {
            throw new IllegalArgumentException("SKU cannot exceed 100 characters");
        }
    }

    private void validateSkuUniqueness(UUID tenantId, String sku) {
        Optional<Product> existing = productRepository.findByTenantIdAndSku(tenantId, sku);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("SKU already exists: " + sku);
        }
    }

    private void validateSkuUniquenessForUpdate(UUID tenantId, String sku, UUID excludeId) {
        boolean exists = productRepository.existsByTenantIdAndSkuExcludingId(tenantId, sku, excludeId);
        if (exists) {
            throw new IllegalArgumentException("SKU already exists: " + sku);
        }
    }

    private void validateBarcodeUniqueness(UUID tenantId, String barcode) {
        Optional<Product> existing = productRepository.findByTenantIdAndBarcode(tenantId, barcode);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Barcode already exists: " + barcode);
        }
    }

    private void validateBarcodeUniquenessForUpdate(UUID tenantId, String barcode, UUID excludeId) {
        boolean exists = productRepository.existsByTenantIdAndBarcodeExcludingId(tenantId, barcode, excludeId);
        if (exists) {
            throw new IllegalArgumentException("Barcode already exists: " + barcode);
        }
    }

    private void validateCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + categoryId));

        if (!category.getAtivo()) {
            throw new IllegalArgumentException("Category is not active: " + categoryId);
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be greater than or equal to 0");
        }
    }

    private void validateCost(BigDecimal cost) {
        if (cost != null && cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost must be greater than or equal to 0");
        }
    }
}
