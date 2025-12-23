package com.estoquecentral.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Profile - Aggregate root for user profiles
 *
 * <p>Represents a tenant-specific group of roles.
 * Each user has ONE profile, which determines their permissions.
 *
 * <p><strong>Examples:</strong>
 * <ul>
 *   <li>"Gerente Loja" - Has roles: GERENTE, VENDEDOR, ESTOQUISTA</li>
 *   <li>"Vendedor Senior" - Has roles: VENDEDOR, RELATORIOS</li>
 *   <li>"Caixa" - Has role: OPERADOR_PDV</li>
 * </ul>
 *
 * <p><strong>Why Profiles?</strong>
 * <ul>
 *   <li>✅ Easy maintenance: Update profile "Gerente" applies to ALL users with that profile</li>
 *   <li>✅ Consistency: All "Gerentes" have exactly the same roles</li>
 *   <li>✅ Scalable: 1 profile serves 100+ users</li>
 * </ul>
 *
 * <p><strong>RBAC Model:</strong>
 * <pre>
 * Role (global) → Profile (tenant-specific) → User (tenant-specific)
 * Many-to-Many      One-to-Many
 * </pre>
 *
 * <p><strong>Storage:</strong> Lives in TENANT schema (schema-per-tenant isolation)
 * <p><strong>Isolation:</strong> Each company has its own profiles table in its dedicated schema
 * <p><strong>Routing:</strong> TenantContext + TenantRoutingDataSource routes to correct schema
 *
 * @see Role
 * @see Usuario
 * @see com.estoquecentral.shared.tenant.TenantContext
 */
@Table("profiles")
public class Profile {

    @Id
    private UUID id;

    /**
     * Profile name (unique per tenant).
     * Examples: "Gerente Loja", "Vendedor Senior", "Caixa"
     */
    private String nome;

    /**
     * Human-readable description of what this profile represents.
     */
    private String descricao;

    /**
     * Whether this profile is active.
     * Inactive profiles cannot be assigned to users.
     */
    private Boolean ativo;

    private Instant dataCriacao;
    private Instant dataAtualizacao;

    /**
     * Default constructor for Spring Data JDBC.
     */
    public Profile() {
    }

    /**
     * Constructor for creating a new profile.
     *
     * <p>Note: tenant isolation is handled by schema routing, not tenant_id column
     *
     * @param id        profile UUID
     * @param nome      profile name (e.g., "Gerente Loja")
     * @param descricao human-readable description
     */
    public Profile(UUID id, String nome, String descricao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = true;
        this.dataCriacao = Instant.now();
        this.dataAtualizacao = Instant.now();
    }

    /**
     * Deactivates this profile (soft delete).
     * Inactive profiles cannot be assigned to new users.
     */
    public void deactivate() {
        this.ativo = false;
        this.dataAtualizacao = Instant.now();
    }

    /**
     * Activates this profile.
     */
    public void activate() {
        this.ativo = true;
        this.dataAtualizacao = Instant.now();
    }

    /**
     * Updates profile metadata.
     */
    public void update(String nome, String descricao) {
        if (nome != null && !nome.isBlank()) {
            this.nome = nome;
        }
        if (descricao != null) {
            this.descricao = descricao;
        }
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
        return "Profile{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
