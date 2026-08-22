package com.prosup.proinsight.infrastructure.persistence.document;

import com.prosup.proinsight.domain.enums.Permissao;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document(collection = "roles")
public class RoleDocument {

    @Id
    private String id;

    private String nome;

    private String descricao;

    private Set<Permissao> permissoes;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public RoleDocument() {
    }

    public RoleDocument(String id, String nome, String descricao, Set<Permissao> permissoes) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.permissoes = permissoes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Set<Permissao> getPermissoes() { return permissoes; }
    public void setPermissoes(Set<Permissao> permissoes) { this.permissoes = permissoes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
