package com.prosup.proinsight.infrastructure.persistence.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "audit_logs")
public class AuditLogDocument {

    @Id
    private String id;

    @Indexed
    private Instant createdAt;

    private String actorUserId;

    private String actorEmail;

    private String academiaId;

    private String action;

    private String targetType;

    private String targetId;

    private int statusCode;

    private Map<String, Object> details;

    public AuditLogDocument() {
    }

    public AuditLogDocument(String actorUserId, String actorEmail, String academiaId,
                            String action, String targetType, String targetId,
                            int statusCode, Map<String, Object> details) {
        this.actorUserId = actorUserId;
        this.actorEmail = actorEmail;
        this.academiaId = academiaId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.statusCode = statusCode;
        this.details = details;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getActorUserId() { return actorUserId; }
    public void setActorUserId(String actorUserId) { this.actorUserId = actorUserId; }

    public String getActorEmail() { return actorEmail; }
    public void setActorEmail(String actorEmail) { this.actorEmail = actorEmail; }

    public String getAcademiaId() { return academiaId; }
    public void setAcademiaId(String academiaId) { this.academiaId = academiaId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
}
