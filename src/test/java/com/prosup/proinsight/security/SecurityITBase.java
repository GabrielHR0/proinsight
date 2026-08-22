package com.prosup.proinsight.security;

import com.prosup.proinsight.config.LoginRateLimiterFilter;
import com.prosup.proinsight.config.TenantRateLimiterFilter;
import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.infrastructure.persistence.document.RoleDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.ApiKeyRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.RefreshTokenRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.RevokedTokenRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import com.prosup.proinsight.service.RateLimitService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Set;

/**
 * Base para testes de segurança (OWASP API Security Top 10).
 *
 * Cada classe filha roda contra a API real (RANDOM_PORT) e limpa as
 * coleções afetadas em cada execução para manter o isolamento.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class SecurityITBase {

    public static final String PW = "Test@123";
    public static final String ACADEMIA_A = "sec-academia-a";
    public static final String ACADEMIA_B = "sec-academia-b";

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected RoleRepository roleRepository;
    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;
    @Autowired
    protected RevokedTokenRepository revokedTokenRepository;
    @Autowired
    protected ClienteRepository clienteRepository;
    @Autowired
    protected ApiKeyRepository apiKeyRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected LoginRateLimiterFilter loginRateLimiter;
    @Autowired
    protected RateLimitService rateLimitService;
    @Autowired
    protected TenantRateLimiterFilter tenantRateLimiter;

    protected String roleFullAccessId;

    @BeforeEach
    void setUpBase() {
        loginRateLimiter.reset();
        rateLimitService.reset();
        tenantRateLimiter.reset();

        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());

        cleanCollections();

        roleFullAccessId = roleRepository.save(
                new RoleDocument(null, "sec_role_full", null, Set.of(Permissao.values()))
        ).getId();
    }

    @AfterEach
    void tearDownBase() {
        cleanCollections();
    }

    private void cleanCollections() {
        refreshTokenRepository.deleteAll();
        revokedTokenRepository.deleteAll();
        clienteRepository.deleteAll();
        apiKeyRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    // ── Helpers de dados ───────────────────────────────────────────────────

    protected String createUser(String userName, String email, Map<String, Set<String>> academiaRoles) {
        var doc = new UserDocument();
        doc.setUserName(userName);
        doc.setEmail(email);
        doc.setPassword(passwordEncoder.encode(PW));
        doc.setActive(true);
        doc.setAcademiaRoles(academiaRoles);
        userRepository.save(doc);
        return doc.getId();
    }

    protected String createUserInAcademia(String userName, String email, String academiaId) {
        return createUser(userName, email, Map.of(academiaId, Set.of(roleFullAccessId)));
    }

    protected String roleWith(String academiaId, Set<Permissao> permissoes) {
        return roleRepository.save(
                new RoleDocument(null, "role_" + academiaId + "_" + permissoes.hashCode(),
                        null, permissoes)
        ).getId();
    }

    // ── Helpers HTTP ───────────────────────────────────────────────────────

    protected String login(String credential) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(Map.of("login", credential, "password", PW), headers);
        var response = restTemplate.exchange("/api/v1/auth/login", HttpMethod.POST, request, Map.class);
        var status = response.getStatusCode().value();
        if (status != 200) {
            throw new IllegalStateException("Login " + credential + " falhou com status " + status);
        }
        return (String) response.getBody().get("token");
    }

    protected ResponseEntity<Map> getJson(String url, String token, String academiaId) {
        return restTemplate.exchange(url, HttpMethod.GET, bearer(token, academiaId), Map.class);
    }

    protected ResponseEntity<String> getString(String url, String token, String academiaId) {
        return restTemplate.exchange(url, HttpMethod.GET, bearer(token, academiaId), String.class);
    }

    protected ResponseEntity<Map> getJson(String url, HttpEntity<Void> request) {
        return restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
    }

    protected ResponseEntity<Map> postJson(String url, Object body, String token, String academiaId) {
        return restTemplate.exchange(url, HttpMethod.POST,
                withJsonObject(body, token, academiaId), Map.class);
    }

    protected ResponseEntity<Map> putJson(String url, Object body, String token, String academiaId) {
        return restTemplate.exchange(url, HttpMethod.PUT,
                withJsonObject(body, token, academiaId), Map.class);
    }

    protected ResponseEntity<Map> deleteJson(String url, String token, String academiaId) {
        return restTemplate.exchange(url, HttpMethod.DELETE, bearer(token, academiaId), Map.class);
    }

    protected HttpEntity<Void> bearer(String token, String academiaId) {
        var headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        if (academiaId != null) {
            headers.set("X-Academia-Id", academiaId);
        }
        return new HttpEntity<>(headers);
    }

    protected HttpEntity<Object> withJsonObject(Object body, String token, String academiaId) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        if (academiaId != null) {
            headers.set("X-Academia-Id", academiaId);
        }
        return new HttpEntity<>(body, headers);
    }
}