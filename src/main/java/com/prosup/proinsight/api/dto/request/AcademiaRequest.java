package com.prosup.proinsight.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AcademiaRequest {

    @NotBlank(message = "ownerId é obrigatório")
    private String ownerId;

    @NotBlank(message = "nomeFantasia é obrigatório")
    private String nomeFantasia;

    private String razaoSocial;

    @NotBlank(message = "cnpj é obrigatório")
    private String cnpj;

    private EnderecoRequest endereco;

    private String telefone;

    public AcademiaRequest() {
    }

    public AcademiaRequest(String ownerId, String nomeFantasia, String razaoSocial, String cnpj, EnderecoRequest endereco, String telefone) {
        this.ownerId = ownerId;
        this.nomeFantasia = nomeFantasia;
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.endereco = endereco;
        this.telefone = telefone;
    }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
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
        @NotBlank(message = "rua é obrigatório")
        private String rua;
        private String numero;
        @NotBlank(message = "cidade é obrigatório")
        private String cidade;
        @NotBlank(message = "estado é obrigatório")
        private String estado;
        @NotBlank(message = "cep é obrigatório")
        private String cep;

        public EnderecoRequest() {}
        public EnderecoRequest(String rua, String numero, String cidade, String estado, String cep) {
            this.rua = rua; this.numero = numero; this.cidade = cidade;
            this.estado = estado; this.cep = cep;
        }

        public String getRua() { return rua; }
        public void setRua(String rua) { this.rua = rua; }
        public String getNumero() { return numero; }
        public void setNumero(String numero) { this.numero = numero; }
        public String getCidade() { return cidade; }
        public void setCidade(String cidade) { this.cidade = cidade; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        public String getCep() { return cep; }
        public void setCep(String cep) { this.cep = cep; }
    }
}
