package com.prosup.proinsight.bootstrap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityProfileGuardTest {

    @Test
    @DisplayName("falha ao iniciar local contra MongoDB remoto")
    void localProfileRejectsRemoteMongo() {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        var guard = new SecurityProfileGuard(environment,
                "mongodb+srv://user:pass@cluster.mongodb.net/proinsight");

        assertThatThrownBy(() -> guard.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-local MongoDB");
    }

    @Test
    @DisplayName("permite local contra MongoDB local")
    void localProfileAcceptsLocalMongo() {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        var guard = new SecurityProfileGuard(environment,
                "mongodb://root:example@localhost:27017/proinsight_dev");

        assertThatCode(() -> guard.run(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("permite prod contra MongoDB remoto")
    void prodProfileAcceptsRemoteMongo() {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        var guard = new SecurityProfileGuard(environment,
                "mongodb+srv://user:pass@cluster.mongodb.net/proinsight");

        assertThatCode(() -> guard.run(null)).doesNotThrowAnyException();
    }
}
