package com.prosup.proinsight.api.dto.response;

import java.util.List;
import java.util.Map;

public record LoginResponse(
        String token,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String userId,
        String userName,
        String email,
        Map<String, List<String>> academiaPermissoes) {
}
