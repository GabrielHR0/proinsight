package com.prosup.proinsight.domain.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class User {

    private String id;
    private String userName;
    private String email;
    private String password;
    private Map<String, Set<String>> academiaRoles;
    private boolean active = true;
    private Integer failedLoginAttempts = 0;
    private Instant lockedUntil;
    private Set<String> academiaIds = new HashSet<>();
    private String cref;
    private String cpf;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {
    }

    public User(String id, String userName, String email, String password,
                Map<String, Set<String>> academiaRoles,
                boolean active, Integer failedLoginAttempts, Instant lockedUntil,
                Set<String> academiaIds, String cref, String cpf,
                Instant createdAt, Instant updatedAt) {
        if (userName == null || userName.isBlank()) throw new IllegalArgumentException("userName não pode ser vazio");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email não pode ser vazio");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("password não pode ser vazia");
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.academiaRoles = academiaRoles;
        this.active = active;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.academiaIds = academiaIds;
        this.cref = cref;
        this.cpf = cpf;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) {
        if (userName == null || userName.isBlank()) throw new IllegalArgumentException("userName não pode ser vazio");
        this.userName = userName;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email não pode ser vazio");
        this.email = email;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        if (password == null || password.isBlank()) throw new IllegalArgumentException("password não pode ser vazia");
        this.password = password;
    }

    public Map<String, Set<String>> getAcademiaRoles() { return academiaRoles; }
    public void setAcademiaRoles(Map<String, Set<String>> academiaRoles) { this.academiaRoles = academiaRoles; }

    public void putAcademiaRole(String academiaId, Set<String> roleIds) {
        if (this.academiaRoles == null) this.academiaRoles = new HashMap<>();
        this.academiaRoles.put(academiaId, roleIds);
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; }

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }

    public Set<String> getAcademiaIds() { return academiaIds; }
    public void setAcademiaIds(Set<String> academiaIds) { this.academiaIds = academiaIds; }

    public String getCref() { return cref; }
    public void setCref(String cref) { this.cref = cref; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public boolean isAvaliador() {
        return cref != null && !cref.isBlank();
    }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
