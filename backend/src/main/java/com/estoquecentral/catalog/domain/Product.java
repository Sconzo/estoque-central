package com.estoquecentral.catalog.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product - Domain entity for products
 *
 * <p>Represents a product in the catalog. Currently supports SIMPLE products
 * (single SKU, no variants). Future expansion will support VARIANT_PARENT and VARIANT types.
 *
 * <p><strong>Product Types:</strong>
 * <ul>
 *   <li>SIMPLE - Standard product with single SKU (e.g., "Notebook Dell")</li>
 *   <li>VARIANT_PARENT - Parent product with variants (future)</li>
 *   <li>VARIANT - Child variant product (future)</li>
 * </ul>
 *
 * <p><strong>Business Rules:</strong>
 * <ul>
 *   <li>SKU must be unique per tenant</li>
 *   <li>Barcode must be unique per tenant (if provided)</li>
 *   <li>Price must be >= 0</li>
 *   <li>Cost must be >= 0 (if provided)</li>
 *   <li>Must belong to an active category</li>
 * </ul>
 *
 * @see ProductType
 * @see ProductStatus
 */
@Table("products")
public class Product implements Persistable<UUID> {

    @Id
    private UUID id;
    private UUID tenantId;

    @Transient
    private boolean isNew = false;
    private ProductType type;
    private BomType bomType;
    private String name;
    private String sku;
    private String barcode;
    private String description;
    private UUID categoryId;
    private BigDecimal price;
    private BigDecimal cost;
    private String unit;
    private Boolean controlsInventory;
    private ProductStatus status;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    /**
     * Constructor for creating new product
     */
    public Product(UUID tenantId, ProductType type, String name, String sku, String barcode,
                   String description, UUID categoryId, BigDecimal price, BigDecimal cost,
                   String unit, Boolean controlsInventory, ProductStatus status) {
        this(tenantId, type, null, name, sku, barcode, description, categoryId, price, cost,
                unit, controlsInventory, status);
    }

    /**
     * Constructor for creating new product with BOM type
     */
    public Product(UUID tenantId, ProductType type, BomType bomType, String name, String sku, String barcode,
                   String description, UUID categoryId, BigDecimal price, BigDecimal cost,
                   String unit, Boolean controlsInventory, ProductStatus status) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.type = type;
        this.bomType = bomType;
        this.name = name;
        this.sku = sku;
        this.barcode = barcode;
        this.description = description;
        this.categoryId = categoryId;
        this.price = price;
        this.cost = cost;
        this.unit = unit != null ? unit : "UN";
        this.controlsInventory = controlsInventory != null ? controlsInventory : true;
        this.status = status != null ? status : ProductStatus.ACTIVE;
        this.ativo = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isNew = true;
    }

    /**
     * Default constructor for Spring Data JDBC
     */
    public Product() {
    }

    /**
     * Updates product information
     *
     * @param name new name
     * @param description new description
     * @param categoryId new category ID
     * @param price new price
     * @param cost new cost
     * @param unit new unit
     * @param controlsInventory new inventory control flag
     * @param status new status
     * @param updatedBy user making the update
     */
    public void update(String name, String description, UUID categoryId, BigDecimal price,
                       BigDecimal cost, String unit, Boolean controlsInventory,
                       ProductStatus status, UUID updatedBy) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.price = price;
        this.cost = cost;
        this.unit = unit;
        this.controlsInventory = controlsInventory;
        if (status != null) {
            this.status = status;
        }
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * Updates product status
     *
     * @param status new status
     * @param updatedBy user making the update
     */
    public void updateStatus(ProductStatus status, UUID updatedBy) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * Soft deletes the product
     */
    public void deactivate() {
        this.ativo = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Activates previously deactivated product
     */
    public void activate() {
        this.ativo = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if product is active
     *
     * @return true if active
     */
    public boolean isActive() {
        return this.ativo != null && this.ativo;
    }

    /**
     * Checks if product has barcode
     *
     * @return true if barcode is not null and not empty
     */
    public boolean hasBarcode() {
        return this.barcode != null && !this.barcode.trim().isEmpty();
    }

    /**
     * Checks if product controls inventory
     *
     * @return true if controls inventory
     */
    public boolean shouldControlInventory() {
        return this.controlsInventory != null && this.controlsInventory;
    }

    /**
     * Calculates profit margin
     *
     * @return profit margin percentage (null if cost is not set)
     */
    public BigDecimal calculateProfitMargin() {
        if (cost == null || cost.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal profit = price.subtract(cost);
        return profit.divide(cost, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    // Persistable implementation

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markNotNew() {
        this.isNew = false;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public BomType getBomType() {
        return bomType;
    }

    public void setBomType(BomType bomType) {
        this.bomType = bomType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Boolean getControlsInventory() {
        return controlsInventory;
    }

    public void setControlsInventory(Boolean controlsInventory) {
        this.controlsInventory = controlsInventory;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }
}
