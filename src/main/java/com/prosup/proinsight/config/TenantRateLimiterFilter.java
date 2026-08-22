package com.prosup.proinsight.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

/**
 * Limite de requisições por academia (tenant) por minuto.
 * Conta por tenant, não por IP — impede que um tenant consuma
 * o recurso dos demais (noisy neighbor).
 */
public class TenantRateLimiterFilter extends OncePerRequestFilter {

    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int DEFAULT_MAX_PER_MINUTE = 600;
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password"
    );

    private final int maxRequestsPerMinute;
    private final Cache<String, Integer> requests;

    public TenantRateLimiterFilter(int maxPerMinute, boolean enabled) {
        this.maxRequestsPerMinute = enabled ? maxPerMinute : DEFAULT_MAX_PER_MINUTE;
        this.requests = Caffeine.newBuilder()
                .expireAfterWrite(WINDOW)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String tenantId = TenantContext.getAcademiaId();
        if (tenantId == null || tenantId.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        int count = requests.get(tenantId, k -> 0) + 1;
        requests.put(tenantId, count);

        int remaining = maxRequestsPerMinute - count;
        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequestsPerMinute));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(remaining, 0)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(WINDOW.toSeconds()));

        if (count > maxRequestsPerMinute) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"Limite de requisições da academia atingido. Aguarde 1 minuto.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return EXCLUDED_PATHS.contains(request.getRequestURI());
    }

    public void reset() {
        requests.invalidateAll();
    }
}