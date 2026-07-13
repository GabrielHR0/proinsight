package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record AvaliacaoListaResponse(
        @JsonProperty("id") String id,
        @JsonProperty("cliente_id") String clienteId,
        @JsonProperty("protocolo_id") String protocoloId,
        @JsonProperty("data_avaliacao") String dataAvaliacao,
        @JsonProperty("tipo") String tipo,
        @JsonProperty("detalhes") Map<String, Object> detalhes
) {
}
