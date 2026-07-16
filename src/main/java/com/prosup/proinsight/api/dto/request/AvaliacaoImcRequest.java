package com.prosup.proinsight.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AvaliacaoImcRequest(
    @NotBlank(message = "clienteId é obrigatório") String clienteId,
    @NotBlank(message = "protocoloId é obrigatório") String protocoloId,
    @NotBlank(message = "avaliadorId é obrigatório") String avaliadorId,
    @NotNull(message = "pesoGramas é obrigatório") @Positive(message = "pesoGramas deve ser positivo") Integer pesoGramas,
    @NotNull(message = "alturaCm é obrigatório") @Positive(message = "alturaCm deve ser positivo") Integer alturaCm
) {}
