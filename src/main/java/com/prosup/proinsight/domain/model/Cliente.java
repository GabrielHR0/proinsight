package com.prosup.proinsight.domain.model;

public class Cliente {

    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String cpf;
    private Endereco endereco;
    private String academiaId;
    private String avaliadorId;

    public Cliente() {
    }

    public Cliente(String id, String fullName, String email, String phone,
                   String cpf, Endereco endereco, String academiaId, String avaliadorId) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.cpf = cpf;
        this.endereco = endereco;
        this.academiaId = academiaId;
        this.avaliadorId = avaliadorId;
    }

    public boolean pertenceAcademia() {
        return academiaId != null;
    }

    public boolean pertenceAvaliador() {
        return avaliadorId != null;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public String getAcademiaId() { return academiaId; }
    public void setAcademiaId(String academiaId) { this.academiaId = academiaId; }

    public String getAvaliadorId() { return avaliadorId; }
    public void setAvaliadorId(String avaliadorId) { this.avaliadorId = avaliadorId; }
}
