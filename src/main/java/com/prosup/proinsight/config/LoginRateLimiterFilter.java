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

public class LoginRateLimiterFilter extends OncePerRequestFilter {

    static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password"
    );

    private final Cache<String, Integer> attempts = Caffeine.newBuilder()
            .expireAfterWrite(WINDOW)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip = resolveIp(request);
        int count = attempts.get(ip, k -> 0) + 1;
        attempts.put(ip, count);

        if (count > MAX_ATTEMPTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"Muitas tentativas. Tente novamente em 1 minuto.\"}");
            return;
        }

        filterChain.doFilter(request, response);

        // Não penaliza tentativas bem-sucedidas.
        if (response.getStatus() >= 200 && response.getStatus() < 300) {
            int current = attempts.get(ip, k -> 0);
            if (current > 0) {
                attempts.put(ip, current - 1);
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !PROTECTED_PATHS.contains(request.getRequestURI());
    }

    private static String resolveIp(HttpServletRequest request) {
        boolean trustProxy = Boolean.parseBoolean(
                System.getProperty("security.trust-proxy-headers", "false"));
        if (trustProxy) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    public void reset() {
        attempts.invalidateAll();
    }
}
