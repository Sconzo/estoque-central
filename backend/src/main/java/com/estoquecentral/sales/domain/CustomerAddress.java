package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("customer_addresses")
public class CustomerAddress {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID customerId;
    private AddressType addressType;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String contactName;
    private String contactPhone;
    private Boolean isDefault;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerAddress() {
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(street);
        if (number != null) sb.append(", ").append(number);
        if (complement != null) sb.append(" - ").append(complement);
        sb.append(", ").append(neighborhood);
        sb.append(", ").append(city).append(" - ").append(state);
        sb.append(", ").append(postalCode);
        return sb.toString();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public AddressType getAddressType() { return addressType; }
    public void setAddressType(AddressType addressType) { this.addressType = addressType; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    // ... outros getters/setters
}
