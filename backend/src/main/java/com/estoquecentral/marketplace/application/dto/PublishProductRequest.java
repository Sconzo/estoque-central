package com.estoquecentral.marketplace.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO for publish product request
 * Story 5.3: Publish Products to Mercado Livre - AC1
 */
public class PublishProductRequest {

    private List<UUID> productIds;

    public PublishProductRequest() {
    }

    public PublishProductRequest(List<UUID> productIds) {
        this.productIds = productIds;
    }

    // Getters and Setters

    public List<UUID> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<UUID> productIds) {
        this.productIds = productIds;
    }
}
