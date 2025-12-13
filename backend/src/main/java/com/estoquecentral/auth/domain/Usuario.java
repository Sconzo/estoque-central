package com.estoquecentral.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Usuario - Aggregate root for authenticated users
 *
 * <p>Represents a user authenticated via Google OAuth 2.0.
 * Each user belongs to ONE tenant and has ONE profile.
 *
 * <p><strong>Multi-tenancy:</strong> This entity lives in TENANT schemas
 * (not public schema). Each tenant has its own `usuarios` table.
 *
 * <p><strong>RBAC Model:</strong>
 * <pre>
 * Role (global) → Profile (tenant-specific) → Usuario (tenant-specific)
 * Many-to-Many      One-to-Many
 * </pre>
 *
 * <p><strong>Authentication Flow:</strong>
 * <ol>
 *   <li>User logs in with Google OAuth</li>
 *   <li>Backend receives Google ID token</li>
 *   <li>Usuario created/updated with googleId, email, nome</li>
 *   <li>JWT token generated containing userId, tenantId, roles (from profile)</li>
 * </ol>
 *
 * @see com.estoquecentral.auth.application.GoogleAuthService
 * @see com.estoquecentral.auth.application.JwtService
 * @see Profile
 * @see Role
 */
@Table("usuarios")
public class Usuario implements Persistable<UUID> {

    @Id
    private UUID id;

    /**
     * Google user ID (sub claim from Google ID token).
     * UNIQUE per tenant.
     */
    private String googleId;

    /**
     * User email from Google.
     * UNIQUE per tenant.
     */
    private String email;

    /**
     * User name from Google.
     */
    private String nome;

    /**
     * Google profile picture URL.
     */
    private String pictureUrl;

    /**
     * Tenant ID this user belongs to.
     * IMMUTABLE after creation.
     */
    private UUID tenantId;

    /**
     * Profile ID this user belongs to.
     * FK to public.profiles.id
     * Determines user roles/permissions via Profile → Roles mapping.
     *
     * <p>A user without a profile has NO access to protected endpoints.
     *
     * @see Profile
     * @see Role
     */
    private UUID profileId;

    /**
     * Whether user is active.
     * Inactive users cannot login.
     */
    private Boolean ativo;

    private Instant dataCriacao;
    private Instant dataAtualizacao;

    /**
     * Transient field to track if this entity is new (for Persistable interface).
     * Not persisted to database.
     *
     * <p>Defaults to false when loaded from DB (since @Transient fields aren't persisted).
     * Set to true explicitly in constructors that create new instances.
     */
    @Transient
    private boolean isNew = false;

    /**
     * Default constructor for Spring Data JDBC.
     */
    public Usuario() {
    }

    /**
     * Constructor for creating a new user from Google OAuth.
     *
     * @param id         user UUID
     * @param googleId   Google user ID (sub)
     * @param email      user email
     * @param nome       user name
     * @param pictureUrl profile picture URL
     * @param tenantId   tenant this user belongs to
     */
    public Usuario(UUID id, String googleId, String email, String nome,
                   String pictureUrl, UUID tenantId) {
        this.id = id;
        this.googleId = googleId;
        this.email = email;
        this.nome = nome;
        this.pictureUrl = pictureUrl;
        this.tenantId = tenantId;
        this.profileId = null; // No profile assigned initially
        this.ativo = true;
        this.dataCriacao = Instant.now();
        this.dataAtualizacao = Instant.now();
        this.isNew = true; // Mark as new entity for Persistable interface
    }

    /**
     * Updates user information from Google OAuth.
     * Called when existing user logs in again.
     */
    public void updateFromGoogle(String nome, String pictureUrl) {
        this.nome = nome;
        this.pictureUrl = pictureUrl;
        this.dataAtualizacao = Instant.now();
    }

    /**
     * Assigns a profile to this user.
     *
     * @param profileId the profile ID to assign
     */
    public void assignProfile(UUID profileId) {
        this.profileId = profileId;
        this.dataAtualizacao = Instant.now();
    }

    /**
     * Checks if user has a profile assigned.
     *
     * @return true if user has profile, false otherwise
     */
    public boolean hasProfile() {
        return this.profileId != null;
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

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
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
        this.dataAtualizacao = Instant.now();
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
        this.dataAtualizacao = Instant.now();
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getProfileId() {
        return profileId;
    }

    public void setProfileId(UUID profileId) {
        this.profileId = profileId;
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

    // ========================================================================
    // Persistable Interface Implementation
    // ========================================================================

    /**
     * Determines if this entity is new (not yet persisted to database).
     *
     * <p>An entity is considered new if the isNew flag is true.
     * After save, Spring Data JDBC will set this to false via the callback.
     *
     * @return true if entity is new, false if it already exists in database
     */
    @Override
    public boolean isNew() {
        return isNew;
    }

    /**
     * Marks this entity as persisted (not new).
     * Called by Spring Data JDBC after successful save.
     */
    public void markNotNew() {
        this.isNew = false;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", googleId='" + googleId + '\'' +
                ", email='" + email + '\'' +
                ", nome='" + nome + '\'' +
                ", tenantId=" + tenantId +
                ", profileId=" + profileId +
                ", ativo=" + ativo +
                '}';
    }
}
