package com.estoquecentral.marketplace.application.dto.ml;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Mercado Livre create item response
 * Story 5.3: Publish Products to Mercado Livre - AC1
 */
public class MLCreateItemResponse {

    private String id;
    private String title;
    private String categoryId;
    private BigDecimal price;
    private String currencyId;
    private Integer availableQuantity;
    private String status;
    private String permalink;
    private List<MLVariation> variations;

    public MLCreateItemResponse() {
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public List<MLVariation> getVariations() {
        return variations;
    }

    public void setVariations(List<MLVariation> variations) {
        this.variations = variations;
    }
}
