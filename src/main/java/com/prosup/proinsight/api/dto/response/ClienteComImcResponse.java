package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClienteComImcResponse(
        ClienteResponse cliente,
        AvaliacaoImcResponse avaliacao
) {
}
