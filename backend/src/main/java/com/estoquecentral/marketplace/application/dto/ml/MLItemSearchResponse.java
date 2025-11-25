package com.estoquecentral.marketplace.application.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for Mercado Livre /users/me/items/search response
 * Story 5.2: Import Products from Mercado Livre - AC2
 */
public class MLItemSearchResponse {

    @JsonProperty("results")
    private List<String> results; // List of item IDs

    @JsonProperty("paging")
    private Paging paging;

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }

    public Paging getPaging() {
        return paging;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }

    public static class Paging {
        @JsonProperty("total")
        private Integer total;

        @JsonProperty("offset")
        private Integer offset;

        @JsonProperty("limit")
        private Integer limit;

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }
    }
}
