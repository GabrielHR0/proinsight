package com.prosup.proinsight.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TesteFuncionalResultadoRequest(
    @NotBlank(message = "teste é obrigatório") @JsonProperty("teste") String teste,
    @NotNull(message = "valor é obrigatório") @JsonProperty("valor") Double valor
) {}
