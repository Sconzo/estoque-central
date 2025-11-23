package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fiscal Event entity - Immutable audit trail for fiscal events
 * Story 4.3: NFCe Emission and Stock Decrease - NFR16 (5-year retention)
 */
@Table("fiscal_events")
public class FiscalEvent {
    @Id
    private UUID id;
    private UUID tenantId;

    // Event classification
    private FiscalEventType eventType;

    // Related entities
    private UUID saleId;
    private String nfceKey;
    private String xmlSnapshot;

    // Event details
    private String errorMessage;
    private Integer httpStatusCode;
    private Integer retryCount;

    // Audit
    private UUID userId;
    private LocalDateTime timestamp;

    // Constructors
    public FiscalEvent() {
        this.retryCount = 0;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public FiscalEventType getEventType() {
        return eventType;
    }

    public void setEventType(FiscalEventType eventType) {
        this.eventType = eventType;
    }

    public UUID getSaleId() {
        return saleId;
    }

    public void setSaleId(UUID saleId) {
        this.saleId = saleId;
    }

    public String getNfceKey() {
        return nfceKey;
    }

    public void setNfceKey(String nfceKey) {
        this.nfceKey = nfceKey;
    }

    public String getXmlSnapshot() {
        return xmlSnapshot;
    }

    public void setXmlSnapshot(String xmlSnapshot) {
        this.xmlSnapshot = xmlSnapshot;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Static factory methods for common events
    public static FiscalEvent nfceEmitted(UUID tenantId, UUID saleId, String nfceKey, String xml, UUID userId) {
        FiscalEvent event = new FiscalEvent();
        event.setTenantId(tenantId);
        event.setEventType(FiscalEventType.NFCE_EMITTED);
        event.setSaleId(saleId);
        event.setNfceKey(nfceKey);
        event.setXmlSnapshot(xml);
        event.setUserId(userId);
        return event;
    }

    public static FiscalEvent nfceFailed(UUID tenantId, UUID saleId, String errorMessage, Integer httpStatus, UUID userId) {
        FiscalEvent event = new FiscalEvent();
        event.setTenantId(tenantId);
        event.setEventType(FiscalEventType.NFCE_FAILED);
        event.setSaleId(saleId);
        event.setErrorMessage(errorMessage);
        event.setHttpStatusCode(httpStatus);
        event.setUserId(userId);
        return event;
    }

    public static FiscalEvent nfceRetry(UUID tenantId, UUID saleId, int retryCount, UUID userId) {
        FiscalEvent event = new FiscalEvent();
        event.setTenantId(tenantId);
        event.setEventType(FiscalEventType.NFCE_RETRY);
        event.setSaleId(saleId);
        event.setRetryCount(retryCount);
        event.setUserId(userId);
        return event;
    }
}
