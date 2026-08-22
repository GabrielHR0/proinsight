package com.prosup.proinsight.service;

import com.prosup.proinsight.config.LoginRateLimiterFilter;
import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import com.prosup.proinsight.infrastructure.persistence.document.RoleDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TenantIsolationIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LoginRateLimiterFilter rateLimiter;

    private static final String PW = "Test@123";
    private static final String ACADEMIA_A = "academia-a-isol";
    private static final String ACADEMIA_B = "academia-b-isol";

    private String roleFullAccessId;
    private String clienteAId;
    private String clienteBId;

    @BeforeEach
    void setUp() {
        rateLimiter.reset();
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());

        refreshTokenRepository.deleteAll();
        clienteRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleFullAccessId = roleRepository.save(
                new RoleDocument(null, "it_isol_admin", null, Set.of(Permissao.values()))
        ).getId();

        createUser("admin-a", "admina@test.com", Map.of(ACADEMIA_A, Set.of(roleFullAccessId)));
        createUser("admin-b", "adminb@test.com", Map.of(ACADEMIA_B, Set.of(roleFullAccessId)));

        clienteAId = clienteRepository.save(cliente(ACADEMIA_A, "Cliente da Academia A")).getId();
        clienteBId = clienteRepository.save(cliente(ACADEMIA_B, "Cliente da Academia B")).getId();
    }

    @AfterEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        clienteRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    private void createUser(String userName, String email, Map<String, Set<String>> academiaRoles) {
        var doc = new UserDocument();
        doc.setUserName(userName);
        doc.setEmail(email);
        doc.setPassword(passwordEncoder.encode(PW));
        doc.setActive(true);
        doc.setAcademiaRoles(academiaRoles);
        userRepository.save(doc);
    }

    private ClienteDocument cliente(String academiaId, String fullName) {
        return new ClienteDocument(
                null,
                fullName,
                fullName.replace(" ", "") + "@test.com",
                "11999990000",
                "12345678901",
                LocalDate.of(1990, 1, 1),
                Sexo.MASCULINO,
                null,
                academiaId,
                null,
                true
        );
    }

    private String login(String credential) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(Map.of("login", credential, "password", PW), headers);
        var response = restTemplate.exchange("/api/v1/auth/login", HttpMethod.POST, request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) response.getBody().get("token");
    }

    private HttpEntity<Void> bearer(String token) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Void> bearer(String token, String academiaId) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Academia-Id", academiaId);
        return new HttpEntity<>(headers);
    }

    @Test
    @DisplayName("GET /clientes → header da propria academia → 200 e ve apenas seus clientes")
    void lista_somenteClientesDaAcademia() {
        String token = login("admina@test.com");

        var response = restTemplate.exchange(
                "/api/v1/clientes", HttpMethod.GET, bearer(token, ACADEMIA_A), List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        List<Map<String, Object>> clientes = response.getBody();
        assertThat(clientes).extracting(c -> c.get("fullName"))
                .containsExactly("Cliente da Academia A");
    }

    @Test
    @DisplayName("GET /clientes → header de academia alheia → 403 (filtro de tenant)")
    void lista_headerAcademiaAlheia_forbidden() {
        String token = login("admina@test.com");

        var response = restTemplate.exchange(
                "/api/v1/clientes", HttpMethod.GET, bearer(token, ACADEMIA_B), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /clientes → sem X-Academia-Id → 403")
    void lista_semHeader_forbidden() {
        String token = login("admina@test.com");

        var response = restTemplate.exchange(
                "/api/v1/clientes", HttpMethod.GET, bearer(token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /clientes/{id} → cliente de OUTRA academia → nao vaza (404)")
    void findById_clienteDeOutraAcademia_naoVaza() {
        String token = login("admina@test.com");

        var response = restTemplate.exchange(
                "/api/v1/clientes/" + clienteBId, HttpMethod.GET,
                bearer(token, ACADEMIA_A), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /clientes/{id} → cliente da propria academia → 200")
    void findById_clienteDaPropriaAcademia_ok() {
        String token = login("admina@test.com");

        var response = restTemplate.exchange(
                "/api/v1/clientes/" + clienteAId, HttpMethod.GET,
                bearer(token, ACADEMIA_A), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("fullName")).isEqualTo("Cliente da Academia A");
    }

    @Test
    @DisplayName("PUT /clientes/{id} → tentativa de mover para academia alheia → 403")
    void update_moverParaAcademiaAlheia_forbidden() {
        String token = login("admina@test.com");

        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Academia-Id", ACADEMIA_A);
        headers.setContentType(MediaType.APPLICATION_JSON);
        var body = Map.of(
                "fullName", "Cliente da Academia A",
                "email", "clientea@test.com",
                "phone", "11999990000",
                "cpf", "111.444.777-35",
                "dataNascimento", "1990-01-01",
                "sexo", "MASCULINO",
                "academiaId", ACADEMIA_B
        );
        var request = new HttpEntity<>(body, headers);

        var response = restTemplate.exchange(
                "/api/v1/clientes/" + clienteAId, HttpMethod.PUT, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /clientes → sem token → 403")
    void lista_semToken_forbidden() {
        var response = restTemplate.getForEntity("/api/v1/clientes", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Dois usuarios, cada um ve apenas a propria academia")
    void doisUsuarios_isolamentoCompleto() {
        String tokenA = login("admina@test.com");
        String tokenB = login("adminb@test.com");

        var respA = restTemplate.exchange(
                "/api/v1/clientes", HttpMethod.GET, bearer(tokenA, ACADEMIA_A), List.class);
        var respB = restTemplate.exchange(
                "/api/v1/clientes", HttpMethod.GET, bearer(tokenB, ACADEMIA_B), List.class);

        assertThat(respA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respB.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<Map<String, Object>>) respA.getBody())
                .extracting(c -> c.get("fullName"))
                .containsExactly("Cliente da Academia A");
        assertThat((List<Map<String, Object>>) respB.getBody())
                .extracting(c -> c.get("fullName"))
                .containsExactly("Cliente da Academia B");
    }
}
