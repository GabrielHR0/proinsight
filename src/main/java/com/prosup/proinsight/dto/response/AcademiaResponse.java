package com.prosup.proinsight.dto.response;

import java.time.Instant;

/**
 * Response DTO for Academia profile.
 */
public record AcademiaResponse(
        String id,
        String userId,
        String nomeFantasia,
        String razaoSocial,
        String cnpj,
        EnderecoResponse endereco,
        String telefone,
        Instant createdAt,
        Instant updatedAt) {

    public static record EnderecoResponse(String rua, String numero, String cidade, String estado, String cep) {}
}

