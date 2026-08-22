package com.prosup.proinsight.api.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record UserResponse(
        String id,
        String userName,
        String email,
        Map<String, Set<String>> academiaRoles,
        boolean active,
        Set<String> academiaIds,
        String cref,
        String cpf,
        Instant createdAt,
        Instant updatedAt) {
}
