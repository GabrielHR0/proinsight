package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProtocoloDetalheResponse(
        @JsonProperty("id") String id,
        @JsonProperty("nome") String nome,
        @JsonProperty("categoria") String categoria,
        @JsonProperty("padrao") Boolean padrao,
        @JsonProperty("strategyKey") String strategyKey,
        @JsonProperty("tabelaClassificacaoId") String tabelaClassificacaoId,
        @JsonProperty("descricao") String descricao,
        @JsonProperty("comoRealizar") String comoRealizar,
        @JsonProperty("calculadora") String calculadora,
        @JsonProperty("referenciaBibliografica") String referenciaBibliografica,
        @JsonProperty("unidadeMedida") String unidadeMedida,
        @JsonProperty("tempoMinimoSegundos") Integer tempoMinimoSegundos,
        @JsonProperty("tempoMaximoSegundos") Integer tempoMaximoSegundos,
        @JsonProperty("equipamentoNecessario") String equipamentoNecessario,
        @JsonProperty("criteriosExclusao") String criteriosExclusao,
        @JsonProperty("observacoes") String observacoes,
        @JsonProperty("createdAt") Instant createdAt,
        @JsonProperty("testes") List<TesteFuncionalDto> testes
) {}
