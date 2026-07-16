package com.prosup.proinsight.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class User {

    private String id;
    private String email;
    private String password;
    private Set<Role> roles;
    private boolean active = true;
    private List<String> academiaIds = new ArrayList<>();
    private String avaliadorId;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {
    }

    public User(String id, String email, String password, Set<Role> roles,
                boolean active, List<String> academiaIds, String avaliadorId,
                Instant createdAt, Instant updatedAt) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email não pode ser vazio");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("password não pode ser vazia");
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.active = active;
        this.academiaIds = academiaIds;
        this.avaliadorId = avaliadorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<String> getAcademiaIds() { return academiaIds; }
    public void setAcademiaIds(List<String> academiaIds) { this.academiaIds = academiaIds; }

    public String getAvaliadorId() { return avaliadorId; }
    public void setAvaliadorId(String avaliadorId) { this.avaliadorId = avaliadorId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
