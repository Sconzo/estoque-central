package com.estoquecentral.purchasing.adapter.in.dto;

import com.estoquecentral.purchasing.domain.SupplierType;
import com.estoquecentral.purchasing.domain.TaxRegime;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * CreateSupplierRequest - DTO for creating a new supplier
 * Story 3.1: Supplier Management
 */
public class CreateSupplierRequest {

    @NotBlank(message = "Supplier code is required")
    @Size(max = 50, message = "Supplier code must not exceed 50 characters")
    private String supplierCode;

    @NotNull(message = "Supplier type is required")
    private SupplierType supplierType;

    // Business details (PJ - Pessoa Jurídica)
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;

    @Size(max = 200, message = "Trade name must not exceed 200 characters")
    private String tradeName;

    @Pattern(regexp = "^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$|^$",
            message = "CNPJ must be in format: 00.000.000/0000-00")
    private String cnpj;

    // Individual details (PF - Pessoa Física)
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$|^$",
            message = "CPF must be in format: 000.000.000-00")
    private String cpf;

    // Contact information
    @Email(message = "Email must be valid")
    @Size(max = 200, message = "Email must not exceed 200 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 20, message = "Mobile must not exceed 20 characters")
    private String mobile;

    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;

    // Address
    @Size(max = 255, message = "Street must not exceed 255 characters")
    private String street;

    @Size(max = 20, message = "Number must not exceed 20 characters")
    private String number;

    @Size(max = 100, message = "Complement must not exceed 100 characters")
    private String complement;

    @Size(max = 100, message = "Neighborhood must not exceed 100 characters")
    private String neighborhood;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    @Pattern(regexp = "^\\d{5}-\\d{3}$|^$",
            message = "Postal code must be in format: 00000-000")
    private String postalCode;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    // Fiscal data
    @Size(max = 50, message = "State registration must not exceed 50 characters")
    private String stateRegistration;

    @Size(max = 50, message = "Municipal registration must not exceed 50 characters")
    private String municipalRegistration;

    private TaxRegime taxRegime;

    private Boolean icmsTaxpayer;

    // Bank details
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @Size(max = 10, message = "Bank code must not exceed 10 characters")
    private String bankCode;

    @Size(max = 20, message = "Bank branch must not exceed 20 characters")
    private String bankBranch;

    @Size(max = 30, message = "Bank account must not exceed 30 characters")
    private String bankAccount;

    @Size(max = 20, message = "Bank account type must not exceed 20 characters")
    private String bankAccountType;

    @Size(max = 200, message = "PIX key must not exceed 200 characters")
    private String pixKey;

    // Business details
    @Size(max = 100, message = "Payment terms must not exceed 100 characters")
    private String paymentTerms;

    @Size(max = 50, message = "Default payment method must not exceed 50 characters")
    private String defaultPaymentMethod;

    @DecimalMin(value = "0.0", message = "Credit limit must be positive")
    private BigDecimal creditLimit;

    @Min(value = 0, message = "Average delivery days must be positive")
    private Integer averageDeliveryDays;

    @DecimalMin(value = "0.0", message = "Minimum order value must be positive")
    private BigDecimal minimumOrderValue;

    // Classification
    @Size(max = 50, message = "Supplier category must not exceed 50 characters")
    private String supplierCategory;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private Boolean isPreferred;

    // Notes
    private String notes;
    private String internalNotes;

    // Constructors
    public CreateSupplierRequest() {
        this.supplierType = SupplierType.BUSINESS;
        this.country = "Brasil";
        this.icmsTaxpayer = true;
        this.isPreferred = false;
    }

    // Getters and Setters
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
}
