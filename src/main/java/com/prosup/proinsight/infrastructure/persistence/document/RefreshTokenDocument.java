package com.prosup.proinsight.infrastructure.persistence.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "refresh_tokens")
public class RefreshTokenDocument {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private Instant expiryDate;

    private boolean revoked;

    @CreatedDate
    private Instant createdAt;

    public RefreshTokenDocument() {
    }

    public RefreshTokenDocument(String id, String userId, Instant expiryDate) {
        this.id = id;
        this.userId = userId;
        this.expiryDate = expiryDate;
        this.revoked = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isValid() {
        return !revoked && expiryDate != null && expiryDate.isAfter(Instant.now());
    }
}
