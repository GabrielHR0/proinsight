package com.prosup.proinsight.config;

import com.prosup.proinsight.AbstractIntegrationTest;
import com.prosup.proinsight.infrastructure.persistence.document.ApiKeyDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.ApiKeyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyIntegrationIT extends AbstractIntegrationTest {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @AfterEach
    void cleanUp() {
        apiKeyRepository.deleteAll();
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    @DisplayName("deve autenticar com API key salva no MongoDB")
    void apiKeyValidaAutentica() throws Exception {
        String rawKey = "pk_test_" + UUID.randomUUID().toString().replace("-", "");
        String hash = sha256(rawKey);
        var doc = new ApiKeyDocument(hash, "academia-it", Set.of("CLIENTES_LER"),
                Instant.now().plus(Duration.ofDays(30)), "it-test-key");
        apiKeyRepository.save(doc);

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + rawKey);
        var response = new MockHttpServletResponse();

        Authentication[] captured = {null};
        apiKeyAuthenticationFilter.doFilter(request, response, (req, res) -> {
            captured[0] = SecurityContextHolder.getContext().getAuthentication();
        });

        assertThat(captured[0]).isNotNull();
        assertThat(captured[0].isAuthenticated()).isTrue();
        assertThat(captured[0].getPrincipal()).isEqualTo("api-key:it-test-key");
        assertThat(captured[0].getAuthorities())
                .extracting(Object::toString)
                .containsExactly("CLIENTES_LER");
    }

    @Test
    @DisplayName("deve ignorar API key com hash inexistente no banco")
    void apiKeyInexistenteIgnorada() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer pk_nao_existe_no_banco");
        var response = new MockHttpServletResponse();

        apiKeyAuthenticationFilter.doFilter(request, response, (req, res) -> {});

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private static String sha256(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
