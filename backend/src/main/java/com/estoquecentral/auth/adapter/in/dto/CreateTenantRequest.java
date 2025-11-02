package com.estoquecentral.auth.adapter.in.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CreateTenantRequest - DTO for tenant creation
 *
 * <p>Request body for POST /api/tenants endpoint.
 *
 * <p>Example:
 * <pre>{@code
 * {
 *   "nome": "Empresa ABC Ltda",
 *   "email": "contato@empresaabc.com"
 * }
 * }</pre>
 *
 * @see com.estoquecentral.auth.adapter.in.TenantController
 */
public class CreateTenantRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    public CreateTenantRequest() {
    }

    public CreateTenantRequest(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
