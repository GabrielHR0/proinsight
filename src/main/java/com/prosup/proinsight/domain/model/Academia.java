package com.prosup.proinsight.domain.model;

import java.time.Instant;

/**
 * Domain model for Academia profile. Does not extend Responsavel; kept as a separate profile type.
 */
public class Academia {

    private String id;
    private String userId;
    private String nomeFantasia;
    private String razaoSocial;
    private String cnpj;
    private Endereco endereco;
    private String telefone;
    private Instant createdAt;
    private Instant updatedAt;

    public Academia() {
    }

    public Academia(String id, String userId, String nomeFantasia, String razaoSocial, String cnpj, Endereco endereco, String telefone, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.nomeFantasia = nomeFantasia;
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.endereco = endereco;
        this.telefone = telefone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // getters / setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}

