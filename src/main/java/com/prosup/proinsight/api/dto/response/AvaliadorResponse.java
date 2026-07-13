package com.prosup.proinsight.api.dto.response;


public record AvaliadorResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String telefone,
        String cpf,
        String cref,
        String academiaId) {
}
