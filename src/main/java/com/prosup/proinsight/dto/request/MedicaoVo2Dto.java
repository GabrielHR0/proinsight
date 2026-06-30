package com.prosup.proinsight.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prosup.proinsight.domain.enums.Unidade;

import java.time.Instant;
import java.util.List;

public class MedicaoVo2Dto {

    @JsonProperty("tabela_classificacao_id")
    private String tabelaClassificacaoId;

    @JsonProperty("medido_em")
    private Instant medidoEm;

    @JsonProperty("observacoes")
    private String observacoes;

    @JsonProperty("testes")
    private List<TesteVo2MaxDto> testes;

    public MedicaoVo2Dto(String tabelaClassificacaoId, Instant medidoEm, String observacoes, List<TesteVo2MaxDto> testes) {
        this.tabelaClassificacaoId = tabelaClassificacaoId;
        this.medidoEm = medidoEm;
        this.observacoes = observacoes;
        this.testes = testes;
    }

    public MedicaoVo2Dto() {
    }

    public String getTabelaClassificacaoId() {
        return tabelaClassificacaoId;
    }

    public void setTabelaClassificacaoId(String tabelaClassificacaoId) {
        this.tabelaClassificacaoId = tabelaClassificacaoId;
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
