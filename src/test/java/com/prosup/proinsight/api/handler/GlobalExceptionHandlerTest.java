package com.prosup.proinsight.api.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNoResourceFoundShouldReturn404ProblemJson() {
        var request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/rota-inexistente");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        var response = handler.handleNoResourceFound(
            new NoResourceFoundException(HttpMethod.GET, "/api/v1/rota-inexistente"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString())
            .startsWith("application/problem+json");

        var body = (Map<?, ?>) response.getBody();
        assertThat(body.get("type")).isEqualTo("proinsight://problems/not-found");
        assertThat(body.get("title")).isEqualTo("Not Found");
        assertThat(body.get("status")).isEqualTo(404);
        assertThat(body.get("detail")).isEqualTo("Rota não encontrada: /api/v1/rota-inexistente");
        assertThat(body.get("instance")).isEqualTo("/api/v1/rota-inexistente");
    }
}
