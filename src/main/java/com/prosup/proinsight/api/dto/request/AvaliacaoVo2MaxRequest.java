package com.prosup.proinsight.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prosup.proinsight.domain.enums.Sexo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;


public class AvaliacaoVo2MaxRequest {

    @NotBlank(message = "cliente_id é obrigatório")
    @JsonProperty("cliente_id")
    private String clienteId;

    @NotBlank(message = "protocolo_id é obrigatório")
    @JsonProperty("protocolo_id")
    private String protocoloId;

    @NotBlank(message = "avaliador_id é obrigatório")
    @JsonProperty("avaliador_id")
    private String avaliadorId;

    @NotNull(message = "resultado é obrigatório")
    @PositiveOrZero(message = "resultado deve ser maior ou igual a zero")
    @JsonProperty("resultado")
    private Double resultado;

    @JsonProperty("observacoes")
    private String observacoes;

    @Positive(message = "frequencia_cardiaca deve ser positiva")
    @JsonProperty("frequencia_cardiaca")
    private Integer frequenciaCardiaca;

    @Positive(message = "peso_kg deve ser positivo")
    @JsonProperty("peso_kg")
    private Double pesoKg;

    @Positive(message = "idade deve ser positiva")
    @JsonProperty("idade")
    private Integer idade;

    @Positive(message = "inclinacao_percent deve ser positiva")
    @JsonProperty("inclinacao_percent")
    private Double inclinacaoPercent;

    @JsonProperty("sexo")
    private Sexo sexo;

    public AvaliacaoVo2MaxRequest() {}

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getProtocoloId() {
        return protocoloId;
    }

    public void setProtocoloId(String protocoloId) {
        this.protocoloId = protocoloId;
    }

    public String getAvaliadorId() {
        return avaliadorId;
    }

    public void setAvaliadorId(String avaliadorId) {
        this.avaliadorId = avaliadorId;
    }

    public Double getResultado() {
        return resultado;
    }

    public void setResultado(Double resultado) {
        this.resultado = resultado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Integer getFrequenciaCardiaca() {
        return frequenciaCardiaca;
    }

    public void setFrequenciaCardiaca(Integer frequenciaCardiaca) {
        this.frequenciaCardiaca = frequenciaCardiaca;
    }

    public Double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public Double getInclinacaoPercent() {
        return inclinacaoPercent;
    }

    public void setInclinacaoPercent(Double inclinacaoPercent) {
        this.inclinacaoPercent = inclinacaoPercent;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }
}
