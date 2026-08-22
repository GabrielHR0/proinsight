package com.prosup.proinsight.domain.model;

import com.prosup.proinsight.domain.enums.Sexo;
import java.time.LocalDate;

public class Cliente {

    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String cpf;
    private LocalDate dataNascimento;
    private Sexo sexo;
    private Endereco endereco;
    private String academiaId;
    private String avaliadorId;
    private boolean active = true;

    public Cliente() {
    }

    public Cliente(String id, String fullName, String email, String phone,
                   String cpf, LocalDate dataNascimento, Sexo sexo, Endereco endereco,
                   String academiaId, String avaliadorId, boolean active) {
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("fullName não pode ser vazio");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email não pode ser vazio");
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.endereco = endereco;
        this.academiaId = academiaId;
        this.avaliadorId = avaliadorId;
        this.active = active;
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
    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("fullName não pode ser vazio");
        this.fullName = fullName;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email não pode ser vazio");
        this.email = email;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public String getAcademiaId() { return academiaId; }
    public void setAcademiaId(String academiaId) { this.academiaId = academiaId; }

    public String getAvaliadorId() { return avaliadorId; }
    public void setAvaliadorId(String avaliadorId) { this.avaliadorId = avaliadorId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
