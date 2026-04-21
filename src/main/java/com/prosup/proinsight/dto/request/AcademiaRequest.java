package com.prosup.proinsight.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Request DTO for Academia profile. Includes an inner EnderecoRequest.
 */
public class AcademiaRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "nomeFantasia is required")
    private String nomeFantasia;

    private String razaoSocial;

    @NotBlank(message = "cnpj is required")
    private String cnpj;

    private EnderecoRequest endereco;

    private String telefone;

    public AcademiaRequest() {
    }

    public AcademiaRequest(String userId, String nomeFantasia, String razaoSocial, String cnpj, EnderecoRequest endereco, String telefone) {
        this.userId = userId;
        this.nomeFantasia = nomeFantasia;
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.endereco = endereco;
        this.telefone = telefone;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public EnderecoRequest getEndereco() { return endereco; }
    public void setEndereco(EnderecoRequest endereco) { this.endereco = endereco; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public static class EnderecoRequest {
        private String rua;
        private String numero;
        private String cidade;
        private String estado;
        private String cep;

        public EnderecoRequest() {}
        public EnderecoRequest(String rua, String numero, String cidade, String estado, String cep) {
            this.rua = rua; this.numero = numero; this.cidade = cidade; this.estado = estado; this.cep = cep;
        }

        public String getRua() { return rua; } public void setRua(String rua) { this.rua = rua; }
        public String getNumero() { return numero; } public void setNumero(String numero) { this.numero = numero; }
        public String getCidade() { return cidade; } public void setCidade(String cidade) { this.cidade = cidade; }
        public String getEstado() { return estado; } public void setEstado(String estado) { this.estado = estado; }
        public String getCep() { return cep; } public void setCep(String cep) { this.cep = cep; }
    }
}

