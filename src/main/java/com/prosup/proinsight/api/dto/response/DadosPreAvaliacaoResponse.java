package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.prosup.proinsight.domain.enums.Sexo;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DadosPreAvaliacaoResponse {

    @JsonProperty("protocolo_id")
    private String protocoloId;

    @JsonProperty("protocolo_imc_id")
    private String protocoloImcId;

    @JsonProperty("sexo")
    private Sexo sexo;

    @JsonProperty("idade")
    private Integer idade;

    @JsonProperty("peso_kg")
    private Double pesoKg;

    @JsonProperty("altura_cm")
    private Integer alturaCm;

    @JsonProperty("data_ultima_avaliacao_imc")
    private Instant dataUltimaAvaliacaoImc;

    public DadosPreAvaliacaoResponse() {}

    public DadosPreAvaliacaoResponse(String protocoloId, String protocoloImcId, Sexo sexo, Integer idade,
                                     Double pesoKg, Integer alturaCm, Instant dataUltimaAvaliacaoImc) {
        this.protocoloId = protocoloId;
        this.protocoloImcId = protocoloImcId;
        this.sexo = sexo;
        this.idade = idade;
        this.pesoKg = pesoKg;
        this.alturaCm = alturaCm;
        this.dataUltimaAvaliacaoImc = dataUltimaAvaliacaoImc;
    }

    public String getProtocoloId() { return protocoloId; }
    public String getProtocoloImcId() { return protocoloImcId; }
    public Sexo getSexo() { return sexo; }
    public Integer getIdade() { return idade; }
    public Double getPesoKg() { return pesoKg; }
    public Integer getAlturaCm() { return alturaCm; }
    public Instant getDataUltimaAvaliacaoImc() { return dataUltimaAvaliacaoImc; }
}
