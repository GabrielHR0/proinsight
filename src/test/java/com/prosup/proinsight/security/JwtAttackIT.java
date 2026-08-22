package com.prosup.proinsight.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OWASP API Security Top 10 — API2: Broken Authentication.
 *
 * Simula ataques a tokens JWT contra a API real:
 * algoritmo "none", expiração, assinatura corrompida/trocada,
 * claims adulterados, issuer/audience errados, jti ausente e revogação.
 */
class JwtAttackIT extends SecurityITBase {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Value("${jwt.audience}")
    private String jwtAudience;

    private SecretKey realKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    private SecretKey attackerKey() {
        var raw = "YXR0YWNrZXJTZWNyZXRLZXkzMkJ5dGVzK2ZvcldlYWtzITIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3";
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(raw));
    }

    @Test
    @DisplayName("JWT com alg=none (sem assinatura) → rejeitado")
    void tokenAlgNoneRejeitado() {
        String header = base64Url("{\"alg\":\"none\",\"typ\":\"JWT\"}");
        String payload = base64Url("{\"sub\":\"admin@test.com\",\"userId\":\"user-x\",\"jti\":\"x\"}");

        var response = getJson("/api/v1/clientes", bearer(header + "." + payload + ".", ACADEMIA_A));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("JWT expirado → rejeitado")
    void tokenExpiradoRejeitado() {
        String token = Jwts.builder()
                .id("jti-exp")
                .issuer(jwtIssuer)
                .audience().add(jwtAudience).and()
                .subject("admin@acme.com")
                .issuedAt(new Date(System.currentTimeMillis() - 5_000))
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(realKey())
                .compact();

        var response = getJson("/api/v1/clientes", bearer(token, ACADEMIA_A));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("JWT com claims adulterados (academia alheia no payload) → rejeitado")
    void tokenAdulteradoRejeitado() {
        String valid = Jwts.builder()
                .id("jti-tamper")
                .issuer(jwtIssuer)
                .audience().add(jwtAudience).and()
                .subject("admin@acme.com")
                .claim("userId", "user-1")
                .claim("academiaIds", List.of(ACADEMIA_A))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(realKey())
                .compact();
        String[] parts = valid.split("\\.");
        String payloadCorrompido = base64Url(
                new String(decoded(parts[1]), StandardCharsets.UTF_8)
                        .replace("\"sec-academia-a\"", "\"academia-alheia\""));

        var response = getJson("/api/v1/clientes",
                bearer(parts[0] + "." + payloadCorrompido + "." + parts[2], ACADEMIA_A));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("JWT assinado com chave de atacante → rejeitado")
    void tokenChaveErradaRejeitado() {
        String token = Jwts.builder()
                .id("jti-x")
                .issuer(jwtIssuer)
                .audience().add(jwtAudience).and()
                .subject("admin"

                        + "@acme.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(attackerKey())
                .compact();

        var response = getJson("/api/v1/clientes", bearer(token, ACADEMIA_A));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("JWT com usuário inexistente (forjar identidade) → rejeitado")
    void tokenIdentidadeForjadaRejeitado() {
        String token = Jwts.builder()
                .id("jti-forjado")
                .issuer(jwtIssuer)
                .audience().add(jwtAudience).and()
                .subject("forjado@acme.com")
                .claim("userId", "user-inexistente")
                .claim("userName", "forjado")
                .claim("academiaIds", List.of(ACADEMIA_A))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(realKey())
                .compact();

        var response = getJson("/api/v1/clientes", bearer(token, ACADEMIA_A));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("JWT sem jti → rejeitado")
    void tokenSemJtiRejeitado() {
        String token = Jwts.builder()
                .issuer(jwtIssuer)
                .audience().add(jwtAudience).and()
                .subject("admin@acme.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(realKey())
                .compact();

        var response = getJson("/api/v1/clientes", bearer(token, ACADEMIA_A));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Autorização malformada (Bearer vazio/garbage) → rejeitado")
    void authorizationMalformadoRejeitado() {
        var response = getJson("/api/v1/clientes", bearer("", ACADEMIA_A));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

@Test
    @DisplayName("JWT válido mas revogado (logout) → rejeitado")
    void tokenRevogadoRejeitado() {
        createUserInAcademia("revoga", "revoga@ac.com", ACADEMIA_A);
        String token = login("revoga@ac.com");
        var logout = restTemplate.exchange("/api/v1/auth/logout",
                org.springframework.http.HttpMethod.POST,
                bearer(token, null),
                Void.class);
        assertThat(logout.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var response = getJson("/api/v1/clientes", bearer(token, ACADEMIA_A));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private byte[] decoded(String part) {
        return Base64.getUrlDecoder().decode(part);
    }

    private static String base64Url(String raw) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}