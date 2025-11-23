package com.estoquecentral.sales.adapter.in.dto;

import com.estoquecentral.sales.domain.Customer;
import com.estoquecentral.sales.domain.CustomerType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Customer response.
 * Story 4.1: Customer Management - AC3
 */
public record CustomerResponseDTO(
    UUID id,
    UUID tenantId,
    CustomerType customerType,
    String firstName,
    String lastName,
    String cpf,
    String companyName,
    String cnpj,
    String tradeName,
    String email,
    String phone,
    String mobile,
    LocalDate birthDate,
    String stateRegistration,
    String customerSegment,
    String loyaltyTier,
    BigDecimal creditLimit,
    Boolean acceptsMarketing,
    String preferredLanguage,
    String notes,
    Boolean ativo,
    Boolean isDefaultConsumer,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String fullName,
    String displayName
) {
    public static CustomerResponseDTO from(Customer customer) {
        return new CustomerResponseDTO(
            customer.getId(),
            customer.getTenantId(),
            customer.getCustomerType(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getCpf(),
            customer.getCompanyName(),
            customer.getCnpj(),
            customer.getTradeName(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getMobile(),
            customer.getBirthDate(),
            customer.getStateRegistration(),
            customer.getCustomerSegment(),
            customer.getLoyaltyTier(),
            customer.getCreditLimit(),
            customer.getAcceptsMarketing(),
            customer.getPreferredLanguage(),
            customer.getNotes(),
            customer.getAtivo(),
            customer.getIsDefaultConsumer(),
            customer.getCreatedAt(),
            customer.getUpdatedAt(),
            customer.getFullName(),
            customer.getDisplayName()
        );
    }
}
