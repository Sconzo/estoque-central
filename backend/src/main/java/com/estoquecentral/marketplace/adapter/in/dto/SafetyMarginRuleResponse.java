package com.estoquecentral.marketplace.adapter.in.dto;

import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.RulePriority;
import com.estoquecentral.marketplace.domain.SafetyMarginRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for safety margin rule responses
 * Story 5.7: Configurable Safety Stock Margin
 */
public class SafetyMarginRuleResponse {

    private UUID id;
    private Marketplace marketplace;
    private RulePriority priority;
    private UUID productId;
    private String productName;  // Enriched field
    private UUID categoryId;
    private String categoryName;  // Enriched field
    private BigDecimal marginPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Factory method

    public static SafetyMarginRuleResponse fromEntity(SafetyMarginRule rule) {
        SafetyMarginRuleResponse response = new SafetyMarginRuleResponse();
        response.id = rule.getId();
        response.marketplace = rule.getMarketplace();
        response.priority = rule.getPriorityEnum();
        response.productId = rule.getProductId();
        response.categoryId = rule.getCategoryId();
        response.marginPercentage = rule.getMarginPercentage();
        response.createdAt = rule.getDataCriacao();
        response.updatedAt = rule.getDataAtualizacao();
        return response;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getMarginPercentage() {
        return marginPercentage;
    }

    public void setMarginPercentage(BigDecimal marginPercentage) {
        this.marginPercentage = marginPercentage;
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
}
