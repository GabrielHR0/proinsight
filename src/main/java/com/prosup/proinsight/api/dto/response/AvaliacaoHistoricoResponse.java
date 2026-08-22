package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AvaliacaoHistoricoResponse(
    @JsonProperty("id") String id,
    @JsonProperty("cliente_id") String clienteId,
    @JsonProperty("protocolo_id") String protocoloId,
    @JsonProperty("protocolo_nome") String protocoloNome,
    @JsonProperty("tipo") String tipo,
    @JsonProperty("data_avaliacao") String dataAvaliacao,
    @JsonProperty("valor") Double valor,
    @JsonProperty("classificacao") String classificacao,
    @JsonProperty("classificacao_legivel") String classificacaoLegivel,
    @JsonProperty("detalhes") Map<String, Object> detalhes,
    @JsonProperty("referencias") ReferenciaClassificacaoResponse referencias
) {}