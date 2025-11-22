package com.estoquecentral.catalog.adapter.in.dto;

import java.util.List;
import java.util.UUID;

/**
 * ImportPreviewResponse - Response for CSV import preview
 *
 * <p>Contains:
 * <ul>
 *   <li>Import log ID for tracking</li>
 *   <li>File name</li>
 *   <li>All parsed rows with validation results</li>
 *   <li>Summary statistics (total, valid, invalid rows)</li>
 * </ul>
 *
 * <p>User can review this preview and decide whether to confirm the import
 */
public class ImportPreviewResponse {
    private UUID importLogId;
    private String fileName;
    private List<ProductCsvRow> rows;
    private int totalRows;
    private int validRows;
    private int invalidRows;

    public ImportPreviewResponse() {
    }

    public ImportPreviewResponse(UUID importLogId, String fileName, List<ProductCsvRow> rows) {
        this.importLogId = importLogId;
        this.fileName = fileName;
        this.rows = rows;
        this.totalRows = rows.size();
        this.validRows = (int) rows.stream().filter(ProductCsvRow::isValid).count();
        this.invalidRows = totalRows - validRows;
    }

    // Getters and Setters

    public UUID getImportLogId() {
        return importLogId;
    }

    public void setImportLogId(UUID importLogId) {
        this.importLogId = importLogId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<ProductCsvRow> getRows() {
        return rows;
    }

    public void setRows(List<ProductCsvRow> rows) {
        this.rows = rows;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getValidRows() {
        return validRows;
    }

    public void setValidRows(int validRows) {
        this.validRows = validRows;
    }

    public int getInvalidRows() {
        return invalidRows;
    }

    public void setInvalidRows(int invalidRows) {
        this.invalidRows = invalidRows;
    }
}
