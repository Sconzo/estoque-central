package com.estoquecentral.sales.adapter.in.dto;

import com.estoquecentral.sales.domain.Customer;
import com.estoquecentral.sales.domain.CustomerType;

import java.util.UUID;

/**
 * DTO for quick search results (autocomplete).
 * Story 4.1: Customer Management - AC8 (Quick Search)
 */
public record CustomerQuickDTO(
    UUID id,
    CustomerType customerType,
    String cpf,
    String cnpj,
    String displayName,
    String email,
    String phone
) {
    public static CustomerQuickDTO from(Customer customer) {
        return new CustomerQuickDTO(
            customer.getId(),
            customer.getCustomerType(),
            customer.getCpf(),
            customer.getCnpj(),
            customer.getDisplayName(),
            customer.getEmail(),
            customer.getPhone()
        );
    }
}
