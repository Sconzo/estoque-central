package com.estoquecentral.auth.adapter.in.dto;

import com.estoquecentral.auth.domain.Role;

import java.time.Instant;
import java.util.UUID;

/**
 * RoleDTO - Data Transfer Object for Role
 *
 * <p>Used for API responses containing role information.
 */
public class RoleDTO {

    private UUID id;
    private String nome;
    private String descricao;
    private String categoria;
    private Boolean ativo;
    private Instant dataCriacao;

    public RoleDTO() {
    }

    public RoleDTO(UUID id, String nome, String descricao, String categoria, Boolean ativo, Instant dataCriacao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.ativo = ativo;
        this.dataCriacao = dataCriacao;
    }

    /**
     * Converts a Role entity to DTO.
     */
    public static RoleDTO fromEntity(Role role) {
        return new RoleDTO(
                role.getId(),
                role.getNome(),
                role.getDescricao(),
                role.getCategoria(),
                role.getAtivo(),
                role.getDataCriacao()
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
}
