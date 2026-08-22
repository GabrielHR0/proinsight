package com.prosup.proinsight.api.dto.request;

import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.model.Endereco;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public class ClienteComImcRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String fullName;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    private String phone;

    @NotBlank(message = "CPF é obrigatório")
    @CPF(message = "CPF inválido")
    private String cpf;

    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate dataNascimento;

    @NotNull(message = "Sexo é obrigatório")
    private Sexo sexo;

    private String rua;
    private String numero;
    private String cidade;
    private String estado;
    private String cep;

    private String academiaId;
    private String avaliadorId;
    private Boolean active;

    private String protocoloId;

    @Positive(message = "pesoGramas deve ser positivo")
    private Integer pesoGramas;

    @Positive(message = "alturaCm deve ser positivo")
    private Integer alturaCm;

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
    public String getAcademiaId() { return academiaId; }
    public void setAcademiaId(String academiaId) { this.academiaId = academiaId; }
    public String getAvaliadorId() { return avaliadorId; }
    public void setAvaliadorId(String avaliadorId) { this.avaliadorId = avaliadorId; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getProtocoloId() { return protocoloId; }
    public void setProtocoloId(String protocoloId) { this.protocoloId = protocoloId; }
    public Integer getPesoGramas() { return pesoGramas; }
    public void setPesoGramas(Integer pesoGramas) { this.pesoGramas = pesoGramas; }
    public Integer getAlturaCm() { return alturaCm; }
    public void setAlturaCm(Integer alturaCm) { this.alturaCm = alturaCm; }

    public boolean temImc() {
        return pesoGramas != null && alturaCm != null;
    }

    public Endereco toEndereco() {
        return new Endereco(rua, numero, cidade, estado, cep);
    }
}
