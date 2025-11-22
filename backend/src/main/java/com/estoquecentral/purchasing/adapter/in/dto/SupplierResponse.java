package com.estoquecentral.purchasing.adapter.in.dto;

import com.estoquecentral.purchasing.domain.Supplier;
import com.estoquecentral.purchasing.domain.SupplierStatus;
import com.estoquecentral.purchasing.domain.SupplierType;
import com.estoquecentral.purchasing.domain.TaxRegime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SupplierResponse - DTO for supplier responses
 * Story 3.1: Supplier Management
 */
public class SupplierResponse {

    private UUID id;
    private String supplierCode;
    private SupplierType supplierType;

    // Business details
    private String companyName;
    private String tradeName;
    private String cnpj;

    // Individual details
    private String firstName;
    private String lastName;
    private String cpf;

    // Contact information
    private String email;
    private String phone;
    private String mobile;
    private String website;

    // Address
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // Complete address formatted
    private String fullAddress;

    // Fiscal data
    private String stateRegistration;
    private String municipalRegistration;
    private TaxRegime taxRegime;
    private Boolean icmsTaxpayer;

    // Bank details
    private String bankName;
    private String bankCode;
    private String bankBranch;
    private String bankAccount;
    private String bankAccountType;
    private String pixKey;

    // Business details
    private String paymentTerms;
    private String defaultPaymentMethod;
    private BigDecimal creditLimit;
    private Integer averageDeliveryDays;
    private BigDecimal minimumOrderValue;

    // Status and classification
    private SupplierStatus status;
    private String supplierCategory;
    private Integer rating;
    private Boolean isPreferred;

    // Notes
    private String notes;
    private String internalNotes;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean ativo;

    /**
     * Factory method to create SupplierResponse from Supplier entity
     */
    public static SupplierResponse fromEntity(Supplier supplier) {
        if (supplier == null) {
            return null;
        }

        SupplierResponse response = new SupplierResponse();
        response.setId(supplier.getId());
        response.setSupplierCode(supplier.getSupplierCode());
        response.setSupplierType(supplier.getSupplierType());

        // Business details
        response.setCompanyName(supplier.getCompanyName());
        response.setTradeName(supplier.getTradeName());
        response.setCnpj(supplier.getCnpj());

        // Individual details
        response.setFirstName(supplier.getFirstName());
        response.setLastName(supplier.getLastName());
        response.setCpf(supplier.getCpf());

        // Contact
        response.setEmail(supplier.getEmail());
        response.setPhone(supplier.getPhone());
        response.setMobile(supplier.getMobile());
        response.setWebsite(supplier.getWebsite());

        // Address
        response.setStreet(supplier.getStreet());
        response.setNumber(supplier.getNumber());
        response.setComplement(supplier.getComplement());
        response.setNeighborhood(supplier.getNeighborhood());
        response.setCity(supplier.getCity());
        response.setState(supplier.getState());
        response.setPostalCode(supplier.getPostalCode());
        response.setCountry(supplier.getCountry());
        response.setFullAddress(buildFullAddress(supplier));

        // Fiscal
        response.setStateRegistration(supplier.getStateRegistration());
        response.setMunicipalRegistration(supplier.getMunicipalRegistration());
        response.setTaxRegime(supplier.getTaxRegime());
        response.setIcmsTaxpayer(supplier.getIcmsTaxpayer());

        // Bank
        response.setBankName(supplier.getBankName());
        response.setBankCode(supplier.getBankCode());
        response.setBankBranch(supplier.getBankBranch());
        response.setBankAccount(supplier.getBankAccount());
        response.setBankAccountType(supplier.getBankAccountType());
        response.setPixKey(supplier.getPixKey());

        // Business
        response.setPaymentTerms(supplier.getPaymentTerms());
        response.setDefaultPaymentMethod(supplier.getDefaultPaymentMethod());
        response.setCreditLimit(supplier.getCreditLimit());
        response.setAverageDeliveryDays(supplier.getAverageDeliveryDays());
        response.setMinimumOrderValue(supplier.getMinimumOrderValue());

        // Classification
        response.setStatus(supplier.getStatus());
        response.setSupplierCategory(supplier.getSupplierCategory());
        response.setRating(supplier.getRating());
        response.setIsPreferred(supplier.getIsPreferred());

        // Notes
        response.setNotes(supplier.getNotes());
        response.setInternalNotes(supplier.getInternalNotes());

        // Audit
        response.setCreatedAt(supplier.getCreatedAt());
        response.setUpdatedAt(supplier.getUpdatedAt());
        response.setAtivo(supplier.getAtivo());

        return response;
    }

