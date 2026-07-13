package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AvaliacaoImcResponse(
    @JsonProperty("classificacao") String classificacao,
    @JsonProperty("protocolo_nome") String protocoloNome,
    @JsonProperty("protocolo_id") String protocoloId,
    @JsonProperty("avaliador_id") String avaliadorId,
    @JsonProperty("cliente_id") String clienteId,
    @JsonProperty("avaliacao_id") String avaliacaoId,
    @JsonProperty("status") String status,
    @JsonProperty("extras") Map<String, Object> extras
) {}
