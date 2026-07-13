package com.prosup.proinsight.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record UserResponse(
        String id,
        String email,
        Set<String> roleIds,
        Set<String> permissionIds,
        boolean active,
        List<String> academiaIds,
        String avaliadorId,
        Instant createdAt,
        Instant updatedAt) {
}
