package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;


public class AvaliacaoVo2MaxResponse {

    @JsonProperty("avaliacao_id")
    private String avaliacaoId;

    @JsonProperty("cliente_id")
    private String clienteId;

    @JsonProperty("avaliador_id")
    private String avaliadorId;

    @JsonProperty("classificacao")
    private ClassificacaoVo2Max classificacao;

    @JsonProperty("referencias")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ReferenciaClassificacaoResponse referencias;

    @JsonProperty("data_avaliacao")
    private LocalDateTime dataAvaliacao;

    public AvaliacaoVo2MaxResponse() {
        this.dataAvaliacao = LocalDateTime.now();
    }

    public AvaliacaoVo2MaxResponse(
        String clienteId,
        String avaliadorId,
        ClassificacaoVo2Max classificacao,
        String avaliacaoId
    ) {
        this.clienteId = clienteId;
        this.avaliadorId = avaliadorId;
        this.classificacao = classificacao;
        this.avaliacaoId = avaliacaoId;
        this.dataAvaliacao = LocalDateTime.now();
    }

    public String getAvaliacaoId() {
        return avaliacaoId;
    }

    public void setAvaliacaoId(String avaliacaoId) {
        this.avaliacaoId = avaliacaoId;
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

    public ClassificacaoVo2Max getClassificacao() {
        return classificacao;
    }

    public void setClassificacao(ClassificacaoVo2Max classificacao) {
        this.classificacao = classificacao;
    }

    public ReferenciaClassificacaoResponse getReferencias() {
        return referencias;
    }

    public void setReferencias(ReferenciaClassificacaoResponse referencias) {
        this.referencias = referencias;
    }

    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDateTime dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }
}
