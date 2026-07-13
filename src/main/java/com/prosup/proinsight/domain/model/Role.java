package com.prosup.proinsight.domain.model;

import java.util.Set;

public class Role {

    private String id;
    private String nome;
    private String descricao;
    private Set<Permission> permissions;

    public Role() {
    }

    public Role(String id, String nome, String descricao, Set<Permission> permissions) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
}
