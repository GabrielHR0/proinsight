package com.prosup.proinsight.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Rate limit in-memory por chave (janela deslizante de 1 minuto).
 * Usado como segundo bucket do OWASP: por IP (no filtro) e por
 * identidade (e-mail no login, hash do refresh token no refresh).
 * Apenas falhas contam — sucesso decrementa o contador.
 */
@Service
public class RateLimitService {

    public static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Cache<String, Integer> attempts = Caffeine.newBuilder()
            .expireAfterWrite(WINDOW)
            .build();

    public boolean isBlocked(String key) {
        return attempts.get(key, k -> 0) >= MAX_ATTEMPTS;
    }

    public void recordFailure(String key) {
        attempts.put(key, attempts.get(key, k -> 0) + 1);
    }

    public void recordSuccess(String key) {
        int current = attempts.get(key, k -> 0);
        if (current > 0) {
            attempts.put(key, current - 1);
        }
    }

    public void reset() {
        attempts.invalidateAll();
    }
}
