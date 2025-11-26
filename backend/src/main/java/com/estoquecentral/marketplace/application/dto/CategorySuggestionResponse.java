package com.estoquecentral.marketplace.application.dto;

/**
 * DTO for category suggestion response
 * Story 5.3: Publish Products to Mercado Livre - AC2
 */
public class CategorySuggestionResponse {

    private String categoryId;
    private String categoryName;
    private String categoryPath;

    public CategorySuggestionResponse() {
    }

    public CategorySuggestionResponse(String categoryId, String categoryName, String categoryPath) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryPath = categoryPath;
    }

    // Getters and Setters

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(String categoryPath) {
        this.categoryPath = categoryPath;
    }
}
