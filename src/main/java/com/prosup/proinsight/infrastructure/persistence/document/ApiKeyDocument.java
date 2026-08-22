package com.prosup.proinsight.infrastructure.persistence.document;

import com.prosup.proinsight.config.ScopedByAcademia;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@ScopedByAcademia

@Document(collection = "api_keys")
@CompoundIndexes({
        @CompoundIndex(name = "academia_keyhash_idx", def = "{'academiaId': 1, 'keyHash': 1}")
})
public class ApiKeyDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String keyHash;

    @Indexed
    private String academiaId;

    private Set<String> permissions;

    private Instant expiresAt;

    private boolean active;

    private String label;

    @CreatedDate
    private Instant createdAt;

    public ApiKeyDocument() {
    }

    public ApiKeyDocument(String keyHash, String academiaId, Set<String> permissions,
                          Instant expiresAt, String label) {
        this.keyHash = keyHash;
        this.academiaId = academiaId;
        this.permissions = permissions;
        this.expiresAt = expiresAt;
        this.active = true;
        this.label = label;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }

    public String getAcademiaId() { return academiaId; }
    public void setAcademiaId(String academiaId) { this.academiaId = academiaId; }

    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isValid() {
        return active && (expiresAt == null || expiresAt.isAfter(Instant.now()));
    }
}
