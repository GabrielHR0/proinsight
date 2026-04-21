package com.prosup.proinsight.dto.response;

import java.time.Instant;

/**
 * Response DTO for Avaliador profile.
 */
public record AvaliadorResponse(
        String id,
        String userId,
        String cref,
        String firstName,
        String lastName,
        String email,
        String telefone,
        String cpf,
        Instant createdAt,
        Instant updatedAt) {
}

