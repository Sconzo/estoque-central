package com.estoquecentral.marketplace.domain;

/**
 * Marketplace enum - Supported marketplace platforms
 * Story 5.1: Mercado Livre OAuth2 Authentication
 */
public enum Marketplace {
    MERCADO_LIVRE("Mercado Livre", "https://api.mercadolibre.com"),
    SHOPEE("Shopee", "https://partner.shopeemobile.com"),
    AMAZON("Amazon", "https://sellingpartnerapi-na.amazon.com");

    private final String displayName;
    private final String apiBaseUrl;

    Marketplace(String displayName, String apiBaseUrl) {
        this.displayName = displayName;
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
}
