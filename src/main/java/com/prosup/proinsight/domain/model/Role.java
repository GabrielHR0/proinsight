package com.prosup.proinsight.domain.model;

import com.prosup.proinsight.domain.enums.Permissao;

import java.util.Set;

public class Role {

    private String id;
    private String nome;
    private String descricao;
    private Set<Permissao> permissoes;

    public Role() {
    }

    public Role(String id, String nome, String descricao, Set<Permissao> permissoes) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.permissoes = permissoes;
    }

    public Set<Permissao> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(Set<Permissao> permissoes) {
        this.permissoes = permissoes;
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
