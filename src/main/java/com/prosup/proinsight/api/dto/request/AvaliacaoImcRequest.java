package com.prosup.proinsight.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AvaliacaoImcRequest(
    @NotBlank String clienteId,
    @NotBlank String protocoloId,
    @NotBlank String avaliadorId,
    @NotNull @Positive Integer pesoGramas,
    @NotNull @Positive Integer alturaCm
) {}
