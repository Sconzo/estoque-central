package com.estoquecentral.marketplace.adapter.in.dto;

import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.RulePriority;
import com.estoquecentral.marketplace.domain.SafetyMarginRule;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating/updating safety margin rules
 * Story 5.7: Configurable Safety Stock Margin
 */
public class SafetyMarginRuleRequest {

    private Marketplace marketplace;
    private RulePriority priority;
    private UUID productId;  // Required if priority=PRODUCT
    private UUID categoryId;  // Required if priority=CATEGORY
    private BigDecimal marginPercentage;  // 0-100

    // Constructors

    public SafetyMarginRuleRequest() {
    }

    public SafetyMarginRuleRequest(Marketplace marketplace, RulePriority priority, BigDecimal marginPercentage) {
        this.marketplace = marketplace;
        this.priority = priority;
        this.marginPercentage = marginPercentage;
    }

    // Validation

    public void validate() {
        if (marketplace == null) {
            throw new IllegalArgumentException("Marketplace is required");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Priority is required");
        }
        if (marginPercentage == null || marginPercentage.compareTo(BigDecimal.ZERO) < 0 ||
            marginPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Margin percentage must be between 0 and 100");
        }

        // Validate scope based on priority
        switch (priority) {
            case PRODUCT:
                if (productId == null) {
                    throw new IllegalArgumentException("Product ID is required for product-specific rule");
                }
                if (categoryId != null) {
                    throw new IllegalArgumentException("Category ID must be null for product-specific rule");
                }
                break;
            case CATEGORY:
                if (categoryId == null) {
                    throw new IllegalArgumentException("Category ID is required for category rule");
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

    // Conversion to entity

    public SafetyMarginRule toEntity(UUID tenantId, UUID userId) {
        validate();

        SafetyMarginRule rule;
        switch (priority) {
            case PRODUCT:
                rule = SafetyMarginRule.createProductRule(tenantId, marketplace, productId, marginPercentage, userId);
                break;
            case CATEGORY:
                rule = SafetyMarginRule.createCategoryRule(tenantId, marketplace, categoryId, marginPercentage, userId);
                break;
            case GLOBAL:
                rule = SafetyMarginRule.createGlobalRule(tenantId, marketplace, marginPercentage, userId);
                break;
            default:
                throw new IllegalArgumentException("Invalid priority: " + priority);
        }

        return rule;
    }

    // Getters and Setters

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    public RulePriority getPriority() {
        return priority;
    }

    public void setPriority(RulePriority priority) {
        this.priority = priority;
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
}
