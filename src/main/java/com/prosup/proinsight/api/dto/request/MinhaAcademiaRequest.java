package com.prosup.proinsight.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public class MinhaAcademiaRequest {

    @NotBlank(message = "Nome da academia é obrigatório")
    private String nomeFantasia;

    private String razaoSocial;
    private String cnpj;
    private String telefone;
    private EnderecoRequest endereco;

    public MinhaAcademiaRequest() {}

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public EnderecoRequest getEndereco() { return endereco; }
    public void setEndereco(EnderecoRequest endereco) { this.endereco = endereco; }

    public static class EnderecoRequest {
        private String rua;
        private String numero;
        private String cidade;
        private String estado;
        private String cep;

        public EnderecoRequest() {}

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
