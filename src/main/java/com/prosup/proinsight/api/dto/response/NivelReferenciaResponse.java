package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Faixa de um nível de classificação (ex: "RUIM", min 35, max 44 exclusivo)
 * extraída da tabela de classificação referenciada pelo protocolo da avaliação.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NivelReferenciaResponse(
    @JsonProperty("classificacao") String classificacao,
    @JsonProperty("classificacao_legivel") String classificacaoLegivel,
    @JsonProperty("min") Double min,
    @JsonProperty("max") Double max,
    @JsonProperty("tipo_min") String tipoMin,
    @JsonProperty("tipo_max") String tipoMax
) {}