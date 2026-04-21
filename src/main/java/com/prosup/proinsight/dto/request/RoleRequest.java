package com.prosup.proinsight.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

/**
 * Request DTO to create/update a Role. Accepts permission ids to reference existing Permission documents.
 */
public class RoleRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String description;

    // Permission ids attached to this role
    private Set<String> permissionIds;

    public RoleRequest() {
    }

    public RoleRequest(String name, String description, Set<String> permissionIds) {
        this.name = name;
        this.description = description;
        this.permissionIds = permissionIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(Set<String> permissionIds) {
        this.permissionIds = permissionIds;
    }
}

