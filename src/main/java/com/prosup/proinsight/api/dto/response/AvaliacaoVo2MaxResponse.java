package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * DTO de resposta para avaliação VO2Max completa.
 * 
 * Retorna ao cliente:
 * - IDs do cliente e avaliador
 * - Classificação obtida
 * - Timestamp de quando foi realizada
 * 
 * Spring: Jackson serializa automaticamente em JSON via @JsonProperty.
 */
public class AvaliacaoVo2MaxResponse {
    
    @JsonProperty("cliente_id")
    private String clienteId;
    
    @JsonProperty("avaliador_id")
    private String avaliadorId;
    
    @JsonProperty("classificacao")
    private ClassificacaoVo2Max classificacao;
    
    @JsonProperty("data_avaliacao")
    private LocalDateTime dataAvaliacao;
    
    public AvaliacaoVo2MaxResponse() {
        this.dataAvaliacao = LocalDateTime.now();
    }
    
    /**
     * Cria resposta com dados da avaliação.
     * 
     * @param clienteId ID do cliente avaliado
     * @param avaliadorId ID do avaliador
     * @param classificacao Resultado da classificação
     */
    public AvaliacaoVo2MaxResponse(
        String clienteId,
        String avaliadorId,
        ClassificacaoVo2Max classificacao
    ) {
        this.clienteId = clienteId;
        this.avaliadorId = avaliadorId;
        this.classificacao = classificacao;
        this.dataAvaliacao = LocalDateTime.now();
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
    
    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }
    
    public void setDataAvaliacao(LocalDateTime dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }
}
