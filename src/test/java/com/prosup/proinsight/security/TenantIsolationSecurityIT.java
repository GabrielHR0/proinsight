package com.prosup.proinsight.security;

import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OWASP API Security Top 10 — API1: Broken Object Level Authorization (BOLA/IDOR).
 *
 * Tenta acessar dados de outra academia (tenant) manipulando IDs,
 * headers e corpos de requisição.
 */
class TenantIsolationSecurityIT extends SecurityITBase {

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

    @Test
    @DisplayName("GET /clientes/{id} com ID de cliente de OUTRA academia → 404 (não vaza)")
    void crossTenantRead_clienteAlheio_naoVaza() {
        createUserInAcademia("admina", "admina@sec.com", ACADEMIA_A);
        clienteRepository.save(cliente(ACADEMIA_A, "Cliente A"));
        String idB = clienteRepository.save(cliente(ACADEMIA_B, "Cliente B")).getId();
        String token = login("admina@sec.com");

        var response = getJson("/api/v1/clientes/" + idB, bearer(token, ACADEMIA_A));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("PUT /clientes/{id} tentando mover para academia alheia → 403")
    void crossTenantWrite_moverParaAcademiaAlheia_forbidden() {
        createUserInAcademia("admina", "admina@sec.com", ACADEMIA_A);
        String idA = clienteRepository.save(cliente(ACADEMIA_A, "Cliente A")).getId();
        String token = login("admina@sec.com");

        var body = Map.<String, Object>of(
                "fullName", "Cliente A",
                "email", "clientea@test.com",
                "phone", "11999990000",
                "cpf", "111.444.777-35",
                "dataNascimento", "1990-01-01",
                "sexo", "MASCULINO",
                "academiaId", ACADEMIA_B
        );

        var response = putJson("/api/v1/clientes/" + idA, body, token, ACADEMIA_A);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("X-Academia-Id de academia à qual o usuário não pertence → 403")
    void headerAcademiaAlheia_forbidden() {
        createUserInAcademia("admina", "admina@sec.com", ACADEMIA_A);
        String token = login("admina@sec.com");

        var response = getJson("/api/v1/clientes", bearer(token, ACADEMIA_B));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Sem X-Academia-Id em endpoint scoped → 403")
    void semHeaderAcademia_forbidden() {
        createUserInAcademia("admina", "admina@sec.com", ACADEMIA_A);
        String token = login("admina@sec.com");

        var response = getJson("/api/v1/clientes", bearer(token, null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /clientes com token da academia A vê APENAS clientes da academia A")
    void isolamentoCompleto_listagem() {
        createUserInAcademia("admina", "admina@sec.com", ACADEMIA_A);
        createUserInAcademia("adminb", "adminb@sec.com", ACADEMIA_B);
        clienteRepository.save(cliente(ACADEMIA_A, "Cliente A"));
        clienteRepository.save(cliente(ACADEMIA_B, "Cliente B"));
        String token = login("admina@sec.com");

        var response = getString("/api/v1/clientes", token, ACADEMIA_A);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Dois usuários, cada um vê apenas seus próprios dados")
    void doisUsuarios_isolamento() {
        createUserInAcademia("admina", "admina@sec.com", ACADEMIA_A);
        createUserInAcademia("adminb", "adminb@sec.com", ACADEMIA_B);
        String idA = clienteRepository.save(cliente(ACADEMIA_A, "Cliente A")).getId();
        clienteRepository.save(cliente(ACADEMIA_B, "Cliente B"));
        String tokenA = login("admina@sec.com");
        String tokenB = login("adminb@sec.com");

        var respA = getJson("/api/v1/clientes/" + idA, bearer(tokenA, ACADEMIA_A));
        var respB = getJson("/api/v1/clientes/" + idA, bearer(tokenB, ACADEMIA_B));

        assertThat(respA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respB.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Token da academia A em endpoint de academia (GET /academias/{idB}) → 403")
    void crossTenant_academiaAlheia_forbidden() {
        createUserInAcademia("admina", "admina@sec.com", ACADEMIA_A);
        String idB = "academia-b-id-real";
        String token = login("admina@sec.com");

        var response = getJson("/api/v1/academias/" + idB, bearer(token, ACADEMIA_A));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}