package com.estoquecentral.marketplace.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SafetyMarginRule Entity - Configurable safety margins for marketplace stock sync
 * Story 5.7: Configurable Safety Stock Margin - AC1
 *
 * <p>Defines safety margins applied when synchronizing stock to marketplaces.
 * Example: margin_percentage = 90 means publish 90% of available stock to prevent overselling.
 *
 * <p><strong>Priority Levels:</strong>
 * <ul>
 *   <li>1 (PRODUCT): Rule for specific product (highest priority)</li>
 *   <li>2 (CATEGORY): Rule for all products in category (medium priority)</li>
 *   <li>3 (GLOBAL): Rule for entire marketplace (lowest priority)</li>
 * </ul>
 */
@Table("safety_margin_rules")
public class SafetyMarginRule {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("marketplace")
    private Marketplace marketplace;

    @Column("product_id")
    private UUID productId;  // Nullable - only set for priority=1

    @Column("category_id")
    private UUID categoryId;  // Nullable - only set for priority=2

    @Column("margin_percentage")
    private BigDecimal marginPercentage;  // 0-100

    @Column("priority")
    private int priority;  // 1=product, 2=category, 3=global

    @Column("data_criacao")
    private LocalDateTime dataCriacao;

    @Column("data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column("created_by_user_id")
    private UUID createdByUserId;

    // Constructors

    public SafetyMarginRule() {
        this.marginPercentage = new BigDecimal("100.00");
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    /**
     * Creates a global marketplace rule (priority=3)
     */
    public static SafetyMarginRule createGlobalRule(UUID tenantId, Marketplace marketplace, BigDecimal marginPercentage, UUID userId) {
        SafetyMarginRule rule = new SafetyMarginRule();
        rule.tenantId = tenantId;
        rule.marketplace = marketplace;
        rule.marginPercentage = marginPercentage;
        rule.priority = RulePriority.GLOBAL.getValue();
        rule.createdByUserId = userId;
        return rule;
    }

    /**
     * Creates a category-specific rule (priority=2)
     */
    public static SafetyMarginRule createCategoryRule(UUID tenantId, Marketplace marketplace, UUID categoryId,
                                                      BigDecimal marginPercentage, UUID userId) {
        SafetyMarginRule rule = new SafetyMarginRule();
        rule.tenantId = tenantId;
        rule.marketplace = marketplace;
        rule.categoryId = categoryId;
        rule.marginPercentage = marginPercentage;
        rule.priority = RulePriority.CATEGORY.getValue();
        rule.createdByUserId = userId;
        return rule;
    }

    /**
     * Creates a product-specific rule (priority=1)
     */
    public static SafetyMarginRule createProductRule(UUID tenantId, Marketplace marketplace, UUID productId,
                                                     BigDecimal marginPercentage, UUID userId) {
        SafetyMarginRule rule = new SafetyMarginRule();
        rule.tenantId = tenantId;
        rule.marketplace = marketplace;
        rule.productId = productId;
        rule.marginPercentage = marginPercentage;
        rule.priority = RulePriority.PRODUCT.getValue();
        rule.createdByUserId = userId;
        return rule;
    }

    // Business methods

    public RulePriority getPriorityEnum() {
        return RulePriority.fromValue(this.priority);
    }

    public boolean isGlobalRule() {
        return priority == RulePriority.GLOBAL.getValue();
    }

    public boolean isCategoryRule() {
        return priority == RulePriority.CATEGORY.getValue();
    }

    public boolean isProductRule() {
        return priority == RulePriority.PRODUCT.getValue();
    }

    public void updateMargin(BigDecimal newMargin) {
        if (newMargin.compareTo(BigDecimal.ZERO) < 0 || newMargin.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Margin percentage must be between 0 and 100");
        }
        this.marginPercentage = newMargin;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void validate() {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }
        if (marketplace == null) {
            throw new IllegalArgumentException("Marketplace is required");
        }
        if (marginPercentage == null || marginPercentage.compareTo(BigDecimal.ZERO) < 0 ||
            marginPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Margin percentage must be between 0 and 100");
        }
        if (priority < 1 || priority > 3) {
            throw new IllegalArgumentException("Priority must be 1, 2, or 3");
        }

        // Validate scope based on priority
        RulePriority priorityEnum = RulePriority.fromValue(priority);
        switch (priorityEnum) {
            case PRODUCT:
                if (productId == null) {
                    throw new IllegalArgumentException("Product ID required for product-specific rule");
                }
                if (categoryId != null) {
                    throw new IllegalArgumentException("Category ID must be null for product-specific rule");
                }
                break;
            case CATEGORY:
                if (categoryId == null) {
                    throw new IllegalArgumentException("Category ID required for category rule");
                }
                if (productId != null) {
                    throw new IllegalArgumentException("Product ID must be null for category rule");
                }
                break;
            case GLOBAL:
                if (productId != null || categoryId != null) {
                    throw new IllegalArgumentException("Product/Category ID must be null for global rule");
                }
                break;
        }
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

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getMarginPercentage() {
        return marginPercentage;
    }

    public void setMarginPercentage(BigDecimal marginPercentage) {
        this.marginPercentage = marginPercentage;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
}
