package com.prosup.proinsight.domain.model;


import com.prosup.proinsight.domain.enums.ResponsavelType;

public class Cliente {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String cpf;
    private Endereco endereco;

    private String responsavelId;
    private ResponsavelType responsavelType;

    public Cliente() {
    }

    public Cliente(String id, String fullName, String email, String phone, String cpf, Endereco endereco, String responsavelId, ResponsavelType responsavelType) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.cpf = cpf;
        this.endereco = endereco;
        this.responsavelId = responsavelId;
        this.responsavelType = responsavelType;
    }

    public boolean pertenceAvaliador(){
        return responsavelType == ResponsavelType.AVALIADOR;
    }

    public boolean pertenceAcademia(){
        return responsavelType == ResponsavelType.ACADEMIA;
    }

    public String getId() {
        return id;
    }

    public ResponsavelType getResponsavelType() {
        return responsavelType;
    }

    public void setResponsavelType(ResponsavelType responsavelType) {
        this.responsavelType = responsavelType;
    }

    public String getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(String responsavelId) {
        this.responsavelId = responsavelId;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
