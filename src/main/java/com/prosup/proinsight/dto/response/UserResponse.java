package com.prosup.proinsight.dto.response;

import java.time.Instant;
import java.util.Set;

/**
 * Response DTO for User. Immutable representation returned by controllers.
 * Roles and permissions are represented as sets of ids here to keep mapping simple.
 */
public record UserResponse(
        String id,
        String email,
        Set<String> roleIds,
        Set<String> permissionIds,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}

