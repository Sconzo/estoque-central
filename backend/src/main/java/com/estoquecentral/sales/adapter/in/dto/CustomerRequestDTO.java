package com.estoquecentral.sales.adapter.in.dto;

import com.estoquecentral.sales.domain.CustomerType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating/updating Customer.
 * Story 4.1: Customer Management - AC3
 */
public record CustomerRequestDTO(
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
    String notes
) {
}
