package com.prosup.proinsight.config;

import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.service.TokenBlacklistService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    private static final String SECRET = "dGVzdGVCYXNlNjRDb20zMkJ5dGVzK1NpbTEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MA==";
    private static final long EXPIRATION_MS = 3600000L;
    private static final String ISSUER = "proinsight-api";
    private static final String AUDIENCE = "proinsight-app";

    private JwtTokenProvider provider;
    private SecretKey secretKey;
    private TokenBlacklistService blacklistService;

    @BeforeEach
    void setUp() {
        blacklistService = mock(TokenBlacklistService.class);
        provider = new JwtTokenProvider(SECRET, EXPIRATION_MS, ISSUER, AUDIENCE, blacklistService);
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    private Authentication createAuth() {
        var user = new User();
        user.setId("user-1");
        user.setEmail("test@test.com");
        user.setUserName("testuser");
        user.setAcademiaIds(Set.of("academia-1"));

        var permissoes = Map.of("academia-1", Set.of(Permissao.CLIENTES_LER));
        var authorities = List.of(new SimpleGrantedAuthority("CLIENTES_LER"));
        var principal = new CustomUserDetails(user, authorities, permissoes);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    private String createTokenWithClaims(String issuer, String audience, String jti) {
        var builder = Jwts.builder()
                .subject("test@test.com")
                .claim("userId", "user-1")
                .claim("userName", "testuser")
                .claim("academiaIds", List.of("academia-1"))
                .claim("academiaPermissoes", Map.of("academia-1", List.of("CLIENTES_LER")))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey);
        if (issuer != null) builder.issuer(issuer);
        if (audience != null) builder.audience().add(audience).and();
        if (jti != null) builder.id(jti);
        return builder.compact();
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("deve gerar token com issuer e audience configurados")
        void geraTokenComIssuerEAudience() {
            String token = provider.generateToken(createAuth());

            var claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(claims.getIssuer()).isEqualTo(ISSUER);
            assertThat(claims.getAudience()).containsExactly(AUDIENCE);
        }

        @Test
        @DisplayName("deve gerar token com jti unico")
        void geraTokenComJti() {
            String token1 = provider.generateToken(createAuth());
            String token2 = provider.generateToken(createAuth());

            var claims1 = Jwts.parser()
                    .verifyWith(secretKey).build().parseSignedClaims(token1).getPayload();
            var claims2 = Jwts.parser()
                    .verifyWith(secretKey).build().parseSignedClaims(token2).getPayload();

            assertThat(claims1.getId()).isNotBlank();
            assertThat(claims2.getId()).isNotBlank();
            assertThat(claims2.getId()).isNotEqualTo(claims1.getId());
        }

        @Test
        @DisplayName("deve gerar token com subject = email do usuario")
        void geraTokenComSubjectEmail() {
            String token = provider.generateToken(createAuth());

            var claims = Jwts.parser()
                    .verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

            assertThat(claims.getSubject()).isEqualTo("test@test.com");
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        @DisplayName("deve retornar true para token valido")
        void tokenValido() {
            String token = provider.generateToken(createAuth());
            assertThat(provider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("deve retornar false para token com issuer errado")
        void issuerErrado() {
            String token = createTokenWithClaims("wrong-issuer", AUDIENCE, UUID.randomUUID().toString());
            assertThat(provider.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para token com audience errada")
        void audienceErrada() {
            String token = createTokenWithClaims(ISSUER, "wrong-audience", UUID.randomUUID().toString());
            assertThat(provider.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para token sem jti")
        void semJti() {
            String token = createTokenWithClaims(ISSUER, AUDIENCE, null);
            assertThat(provider.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para token expirado")
        void expirado() {
            String token = Jwts.builder()
                    .id(UUID.randomUUID().toString())
                    .issuer(ISSUER)
                    .audience().add(AUDIENCE).and()
                    .subject("test@test.com")
                    .issuedAt(new Date(System.currentTimeMillis() - 100000))
                    .expiration(new Date(System.currentTimeMillis() - 50000))
                    .signWith(secretKey)
                    .compact();
            assertThat(provider.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para token com assinatura corrompida")
        void assinaturaCorrompida() {
            String token = provider.generateToken(createAuth());
            String corrompido = token.substring(0, token.length() - 5) + "XXXXX";
            assertThat(provider.validateToken(corrompido)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para string aleatoria")
        void stringAleatoria() {
            assertThat(provider.validateToken("nao.eh.um.token")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para token null")
        void tokenNull() {
            assertThat(provider.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para token revogado")
        void tokenRevogado() {
            String token = provider.generateToken(createAuth());
            var jti = Jwts.parser()
                    .verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getId();
            when(blacklistService.isRevoked(jti)).thenReturn(true);

            assertThat(provider.validateToken(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("getAuthentication")
    class GetAuthentication {

        @Test
        @DisplayName("deve retornar Authentication com authorities para token valido")
        void tokenValido() {
            String token = provider.generateToken(createAuth());
            var request = new MockHttpServletRequest();
            request.addHeader("X-Academia-Id", "academia-1");

            Authentication auth = provider.getAuthentication(token, request);

            assertThat(auth).isNotNull();
            assertThat(auth.isAuthenticated()).isTrue();
            assertThat(auth.getAuthorities()).extracting(Object::toString)
                    .containsExactly("CLIENTES_LER");
        }

        @Test
        @DisplayName("deve lancar JwtException para token com issuer errado")
        void issuerErrado() {
            String token = createTokenWithClaims("wrong-issuer", AUDIENCE, "some-jti");
            var request = new MockHttpServletRequest();

            assertThatThrownBy(() -> provider.getAuthentication(token, request))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("deve lancar JwtException para token com audience errada")
        void audienceErrada() {
            String token = createTokenWithClaims(ISSUER, "wrong-audience", "some-jti");
            var request = new MockHttpServletRequest();

            assertThatThrownBy(() -> provider.getAuthentication(token, request))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("deve lancar JwtException para token sem jti")
        void semJti() {
            String token = createTokenWithClaims(ISSUER, AUDIENCE, null);
            var request = new MockHttpServletRequest();

            assertThatThrownBy(() -> provider.getAuthentication(token, request))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("deve filtrar permissoes pelo cabecalho X-Academia-Id")
        void filtraPermissoesPorAcademia() {
            var permissoes = Map.of(
                    "academia-1", Set.of(Permissao.CLIENTES_LER),
                    "academia-2", Set.of(Permissao.AVALIACOES_LER)
            );
            var authorities = List.of(
                    new SimpleGrantedAuthority("CLIENTES_LER"),
                    new SimpleGrantedAuthority("AVALIACOES_LER")
            );
            var user = new User();
            user.setId("user-1");
            user.setEmail("test@test.com");
            user.setUserName("testuser");
            user.setAcademiaIds(Set.of("academia-1", "academia-2"));
            var principal = new CustomUserDetails(user, authorities, permissoes);
            var authToken = new UsernamePasswordAuthenticationToken(principal, "", authorities);
            String token = provider.generateToken(authToken);

            var request = new MockHttpServletRequest();
            request.addHeader("X-Academia-Id", "academia-1");
            Authentication result = provider.getAuthentication(token, request);

            assertThat(result.getAuthorities())
                    .extracting(Object::toString)
                    .containsExactly("CLIENTES_LER");
        }

        @Test
        @DisplayName("nao deve unir permissoes quando header ausente")
        void naoUnePermissoesSemHeader() {
            var permissoes = Map.of(
                    "academia-1", Set.of(Permissao.CLIENTES_LER),
                    "academia-2", Set.of(Permissao.AVALIACOES_LER)
            );
            var authorities = List.of(
                    new SimpleGrantedAuthority("CLIENTES_LER"),
                    new SimpleGrantedAuthority("AVALIACOES_LER")
            );
            var user = new User();
            user.setId("user-1");
            user.setEmail("test@test.com");
            user.setUserName("testuser");
            user.setAcademiaIds(Set.of("academia-1", "academia-2"));
            var principal = new CustomUserDetails(user, authorities, permissoes);
            var authToken = new UsernamePasswordAuthenticationToken(principal, "", authorities);
            String token = provider.generateToken(authToken);

            var request = new MockHttpServletRequest();
            Authentication result = provider.getAuthentication(token, request);

            assertThat(result.getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("deve retornar authorities vazias para academia nao presente no token")
        void academiaNaoPresenteNoToken() {
            String token = provider.generateToken(createAuth());
            var request = new MockHttpServletRequest();
            request.addHeader("X-Academia-Id", "academia-desconhecida");

            Authentication result = provider.getAuthentication(token, request);

            assertThat(result.getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("deve lancar JwtException para token revogado")
        void tokenRevogado() {
            String token = provider.generateToken(createAuth());
            var jti = Jwts.parser()
                    .verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getId();
            when(blacklistService.isRevoked(jti)).thenReturn(true);

            var request = new MockHttpServletRequest();
            request.addHeader("X-Academia-Id", "academia-1");

            assertThatThrownBy(() -> provider.getAuthentication(token, request))
                    .isInstanceOf(JwtException.class);
        }
    }
}
