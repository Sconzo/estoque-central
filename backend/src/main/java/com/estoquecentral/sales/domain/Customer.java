package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Customer entity - supports both Individual (PF) and Business (PJ) customers.
 * Story 4.1: Customer Management
 * NFR14: CPF, CNPJ, and Email are encrypted at rest using AES-256
 */
@Table("customers")
public class Customer implements Persistable<UUID> {

    @Id
    private UUID id;

    @Transient
    private boolean isNew = false;
    private UUID tenantId;
    private CustomerType customerType;
    private String firstName;
    private String lastName;

    // CPF stored encrypted in database (AES-256)
    private String cpf;

    private String companyName;

    // CNPJ stored encrypted in database (AES-256)
    private String cnpj;

    private String tradeName;

    // Email stored encrypted in database (AES-256)
    private String email;

    private String phone;
    private String mobile;
    private LocalDate birthDate;
    private String taxId;
    private String stateRegistration;
    private String customerSegment;
    private String loyaltyTier;
    private BigDecimal creditLimit;
    private Boolean acceptsMarketing;
    private String preferredLanguage;
    private String notes;
    private Boolean ativo;
    private Boolean isDefaultConsumer; // Flag for "Consumidor Final" (PDV default customer)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    public Customer() {
    }

    public String getFullName() {
        if (customerType == CustomerType.INDIVIDUAL) {
            return firstName + " " + lastName;
        }
        return companyName;
    }

    public String getDisplayName() {
        return customerType == CustomerType.BUSINESS ? tradeName != null ? tradeName : companyName : getFullName();
    }

    public boolean isIndividual() {
        return customerType == CustomerType.INDIVIDUAL;
    }

    public boolean isBusiness() {
        return customerType == CustomerType.BUSINESS;
    }

    // Persistable
    @Override
    public boolean isNew() { return isNew; }

    public void markAsNew() { this.isNew = true; }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(CustomerType customerType) { this.customerType = customerType; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public String getStateRegistration() { return stateRegistration; }
    public void setStateRegistration(String stateRegistration) { this.stateRegistration = stateRegistration; }

    public String getCustomerSegment() { return customerSegment; }
    public void setCustomerSegment(String customerSegment) { this.customerSegment = customerSegment; }

    public String getLoyaltyTier() { return loyaltyTier; }
    public void setLoyaltyTier(String loyaltyTier) { this.loyaltyTier = loyaltyTier; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public Boolean getAcceptsMarketing() { return acceptsMarketing; }
    public void setAcceptsMarketing(Boolean acceptsMarketing) { this.acceptsMarketing = acceptsMarketing; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Boolean getIsDefaultConsumer() { return isDefaultConsumer; }
    public void setIsDefaultConsumer(Boolean isDefaultConsumer) { this.isDefaultConsumer = isDefaultConsumer; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
}
