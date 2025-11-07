package com.estoquecentral.sales.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("customers")
public class Customer {

    @Id
    private UUID id;
    private UUID tenantId;
    private CustomerType customerType;
    private String firstName;
    private String lastName;
    private String cpf;
    private String companyName;
    private String cnpj;
    private String tradeName;
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
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    // ... outros getters/setters omitidos para brevidade
}
