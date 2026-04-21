package com.prosup.proinsight.domain.model;

import java.time.Instant;
import java.util.Set;

/**
 * Domain model for core user identity (authentication/authorization).
 * Keep domain object free of persistence annotations; adapters will map to documents.
 */
public class User {

    private String id;
    private String email;
    private String password;
    private Set<Role> roles;
    private boolean active = true;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {
    }

    public User(String id,
                String email,
                String password,
                Set<Role> roles,
                boolean active,
                Instant createdAt,
                Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

