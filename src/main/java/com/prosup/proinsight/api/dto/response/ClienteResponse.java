package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.model.Endereco;

import java.time.LocalDate;

public record ClienteResponse(
        String id,
        String fullName,
        String email,
        String phone,
        String cpf,
        LocalDate dataNascimento,
        Sexo sexo,
        Endereco endereco,
        String academiaId,
        String avaliadorId,
        @JsonProperty("ativo") boolean active
) {
}
