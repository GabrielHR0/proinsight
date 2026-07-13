package com.prosup.proinsight.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class MedicaoVo2MaxDto {

    @JsonProperty("medido_em")
    private Instant medidoEm;

    @JsonProperty("observacoes")
    private String observacoes;

    @JsonProperty("testes")
    private List<TesteVo2MaxDto> testes;

    public MedicaoVo2MaxDto(Instant medidoEm, String observacoes, List<TesteVo2MaxDto> testes) {
        this.medidoEm = medidoEm;
        this.observacoes = observacoes;
        this.testes = testes;
    }

    public MedicaoVo2MaxDto() {
    }

    public Instant getMedidoEm() {
        return medidoEm;
    }

    public void setMedidoEm(Instant medidoEm) {
        this.medidoEm = medidoEm;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<TesteVo2MaxDto> getTestes() {
        return testes;
    }

    public void setTestes(List<TesteVo2MaxDto> testes) {
        this.testes = testes;
    }
}
