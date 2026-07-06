package com.prosup.proinsight.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Request DTO for creating or updating a User.
 * Does not contain id or timestamps.
 */
public class UserRequest {

    @Email(message = "must be a well-formed email address")
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters long")
    private String password;

    // Accept role ids to associate the user with existing roles
    private Set<String> roleIds;

    // Accept permission ids to associate directly (optional)
    private Set<String> permissionIds;

    public UserRequest() {
    }

    public UserRequest(String email, String password, Set<String> roleIds, Set<String> permissionIds) {
        this.email = email;
        this.password = password;
        this.roleIds = roleIds;
        this.permissionIds = permissionIds;
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

    public Set<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<String> roleIds) {
        this.roleIds = roleIds;
    }

    public Set<String> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(Set<String> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
