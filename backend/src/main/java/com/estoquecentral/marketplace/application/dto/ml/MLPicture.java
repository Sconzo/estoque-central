package com.estoquecentral.marketplace.application.dto.ml;

/**
 * DTO for Mercado Livre picture
 * Story 5.3: Publish Products to Mercado Livre - AC4
 */
public class MLPicture {

    private String id;
    private String source;

    public MLPicture() {
    }

    public MLPicture(String source) {
        this.source = source;
    }

    public MLPicture(String id, String source) {
        this.id = id;
        this.source = source;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
