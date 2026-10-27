package com.prosup.proinsight.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    @Test
    @DisplayName("ignora X-Forwarded-For quando o proxy não é confiável")
    void ignoresForwardedHeadersWhenUntrusted() {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("X-Forwarded-For", "198.51.100.20, 10.0.0.1");

        assertThat(ClientIpResolver.resolve(request, false))
                .isEqualTo("203.0.113.10");
    }

    @Test
    @DisplayName("usa o IP anexado pelo proxy confiável no fim de X-Forwarded-For")
    void usesLastForwardedForWhenTrusted() {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "198.51.100.20, 10.0.0.5");

        assertThat(ClientIpResolver.resolve(request, true))
                .isEqualTo("10.0.0.5");
    }

    @Test
    @DisplayName("ignora IP forjado pelo cliente à esquerda de X-Forwarded-For")
    void ignoresClientSuppliedForwardedForPrefix() {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.99, 198.51.100.20");

        assertThat(ClientIpResolver.resolve(request, true))
                .isEqualTo("198.51.100.20");
    }
}
