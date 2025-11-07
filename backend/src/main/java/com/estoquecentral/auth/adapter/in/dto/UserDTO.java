package com.estoquecentral.auth.adapter.in.dto;

import com.estoquecentral.auth.domain.Usuario;

import java.time.Instant;
import java.util.UUID;

/**
 * UserDTO - Data Transfer Object for User responses
 *
 * <p>Response body for GET /api/auth/me endpoint.
 *
 * <p>Example:
 * <pre>{@code
 * {
 *   "id": "uuid",
 *   "email": "user@example.com",
 *   "nome": "João Silva",
 *   "pictureUrl": "https://lh3.googleusercontent.com/...",
 *   "tenantId": "uuid",
 *   "role": "GERENTE",
 *   "ativo": true,
 *   "dataCriacao": "2025-01-30T10:15:30Z"
 * }
 * }</pre>
 *
 * @see com.estoquecentral.auth.adapter.in.AuthController
 */
public class UserDTO {

    private UUID id;
    private String email;
    private String nome;
    private String pictureUrl;
    private UUID tenantId;
    private String role;
    private Boolean ativo;
    private Instant dataCriacao;

    public UserDTO() {
    }

    public UserDTO(UUID id, String email, String nome, String pictureUrl,
                   UUID tenantId, String role, Boolean ativo, Instant dataCriacao) {
        this.id = id;
        this.email = email;
        this.nome = nome;
        this.pictureUrl = pictureUrl;
        this.tenantId = tenantId;
        this.role = role;
        this.ativo = ativo;
        this.dataCriacao = dataCriacao;
    }

    /**
     * Factory method to create DTO from domain entity.
     */
    public static UserDTO fromEntity(Usuario usuario) {
        return new UserDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNome(),
                usuario.getPictureUrl(),
                usuario.getTenantId(),
                usuario.getRole(),
                usuario.getAtivo(),
                usuario.getDataCriacao()
        );
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
}
