package com.prosup.proinsight.infrastructure.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Permission document. Represents a granular permission that can be granted to users via ids.
 */
@Document(collection = "permissions")
public class PermissionDocument {
    @Id
    private String id;
    private String resource;
    private String action;
    private String description;

    public PermissionDocument() {}

    public PermissionDocument(String id, String resource, String action, String description) {
        this.id = id;
        this.resource = resource;
        this.action = action;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
