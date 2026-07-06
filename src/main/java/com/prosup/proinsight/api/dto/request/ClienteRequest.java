package com.prosup.proinsight.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for Cliente (placeholder).
 */
public class ClienteRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String contact;

    public ClienteRequest() {}

    public ClienteRequest(String name, String contact) {
        this.name = name;
        this.contact = contact;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }

    public void setContact(String contact) { this.contact = contact; }
}
