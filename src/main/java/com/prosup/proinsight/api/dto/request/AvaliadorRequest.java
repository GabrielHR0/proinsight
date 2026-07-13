package com.prosup.proinsight.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO used when creating or updating an Avaliador profile.
 */
public class AvaliadorRequest {

    @NotBlank(message = "cref is required")
    private String cref;

    @NotBlank(message = "firstName is required")
    private String firstName;

    @NotBlank(message = "lastName is required")
    private String lastName;

    @Email(message = "must be a well-formed email address")
    private String email;

    @NotBlank(message = "telefone is required")
    @Size(min = 8, max = 20, message = "telefone length must be between 8 and 20")
    private String telefone;

    private String cpf;

    @NotBlank(message = "userId is required")
    private String userId;

    private String academiaId;

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
}
