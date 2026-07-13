package com.prosup.proinsight.infrastructure.persistence.document;

import com.prosup.proinsight.domain.model.Endereco;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "clientes")
@CompoundIndex(name = "academia_cliente_idx", def = "{'academiaId': 1, 'fullName': 1}")
public class ClienteDocument {

    @Id
    private String id;

    private String fullName;

    private String email;

    private String phone;

    private String cpf;

    private Endereco endereco;

    @Indexed
    private String academiaId;

    @Indexed
    private String avaliadorId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public ClienteDocument() {
    }

    public ClienteDocument(String id, String fullName, String email, String phone,
                           String cpf, Endereco endereco, String academiaId, String avaliadorId) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.cpf = cpf;
        this.endereco = endereco;
        this.academiaId = academiaId;
        this.avaliadorId = avaliadorId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public String getAcademiaId() { return academiaId; }
    public void setAcademiaId(String academiaId) { this.academiaId = academiaId; }

    public String getAvaliadorId() { return avaliadorId; }
    public void setAvaliadorId(String avaliadorId) { this.avaliadorId = avaliadorId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
