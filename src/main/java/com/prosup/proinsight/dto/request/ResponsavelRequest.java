package com.prosup.proinsight.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for Responsavel base (used by profiles like Avaliador if necessary).
 */
public class ResponsavelRequest {

    @NotBlank(message = "cref is required")
    private String cref;

    public ResponsavelRequest() {}

    public ResponsavelRequest(String cref) { this.cref = cref; }

    public String getCref() { return cref; }
    public void setCref(String cref) { this.cref = cref; }
}

