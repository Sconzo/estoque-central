package com.estoquecentral.catalog.adapter.in.dto;

import java.util.UUID;

/**
 * ImportConfirmResponse - Response after confirming CSV import
 *
 * <p>Contains:
 * <ul>
 *   <li>Import log ID for tracking</li>
 *   <li>Success/error statistics</li>
 *   <li>Status message</li>
 * </ul>
 */
public class ImportConfirmResponse {
    private UUID importLogId;
    private int totalRows;
    private int successRows;
    private int errorRows;
    private String status;
    private String message;

    public ImportConfirmResponse() {
    }

    public ImportConfirmResponse(UUID importLogId, int totalRows, int successRows, int errorRows,
                                 String status, String message) {
        this.importLogId = importLogId;
        this.totalRows = totalRows;
        this.successRows = successRows;
        this.errorRows = errorRows;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters

    public UUID getImportLogId() {
        return importLogId;
    }

    public void setImportLogId(UUID importLogId) {
        this.importLogId = importLogId;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessRows() {
        return successRows;
    }

    public void setSuccessRows(int successRows) {
        this.successRows = successRows;
    }

    public int getErrorRows() {
        return errorRows;
    }

    public void setErrorRows(int errorRows) {
        this.errorRows = errorRows;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
