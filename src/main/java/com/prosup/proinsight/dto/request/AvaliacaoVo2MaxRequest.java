package com.prosup.proinsight.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;


public class AvaliacaoVo2MaxRequest {

    @JsonProperty("cliente_id")
    private String clienteId;

    @JsonProperty("avaliacaoFisicaId")
    private String avaliacaoFisicaId;

    @JsonProperty("avaliador_id")
    private String avaliadorId;

    @JsonProperty("medicao")
    private MedicaoVo2Dto medicaoVo2Dto;

    public AvaliacaoVo2MaxRequest(String clienteId, String avaliacaoFisicaId, String avaliadorId, MedicaoVo2Dto medicaoVo2Dto) {
        this.clienteId = clienteId;
        this.avaliacaoFisicaId = avaliacaoFisicaId;
        this.avaliadorId = avaliadorId;
        this.medicaoVo2Dto = medicaoVo2Dto;
    }

    public String getAvaliacaoFisicaId() {
        return avaliacaoFisicaId;
    }

    public void setAvaliacaoFisicaId(String avaliacaoFisicaId) {
        this.avaliacaoFisicaId = avaliacaoFisicaId;
    }

    public AvaliacaoVo2MaxRequest() {
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getAvaliadorId() {
        return avaliadorId;
    }

    public void setAvaliadorId(String avaliadorId) {
        this.avaliadorId = avaliadorId;
    }

    public MedicaoVo2Dto getMedicaoVo2Dto() {
        return medicaoVo2Dto;
    }

    public void setMedicaoVo2Dto(MedicaoVo2Dto medicaoVo2Dto) {
        this.medicaoVo2Dto = medicaoVo2Dto;
    }
}
