package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TesteFuncionalDto(
        @JsonProperty("teste") String teste,
        @JsonProperty("nome") String nome,
        @JsonProperty("unidade") String unidade,
        @JsonProperty("timer") String timer,
        @JsonProperty("segundos") Integer segundos
) {}
