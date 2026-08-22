package com.prosup.proinsight.infrastructure.persistence.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "password_reset_tokens")
public class PasswordResetTokenDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String tokenHash;

    @Indexed
    private String userId;

    private boolean used;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;

    @CreatedDate
    private Instant createdAt;

    public PasswordResetTokenDocument() {
    }

    public PasswordResetTokenDocument(String tokenHash, String userId, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
