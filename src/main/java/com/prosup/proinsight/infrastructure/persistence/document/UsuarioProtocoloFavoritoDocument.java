package com.prosup.proinsight.infrastructure.persistence.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "usuario_protocolo_favoritos")
@CompoundIndex(name = "user_protocolo_idx", def = "{'userId': 1, 'protocoloId': 1}", unique = true)
public class UsuarioProtocoloFavoritoDocument {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String protocoloId;

    @CreatedDate
    private Instant createdAt;

    public UsuarioProtocoloFavoritoDocument() {
    }

    public UsuarioProtocoloFavoritoDocument(String userId, String protocoloId) {
        this.userId = userId;
        this.protocoloId = protocoloId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProtocoloId() { return protocoloId; }
    public void setProtocoloId(String protocoloId) { this.protocoloId = protocoloId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
