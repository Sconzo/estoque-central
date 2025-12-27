package com.estoquecentral.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * User - Domain entity for public.users table
 *
 * <p>This represents a user in the global authentication table (public.users).
 * Users can belong to multiple companies via public.company_users table.
 *
 * <p><strong>Important:</strong> This is different from {@link Usuario} which
 * represents tenant-specific users in tenant schemas.
 *
 * @since 1.0
 */
@Table("users")
public class User {

    @Id
    private Long id;
    private String nome;
    private String email;
    private String googleId;
    private Boolean ativo;
    private LocalDateTime ultimoLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor for new users (before persistence).
     */
    public User(String nome, String email, String googleId) {
        this.nome = nome;
        this.email = email;
        this.googleId = googleId;
        this.ativo = true;
        this.ultimoLogin = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor for existing users (from database).
     */
    public User(Long id, String nome, String email, String googleId, Boolean ativo,
                LocalDateTime ultimoLogin, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.googleId = googleId;
        this.ativo = ativo;
        this.ultimoLogin = ultimoLogin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * No-args constructor for Spring Data JDBC.
     */
    public User() {
    }

    /**
     * Updates user information from Google OAuth.
     */
    public void updateFromGoogle(String nome) {
        this.nome = nome;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates last login timestamp.
     */
    public void updateLastLogin() {
        this.ultimoLogin = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", googleId='" + googleId + '\'' +
                ", ativo=" + ativo +
                ", ultimoLogin=" + ultimoLogin +
                '}';
    }
}
