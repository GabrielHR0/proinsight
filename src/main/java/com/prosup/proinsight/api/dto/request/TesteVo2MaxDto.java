package com.prosup.proinsight.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;

public class TesteVo2MaxDto {

    @JsonProperty("protocolo")
    private ProtocoloVo2Max protocolo;

    @JsonProperty("resultado")
    private double resultado;

    @JsonProperty("frequencia_cardiaca")
    private Integer frequenciaCardiaca;

    @JsonProperty("peso_kg")
    private Double pesoKg;

    @JsonProperty("inclinacao_percent")
    private Double inclinacaoPercent;

    public TesteVo2MaxDto() {}

    public TesteVo2MaxDto(ProtocoloVo2Max protocolo, double resultado) {
        this.protocolo = protocolo;
        this.resultado = resultado;
    }

    public ProtocoloVo2Max getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(ProtocoloVo2Max protocolo) {
        this.protocolo = protocolo;
    }

    public double getResultado() {
        return resultado;
    }

    public void setResultado(double resultado) {
        this.resultado = resultado;
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

    public Double getInclinacaoPercent() {
        return inclinacaoPercent;
    }

    public void setInclinacaoPercent(Double inclinacaoPercent) {
        this.inclinacaoPercent = inclinacaoPercent;
    }
}
