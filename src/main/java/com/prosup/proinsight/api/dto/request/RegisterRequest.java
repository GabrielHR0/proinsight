package com.prosup.proinsight.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String password;

    @NotBlank(message = "Nome de usuário é obrigatório")
    private String userName;

    // ── Avaliador (obrigatório — usuário sempre tem registro profissional) ─
    @NotBlank(message = "CREF é obrigatório")
    private String cref;

    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    // ── Academia (opcional — se nulo, cria usuário sem academia) ──────────
    private String academiaNome;
    private String cnpj;
    private String razaoSocial;
    private String telefone;
    private EnderecoRequest endereco;

    public RegisterRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCref() { return cref; }
    public void setCref(String cref) { this.cref = cref; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getAcademiaNome() { return academiaNome; }
    public void setAcademiaNome(String academiaNome) { this.academiaNome = academiaNome; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public EnderecoRequest getEndereco() { return endereco; }
    public void setEndereco(EnderecoRequest endereco) { this.endereco = endereco; }

    public boolean hasAcademia() {
        return academiaNome != null && !academiaNome.isBlank();
    }

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
