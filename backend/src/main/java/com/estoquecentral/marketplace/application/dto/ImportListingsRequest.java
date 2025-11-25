package com.estoquecentral.marketplace.application.dto;

import java.util.List;

/**
 * DTO for import listings request
 * Story 5.2: Import Products from Mercado Livre - AC3
 */
public class ImportListingsRequest {
    private List<String> listingIds;

    public ImportListingsRequest() {
    }

    public ImportListingsRequest(List<String> listingIds) {
        this.listingIds = listingIds;
    }

    public List<String> getListingIds() {
        return listingIds;
    }

    public void setListingIds(List<String> listingIds) {
        this.listingIds = listingIds;
    }
}
