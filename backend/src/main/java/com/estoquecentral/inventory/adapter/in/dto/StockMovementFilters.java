package com.estoquecentral.inventory.adapter.in.dto;

import com.estoquecentral.inventory.domain.MovementType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StockMovementFilters - Filter criteria for stock movement queries
 * Story 2.8: Stock Movement History - AC2
 *
 * Supports multiple optional filters for complex queries
 */
public class StockMovementFilters {

    private UUID productId;
    private UUID variantId;
    private UUID locationId;
    private MovementType type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String documentType;
    private UUID documentId;
    private UUID userId;

    // Pagination
    private Integer page = 0;
    private Integer size = 50;

    // ============================================================
    // Constructors
    // ============================================================

    public StockMovementFilters() {
    }

    // ============================================================
    // Validation
    // ============================================================

    /**
     * Checks if any filter is set
     */
    public boolean hasFilters() {
        return productId != null || variantId != null || locationId != null ||
               type != null || startDate != null || endDate != null ||
               documentType != null || documentId != null || userId != null;
    }

    /**
     * Validates date range
     */
    public void validate() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    // ============================================================
    // Getters and Setters
    // ============================================================

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
