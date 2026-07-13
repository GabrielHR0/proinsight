package com.prosup.proinsight.api.dto.response;

import java.util.Set;

public record RoleResponse(
        String id,
        String name,
        String description,
        Set<PermissionResponse> permissions) {
}
