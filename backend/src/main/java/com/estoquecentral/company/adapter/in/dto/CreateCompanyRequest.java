package com.estoquecentral.company.adapter.in.dto;

/**
 * Request DTO for creating a new company.
 *
 * @since 1.0
 */
public record CreateCompanyRequest(
    String name,
    String cnpj,
    String email,
    String phone
) {
    public CreateCompanyRequest {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException("CNPJ is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
    }
}
