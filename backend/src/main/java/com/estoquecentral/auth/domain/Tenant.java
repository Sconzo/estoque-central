package com.estoquecentral.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Tenant - Aggregate root for multi-tenancy
 *
 * <p>Represents a tenant (client/customer) in the multi-tenant system.
 * Each tenant has an isolated PostgreSQL schema containing all their business data.
 *
 * <p><strong>Schema Isolation:</strong> This entity lives in the PUBLIC schema.
 * All business entities (produtos, vendas, etc.) live in tenant-specific schemas.
 *
 * <p><strong>Schema Naming:</strong> tenant_{uuid_without_hyphens}
 * Example: tenant_a1b2c3d4e5f67890abcdef1234567890
 *
 * @see com.estoquecentral.shared.tenant.TenantContext
 * @see com.estoquecentral.shared.tenant.TenantRoutingDataSource
 */
@Table("tenants")
public class Tenant {

    @Id
    private UUID id;

    private String nome;
    private String schemaName;
    private String email;
    private Boolean ativo;
    private Instant dataCriacao;
    private Instant dataAtualizacao;

    /**
     * Default constructor for Spring Data JDBC
     */
    public Tenant() {
    }

    /**
     * Constructor for creating a new tenant
     *
     * @param id         tenant UUID
     * @param nome       tenant business name
     * @param schemaName PostgreSQL schema name
     * @param email      tenant contact email
     */
    public Tenant(UUID id, String nome, String schemaName, String email) {
        this.id = id;
        this.nome = nome;
        this.schemaName = schemaName;
        this.email = email;
        this.ativo = true;
        this.dataCriacao = Instant.now();
        this.dataAtualizacao = Instant.now();
    }

    // ========================================================================
    // Getters and Setters
    // ========================================================================

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
        this.dataAtualizacao = Instant.now();
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
        this.dataAtualizacao = Instant.now();
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
        this.dataAtualizacao = Instant.now();
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

    @Override
    public String toString() {
        return "Tenant{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", schemaName='" + schemaName + '\'' +
                ", email='" + email + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
