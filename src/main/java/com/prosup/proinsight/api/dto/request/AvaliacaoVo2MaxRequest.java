package com.prosup.proinsight.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prosup.proinsight.domain.enums.Sexo;


public class AvaliacaoVo2MaxRequest {

    @JsonProperty("cliente_id")
    private String clienteId;

    @JsonProperty("avaliacaoFisicaId")
    private String avaliacaoFisicaId;

    @JsonProperty("avaliador_id")
    private String avaliadorId;

    @JsonProperty("medicao")
    private MedicaoVo2MaxDto medicaoVo2MaxDto;

    @JsonProperty("idade")
    private Integer idade;

    @JsonProperty("sexo")
    private Sexo sexo;

    public AvaliacaoVo2MaxRequest(String clienteId, String avaliacaoFisicaId, String avaliadorId, MedicaoVo2MaxDto medicaoVo2MaxDto) {
        this.clienteId = clienteId;
        this.avaliacaoFisicaId = avaliacaoFisicaId;
        this.avaliadorId = avaliadorId;
        this.medicaoVo2MaxDto = medicaoVo2MaxDto;
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

    public MedicaoVo2MaxDto getMedicaoVo2MaxDto() {
        return medicaoVo2MaxDto;
    }

    public void setMedicaoVo2MaxDto(MedicaoVo2MaxDto medicaoVo2MaxDto) {
        this.medicaoVo2MaxDto = medicaoVo2MaxDto;
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
