package com.prosup.proinsight.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecurityHeadersIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("deve incluir X-Content-Type-Options: nosniff")
    void contentTypeOptionsHeader() {
        var response = sendInvalidLogin();

        assertThat(response.getHeaders().get("X-Content-Type-Options"))
                .containsExactly("nosniff");
    }

    @Test
    @DisplayName("deve incluir X-Frame-Options: DENY")
    void frameOptionsHeader() {
        var response = sendInvalidLogin();

        assertThat(response.getHeaders().get("X-Frame-Options"))
                .containsExactly("DENY");
    }

    private ResponseEntity<Map> sendInvalidLogin() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(Map.of("email", "", "password", ""), headers);
        return restTemplate.exchange(
                "/api/v1/auth/login", HttpMethod.POST, request, Map.class);
    }
}
