package com.estoquecentral.marketplace.application.dto.ml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Mercado Livre variation (variant)
 * Story 5.3: Publish Products to Mercado Livre - AC3
 */
public class MLVariation {

    private Long id;
    private Integer availableQuantity;
    private BigDecimal price;
    private List<MLAttribute> attributeCombinations = new ArrayList<>();
    private List<String> pictureIds = new ArrayList<>();

    public MLVariation() {
    }

    public MLVariation(Integer availableQuantity, BigDecimal price) {
        this.availableQuantity = availableQuantity;
        this.price = price;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<MLAttribute> getAttributeCombinations() {
        return attributeCombinations;
    }

    public void setAttributeCombinations(List<MLAttribute> attributeCombinations) {
        this.attributeCombinations = attributeCombinations;
    }

    public List<String> getPictureIds() {
        return pictureIds;
    }

    public void setPictureIds(List<String> pictureIds) {
        this.pictureIds = pictureIds;
    }
}