    /**
     * Build formatted full address
     */
    private static String buildFullAddress(Supplier supplier) {
        StringBuilder address = new StringBuilder();

        if (supplier.getStreet() != null && !supplier.getStreet().isEmpty()) {
            address.append(supplier.getStreet());
        }

        if (supplier.getNumber() != null && !supplier.getNumber().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(supplier.getNumber());
        }

        if (supplier.getComplement() != null && !supplier.getComplement().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(supplier.getComplement());
        }

        if (supplier.getNeighborhood() != null && !supplier.getNeighborhood().isEmpty()) {
            if (address.length() > 0) address.append(" - ");
            address.append(supplier.getNeighborhood());
        }

        if (supplier.getCity() != null && !supplier.getCity().isEmpty()) {
            if (address.length() > 0) address.append(" - ");
            address.append(supplier.getCity());
        }

        if (supplier.getState() != null && !supplier.getState().isEmpty()) {
            address.append("/").append(supplier.getState());
        }

        if (supplier.getPostalCode() != null && !supplier.getPostalCode().isEmpty()) {
            if (address.length() > 0) address.append(" - ");
            address.append(supplier.getPostalCode());
        }

        return address.toString();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public SupplierType getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(SupplierType supplierType) {
        this.supplierType = supplierType;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
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

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getStateRegistration() {
        return stateRegistration;
    }

    public void setStateRegistration(String stateRegistration) {
        this.stateRegistration = stateRegistration;
    }

    public String getMunicipalRegistration() {
        return municipalRegistration;
    }

    public void setMunicipalRegistration(String municipalRegistration) {
        this.municipalRegistration = municipalRegistration;
    }

    public TaxRegime getTaxRegime() {
        return taxRegime;
    }

    public void setTaxRegime(TaxRegime taxRegime) {
        this.taxRegime = taxRegime;
    }

    public Boolean getIcmsTaxpayer() {
        return icmsTaxpayer;
    }

    public void setIcmsTaxpayer(Boolean icmsTaxpayer) {
        this.icmsTaxpayer = icmsTaxpayer;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankBranch() {
        return bankBranch;
    }

    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(String bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    public String getPixKey() {
        return pixKey;
    }

    public void setPixKey(String pixKey) {
        this.pixKey = pixKey;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getDefaultPaymentMethod() {
        return defaultPaymentMethod;
    }

    public void setDefaultPaymentMethod(String defaultPaymentMethod) {
        this.defaultPaymentMethod = defaultPaymentMethod;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Integer getAverageDeliveryDays() {
        return averageDeliveryDays;
    }

    public void setAverageDeliveryDays(Integer averageDeliveryDays) {
        this.averageDeliveryDays = averageDeliveryDays;
    }

    public BigDecimal getMinimumOrderValue() {
        return minimumOrderValue;
    }

    public void setMinimumOrderValue(BigDecimal minimumOrderValue) {
        this.minimumOrderValue = minimumOrderValue;
    }

    public SupplierStatus getStatus() {
        return status;
    }

    public void setStatus(SupplierStatus status) {
        this.status = status;
    }

    public String getSupplierCategory() {
        return supplierCategory;
    }

    public void setSupplierCategory(String supplierCategory) {
        this.supplierCategory = supplierCategory;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Boolean getIsPreferred() {
        return isPreferred;
    }

    public void setIsPreferred(Boolean isPreferred) {
        this.isPreferred = isPreferred;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
