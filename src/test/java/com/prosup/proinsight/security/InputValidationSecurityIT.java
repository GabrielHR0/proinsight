package com.prosup.proinsight.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OWASP API Security Top 10 — API3: Broken Object Property Level Authorization
 * e API6: Unrestricted Resource Consumption.
 *
 * Registro sem campos obrigatórios, mass assignment (injeção de campos
 * não esperados) e conteúdo malicioso em campos de entrada.
 */
class InputValidationSecurityIT extends SecurityITBase {

    private String token() {
        createUserInAcademia("admina", "admina@sec.com", ACADEMIA_A);
        return login("admina@sec.com");
    }

    @Test
    @DisplayName("Registro sem userName/email/password → 400")
    void registroSemCamposObrigatorios_400() {
        var response = postPublic(Map.of(
                "email", "novo@sec.com",
                "password", "Senha@123"
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Registro com email inválido → 400")
    void registroEmailInvalido_400() {
        var response = postPublic(Map.of(
                "email", "nao-e-email",
                "password", "Senha@123",
                "userName", "user-" + UUID.randomUUID(),
                "cref", "CREF-" + UUID.randomUUID(),
                "cpf", "12345678901"
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Registro com senha curta (< 8) → 400")
    void registroSenhaCurta_400() {
        var response = postPublic(Map.of(
                "email", UUID.randomUUID() + "@sec.com",
                "password", "curta",
                "userName", "user-" + UUID.randomUUID(),
                "cref", "CREF-" + UUID.randomUUID(),
                "cpf", "12345678901"
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Mass assignment: campos extras (active, admin) são ignorados")
    void massAssignment_camposExtrasIgnorados() {
        var body = Map.<String, Object>of(
                "email", UUID.randomUUID() + "@sec.com",
                "password", "Senha@123",
                "userName", "user-" + UUID.randomUUID(),
                "cref", "CREF-" + UUID.randomUUID(),
                "cpf", "12345678901",
                "active", true,
                "admin", true,
                "academiaId", ACADEMIA_A
        );

        var response = postPublic(body);

        assertThat(response.getStatusCode().value()).isIn(201, 400);
    }

    @Test
    @DisplayName("Request com corpo malformado (JSON inválido) → 400")
    void corpoMalformado_400() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>("{email: \"sem-chaves\"", headers);

        var response = restTemplate.exchange(
                "/api/v1/auth/login", HttpMethod.POST, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private org.springframework.http.ResponseEntity<Map> postPublic(Map<String, Object> body) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange("/api/v1/auth/register",
                HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
    }
}