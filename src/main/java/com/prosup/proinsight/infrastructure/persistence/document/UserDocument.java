package com.prosup.proinsight.infrastructure.persistence.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Document(collection = "users")
public class UserDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userName;

    @Indexed(unique = true)
    private String email;

    private String password;

    private Map<String, Set<String>> academiaRoles;

    private boolean active = true;

    private Integer failedLoginAttempts = 0;

    private Instant lockedUntil;

    private Set<String> academiaIds = new HashSet<>();

    @Indexed(unique = true, sparse = true)
    private String cref;

    @Indexed(unique = true, sparse = true)
    private String cpf;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public UserDocument() {
    }

    public UserDocument(String id, String userName, String email, String password,
                        Map<String, Set<String>> academiaRoles, boolean active) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.academiaRoles = academiaRoles;
        this.active = active;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

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

    public Set<String> getAcademiaIds() { return academiaIds; }
    public void setAcademiaIds(Set<String> academiaIds) { this.academiaIds = academiaIds; }

    public void addAcademiaId(String academiaId) {
        if (this.academiaIds == null) this.academiaIds = new HashSet<>();
        this.academiaIds.add(academiaId);
    }

    public void removeAcademiaId(String academiaId) {
        if (this.academiaIds != null) {
            this.academiaIds.remove(academiaId);
        }
    }

    public String getCref() { return cref; }
    public void setCref(String cref) { this.cref = cref; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
