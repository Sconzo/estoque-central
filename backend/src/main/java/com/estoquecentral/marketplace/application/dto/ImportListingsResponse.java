package com.estoquecentral.marketplace.application.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for import listings response
 * Story 5.2: Import Products from Mercado Livre - AC3
 */
public class ImportListingsResponse {
    private int imported;
    private int skipped;
    private List<String> errors;

    public ImportListingsResponse() {
        this.imported = 0;
        this.skipped = 0;
        this.errors = new ArrayList<>();
    }

    public void incrementImported() {
        this.imported++;
    }

    public void incrementSkipped() {
        this.skipped++;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    // Getters and Setters

    public int getImported() {
        return imported;
    }

    public void setImported(int imported) {
        this.imported = imported;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
