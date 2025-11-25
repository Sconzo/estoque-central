package com.estoquecentral.marketplace.application.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Mercado Livre /items/{id} response
 * Story 5.2: Import Products from Mercado Livre - AC3
 */
public class MLItemResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("available_quantity")
    private Integer availableQuantity;

    @JsonProperty("status")
    private String status;

    @JsonProperty("condition")
    private String condition;

    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("pictures")
    private List<Picture> pictures;

    @JsonProperty("attributes")
    private List<Attribute> attributes;

    @JsonProperty("variations")
    private List<Variation> variations;

    @JsonProperty("description")
    private Description description;

    @JsonProperty("category_id")
    private String categoryId;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(List<Picture> pictures) {
        this.pictures = pictures;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<Variation> getVariations() {
        return variations;
    }

    public void setVariations(List<Variation> variations) {
        this.variations = variations;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    // Nested classes

    public static class Picture {
        @JsonProperty("url")
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Attribute {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("value_name")
        private String valueName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValueName() {
            return valueName;
        }

        public void setValueName(String valueName) {
            this.valueName = valueName;
        }
    }

    public static class Variation {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("price")
        private BigDecimal price;

        @JsonProperty("available_quantity")
        private Integer availableQuantity;

        @JsonProperty("sold_quantity")
        private Integer soldQuantity;

        @JsonProperty("attribute_combinations")
        private List<AttributeCombination> attributeCombinations;

        @JsonProperty("picture_ids")
        private List<String> pictureIds;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getAvailableQuantity() {
            return availableQuantity;
        }

        public void setAvailableQuantity(Integer availableQuantity) {
            this.availableQuantity = availableQuantity;
        }

        public Integer getSoldQuantity() {
            return soldQuantity;
        }

        public void setSoldQuantity(Integer soldQuantity) {
            this.soldQuantity = soldQuantity;
        }

        public List<AttributeCombination> getAttributeCombinations() {
            return attributeCombinations;
        }

        public void setAttributeCombinations(List<AttributeCombination> attributeCombinations) {
            this.attributeCombinations = attributeCombinations;
        }

        public List<String> getPictureIds() {
            return pictureIds;
        }

        public void setPictureIds(List<String> pictureIds) {
            this.pictureIds = pictureIds;
        }
    }

    public static class AttributeCombination {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("value_name")
        private String valueName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValueName() {
            return valueName;
        }

        public void setValueName(String valueName) {
            this.valueName = valueName;
        }
    }

    public static class Description {
        @JsonProperty("plain_text")
        private String plainText;

        public String getPlainText() {
            return plainText;
        }

        public void setPlainText(String plainText) {
            this.plainText = plainText;
        }
    }

    // Helper methods

    public boolean hasVariations() {
        return variations != null && !variations.isEmpty();
    }
}
