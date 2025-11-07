package com.estoquecentral.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Role - Aggregate root for RBAC roles/permissions
 *
 * <p>Represents a global permission or capability in the system.
 * Roles are shared across ALL tenants (stored in public schema).
 *
 * <p><strong>Examples:</strong>
 * <ul>
 *   <li>ADMIN - Full system access</li>
 *   <li>GERENTE - Access to reports and configurations</li>
 *   <li>VENDEDOR - Access to sales B2B/B2C</li>
 *   <li>ESTOQUISTA - Access to inventory and purchasing</li>
 *   <li>OPERADOR_PDV - Access to POS (Point of Sale)</li>
 * </ul>
 *
 * <p><strong>RBAC Model:</strong>
 * <pre>
 * Role (global) → Profile (tenant-specific) → User (tenant-specific)
 * Many-to-Many      One-to-Many
 * </pre>
 *
 * <p><strong>Storage:</strong> Lives in PUBLIC schema (not tenant schemas)
 *
 * @see Profile
 * @see Usuario
 */
@Table("public.roles")
public class Role {

    @Id
    private UUID id;

    /**
     * Role name (unique globally).
     * Convention: UPPERCASE with underscores (e.g., OPERADOR_PDV)
     */
    private String nome;

    /**
     * Human-readable description of what this role allows.
     */
    private String descricao;

    /**
     * Category for grouping roles.
     * Possible values: GESTAO, OPERACIONAL, SISTEMA
     */
    private String categoria;

    /**
     * Whether this role is active.
     * Inactive roles cannot be assigned to profiles.
     */
    private Boolean ativo;

    private Instant dataCriacao;

    /**
     * Default constructor for Spring Data JDBC.
     */
    public Role() {
    }

    /**
     * Constructor for creating a new role.
     *
     * @param id         role UUID
     * @param nome       role name (e.g., "ADMIN")
     * @param descricao  human-readable description
     * @param categoria  role category (GESTAO, OPERACIONAL, SISTEMA)
     */
    public Role(UUID id, String nome, String descricao, String categoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.ativo = true;
        this.dataCriacao = Instant.now();
    }

    /**
     * Deactivates this role (soft delete).
     * Inactive roles cannot be assigned to new profiles.
     */
    public void deactivate() {
        this.ativo = false;
    }

    /**
     * Activates this role.
     */
    public void activate() {
        this.ativo = true;
    }

    /**
     * Updates role metadata.
     */
    public void update(String descricao, String categoria) {
        if (descricao != null) {
            this.descricao = descricao;
        }
        if (categoria != null) {
            this.categoria = categoria;
        }
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

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", categoria='" + categoria + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
