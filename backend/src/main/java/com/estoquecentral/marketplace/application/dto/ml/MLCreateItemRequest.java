package com.estoquecentral.marketplace.application.dto.ml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating an item (listing) on Mercado Livre
 * Story 5.3: Publish Products to Mercado Livre - AC1
 *
 * API Reference: https://developers.mercadolibre.com.ar/en_us/items-and-searches
 */
public class MLCreateItemRequest {

    private String title;
    private String categoryId;
    private BigDecimal price;
    private String currencyId = "BRL";
    private Integer availableQuantity;
    private String buyingMode = "buy_it_now";
    private String condition = "new";
    private String listingTypeId = "gold_special";
    private String description;
    private List<MLPicture> pictures = new ArrayList<>();
    private List<MLAttribute> attributes = new ArrayList<>();
    private List<MLVariation> variations = new ArrayList<>();

    // Constructors

    public MLCreateItemRequest() {
    }

    // Getters and Setters

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

    public String getBuyingMode() {
        return buyingMode;
    }

    public void setBuyingMode(String buyingMode) {
        this.buyingMode = buyingMode;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getListingTypeId() {
        return listingTypeId;
    }

    public void setListingTypeId(String listingTypeId) {
        this.listingTypeId = listingTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MLPicture> getPictures() {
        return pictures;
    }

    public void setPictures(List<MLPicture> pictures) {
        this.pictures = pictures;
    }

    public List<MLAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<MLAttribute> attributes) {
        this.attributes = attributes;
    }

    public List<MLVariation> getVariations() {
        return variations;
    }

    public void setVariations(List<MLVariation> variations) {
        this.variations = variations;
    }

    /**
     * Check if this is a variant product (has variations)
     */
    public boolean hasVariations() {
        return variations != null && !variations.isEmpty();
    }
}
