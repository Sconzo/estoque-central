package com.estoquecentral.inventory.adapter.in.dto;

import com.estoquecentral.inventory.domain.Inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class InventoryDTO {
    private UUID id;
    private UUID productId;
    private BigDecimal quantity;
    private BigDecimal reservedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal minQuantity;
    private BigDecimal maxQuantity;
    private String location;
    private Boolean isBelowMinimum;
    private Boolean isAboveMaximum;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InventoryDTO fromEntity(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProductId());
        dto.setQuantity(inventory.getQuantity());
        dto.setReservedQuantity(inventory.getReservedQuantity());
        dto.setAvailableQuantity(inventory.getAvailableQuantity());
        dto.setMinQuantity(inventory.getMinQuantity());
        dto.setMaxQuantity(inventory.getMaxQuantity());
        dto.setLocation(inventory.getLocation());
        dto.setIsBelowMinimum(inventory.isBelowMinimum());
        dto.setIsAboveMaximum(inventory.isAboveMaximum());
        dto.setCreatedAt(inventory.getCreatedAt());
        dto.setUpdatedAt(inventory.getUpdatedAt());
        return dto;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }
    public BigDecimal getMinQuantity() { return minQuantity; }
    public void setMinQuantity(BigDecimal minQuantity) { this.minQuantity = minQuantity; }
    public BigDecimal getMaxQuantity() { return maxQuantity; }
    public void setMaxQuantity(BigDecimal maxQuantity) { this.maxQuantity = maxQuantity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Boolean getIsBelowMinimum() { return isBelowMinimum; }
    public void setIsBelowMinimum(Boolean isBelowMinimum) { this.isBelowMinimum = isBelowMinimum; }
    public Boolean getIsAboveMaximum() { return isAboveMaximum; }
    public void setIsAboveMaximum(Boolean isAboveMaximum) { this.isAboveMaximum = isAboveMaximum; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
