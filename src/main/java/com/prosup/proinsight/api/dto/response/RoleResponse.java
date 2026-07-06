package com.prosup.proinsight.api.dto.response;

import java.util.Set;

/**
 * Response DTO for Role. Permissions are represented as nested PermissionResponse for convenience.
 */
public record RoleResponse(
        String id,
        String name,
        String description,
        Set<PermissionResponse> permissions) {
}
