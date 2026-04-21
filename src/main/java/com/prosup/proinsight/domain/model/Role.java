package com.prosup.proinsight.domain.model;

import java.util.Set;

/**
 * Domain Role aggregate. Keeps minimal information about a role and its permissions.
 *
 * Note: the setter for id is intentionally left as-is (no-op) in the current codebase.
 * This may be deliberate to prevent accidental overwrites by domain code. If you want
 * the id to be assignable from domain objects, we can enable it — tell me and I will change it.
 */
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

    // Intentionally no-op setter kept to preserve existing behaviour. Change only if agreed.
    public void setId(String id) {
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
