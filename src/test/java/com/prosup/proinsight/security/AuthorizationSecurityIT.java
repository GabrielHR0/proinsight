package com.prosup.proinsight.security;

import com.prosup.proinsight.domain.enums.Permissao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OWASP API Security Top 10 — API5: Broken Function Level Authorization.
 *
 * Usuário autenticado mas sem permissão tenta executar operações de
 * administrador (escalada de privilégio) e acessar endpoints restritos.
 */
class AuthorizationSecurityIT extends SecurityITBase {

    @Test
    @DisplayName("Endpoint protegido sem token → 403")
    void semTokenEndpointProtegido_forbidden() {
        var response = getJson("/api/v1/clientes", bearer(null, null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Usuário com CLIENTES_* (sem ACADEMIAS_CRIAR) cria academia → 403")
    void tokenSemPermissaoCriarAcademia_forbidden() {
        String roleId = roleWith(ACADEMIA_A, Set.of(Permissao.CLIENTES_LER));
        createUser("reader", "reader@sec.com", Map.of(ACADEMIA_A, Set.of(roleId)));
        String token = login("reader@sec.com");

        var body = Map.of("nomeFantasia", "Academia Nova",
                "cnpj", "12.345.678/0001-90",
                "ownerId", "owner-fake");
        var response = postJson("/api/v1/academias", body, token, ACADEMIA_A);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Usuário sem USUARIOS_CRIAR tenta criar usuário → 403")
    void usuarioComumNaoCriaUsuario_forbidden() {
        String roleId = roleWith(ACADEMIA_A, Set.of(Permissao.CLIENTES_LER));
        createUser("common", "common@sec.com", Map.of(ACADEMIA_A, Set.of(roleId)));
        String token = login("common@sec.com");

        var body = Map.<String, Object>of(
                "userName", "novo",
                "email", "novo@sec.com",
                "password", "Senha@123",
                "cref", "CREF-12345",
                "academiaRoles", Map.of(ACADEMIA_A, Set.of(roleId))
        );
        var response = postJson("/api/v1/users", body, token, ACADEMIA_A);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Escalada: usuário da academia A tenta criar usuário na academia B → 403")
    void escalada_academiaAlheia_forbidden() {
        String roleIdA = roleWith(ACADEMIA_A, Set.of(Permissao.USUARIOS_CRIAR));
        String roleIdB = roleWith(ACADEMIA_B, Set.of(Permissao.USUARIOS_CRIAR));
        createUser("admin-a", "admina@sec.com", Map.of(ACADEMIA_A, Set.of(roleIdA)));
        String token = login("admina@sec.com");

        var body = Map.<String, Object>of(
                "userName", "invasor",
                "email", "invasor@sec.com",
                "password", "Senha@123",
                "cref", "CREF-99999",
                "academiaRoles", Map.of(ACADEMIA_B, Set.of(roleIdB))
        );
        var response = postJson("/api/v1/users", body, token, ACADEMIA_A);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("DELETE /academias/{id} sem SUPER_ADMIN → 403")
    void deleteAcademiaSemSuperAdmin_forbidden() {
        String roleId = roleUser(ACADEMIA_A);
        createUser("admina", "admina@sec.com", Map.of(ACADEMIA_A, Set.of(roleId)));
        String token = login("admina@sec.com");

        var response = deleteJson("/api/v1/academias/academia-xyz", token, ACADEMIA_A);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("actuator/prometheus com token comum (sem SUPER_ADMIN) → 403")
    void actuatorPrometheus_semSuperAdmin_forbidden() {
        String roleId = roleUser(ACADEMIA_A);
        createUser("admina", "admina@sec.com", Map.of(ACADEMIA_A, Set.of(roleId)));
        String token = login("admina@sec.com");

        var response = getString("/actuator/prometheus", token, ACADEMIA_A);

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    @DisplayName("actuator/prometheus sem token → 403")
    void actuatorPrometheus_semToken_forbidden() {
        var response = getString("/actuator/prometheus", null, null);

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    private String roleUser(String academiaId) {
        Set<Permissao> naoSudo = java.util.EnumSet.allOf(Permissao.class);
        naoSudo.remove(Permissao.SUPER_ADMIN);
        return roleWith(academiaId, naoSudo);
    }
}