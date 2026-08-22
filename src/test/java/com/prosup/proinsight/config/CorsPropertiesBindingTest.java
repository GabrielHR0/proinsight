package com.prosup.proinsight.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorsPropertiesBindingTest {

    @Test
    @DisplayName("deve criar record com origens definidas")
    void origensDefinidas() {
        var cors = new CorsProperties(List.of("http://localhost:3000", "https://app.example.com"));
        assertThat(cors.allowedOrigins()).containsExactly("http://localhost:3000", "https://app.example.com");
    }

    @Test
    @DisplayName("deve aceitar lista vazia")
    void listaVazia() {
        var cors = new CorsProperties(List.of());
        assertThat(cors.allowedOrigins()).isEmpty();
    }

    @Test
    @DisplayName("deve aceitar null como allowedOrigins")
    void nullAllowedOrigins() {
        var cors = new CorsProperties(null);
        assertThat(cors.allowedOrigins()).isNull();
    }

    @Test
    @DisplayName("dois records iguais devem ser considerados iguais")
    void equals() {
        var a = new CorsProperties(List.of("http://localhost:3000"));
        var b = new CorsProperties(List.of("http://localhost:3000"));
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
