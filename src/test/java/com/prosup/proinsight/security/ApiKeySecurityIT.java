package com.prosup.proinsight.security;

import com.prosup.proinsight.infrastructure.persistence.document.ApiKeyDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OWASP API Security Top 10 — API2: Broken Authentication (API keys)
 * e API1: BOLA (chave de academia alheia).
 */
class ApiKeySecurityIT extends SecurityITBase {

    @Test
    @DisplayName("API key inexistente → não autentica (403 no endpoint protegido)")
    void apiKeyInvalida_rejeitada() {
        var response = callWithRawKey("pk_" + UUID.randomUUID(), ACADEMIA_A);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("API key expirada → rejeitada")
    void apiKeyExpirada_rejeitada() {
        String raw = "pk_" + UUID.randomUUID().toString().replace("-", "");
        saveApiKey(raw, ACADEMIA_A, Set.of("CLIENTES_LER"),
                Instant.now().minus(Duration.ofHours(1)));

        var response = callWithRawKey(raw, ACADEMIA_A);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("API key inativa (active=false) → rejeitada")
    void apiKeyInativa_rejeitada() {
        String raw = "pk_" + UUID.randomUUID().toString().replace("-", "");
        var doc = new ApiKeyDocument(sha256(raw), ACADEMIA_A, Set.of("CLIENTES_LER"),
                Instant.now().plus(Duration.ofDays(30)), "chave-inativa");
        doc.setActive(false);
        apiKeyRepository.save(doc);

        var response = callWithRawKey(raw, ACADEMIA_A);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("API key válida da academia A → acessa endpoints da academia A")
    void apiKeyValida_acessaPropriaAcademia() {
        String raw = "pk_" + UUID.randomUUID().toString().replace("-", "");
        saveApiKey(raw, ACADEMIA_A, Set.of("CLIENTES_LER"));

        var response = callWithRawKey(raw, ACADEMIA_A);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("API key da academia A usada com header X-Academia-Id=B → 403")
    void apiKeyDeOutraAcademia_isolamento() {
        String raw = "pk_" + UUID.randomUUID().toString().replace("-", "");
        saveApiKey(raw, ACADEMIA_A, Set.of("CLIENTES_LER"));

        var response = callWithRawKey(raw, ACADEMIA_B);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private void saveApiKey(String raw, String academiaId, Set<String> permissions) {
        apiKeyRepository.save(new ApiKeyDocument(
                sha256(raw), academiaId, permissions,
                Instant.now().plus(Duration.ofDays(30)), "it-key"));
    }

    private void saveApiKey(String raw, String academiaId, Set<String> permissions,
                            Instant expiresAt) {
        apiKeyRepository.save(new ApiKeyDocument(
                sha256(raw), academiaId, permissions, expiresAt, "it-key"));
    }

    private ResponseEntity<String> callWithRawKey(String rawKey, String academiaId) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(rawKey);
        if (academiaId != null) {
            headers.set("X-Academia-Id", academiaId);
        }
        return restTemplate.exchange("/api/v1/clientes", HttpMethod.GET,
                new org.springframework.http.HttpEntity<Void>(headers), String.class);
    }

    private static String sha256(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}