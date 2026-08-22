package com.prosup.proinsight.infrastructure.persistence.document;

import com.prosup.proinsight.config.ScopedByAcademia;
import com.prosup.proinsight.domain.model.Endereco;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.prosup.proinsight.domain.enums.Sexo;
import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "clientes")
@CompoundIndex(name = "academia_cliente_idx", def = "{'academiaId': 1, 'fullName': 1}")
@ScopedByAcademia
public class ClienteDocument {

    @Id
    private String id;

    private String fullName;

    private String email;

    private String phone;

    private String cpf;

    private LocalDate dataNascimento;

    private Sexo sexo;

    private Endereco endereco;

    @Indexed
    private String academiaId;

    @Indexed
    private String avaliadorId;

    private boolean active = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public ClienteDocument() {
    }

    public ClienteDocument(String id, String fullName, String email, String phone,
                           String cpf, LocalDate dataNascimento, Sexo sexo, Endereco endereco,
                           String academiaId, String avaliadorId, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.endereco = endereco;
        this.academiaId = academiaId;
        this.avaliadorId = avaliadorId;
        this.active = active;
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

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public String getAcademiaId() { return academiaId; }
    public void setAcademiaId(String academiaId) { this.academiaId = academiaId; }

    public String getAvaliadorId() { return avaliadorId; }
    public void setAvaliadorId(String avaliadorId) { this.avaliadorId = avaliadorId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
