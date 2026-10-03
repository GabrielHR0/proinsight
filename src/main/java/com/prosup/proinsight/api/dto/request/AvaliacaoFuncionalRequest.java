package com.prosup.proinsight.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AvaliacaoFuncionalRequest(
    @NotBlank(message = "clienteId é obrigatório") @JsonProperty("cliente_id") String clienteId,
    @NotBlank(message = "protocoloId é obrigatório") @JsonProperty("protocolo_id") String protocoloId,
    @NotBlank(message = "avaliadorId é obrigatório") @JsonProperty("avaliador_id") String avaliadorId,
    @JsonProperty("idade") Integer idade,
    @JsonProperty("observacoes") String observacoes,
    @NotEmpty(message = "ao menos um teste deve ser informado")
    @Valid @JsonProperty("testes") List<TesteFuncionalResultadoRequest> testes
) {}
