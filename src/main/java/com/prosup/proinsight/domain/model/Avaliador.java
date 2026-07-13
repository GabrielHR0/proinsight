package com.prosup.proinsight.domain.model;

/**
 * Domain model for Avaliador (pure domain, no persistence annotations).
 */
public class Avaliador {

    private String id;
    private String cref;
    private String firstName;
    private String lastName;
    private String email;
    private String telefone;
    private String cpf;
    private String userId;
    private String academiaId;

    public Avaliador() {
        super();
    }

    public Avaliador(
            String userId,
            String cref,
            String firstName,
            String lastName,
            String email,
            String telefone,
            String cpf) {
        this.cref = cref;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
    }

    public Avaliador(String id, String userId, String cref, String firstName, String lastName, String email, String telefone, String cpf) {
        this.id = id;
        this.cref = cref;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
    }

    public Avaliador(String id, String userId, String academiaId, String cref, String firstName, String lastName, String email, String telefone, String cpf) {
        this.id = id;
        this.userId = userId;
        this.academiaId = academiaId;
        this.cref = cref;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
    }

    public boolean isAutonomo() {
        return academiaId == null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCref() {
        return cref;
    }

    public void setCref(String cref) {
        this.cref = cref;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAcademiaId() {
        return academiaId;
    }

    public void setAcademiaId(String academiaId) {
        this.academiaId = academiaId;
    }
}

