package com.estoquecentral.marketplace.application.dto.ml;

/**
 * DTO for Mercado Livre upload picture response
 * Story 5.3: Publish Products to Mercado Livre - AC4
 *
 * API: POST /pictures (multipart/form-data)
 */
public class MLUploadPictureResponse {

    private String id;
    private String url;
    private String secureUrl;
    private Integer size;
    private Integer maxSize;
    private String quality;

    public MLUploadPictureResponse() {
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }
}
