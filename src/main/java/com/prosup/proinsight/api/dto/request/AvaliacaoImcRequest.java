package com.prosup.proinsight.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AvaliacaoImcRequest(
    @NotBlank(message = "clienteId é obrigatório") @JsonProperty("cliente_id") String clienteId,
    @NotBlank(message = "protocoloId é obrigatório") @JsonProperty("protocolo_id") String protocoloId,
    @NotBlank(message = "avaliadorId é obrigatório") @JsonProperty("avaliador_id") String avaliadorId,
    @NotNull(message = "pesoGramas é obrigatório") @Positive(message = "pesoGramas deve ser positivo") @JsonProperty("peso_gramas") Integer pesoGramas,
    @NotNull(message = "alturaCm é obrigatório") @Positive(message = "alturaCm deve ser positivo") @JsonProperty("altura_cm") Integer alturaCm
) {}
