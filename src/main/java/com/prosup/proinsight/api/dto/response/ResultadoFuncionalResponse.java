package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultadoFuncionalResponse(
    @JsonProperty("teste") String teste,
    @JsonProperty("teste_nome") String testeNome,
    @JsonProperty("unidade") String unidade,
    @JsonProperty("valor") Double valor,
    @JsonProperty("percentil") Integer percentil,
    @JsonProperty("classificacao") String classificacao,
    @JsonProperty("classificacao_legivel") String classificacaoLegivel
) {
    public ResultadoFuncionalResponse(
        String teste,
        String testeNome,
        String unidade,
        Double valor,
        String classificacao,
        String classificacaoLegivel
    ) {
        this(teste, testeNome, unidade, valor, null, classificacao, classificacaoLegivel);
    }
}
