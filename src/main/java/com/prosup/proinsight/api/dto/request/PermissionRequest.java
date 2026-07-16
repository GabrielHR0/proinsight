package com.prosup.proinsight.api.dto.request;

import jakarta.validation.constraints.NotBlank;


public class PermissionRequest {

    @NotBlank(message = "resource is required")
    private String resource;

    @NotBlank(message = "action is required")
    private String action;

    private String description;

    public PermissionRequest() {
    }

    public PermissionRequest(String resource, String action, String description) {
        this.resource = resource;
        this.action = action;
        this.description = description;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
