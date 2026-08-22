package com.prosup.proinsight.service;

import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.domain.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationServiceTest {

    private final AuthorizationService auth = new AuthorizationService();

    private static final String USER_ID = "user-123";
    private static final String ACADEMIA_A = "academia-a";
    private static final String ACADEMIA_B = "academia-b";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String userId, Map<String, Set<Permissao>> academiaPermissoes,
                                List<String> authorities) {
        var user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");
        user.setUserName("testuser");

        var granted = authorities.stream().map(SimpleGrantedAuthority::new).toList();
        var principal = new CustomUserDetails(user, granted, academiaPermissoes);
        var authToken = new org.springframework.security.authentication
                .UsernamePasswordAuthenticationToken(principal, "", granted);

        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
    }

    @Nested
    @DisplayName("hasAcademiaAccess")
    class HasAcademiaAccess {

        @BeforeEach
        void setUp() {
            autenticarComo(USER_ID, Map.of(
                    ACADEMIA_A, Set.of(Permissao.CLIENTES_LER),
                    ACADEMIA_B, Set.of(Permissao.AVALIACOES_LER)
            ), List.of());
        }

        @Test
        @DisplayName("deve retornar true quando usuario tem acesso a academia")
        void quandoTemAcesso() {
            assertThat(auth.hasAcademiaAccess(ACADEMIA_A)).isTrue();
        }

        @Test
        @DisplayName("deve retornar true para outra academia que usuario tem acesso")
        void quandoTemAcessoOutraAcademia() {
            assertThat(auth.hasAcademiaAccess(ACADEMIA_B)).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando usuario nao tem acesso a academia")
        void quandoNaoTemAcesso() {
            assertThat(auth.hasAcademiaAccess("academia-x")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando academiaId e null")
        void quandoAcademiaIdNull() {
            assertThat(auth.hasAcademiaAccess(null)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando nao ha autenticacao")
        void quandoNaoAutenticado() {
            SecurityContextHolder.clearContext();
            assertThat(auth.hasAcademiaAccess(ACADEMIA_A)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para usuario pessoal (academiaPermissoes vazio) com academiaId null")
        void usuarioPessoalComAcademiaIdNull() {
            autenticarComo("personal-id", Map.of(), List.of());
            assertThat(auth.hasAcademiaAccess(null)).isFalse();
        }

        @Test
        @DisplayName("deve retornar true para usuario pessoal quando academiaId = userId")
        void usuarioPessoalComAcademiaIdIgualAoUserId() {
            autenticarComo("personal-id", Map.of(
                    "personal-id", Set.of(Permissao.CLIENTES_LER)
            ), List.of());
            assertThat(auth.hasAcademiaAccess("personal-id")).isTrue();
        }

        @Test
        @DisplayName("deve retornar false para usuario pessoal quando academiaId e diferente do userId")
        void usuarioPessoalComAcademiaIdDiferente() {
            autenticarComo("personal-id", Map.of(
                    "personal-id", Set.of(Permissao.CLIENTES_LER)
            ), List.of());
            assertThat(auth.hasAcademiaAccess("outra-academia")).isFalse();
        }
    }

    @Nested
    @DisplayName("hasAnyAcademiaAccess")
    class HasAnyAcademiaAccess {

        @BeforeEach
        void setUp() {
            autenticarComo(USER_ID, Map.of(
                    ACADEMIA_A, Set.of(Permissao.CLIENTES_LER),
                    ACADEMIA_B, Set.of(Permissao.AVALIACOES_LER)
            ), List.of());
        }

        @Test
        @DisplayName("deve retornar true quando usuario tem acesso a todas as academias")
        void quandoTemAcessoATodas() {
            assertThat(auth.hasAnyAcademiaAccess(List.of(ACADEMIA_A, ACADEMIA_B))).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando usuario nao tem acesso a alguma academia")
        void quandoNaoTemAcessoATodas() {
            assertThat(auth.hasAnyAcademiaAccess(List.of(ACADEMIA_A, "academia-x"))).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando lista e vazia")
        void quandoListaVazia() {
            assertThat(auth.hasAnyAcademiaAccess(List.of())).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando lista e null")
        void quandoListaNull() {
            assertThat(auth.hasAnyAcademiaAccess(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isCurrentUser")
    class IsCurrentUser {

        @BeforeEach
        void setUp() {
            autenticarComo(USER_ID, Map.of(), List.of());
        }

        @Test
        @DisplayName("deve retornar true quando userId corresponde ao usuario autenticado")
        void quandoCorresponde() {
            assertThat(auth.isCurrentUser(USER_ID)).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando userId nao corresponde")
        void quandoNaoCorresponde() {
            assertThat(auth.isCurrentUser("outro-user")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando userId e null")
        void quandoUserIdNull() {
            assertThat(auth.isCurrentUser(null)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando nao ha autenticacao")
        void quandoNaoAutenticado() {
            SecurityContextHolder.clearContext();
            assertThat(auth.isCurrentUser(USER_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSuperAdmin")
    class IsSuperAdmin {

        @Test
        @DisplayName("deve retornar true quando usuario tem autoridade SUPER_ADMIN")
        void quandoTemSuperAdmin() {
            autenticarComo(USER_ID, Map.of(), List.of("SUPER_ADMIN"));
            assertThat(auth.isSuperAdmin()).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando usuario nao tem SUPER_ADMIN")
        void quandoNaoTemSuperAdmin() {
            autenticarComo(USER_ID, Map.of(), List.of("CLIENTES_LER"));
            assertThat(auth.isSuperAdmin()).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando authorities estao vazias")
        void quandoAuthoritiesVazias() {
            autenticarComo(USER_ID, Map.of(), List.of());
            assertThat(auth.isSuperAdmin()).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando nao ha autenticacao")
        void quandoNaoAutenticado() {
            SecurityContextHolder.clearContext();
            assertThat(auth.isSuperAdmin()).isFalse();
        }
    }
}
