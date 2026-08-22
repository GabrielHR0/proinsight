package com.prosup.proinsight.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.prosup.proinsight.infrastructure.persistence.document.ApiKeyDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_PREFIX = "pk_";

    private static final int MAX_FAILURES_PER_WINDOW = 10;
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(1);
    private static final int MAX_CONSECUTIVE_FAILURES_PER_KEY = 5;

    private final ApiKeyRepository apiKeyRepository;

    private final Cache<String, Integer> failuresByIp = Caffeine.newBuilder()
            .expireAfterWrite(FAILURE_WINDOW)
            .build();

    private final Cache<String, Integer> consecutiveFailuresByKey = Caffeine.newBuilder()
            .maximumSize(10_000)
            .build();

    public ApiKeyAuthenticationFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer " + API_KEY_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String rawKey = header.substring(7);

        String ip = request.getRemoteAddr();
        if (isIpBlocked(ip)) {
            writeTooManyRequests(response);
            return;
        }

        String keyHash = sha256(rawKey);

        var apiKeyOpt = apiKeyRepository.findByKeyHashAndActiveTrue(keyHash);
        if (apiKeyOpt.isEmpty()) {
            recordFailure(ip);
            recordKeyFailure(rawKey);
            chain.doFilter(request, response);
            return;
        }

        ApiKeyDocument apiKey = apiKeyOpt.get();
        if (!apiKey.isValid()) {
            chain.doFilter(request, response);
            return;
        }

        resetFailures(ip, rawKey);

        var authorities = apiKey.getPermissions() != null
                ? apiKey.getPermissions().stream().map(SimpleGrantedAuthority::new).toList()
                : List.<SimpleGrantedAuthority>of();

        var auth = new UsernamePasswordAuthenticationToken(
                "api-key:" + apiKey.getLabel(), null, authorities);

        TenantContext.setAcademiaId(apiKey.getAcademiaId());

        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private boolean isIpBlocked(String ip) {
        return failuresByIp.get(ip, k -> 0) > MAX_FAILURES_PER_WINDOW;
    }

    private void recordFailure(String ip) {
        failuresByIp.put(ip, failuresByIp.get(ip, k -> 0) + 1);
    }

    private void resetFailures(String ip, String rawKey) {
        failuresByIp.invalidate(ip);
        consecutiveFailuresByKey.invalidate(sha256(rawKey));
    }

    private void recordKeyFailure(String rawKey) {
        String keyHash = sha256(rawKey);
        int failures = consecutiveFailuresByKey.get(keyHash, k -> 0) + 1;
        consecutiveFailuresByKey.put(keyHash, failures);

        if (failures >= MAX_CONSECUTIVE_FAILURES_PER_KEY) {
            apiKeyRepository.findByKeyHashAndActiveTrue(keyHash)
                    .ifPresent(apiKey -> {
                        apiKey.setActive(false);
                        apiKeyRepository.save(apiKey);
                    });
            consecutiveFailuresByKey.invalidate(keyHash);
        }
    }

    private static void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"message\":\"Muitas tentativas com API key. Tente novamente em 1 minuto.\"}");
    }

    private static String sha256(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

