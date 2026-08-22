package com.prosup.proinsight.config;

import com.prosup.proinsight.infrastructure.persistence.document.ApiKeyDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Proxy;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyAuthenticationFilterTest {

    private static final String ACADEMIA_ID = "academia-test";
    private static final String RAW_KEY = "pk_test_" + UUID.randomUUID().toString().replace("-", "");
    private static final String KEY_HASH = sha256(RAW_KEY);

    private ApiKeyDocument validDoc;

    @BeforeEach
    void setUp() {
        validDoc = new ApiKeyDocument(KEY_HASH, ACADEMIA_ID,
                Set.of("CLIENTES_LER", "AVALIACOES_LER"),
                Instant.now().plus(Duration.ofDays(30)), "test-key");
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @SuppressWarnings("unchecked")
    private static ApiKeyRepository createRepo(String keyHash, ApiKeyDocument doc) {
        return (ApiKeyRepository) Proxy.newProxyInstance(
                ApiKeyRepository.class.getClassLoader(),
                new Class<?>[]{ApiKeyRepository.class},
                (proxy, method, args) -> {
                    if ("findByKeyHashAndActiveTrue".equals(method.getName())) {
                        String kh = (String) args[0];
                        return kh.equals(keyHash) ? Optional.ofNullable(doc) : Optional.empty();
                    }
                    throw new UnsupportedOperationException(
                            "Unexpected method call: " + method.getName());
                });
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

    @Nested
    @DisplayName("API key valida")
    class ChaveValida {

        @Test
        @DisplayName("deve definir autenticacao com permissoes corretas")
        void autenticaComPermissoes() throws Exception {
            var filter = new ApiKeyAuthenticationFilter(createRepo(KEY_HASH, validDoc));
            var request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + RAW_KEY);
            var response = new MockHttpServletResponse();

            Authentication[] captured = {null};
            filter.doFilter(request, response, (req, res) -> {
                captured[0] = SecurityContextHolder.getContext().getAuthentication();
            });

            assertThat(captured[0]).isNotNull();
            assertThat(captured[0].getPrincipal()).isEqualTo("api-key:test-key");
            assertThat(captured[0].getAuthorities())
                    .extracting(Object::toString)
                    .containsExactlyInAnyOrder("CLIENTES_LER", "AVALIACOES_LER");
        }

        @Test
        @DisplayName("deve configurar TenantContext com academiaId da chave")
        void configuraTenantContext() throws Exception {
            var filter = new ApiKeyAuthenticationFilter(createRepo(KEY_HASH, validDoc));
            var request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + RAW_KEY);
            var response = new MockHttpServletResponse();

            String[] capturedTenant = {null};
            filter.doFilter(request, response, (req, res) -> {
                capturedTenant[0] = TenantContext.getAcademiaId();
            });

            assertThat(capturedTenant[0]).isEqualTo(ACADEMIA_ID);
        }
    }

    @Nested
    @DisplayName("Casos de autenticacao negada")
    class AutenticacaoNegada {

        @Test
        @DisplayName("deve ignorar request sem cabecalho Authorization")
        void semAuthorization() throws Exception {
            var filter = new ApiKeyAuthenticationFilter(createRepo(KEY_HASH, validDoc));
            var request = new MockHttpServletRequest();
            var response = new MockHttpServletResponse();

            filter.doFilter(request, response, (req, res) -> {});

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("deve ignorar request sem prefixo pk_ no token")
        void semPrefixoPk() throws Exception {
            var filter = new ApiKeyAuthenticationFilter(createRepo(KEY_HASH, validDoc));
            var request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer token-comum-jwt");
            var response = new MockHttpServletResponse();

            filter.doFilter(request, response, (req, res) -> {});

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("deve ignorar request com chave inexistente")
        void chaveInexistente() throws Exception {
            var filter = new ApiKeyAuthenticationFilter(createRepo("hash_inexistente", null));
            var request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer pk_chave_inexistente");
            var response = new MockHttpServletResponse();

            filter.doFilter(request, response, (req, res) -> {});

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("deve ignorar request com chave expirada")
        void chaveExpirada() throws Exception {
            var hash = sha256("pk_expirada_key");
            var doc = new ApiKeyDocument(hash, ACADEMIA_ID, Set.of("CLIENTES_LER"),
                    Instant.now().minus(Duration.ofDays(1)), "expirada");
            var filter = new ApiKeyAuthenticationFilter(createRepo(hash, doc));

            var request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer pk_expirada_key");
            var response = new MockHttpServletResponse();

            filter.doFilter(request, response, (req, res) -> {});

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("deve ignorar request com chave inativa")
        void chaveInativa() throws Exception {
            var hash = sha256("pk_inativa_key");
            var doc = new ApiKeyDocument(hash, ACADEMIA_ID, Set.of("CLIENTES_LER"),
                    Instant.now().plus(Duration.ofDays(30)), "inativa");
            doc.setActive(false);
            var filter = new ApiKeyAuthenticationFilter(createRepo(hash, doc));

            var request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer pk_inativa_key");
            var response = new MockHttpServletResponse();

            filter.doFilter(request, response, (req, res) -> {});

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }
}
