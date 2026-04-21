package com.prosup.proinsight.dto.response;

/**
 * Response DTO for Permission.
 */
public record PermissionResponse(
        String id,
        String resource,
        String action,
        String description) {
}

