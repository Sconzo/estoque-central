package com.estoquecentral.purchasing.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("mobile_receiving_sessions")
public class MobileReceivingSession {

    @Id
    private UUID id;
    private UUID tenantId;
    private String sessionNumber;
    private UUID purchaseOrderId;
    private UUID userId;
    private String deviceId;
    private String deviceName;
    private UUID locationId;
    private MobileSessionStatus status;
    private Integer totalItemsExpected;
    private Integer totalItemsScanned;
    private BigDecimal totalQuantityExpected;
    private BigDecimal totalQuantityScanned;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer durationSeconds;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MobileReceivingSession() {
        this.status = MobileSessionStatus.IN_PROGRESS;
        this.totalItemsExpected = 0;
        this.totalItemsScanned = 0;
        this.totalQuantityExpected = BigDecimal.ZERO;
        this.totalQuantityScanned = BigDecimal.ZERO;
        this.startedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isInProgress() {
        return this.status == MobileSessionStatus.IN_PROGRESS;
    }

    public boolean isPaused() {
        return this.status == MobileSessionStatus.PAUSED;
    }

    public boolean isCompleted() {
        return this.status == MobileSessionStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return this.status == MobileSessionStatus.CANCELLED;
    }

    public void pause() {
        this.status = MobileSessionStatus.PAUSED;
        this.updatedAt = LocalDateTime.now();
    }

    public void resume() {
        this.status = MobileSessionStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = MobileSessionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.durationSeconds = calculateDuration();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = MobileSessionStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
        this.durationSeconds = calculateDuration();
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementScanned(BigDecimal quantity) {
        this.totalItemsScanned++;
        this.totalQuantityScanned = this.totalQuantityScanned.add(quantity);
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getCompletionPercentage() {
        if (this.totalQuantityExpected.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return this.totalQuantityScanned
                .divide(this.totalQuantityExpected, 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private Integer calculateDuration() {
        if (this.startedAt == null) return 0;
        LocalDateTime endTime = this.completedAt != null ? this.completedAt : LocalDateTime.now();
        return (int) java.time.Duration.between(this.startedAt, endTime).getSeconds();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(String sessionNumber) { this.sessionNumber = sessionNumber; }

    public UUID getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(UUID purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public MobileSessionStatus getStatus() { return status; }
    public void setStatus(MobileSessionStatus status) { this.status = status; }

    public Integer getTotalItemsExpected() { return totalItemsExpected; }
    public void setTotalItemsExpected(Integer totalItemsExpected) { this.totalItemsExpected = totalItemsExpected; }

    public Integer getTotalItemsScanned() { return totalItemsScanned; }
    public void setTotalItemsScanned(Integer totalItemsScanned) { this.totalItemsScanned = totalItemsScanned; }

    public BigDecimal getTotalQuantityExpected() { return totalQuantityExpected; }
    public void setTotalQuantityExpected(BigDecimal totalQuantityExpected) { this.totalQuantityExpected = totalQuantityExpected; }

    public BigDecimal getTotalQuantityScanned() { return totalQuantityScanned; }
    public void setTotalQuantityScanned(BigDecimal totalQuantityScanned) { this.totalQuantityScanned = totalQuantityScanned; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
