package com.estoquecentral.catalog.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * ImportLog - Tracks CSV import operations
 *
 * <p>Stores metadata about product imports including:
 * <ul>
 *   <li>File information and user who performed import</li>
 *   <li>Row statistics (total, success, errors)</li>
 *   <li>Import status (PREVIEW, PROCESSING, COMPLETED, FAILED)</li>
 *   <li>Row-level error details as JSONB</li>
 * </ul>
 */
@Table("import_logs")
public class ImportLog {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("user_id")
    private UUID userId;

    @Column("file_name")
    private String fileName;

    @Column("total_rows")
    private Integer totalRows;

    @Column("success_rows")
    private Integer successRows;

    @Column("error_rows")
    private Integer errorRows;

    @Column("status")
    private ImportStatus status;

    /**
     * JSONB column storing row-level errors
     * Format: [{"row": 1, "errors": ["SKU is required", "Invalid price"]}, ...]
     */
    @Column("error_details")
    private String errorDetails;

    @Column("created_at")
    private Instant createdAt;

    // Constructors

    public ImportLog() {
        this.totalRows = 0;
        this.successRows = 0;
        this.errorRows = 0;
        this.createdAt = Instant.now();
    }

    public ImportLog(UUID tenantId, UUID userId, String fileName, ImportStatus status) {
        this();
        this.tenantId = tenantId;
        this.userId = userId;
        this.fileName = fileName;
        this.status = status;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getSuccessRows() {
        return successRows;
    }

    public void setSuccessRows(Integer successRows) {
        this.successRows = successRows;
    }

    public Integer getErrorRows() {
        return errorRows;
    }

    public void setErrorRows(Integer errorRows) {
        this.errorRows = errorRows;
    }

    public ImportStatus getStatus() {
        return status;
    }

    public void setStatus(ImportStatus status) {
        this.status = status;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
