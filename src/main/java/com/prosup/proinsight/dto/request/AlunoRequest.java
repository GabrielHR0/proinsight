package com.prosup.proinsight.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for Aluno (placeholder). Add fields when domain is defined.
 */
public class AlunoRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String email;

    public AlunoRequest() {}

    public AlunoRequest(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

