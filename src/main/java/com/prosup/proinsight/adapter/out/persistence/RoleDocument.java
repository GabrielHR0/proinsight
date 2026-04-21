package com.prosup.proinsight.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

/**
 * Simple role document. Roles can be managed separately and referenced by id from UserDocument.
 */
@Document(collection = "roles")
public class RoleDocument {

    @Id
    private String id;

    private String nome;

    private String descricao;

    private Set<String> permissions;

    public RoleDocument() {
    }

    public RoleDocument(String id, String nome, String descricao, Set<String> permissions) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
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

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
}

