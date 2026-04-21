package com.prosup.proinsight.dto.response;

/**
 * Response DTO for Avaliador. Using Java record for immutability and concise representation.
 */
public record AvaliadorDto(
        String id,
        String firstName,
        String lastName,
        String email,
        String telefone,
        String cpf,
        String cref) {
}
