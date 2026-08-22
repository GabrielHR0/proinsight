package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Referência de classificação resolvida para o cliente (sexo + faixa etária
 * correspondente na tabela do protocolo) com todas as faixas de níveis.
 * Para tabelas universais (ex: IMC), sexo e faixa etária são null.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReferenciaClassificacaoResponse(
    @JsonProperty("sexo") String sexo,
    @JsonProperty("idade_min") Integer idadeMin,
    @JsonProperty("idade_max") Integer idadeMax,
    @JsonProperty("niveis") List<NivelReferenciaResponse> niveis
) {}