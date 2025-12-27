package com.estoquecentral.company.adapter.in.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating company information (Story 10.5).
 *
 * @since 1.0
 */
public record UpdateCompanyRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    String name,

    @Email(message = "Invalid email format")
    String email,

    @Size(max = 20, message = "Phone must be less than 20 characters")
    String phone
) {
}
