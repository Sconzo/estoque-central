package com.estoquecentral.inventory.adapter.in.dto;

import com.estoquecentral.inventory.domain.Location;
import com.estoquecentral.inventory.domain.LocationType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * LocationResponse - DTO for returning location data
 */
public class LocationResponse {

    private UUID id;
    private UUID tenantId;
    private String code;
    private String name;
    private String description;
    private LocationType type;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;
    private String email;
    private String managerName;
    private UUID managerId;
    private Boolean isDefault;
    private Boolean allowNegativeStock;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LocationResponse() {
    }

    /**
     * Static factory method to create response from domain entity
     */
    public static LocationResponse from(Location location) {
        LocationResponse response = new LocationResponse();
        response.setId(location.getId());
        response.setTenantId(location.getTenantId());
        response.setCode(location.getCode());
        response.setName(location.getName());
        response.setDescription(location.getDescription());
        response.setType(location.getType());
        response.setAddress(location.getAddress());
        response.setCity(location.getCity());
        response.setState(location.getState());
        response.setPostalCode(location.getPostalCode());
        response.setCountry(location.getCountry());
        response.setPhone(location.getPhone());
        response.setEmail(location.getEmail());
        response.setManagerName(location.getManagerName());
        response.setManagerId(location.getManagerId());
        response.setIsDefault(location.getIsDefault());
        response.setAllowNegativeStock(location.getAllowNegativeStock());
        response.setAtivo(location.getAtivo());
        response.setCreatedAt(location.getCreatedAt());
        response.setUpdatedAt(location.getUpdatedAt());
        return response;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public UUID getManagerId() {
        return managerId;
    }

    public void setManagerId(UUID managerId) {
        this.managerId = managerId;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getAllowNegativeStock() {
        return allowNegativeStock;
    }

    public void setAllowNegativeStock(Boolean allowNegativeStock) {
        this.allowNegativeStock = allowNegativeStock;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
