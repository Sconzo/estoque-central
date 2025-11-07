package com.estoquecentral.auth.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * RoleCreateRequest - Request DTO for creating a new role
 */
public class RoleCreateRequest {

    @NotBlank(message = "Role name is required")
    @Pattern(regexp = "^[A-Z_]+$", message = "Role name must be uppercase with underscores (e.g., CUSTOM_ROLE)")
    private String nome;

    private String descricao;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^(GESTAO|OPERACIONAL|SISTEMA)$", message = "Category must be GESTAO, OPERACIONAL, or SISTEMA")
    private String categoria;

    public RoleCreateRequest() {
    }

    public RoleCreateRequest(String nome, String descricao, String categoria) {
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
    }

    // Getters and Setters

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
