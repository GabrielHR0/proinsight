package com.prosup.proinsight.api.dto.response;

import java.time.Instant;

public record AcademiaResponse(
        String id,
        String ownerId,
        String nomeFantasia,
        String razaoSocial,
        String cnpj,
        EnderecoResponse endereco,
        String telefone,
        Instant createdAt,
        Instant updatedAt) {

    public static record EnderecoResponse(String rua, String numero, String cidade, String estado, String cep) {}
}
