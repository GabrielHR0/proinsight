package com.prosup.proinsight.adapter.out.persistence;

import com.prosup.proinsight.domain.model.Endereco;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ClienteDocument {
    @Id
    private String id;
    @NotBlank(message = "fullName is required")
    private String fullName;
    @Email
    private String email;
    private String phone;
    @CPF
    private String cpf;
    private Endereco endereco;

    public ClienteDocument() {
    }

    public ClienteDocument(String id, String fullName, String email, String phone, String cpf, Endereco endereco) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.cpf = cpf;
        this.endereco = endereco;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}
