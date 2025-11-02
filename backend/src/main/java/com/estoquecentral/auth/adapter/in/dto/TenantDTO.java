package com.estoquecentral.auth.adapter.in.dto;

import com.estoquecentral.auth.domain.Tenant;

import java.time.Instant;
import java.util.UUID;

/**
 * TenantDTO - Data Transfer Object for Tenant responses
 *
 * <p>Response body for tenant-related endpoints.
 *
 * <p>Example:
 * <pre>{@code
 * {
 *   "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
 *   "nome": "Empresa ABC Ltda",
 *   "schemaName": "tenant_a1b2c3d4e5f67890abcdef1234567890",
 *   "email": "contato@empresaabc.com",
 *   "ativo": true,
 *   "dataCriacao": "2025-01-30T10:15:30Z"
 * }
 * }</pre>
 *
 * @see com.estoquecentral.auth.adapter.in.TenantController
 */
public class TenantDTO {

    private UUID id;
    private String nome;
    private String schemaName;
    private String email;
    private Boolean ativo;
    private Instant dataCriacao;

    public TenantDTO() {
    }

    public TenantDTO(UUID id, String nome, String schemaName, String email, Boolean ativo, Instant dataCriacao) {
        this.id = id;
        this.nome = nome;
        this.schemaName = schemaName;
        this.email = email;
        this.ativo = ativo;
        this.dataCriacao = dataCriacao;
    }

    /**
     * Factory method to create DTO from domain entity
     */
    public static TenantDTO fromEntity(Tenant tenant) {
        return new TenantDTO(
                tenant.getId(),
                tenant.getNome(),
                tenant.getSchemaName(),
                tenant.getEmail(),
                tenant.getAtivo(),
                tenant.getDataCriacao()
        );
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
