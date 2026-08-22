package com.prosup.proinsight.api.dto.response;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record MeResponse(
        String userId,
        String userName,
        String email,
        Map<String, List<String>> academiaPermissoes,
        Set<String> academiaIds) {
}
