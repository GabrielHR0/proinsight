package com.prosup.proinsight.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.Set;

public class UserRequest {

    @NotBlank(message = "Username é obrigatório")
    private String userName;

    @Email(message = "E-mail inválido")
    @NotBlank(message = "E-mail é obrigatório")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String password;

    private Map<String, Set<String>> academiaRoles;

    public UserRequest() {
    }

    public UserRequest(String userName, String email, String password,
                       Map<String, Set<String>> academiaRoles) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.academiaRoles = academiaRoles;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, Set<String>> getAcademiaRoles() {
        return academiaRoles;
    }

    public void setAcademiaRoles(Map<String, Set<String>> academiaRoles) {
        this.academiaRoles = academiaRoles;
    }
}
