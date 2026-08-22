package com.prosup.proinsight.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitServiceTest {

    private final RateLimitService service = new RateLimitService();

    @Test
    @DisplayName("Chave limpa não está bloqueada")
    void chaveLivre() {
        assertThat(service.isBlocked("admin@test.com")).isFalse();
    }

    @Test
    @DisplayName("5 falhas bloqueiam a 6ª tentativa")
    void bloqueiaAposCincoFalhas() {
        for (int i = 0; i < RateLimitService.MAX_ATTEMPTS - 1; i++) {
            service.recordFailure("admin@test.com");
            assertThat(service.isBlocked("admin@test.com")).isFalse();
        }

        service.recordFailure("admin@test.com");
        assertThat(service.isBlocked("admin@test.com")).isTrue();
    }

    @Test
    @DisplayName("Sucesso decrementa o contador de falhas")
    void sucessoDecrementa() {
        service.recordFailure("admin@test.com");
        service.recordFailure("admin@test.com");
        service.recordSuccess("admin@test.com");

        service.recordFailure("admin@test.com");
        service.recordFailure("admin@test.com");
        assertThat(service.isBlocked("admin@test.com")).isFalse();
    }

    @Test
    @DisplayName("Chaves diferentes não interferem entre si")
    void chavesIsoladas() {
        service.recordFailure("a@test.com");
        service.recordFailure("a@test.com");
        service.recordFailure("a@test.com");
        service.recordFailure("a@test.com");
        service.recordFailure("a@test.com");

        assertThat(service.isBlocked("b@test.com")).isFalse();
    }

    @Test
    @DisplayName("reset limpa todos os contadores")
    void resetLimpa() {
        for (int i = 0; i < RateLimitService.MAX_ATTEMPTS; i++) {
            service.recordFailure("admin@test.com");
        }
        service.reset();

        assertThat(service.isBlocked("admin@test.com")).isFalse();
    }
}
