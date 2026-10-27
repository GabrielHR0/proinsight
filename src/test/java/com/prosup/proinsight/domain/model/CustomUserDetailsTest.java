package com.prosup.proinsight.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    @DisplayName("Conta com lockedUntil no futuro nao deve ser considerada non-locked")
    void contaBloqueadaNaoDeveSerNonLocked() {
        var user = new User();
        user.setLockedUntil(Instant.now().plusSeconds(300));

        var details = new CustomUserDetails(user, List.of(), Map.of());

        assertThat(details.isAccountNonLocked()).isFalse();
    }

    @Test
    @DisplayName("Conta sem bloqueio ativo deve ser considerada non-locked")
    void contaSemBloqueioDeveSerNonLocked() {
        var user = new User();
        user.setLockedUntil(Instant.now().minusSeconds(300));

        var details = new CustomUserDetails(user, List.of(), Map.of());

        assertThat(details.isAccountNonLocked()).isTrue();
    }
}
