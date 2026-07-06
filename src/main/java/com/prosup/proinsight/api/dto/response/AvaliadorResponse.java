package com.prosup.proinsight.api.dto.response;

/**
 * Response DTO for Avaliador. Using Java record for immutability and concise representation.
 */
public record AvaliadorResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String telefone,
        String cpf,
        String cref) {
}
