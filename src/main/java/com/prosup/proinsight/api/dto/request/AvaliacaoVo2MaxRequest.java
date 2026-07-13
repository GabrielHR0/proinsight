package com.prosup.proinsight.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prosup.proinsight.domain.enums.Sexo;


public class AvaliacaoVo2MaxRequest {

    @JsonProperty("cliente_id")
    private String clienteId;

    @JsonProperty("protocolo_id")
    private String protocoloId;

    @JsonProperty("avaliador_id")
    private String avaliadorId;

    @JsonProperty("resultado")
    private double resultado;

    @JsonProperty("observacoes")
    private String observacoes;

    @JsonProperty("frequencia_cardiaca")
    private Integer frequenciaCardiaca;

    @JsonProperty("peso_kg")
    private Double pesoKg;

    @JsonProperty("idade")
    private Integer idade;

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

    public double getResultado() {
        return resultado;
    }

    public void setResultado(double resultado) {
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

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }
}
