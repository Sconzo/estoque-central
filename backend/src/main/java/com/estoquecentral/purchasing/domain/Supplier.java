package com.estoquecentral.purchasing.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("suppliers")
public class Supplier {

    @Id
    private UUID id;
    private UUID tenantId;
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

    // Internal notes
    private String notes;
    private String internalNotes;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
    private Boolean ativo;

    public Supplier() {
        this.supplierType = SupplierType.BUSINESS;
        this.status = SupplierStatus.ACTIVE;
        this.country = "Brasil";
        this.icmsTaxpayer = true;
        this.isPreferred = false;
        this.ativo = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == SupplierStatus.ACTIVE && this.ativo;
    }

    public boolean isInactive() {
        return this.status == SupplierStatus.INACTIVE;
    }

    public boolean isBlocked() {
        return this.status == SupplierStatus.BLOCKED;
    }

    public boolean isPendingApproval() {
        return this.status == SupplierStatus.PENDING_APPROVAL;
    }

    public boolean isBusiness() {
        return this.supplierType == SupplierType.BUSINESS;
    }

    public boolean isIndividual() {
        return this.supplierType == SupplierType.INDIVIDUAL;
    }

    public void activate() {
        this.status = SupplierStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = SupplierStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void block() {
        this.status = SupplierStatus.BLOCKED;
        this.updatedAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = SupplierStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsPreferred() {
        this.isPreferred = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void removePreferred() {
        this.isPreferred = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRating(int newRating) {
        if (newRating < 1 || newRating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = newRating;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDisplayName() {
        if (isBusiness()) {
            return tradeName != null ? tradeName : companyName;
        } else {
            return firstName + " " + lastName;
        }
    }

    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (street != null) address.append(street);
        if (number != null) address.append(", ").append(number);
        if (complement != null) address.append(" - ").append(complement);
        if (neighborhood != null) address.append(", ").append(neighborhood);
        if (city != null) address.append(", ").append(city);
        if (state != null) address.append(" - ").append(state);
        if (postalCode != null) address.append(", CEP: ").append(postalCode);
        return address.toString();
    }

    public String getTaxIdentification() {
        return isBusiness() ? cnpj : cpf;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public SupplierType getSupplierType() { return supplierType; }
    public void setSupplierType(SupplierType supplierType) { this.supplierType = supplierType; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getComplement() { return complement; }
    public void setComplement(String complement) { this.complement = complement; }

    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getStateRegistration() { return stateRegistration; }
    public void setStateRegistration(String stateRegistration) { this.stateRegistration = stateRegistration; }

    public String getMunicipalRegistration() { return municipalRegistration; }
    public void setMunicipalRegistration(String municipalRegistration) { this.municipalRegistration = municipalRegistration; }

    public TaxRegime getTaxRegime() { return taxRegime; }
    public void setTaxRegime(TaxRegime taxRegime) { this.taxRegime = taxRegime; }

    public Boolean getIcmsTaxpayer() { return icmsTaxpayer; }
    public void setIcmsTaxpayer(Boolean icmsTaxpayer) { this.icmsTaxpayer = icmsTaxpayer; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getBankBranch() { return bankBranch; }
    public void setBankBranch(String bankBranch) { this.bankBranch = bankBranch; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public String getBankAccountType() { return bankAccountType; }
    public void setBankAccountType(String bankAccountType) { this.bankAccountType = bankAccountType; }

    public String getPixKey() { return pixKey; }
    public void setPixKey(String pixKey) { this.pixKey = pixKey; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public String getDefaultPaymentMethod() { return defaultPaymentMethod; }
    public void setDefaultPaymentMethod(String defaultPaymentMethod) { this.defaultPaymentMethod = defaultPaymentMethod; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public Integer getAverageDeliveryDays() { return averageDeliveryDays; }
    public void setAverageDeliveryDays(Integer averageDeliveryDays) { this.averageDeliveryDays = averageDeliveryDays; }

    public BigDecimal getMinimumOrderValue() { return minimumOrderValue; }
    public void setMinimumOrderValue(BigDecimal minimumOrderValue) { this.minimumOrderValue = minimumOrderValue; }

    public SupplierStatus getStatus() { return status; }
    public void setStatus(SupplierStatus status) { this.status = status; }

    public String getSupplierCategory() { return supplierCategory; }
    public void setSupplierCategory(String supplierCategory) { this.supplierCategory = supplierCategory; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Boolean getIsPreferred() { return isPreferred; }
    public void setIsPreferred(Boolean isPreferred) { this.isPreferred = isPreferred; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
