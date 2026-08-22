package com.prosup.proinsight.service;

import com.prosup.proinsight.config.LoginRateLimiterFilter;
import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.infrastructure.persistence.document.RoleDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.PasswordResetTokenRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.RefreshTokenRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private LoginRateLimiterFilter rateLimiter;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private RateLimitService rateLimitService;

    private static final String PW = "Test@123";
    private static final String ACADEMIA_A = "academia-a-it";
    private static final String ACADEMIA_X = "academia-x";

    private String roleFullAccessId;
    private String roleClientReaderId;
    private String adminUserId;

    @BeforeEach
    void setUp() {
        rateLimiter.reset();
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    }

    @AfterEach
    void cleanUp() {
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        rateLimitService.reset();
    }

    private void seed() {
        roleFullAccessId = roleRepository.save(
                new RoleDocument(null, "it_admin", null, Set.of(Permissao.values()))
        ).getId();

        roleClientReaderId = roleRepository.save(
                new RoleDocument(null, "it_reader", null, Set.of(Permissao.CLIENTES_LER))
        ).getId();

        adminUserId = createUser("admin", "admin@test.com", PW,
                Map.of(ACADEMIA_A, Set.of(roleFullAccessId)));

        createUser("limited", "limited@test.com", PW,
                Map.of(ACADEMIA_A, Set.of(roleClientReaderId)));
    }

    private String createUser(String userName, String email, String plainPassword,
                              Map<String, Set<String>> academiaRoles) {
        var doc = new UserDocument();
        doc.setUserName(userName);
        doc.setEmail(email);
        doc.setPassword(passwordEncoder.encode(plainPassword));
        doc.setActive(true);
        doc.setAcademiaRoles(academiaRoles);
        return userRepository.save(doc).getId();
    }

    private ResponseEntity<Map> login(String credential, String password) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(Map.of("login", credential, "password", password), headers);
        return restTemplate.exchange("/api/v1/auth/login", HttpMethod.POST, request, Map.class);
    }

    private HttpEntity<Void> bearerHeader(String token) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Void> bearerHeader(String token, String academiaId) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Academia-Id", academiaId);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Map> jsonBody(Map<String, Object> body) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    // ─── POST /api/v1/auth/login ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/auth/login → credenciais válidas → 200 + token + refresh")
    void login_sucesso() {
        seed();

        var response = login("admin@test.com", PW);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("token")).isNotNull();
        assertThat(response.getBody().get("refreshToken")).isNotNull();
        assertThat(response.getBody().get("tokenType")).isEqualTo("Bearer");
        assertThat(response.getBody().get("userId")).isEqualTo(adminUserId);
        assertThat(response.getBody().get("userName")).isEqualTo("admin");
        assertThat(response.getBody().get("email")).isEqualTo("admin@test.com");
        assertThat(response.getBody().get("academiaPermissoes")).isNotNull();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login → senha errada → 401")
    void login_senhaErrada() {
        seed();

        var response = login("admin@test.com", "wrong-password");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /api/v1/auth/login → username (não email) → 200")
    void login_comUsername() {
        seed();

        var response = login("admin", PW);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("token")).isNotNull();
        assertThat(response.getBody().get("userName")).isEqualTo("admin");
    }

    @Test
    @DisplayName("POST /api/v1/auth/login → email inexistente → 401")
    void login_emailInexistente() {
        seed();

        var response = login("unknown@test.com", PW);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /api/v1/auth/login → campos inválidos → 400")
    void login_camposInvalidos() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(Map.of("login", "", "password", ""), headers);
        var response = restTemplate.exchange("/api/v1/auth/login",
                HttpMethod.POST, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── POST /api/v1/auth/refresh ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/auth/refresh → refresh token válido → 200 + novo par")
    void refresh_sucesso() {
        seed();
        var loginResp = login("admin@test.com", PW);
        assertThat(loginResp.getBody()).isNotNull();
        String refreshToken = (String) loginResp.getBody().get("refreshToken");

        var response = restTemplate.postForEntity("/api/v1/auth/refresh",
                Map.of("refreshToken", refreshToken), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("token")).isNotNull();
        assertThat(response.getBody().get("refreshToken")).isNotNull();
        assertThat(response.getBody().get("refreshToken")).isNotEqualTo(refreshToken);
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh → refresh token reutilizado → 400 (já revogado)")
    void refresh_tokenReutilizado() {
        seed();
        var loginResp = login("admin@test.com", PW);
        assertThat(loginResp.getBody()).isNotNull();
        String refreshToken = (String) loginResp.getBody().get("refreshToken");

        restTemplate.postForEntity("/api/v1/auth/refresh",
                Map.of("refreshToken", refreshToken), Map.class);

        var response = restTemplate.postForEntity("/api/v1/auth/refresh",
                Map.of("refreshToken", refreshToken), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh → refresh token inválido → 400")
    void refresh_tokenInvalido() {
        var response = restTemplate.postForEntity("/api/v1/auth/refresh",
                Map.of("refreshToken", "id-que-nao-existe"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── GET /api/v1/auth/me ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/auth/me → autenticado → 200 + dados do usuário")
    void me_autenticado() {
        seed();
        var loginResp = login("admin@test.com", PW);
        assertThat(loginResp.getBody()).isNotNull();
        String token = (String) loginResp.getBody().get("token");

        var response = restTemplate.exchange(
                "/api/v1/auth/me", HttpMethod.GET, bearerHeader(token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("userId")).isEqualTo(adminUserId);
        assertThat(response.getBody().get("email")).isEqualTo("admin@test.com");
        assertThat(response.getBody().get("userName")).isEqualTo("admin");
        assertThat(response.getBody().get("academiaPermissoes")).isNotNull();
    }

    @Test
    @DisplayName("GET /api/v1/auth/me → não autenticado → 403")
    void me_naoAutenticado() {
        var response = restTemplate.getForEntity("/api/v1/auth/me", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /api/v1/auth/me → token inválido → 403")
    void me_tokenInvalido() {
        var response = restTemplate.exchange(
                "/api/v1/auth/me", HttpMethod.GET, bearerHeader("token.invalido"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── Controle de acesso (@PreAuthorize) ──────────────────────────────────

    @Test
    @DisplayName("@PreAuthorize → sem token → 403")
    void preAuthorize_semToken() {
        seed();

        var response = restTemplate.getForEntity("/api/v1/avaliacoes/protocolos", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("@PreAuthorize → com token + permissão → 200")
    void preAuthorize_comPermissao() {
        seed();
        var loginResp = login("admin@test.com", PW);
        assertThat(loginResp.getBody()).isNotNull();
        String token = (String) loginResp.getBody().get("token");

        var response = restTemplate.exchange(
                "/api/v1/avaliacoes/protocolos", HttpMethod.GET,
                bearerHeader(token, ACADEMIA_A), List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("@PreAuthorize → sem permissão → 403")
    void preAuthorize_semPermissao() {
        seed();
        var loginResp = login("limited@test.com", PW);
        assertThat(loginResp.getBody()).isNotNull();
        String token = (String) loginResp.getBody().get("token");

        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Academia-Id", ACADEMIA_A);
        var request = new HttpEntity<>(Map.of(
                "userName", "new-user",
                "email", "new@test.com",
                "password", "StrongPass@123",
                "academiaRoles", Map.of(ACADEMIA_A, Set.of(roleClientReaderId))
        ), headers);

        var response = restTemplate.exchange(
                "/api/v1/users", HttpMethod.POST, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("@PreAuthorize → academia sem acesso → 403")
    void preAuthorize_academiaSemAcesso() {
        seed();
        var loginResp = login("limited@test.com", PW);
        assertThat(loginResp.getBody()).isNotNull();
        String token = (String) loginResp.getBody().get("token");

        var response = restTemplate.exchange(
                "/api/v1/avaliacoes/protocolos", HttpMethod.GET,
                bearerHeader(token, ACADEMIA_X), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── Rate limiting ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Rate limiting → 5 falhas → 6ª requisição 429")
    void rateLimit_limiteDeTentativas() {
        seed();

        for (int i = 0; i < 5; i++) {
            var r = login("nao-existe@test.com", "senha-errada");
            assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        var response = login("nao-existe@test.com", "senha-errada");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    // ─── Troca de senha ─────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /me/password → senha atual correta → 204; login com nova senha → 200; antiga → 401")
    void changePassword_sucesso() {
        seed();
        var loginResp = login("admin@test.com", PW);
        assertThat(loginResp.getBody()).isNotNull();
        String token = (String) loginResp.getBody().get("token");

        var changePwResponse = restTemplate.exchange(
                "/api/v1/auth/me/password", HttpMethod.PUT,
                bearerJsonHeader(token, Map.of("currentPassword", PW, "newPassword", "NovaSenha@123")),
                Void.class);

        assertThat(changePwResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var loginOldPw = login("admin@test.com", PW);
        assertThat(loginOldPw.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        var loginNewPw = login("admin@test.com", "NovaSenha@123");
        assertThat(loginNewPw.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginNewPw.getBody().get("token")).isNotNull();
    }

    @Test
    @DisplayName("PUT /me/password → senha atual incorreta → 400")
    void changePassword_senhaAtualIncorreta() {
        seed();
        var loginResp = login("admin@test.com", PW);
        assertThat(loginResp.getBody()).isNotNull();
        String token = (String) loginResp.getBody().get("token");

        var response = restTemplate.exchange(
                "/api/v1/auth/me/password", HttpMethod.PUT,
                bearerJsonHeader(token, Map.of("currentPassword", "wrong", "newPassword", "NovaSenha@123")),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── Forgot/Reset password ──────────────────────────────────────────────

    @Test
    @DisplayName("Forgot + Reset → token obtido → reset bem-sucedido → login com nova senha")
    void forgotReset_sucesso() {
        seed();
        var forgotResponse = restTemplate.postForEntity(
                "/api/v1/auth/forgot-password",
                Map.of("email", "admin@test.com"), Map.class);

        assertThat(forgotResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String resetToken = (String) forgotResponse.getBody().get("resetToken");

        var resetResponse = restTemplate.postForEntity(
                "/api/v1/auth/reset-password",
                Map.of("token", resetToken, "newPassword", "Resetada@123"),
                Void.class);

        assertThat(resetResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var loginOldPw = login("admin@test.com", PW);
        assertThat(loginOldPw.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        var loginNewPw = login("admin@test.com", "Resetada@123");
        assertThat(loginNewPw.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Forgot → email inexistente → 200 sem token (anti-enumeração)")
    void forgotPassword_emailInexistente() {
        var response = restTemplate.postForEntity(
                "/api/v1/auth/forgot-password",
                Map.of("email", "naoexiste@test.com"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("Reset → token inválido → 404")
    void resetPassword_tokenInvalido() {
        var response = restTemplate.postForEntity(
                "/api/v1/auth/reset-password",
                Map.of("token", "token-falso", "newPassword", "NovaSenha@123"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Reset → token reutilizado → 404")
    void resetPassword_tokenReutilizado() {
        seed();
        var forgotResponse = restTemplate.postForEntity(
                "/api/v1/auth/forgot-password",
                Map.of("email", "admin@test.com"), Map.class);
        String resetToken = (String) forgotResponse.getBody().get("resetToken");

        restTemplate.postForEntity(
                "/api/v1/auth/reset-password",
                Map.of("token", resetToken, "newPassword", "Resetada@123"),
                Void.class);

        var response = restTemplate.postForEntity(
                "/api/v1/auth/reset-password",
                Map.of("token", resetToken, "newPassword", "Outra@123"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ─── Rate limit por identidade ──────────────────────────────────────────

    @Test
    @DisplayName("Rate limit por identidade → 5 falhas com IP resetado → 6ª 429")
    void rateLimit_identidadeLogin() {
        seed();
        for (int i = 0; i < 5; i++) {
            rateLimiter.reset();
            var r = login("admin@test.com", "senha-errada");
            assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
        rateLimiter.reset();
        var response = login("admin@test.com", "senha-errada");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("Refresh token revogado após reset de senha")
    void resetPassword_revogaRefreshTokens() {
        seed();
        var loginResp = login("admin@test.com", PW);
        assertThat(loginResp.getBody()).isNotNull();
        String refreshToken = (String) loginResp.getBody().get("refreshToken");

        var forgotResp = restTemplate.postForEntity(
                "/api/v1/auth/forgot-password",
                Map.of("email", "admin@test.com"), Map.class);
        String resetToken = (String) forgotResp.getBody().get("resetToken");
        restTemplate.postForEntity(
                "/api/v1/auth/reset-password",
                Map.of("token", resetToken, "newPassword", "Resetada@123"),
                Void.class);

        var refreshOld = restTemplate.postForEntity(
                "/api/v1/auth/refresh",
                Map.of("refreshToken", refreshToken), Map.class);

        assertThat(refreshOld.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private HttpEntity<?> bearerJsonHeader(String token, Map<String, String> body) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
