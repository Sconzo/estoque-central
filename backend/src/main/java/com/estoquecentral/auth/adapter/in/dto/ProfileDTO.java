package com.estoquecentral.auth.adapter.in.dto;

import com.estoquecentral.auth.domain.Profile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * ProfileDTO - Data Transfer Object for Profile
 *
 * <p>Used for API responses containing profile information.
 */
public class ProfileDTO {

    private UUID id;
    private UUID tenantId;
    private String nome;
    private String descricao;
    private Boolean ativo;
    private Instant dataCriacao;
    private Instant dataAtualizacao;
    private List<RoleDTO> roles; // Optional: roles associated with this profile

    public ProfileDTO() {
    }

    public ProfileDTO(UUID id, UUID tenantId, String nome, String descricao, Boolean ativo,
                      Instant dataCriacao, Instant dataAtualizacao, List<RoleDTO> roles) {
        this.id = id;
        this.tenantId = tenantId;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo;
        this.dataCriacao = dataCriacao;
        this.dataAtualizacao = dataAtualizacao;
        this.roles = roles;
    }

    /**
     * Converts a Profile entity to DTO (without roles).
     */
    public static ProfileDTO fromEntity(Profile profile) {
        return new ProfileDTO(
                profile.getId(),
                profile.getTenantId(),
                profile.getNome(),
                profile.getDescricao(),
                profile.getAtivo(),
                profile.getDataCriacao(),
                profile.getDataAtualizacao(),
                null // Roles must be loaded separately
        );
    }

    /**
     * Converts a Profile entity to DTO with roles.
     */
    public static ProfileDTO fromEntityWithRoles(Profile profile, List<RoleDTO> roles) {
        return new ProfileDTO(
                profile.getId(),
                profile.getTenantId(),
                profile.getNome(),
                profile.getDescricao(),
                profile.getAtivo(),
                profile.getDataCriacao(),
                profile.getDataAtualizacao(),
                roles
        );
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Instant getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Instant dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Instant getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Instant dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public List<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }
}
