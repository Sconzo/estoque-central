package com.estoquecentral.marketplace.application.dto.ml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Mercado Livre Order API response
 * Story 5.5: Import and Process Orders from Mercado Livre - AC3, AC4
 *
 * Represents the response from GET /orders/{id}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MLOrderResponse {

    private Long id;
    private String status;  // confirmed, payment_required, payment_in_process, paid, etc

    @JsonProperty("date_created")
    private String dateCreated;

    @JsonProperty("date_closed")
    private String dateClosed;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("currency_id")
    private String currencyId;

    private MLBuyer buyer;

    @JsonProperty("order_items")
    private List<MLOrderItem> orderItems;

    private List<MLPayment> payments;

    private MLShipping shipping;

    // Nested Classes

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MLBuyer {
        private Long id;
        private String nickname;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        private String email;

        private MLPhone phone;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MLPhone {
            @JsonProperty("area_code")
            private String areaCode;
            private String number;
            private String extension;

            public String getAreaCode() {
                return areaCode;
            }

            public void setAreaCode(String areaCode) {
                this.areaCode = areaCode;
            }

            public String getNumber() {
                return number;
            }

            public void setNumber(String number) {
                this.number = number;
            }

            public String getExtension() {
                return extension;
            }

            public void setExtension(String extension) {
                this.extension = extension;
            }

            public String getFullPhone() {
                if (areaCode != null && number != null) {
                    return "(" + areaCode + ") " + number;
                }
                return number;
            }
        }

        // Getters and Setters

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public MLPhone getPhone() {
            return phone;
        }

        public void setPhone(MLPhone phone) {
            this.phone = phone;
        }

        public String getFullName() {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            return nickname;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MLOrderItem {
        private MLItem item;
        private Integer quantity;

        @JsonProperty("unit_price")
        private BigDecimal unitPrice;

        @JsonProperty("full_unit_price")
        private BigDecimal fullUnitPrice;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MLItem {
            private String id;  // listing_id
            private String title;

            @JsonProperty("variation_id")
            private Long variationId;

            @JsonProperty("variation_attributes")
            private List<MLVariationAttribute> variationAttributes;

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class MLVariationAttribute {
                private String id;
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

            public Long getVariationId() {
                return variationId;
            }

            public void setVariationId(Long variationId) {
                this.variationId = variationId;
            }

            public List<MLVariationAttribute> getVariationAttributes() {
                return variationAttributes;
            }

            public void setVariationAttributes(List<MLVariationAttribute> variationAttributes) {
                this.variationAttributes = variationAttributes;
            }
        }

        // Getters and Setters

        public MLItem getItem() {
            return item;
        }

        public void setItem(MLItem item) {
            this.item = item;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getFullUnitPrice() {
            return fullUnitPrice;
        }

        public void setFullUnitPrice(BigDecimal fullUnitPrice) {
            this.fullUnitPrice = fullUnitPrice;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MLPayment {
        private Long id;
        private String status;  // approved, pending, rejected, etc

        @JsonProperty("status_detail")
        private String statusDetail;

        @JsonProperty("payment_method_id")
        private String paymentMethodId;

        @JsonProperty("transaction_amount")
        private BigDecimal transactionAmount;

        @JsonProperty("date_approved")
        private String dateApproved;

        // Getters and Setters

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStatusDetail() {
            return statusDetail;
        }

        public void setStatusDetail(String statusDetail) {
            this.statusDetail = statusDetail;
        }

        public String getPaymentMethodId() {
            return paymentMethodId;
        }

        public void setPaymentMethodId(String paymentMethodId) {
            this.paymentMethodId = paymentMethodId;
        }

        public BigDecimal getTransactionAmount() {
            return transactionAmount;
        }

        public void setTransactionAmount(BigDecimal transactionAmount) {
            this.transactionAmount = transactionAmount;
        }

        public String getDateApproved() {
            return dateApproved;
        }

        public void setDateApproved(String dateApproved) {
            this.dateApproved = dateApproved;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MLShipping {
        private Long id;
        private String status;  // to_be_agreed, pending, handling, ready_to_ship, shipped, delivered, etc

        @JsonProperty("substatus")
        private String subStatus;

        // Getters and Setters

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSubStatus() {
            return subStatus;
        }

        public void setSubStatus(String subStatus) {
            this.subStatus = subStatus;
        }
    }

    // Main Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateClosed() {
        return dateClosed;
    }

    public void setDateClosed(String dateClosed) {
        this.dateClosed = dateClosed;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public MLBuyer getBuyer() {
        return buyer;
    }

    public void setBuyer(MLBuyer buyer) {
        this.buyer = buyer;
    }

    public List<MLOrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<MLOrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public List<MLPayment> getPayments() {
        return payments;
    }

    public void setPayments(List<MLPayment> payments) {
        this.payments = payments;
    }

    public MLShipping getShipping() {
        return shipping;
    }

    public void setShipping(MLShipping shipping) {
        this.shipping = shipping;
    }

    /**
     * Helper: Check if payment is approved
     */
    public boolean isPaymentApproved() {
        if (payments == null || payments.isEmpty()) {
            return false;
        }
        return payments.stream().anyMatch(p -> "approved".equalsIgnoreCase(p.getStatus()));
    }
}
