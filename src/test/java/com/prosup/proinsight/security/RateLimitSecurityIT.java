package com.prosup.proinsight.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OWASP API Security Top 10 — API4: Unrestricted Resource Consumption
 * e API6: rate limiting.
 *
 * Limite de tentativas de login por IP/identidade e limite de
 * requisições por academia (tenant rate limiter).
 */
@TestPropertySource(properties = "security.tenant-rate-limit.max-per-minute=8")
class RateLimitSecurityIT extends SecurityITBase {

    @Test
    @DisplayName("6 tentativas de login com senha errada → 429 na excedência")
    void loginRateLimit_429() {
        createUserInAcademia("v1timad", "v1timad@sec.com", ACADEMIA_A);

        ResponseEntity<Map> last = null;
        for (int i = 0; i < 7; i++) {
            last = attemptLogin("v1timad@sec.com", "SenhaErrada!");
        }

        assertThat(last).isNotNull();
        assertThat(last.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("Login correto após falhas reseta parcialmente (sucesso não é 429)")
    void loginCorreto_aposErros_naoBloqueia() {
        createUserWithAcademia("ok@sec.gov", ACADEMIA_A);

        for (int i = 0; i < 4; i++) {
            attemptLogin("ok@sec.gov", "SenhaErrada!");
        }

        var response = attemptLogin("ok@sec.gov", SecurityITBase.PW);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Acima do limite de requisições por academia (tenant) → 429")
    void tenantRateLimit_429() {
        createUserWithAcademia("admina@rate.com", ACADEMIA_A);
        String token = login("admina@rate.com");

        ResponseEntity<String> last = null;
        for (int i = 0; i < 10; i++) {
            var headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("X-Academia-Id", ACADEMIA_A);
            last = restTemplate.exchange("/api/v1/clientes", HttpMethod.GET,
                    new HttpEntity<Void>(headers), String.class);
        }

        assertThat(last).isNotNull();
        assertThat(last.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("Headers X-RateLimit presentes nas respostas autenticadas")
    void rateLimitHeadersPresentes() {
        createUserWithAcademia("headers@rate.com", ACADEMIA_A);
        String token = login("headers@rate.com");

        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Academia-Id", ACADEMIA_A);
        var response = restTemplate.exchange("/api/v1/clientes", HttpMethod.GET,
                new HttpEntity<Void>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("8");
        assertThat(response.getHeaders().getFirst("X-RateLimit-Remaining")).isNotBlank();
    }

    private void createUserWithAcademia(String email, String academiaId) {
        createUserInAcademia("rate-" + UUID.randomUUID().toString().substring(0, 8), email, academiaId);
    }

    private ResponseEntity<Map> attemptLogin(String login, String password) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(Map.of("login", login, "password", password), headers);
        return restTemplate.exchange("/api/v1/auth/login", HttpMethod.POST,
                request, Map.class);
    }
}