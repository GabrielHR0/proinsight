package com.prosup.proinsight.infrastructure.persistence.document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "avaliadores")
public class AvaliadorDocument {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String cref;

    @NotBlank(message = "O nome é obrigatório")
    private String firstName;

    private String lastName;

    @Email
    private String email;

    private String telefone;

    @CPF
    @Indexed(unique = true)
    private String cpf;

    public AvaliadorDocument() {
    }

    public AvaliadorDocument(String id, String userId, String cref, String firstName, String lastName, String email, String telefone, String cpf) {
        this.id = id;
        this.userId = userId;
        this.cref = cref;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCref() {
        return cref;
    }

    public void setCref(String cref) {
        this.cref = cref;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}
