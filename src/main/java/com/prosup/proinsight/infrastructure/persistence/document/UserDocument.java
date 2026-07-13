package com.prosup.proinsight.infrastructure.persistence.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Document(collection = "users")
public class UserDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private Set<String> roles;

    private boolean active = true;

    private List<String> academiaIds = new ArrayList<>();

    private String avaliadorId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public UserDocument() {
    }

    public UserDocument(String id, String email, String password, Set<String> roles, boolean active) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.active = active;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<String> getAcademiaIds() { return academiaIds; }
    public void setAcademiaIds(List<String> academiaIds) { this.academiaIds = academiaIds; }

    public void addAcademiaId(String academiaId) {
        if (this.academiaIds == null) this.academiaIds = new ArrayList<>();
        if (!this.academiaIds.contains(academiaId)) {
            this.academiaIds.add(academiaId);
        }
    }

    public void removeAcademiaId(String academiaId) {
        if (this.academiaIds != null) {
            this.academiaIds.remove(academiaId);
        }
    }

    public String getAvaliadorId() { return avaliadorId; }
    public void setAvaliadorId(String avaliadorId) { this.avaliadorId = avaliadorId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
