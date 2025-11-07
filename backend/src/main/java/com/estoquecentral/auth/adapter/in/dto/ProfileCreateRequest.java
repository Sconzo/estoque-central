package com.estoquecentral.auth.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * ProfileCreateRequest - Request DTO for creating a new profile
 */
public class ProfileCreateRequest {

    @NotBlank(message = "Profile name is required")
    private String nome;

    private String descricao;

    @NotEmpty(message = "At least one role is required")
    private List<UUID> roleIds;

    public ProfileCreateRequest() {
    }

    public ProfileCreateRequest(String nome, String descricao, List<UUID> roleIds) {
        this.nome = nome;
        this.descricao = descricao;
        this.roleIds = roleIds;
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

    public List<UUID> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<UUID> roleIds) {
        this.roleIds = roleIds;
    }
}
