package com.prosup.proinsight.api.dto.response;


public record PermissionResponse(
        String id,
        String resource,
        String action,
        String description) {
}
