package com.estoquecentral.marketplace.application.dto;

import java.math.BigDecimal;

/**
 * DTO for listing preview (before import)
 * Story 5.2: Import Products from Mercado Livre - AC2
 */
public class ListingPreviewResponse {
    private String listingId;
    private String title;
    private BigDecimal price;
    private Integer quantity;
    private String thumbnail;
    private boolean alreadyImported;
    private boolean hasVariations;

    // Constructors

    public ListingPreviewResponse() {
    }

    public ListingPreviewResponse(
        String listingId,
        String title,
        BigDecimal price,
        Integer quantity,
        String thumbnail,
        boolean alreadyImported,
        boolean hasVariations
    ) {
        this.listingId = listingId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.thumbnail = thumbnail;
        this.alreadyImported = alreadyImported;
        this.hasVariations = hasVariations;
    }

    // Getters and Setters

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isAlreadyImported() {
        return alreadyImported;
    }

    public void setAlreadyImported(boolean alreadyImported) {
        this.alreadyImported = alreadyImported;
    }

    public boolean isHasVariations() {
        return hasVariations;
    }

    public void setHasVariations(boolean hasVariations) {
        this.hasVariations = hasVariations;
    }
}
