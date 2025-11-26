package com.estoquecentral.marketplace.application.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO for publish product response
 * Story 5.3: Publish Products to Mercado Livre - AC1
 */
public class PublishProductResponse {

    private int published;
    private List<PublishError> errors = new ArrayList<>();

    public PublishProductResponse() {
    }

    public PublishProductResponse(int published, List<PublishError> errors) {
        this.published = published;
        this.errors = errors;
    }

    // Getters and Setters

    public int getPublished() {
        return published;
    }

    public void setPublished(int published) {
        this.published = published;
    }

    public List<PublishError> getErrors() {
        return errors;
    }

    public void setErrors(List<PublishError> errors) {
        this.errors = errors;
    }

    /**
     * Add published count
     */
    public void incrementPublished() {
        this.published++;
    }

    /**
     * Add error
     */
    public void addError(UUID productId, String productName, String errorMessage) {
        this.errors.add(new PublishError(productId, productName, errorMessage));
    }

    /**
     * PublishError - Error details for a failed product publish
     */
    public static class PublishError {
        private UUID productId;
        private String productName;
        private String errorMessage;

        public PublishError() {
        }

        public PublishError(UUID productId, String productName, String errorMessage) {
            this.productId = productId;
            this.productName = productName;
            this.errorMessage = errorMessage;
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

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
