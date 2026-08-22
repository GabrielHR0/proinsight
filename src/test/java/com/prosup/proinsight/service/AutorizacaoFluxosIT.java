package com.prosup.proinsight.service;

import com.prosup.proinsight.AbstractIntegrationTest;
import com.prosup.proinsight.config.JwtTokenProvider;
import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.infrastructure.persistence.document.RoleDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.RefreshTokenRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AutorizacaoFluxosIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AcademiaRepository academiaRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String roleFullAccessId;
    private String roleEmployeeLimitedId;
    private String roleEvaluatorId;
    private String roleSoloEvaluatorId;

    private static final String ACADEMIA_A = "academia_a_test";
    private static final String ACADEMIA_B = "academia_b_test";
    private static final String ACADEMIA_SOLO = "academia_solo_test";
    private static final String ACADEMIA_INVALIDA = "academia_inexistente";

    @BeforeEach
    void cleanAndSetUp() {
        refreshTokenRepository.deleteAll();
        academiaRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        setUpRoles();
    }

    void setUpRoles() {
        roleFullAccessId = roleRepository.save(new RoleDocument(
                null, "admin_full", "Acesso total", Set.of(Permissao.values()))).getId();

        roleEmployeeLimitedId = roleRepository.save(new RoleDocument(
                null, "employee_limited", "Acesso limitado funcionario",
                Set.of(Permissao.CLIENTES_LER, Permissao.AVALIACOES_LER))).getId();

        roleEvaluatorId = roleRepository.save(new RoleDocument(
                null, "evaluator", "Avaliador padrao",
                Set.of(Permissao.CLIENTES_CRIAR, Permissao.CLIENTES_LER,
                        Permissao.AVALIACOES_CRIAR, Permissao.AVALIACOES_LER))).getId();

        roleSoloEvaluatorId = roleRepository.save(new RoleDocument(
                null, "solo_evaluator", "Avaliador solo",
                Set.of(Permissao.CLIENTES_CRIAR, Permissao.CLIENTES_LER,
                        Permissao.AVALIACOES_CRIAR, Permissao.AVALIACOES_LER,
                        Permissao.AVALIACOES_ATUALIZAR))).getId();
    }

    private String criarUser(String userName, String email,
                             Map<String, Set<String>> academiaRoles) {
        var doc = new UserDocument();
        doc.setUserName(userName);
        doc.setEmail(email);
        doc.setPassword("$2a$12$dummy");
        doc.setActive(true);
        doc.setAcademiaRoles(academiaRoles);
        return userRepository.save(doc).getId();
    }

    private String gerarToken(String userName) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        return jwtTokenProvider.generateToken(auth);
    }

    private Collection<? extends GrantedAuthority> extrairAutoridades(
            String token, String academiaId) {
        var request = new MockHttpServletRequest();
        if (academiaId != null) {
            request.addHeader("X-Academia-Id", academiaId);
        }
        Authentication auth = jwtTokenProvider.getAuthentication(token, request);
        return auth.getAuthorities();
    }

    @SafeVarargs
    private static Set<String> nomesAutoridades(Collection<? extends GrantedAuthority>... authorities) {
        return List.of(authorities).stream()
                .flatMap(Collection::stream)
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    private static Set<String> nomesPermissoes(Permissao... permissoes) {
        return Set.of(permissoes).stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Nested
    @DisplayName("Cenario 1: Dono da academia com funcionarios")
    class DonoComFuncionarios {

        private String tokenDono;
        private String tokenFuncionario;

        @BeforeEach
        void setUp() {
            criarUser("dono-academia-a", "dono@test.com",
                    Map.of(ACADEMIA_A, Set.of(roleFullAccessId)));
            criarUser("funcionario-academia-a", "func@test.com",
                    Map.of(ACADEMIA_A, Set.of(roleEmployeeLimitedId)));

            tokenDono = gerarToken("dono-academia-a");
            tokenFuncionario = gerarToken("funcionario-academia-a");
        }

        @Test
        @DisplayName("Dono tem TODAS as permissoes dentro da academia")
        void donoAcessaTudoNaAcademia() {
            var authorities = extrairAutoridades(tokenDono, ACADEMIA_A);
            var nomes = nomesAutoridades(authorities);

            assertThat(nomes).containsAll(nomesPermissoes(Permissao.values()));
            assertThat(nomes).hasSize(Permissao.values().length);
        }

        @Test
        @DisplayName("Funcionario tem APENAS CLIENTES_LER e AVALIACOES_LER")
        void funcionarioSoTemPermissoesLimitadas() {
            var authorities = extrairAutoridades(tokenFuncionario, ACADEMIA_A);
            var nomes = nomesAutoridades(authorities);

            assertThat(nomes)
                    .containsExactlyInAnyOrder(Permissao.CLIENTES_LER.name(),
                            Permissao.AVALIACOES_LER.name());
        }
    }

    @Nested
    @DisplayName("Cenario 2: Avaliador vinculado a academia, escopo reduzido")
    class AvaliadorVinculado {

        private String tokenAvaliador;

        @BeforeEach
        void setUp() {
            criarUser("avaliador-academia", "avaliador@test.com",
                    Map.of(ACADEMIA_A, Set.of(roleEvaluatorId)));
            tokenAvaliador = gerarToken("avaliador-academia");
        }

        @Test
        @DisplayName("Avaliador tem permissoes de CRUD clientes + avaliacoes, mas nao outras")
        void avaliadorTemEscopoReduzido() {
            var authorities = extrairAutoridades(tokenAvaliador, ACADEMIA_A);
            var nomes = nomesAutoridades(authorities);

            Set<String> esperadas = nomesPermissoes(
                    Permissao.CLIENTES_CRIAR, Permissao.CLIENTES_LER,
                    Permissao.AVALIACOES_CRIAR, Permissao.AVALIACOES_LER);
            Set<String> naoEsperadas = nomesPermissoes(
                    Permissao.CLIENTES_ATUALIZAR, Permissao.CLIENTES_EXCLUIR,
                    Permissao.AVALIACOES_ATUALIZAR, Permissao.AVALIACOES_EXCLUIR,
                    Permissao.AVALIADORES_CRIAR, Permissao.AVALIADORES_LER, Permissao.AVALIADORES_ATUALIZAR,
                    Permissao.PROTOCOLOS_LER,
                    Permissao.USUARIOS_CRIAR, Permissao.USUARIOS_LER,
                    Permissao.USUARIOS_ATUALIZAR, Permissao.USUARIOS_EXCLUIR,
                    Permissao.ACADEMIAS_CRIAR, Permissao.ACADEMIAS_LER, Permissao.ACADEMIAS_ATUALIZAR,
                    Permissao.RELATORIOS_LER, Permissao.RELATORIOS_EXPORTAR,
                    Permissao.SUPER_ADMIN);

            assertThat(nomes).containsExactlyInAnyOrderElementsOf(esperadas);
            assertThat(nomes).doesNotContainAnyElementsOf(naoEsperadas);
        }
    }

    @Nested
    @DisplayName("Cenario 3: Usuario gerencia multiplas academias com papeis diferentes")
    class MultiplasAcademias {

        private String tokenMulti;

        @BeforeEach
        void setUp() {
            criarUser("multi-academia", "multi@test.com",
                    Map.of(ACADEMIA_A, Set.of(roleFullAccessId),
                            ACADEMIA_B, Set.of(roleEmployeeLimitedId)));
            tokenMulti = gerarToken("multi-academia");
        }

        @Test
        @DisplayName("Escopo ACADEMIA_A: tem TODAS as permissoes")
        void acessoAcademiaA_temPermissoesCheias() {
            var authorities = extrairAutoridades(tokenMulti, ACADEMIA_A);
            var nomes = nomesAutoridades(authorities);

            assertThat(nomes).containsAll(nomesPermissoes(Permissao.values()));
            assertThat(nomes).hasSize(Permissao.values().length);
        }

        @Test
        @DisplayName("Escopo ACADEMIA_B: tem APENAS CLIENTES_LER e AVALIACOES_LER")
        void acessoAcademiaB_temPermissoesRestritas() {
            var authorities = extrairAutoridades(tokenMulti, ACADEMIA_B);
            var nomes = nomesAutoridades(authorities);

            assertThat(nomes)
                    .containsExactlyInAnyOrder(Permissao.CLIENTES_LER.name(),
                            Permissao.AVALIACOES_LER.name());
        }

        @Test
        @DisplayName("Sem cabecalho X-Academia-Id: SEM autoridades (fail-closed)")
        void semCabecalho_semAutoridades() {
            var authorities = extrairAutoridades(tokenMulti, null);

            assertThat(authorities).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cenario 4: Avaliador solo com seus proprios alunos")
    class AvaliadorSolo {

        private String tokenSolo;

        @BeforeEach
        void setUp() {
            criarUser("avaliador-solo", "solo@test.com",
                    Map.of(ACADEMIA_SOLO, Set.of(roleSoloEvaluatorId)));
            tokenSolo = gerarToken("avaliador-solo");
        }

        @Test
        @DisplayName("Avaliador solo tem permissoes de CRUD clientes e avaliacoes (exceto excluir)")
        void avaliadorSoloTemPermissoesCorretas() {
            var authorities = extrairAutoridades(tokenSolo, ACADEMIA_SOLO);
            var nomes = nomesAutoridades(authorities);

            Set<String> esperadas = nomesPermissoes(
                    Permissao.CLIENTES_CRIAR, Permissao.CLIENTES_LER,
                    Permissao.AVALIACOES_CRIAR, Permissao.AVALIACOES_LER,
                    Permissao.AVALIACOES_ATUALIZAR);
            Set<String> naoEsperadas = nomesPermissoes(
                    Permissao.CLIENTES_ATUALIZAR, Permissao.CLIENTES_EXCLUIR,
                    Permissao.AVALIACOES_EXCLUIR,
                    Permissao.AVALIADORES_CRIAR, Permissao.AVALIADORES_LER, Permissao.AVALIADORES_ATUALIZAR,
                    Permissao.PROTOCOLOS_LER,
                    Permissao.USUARIOS_CRIAR, Permissao.USUARIOS_LER,
                    Permissao.USUARIOS_ATUALIZAR, Permissao.USUARIOS_EXCLUIR,
                    Permissao.ACADEMIAS_CRIAR, Permissao.ACADEMIAS_LER, Permissao.ACADEMIAS_ATUALIZAR,
                    Permissao.RELATORIOS_LER, Permissao.RELATORIOS_EXPORTAR,
                    Permissao.SUPER_ADMIN);

            assertThat(nomes).containsExactlyInAnyOrderElementsOf(esperadas);
            assertThat(nomes).doesNotContainAnyElementsOf(naoEsperadas);
        }
    }

    @Nested
    @DisplayName("Casos de borda")
    class CasosDeBorda {

        @Test
        @DisplayName("Usuario com academiaRoles vazio: SEM autoridades")
        void academiaRolesVazio_semAutoridades() {
            criarUser("sem-roles", "semroles@test.com", Map.of());
            String token = gerarToken("sem-roles");

            var authorities = extrairAutoridades(token, ACADEMIA_A);
            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("RoleId inexistente dentro de academiaRoles: ignorado sem erro")
        void roleIdInexistente_ignorado() {
            criarUser("role-invalida", "roleinv@test.com",
                    Map.of(ACADEMIA_A, Set.of("id_que_nao_existe_no_banco")));
            String token = gerarToken("role-invalida");

            var authorities = extrairAutoridades(token, ACADEMIA_A);
            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("X-Academia-Id inexistente no mapa: SEM autoridades (fail-closed)")
        void academiaIdInvalido_semAutoridades() {
            criarUser("fallback-user", "fallback@test.com",
                    Map.of(ACADEMIA_A, Set.of(roleFullAccessId)));
            String token = gerarToken("fallback-user");

            var authorities = extrairAutoridades(token, ACADEMIA_INVALIDA);

            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("Usuario inativo: isEnabled retorna false")
        void usuarioInativo_isEnabledFalse() {
            var doc = new UserDocument();
            doc.setUserName("user-inativo");
            doc.setEmail("inativo@test.com");
            doc.setPassword("$2a$12$dummy");
            doc.setActive(false);
            doc.setAcademiaRoles(Map.of(ACADEMIA_A, Set.of(roleFullAccessId)));
            userRepository.save(doc);

            var userDetails = userDetailsService.loadUserByUsername("user-inativo");
            assertThat(userDetails.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("Token JWT invalido: validateToken retorna false")
        void tokenInvalido_validateRetornaFalse() {
            assertThat(jwtTokenProvider.validateToken("token.invalido.aqui")).isFalse();
        }

        @Test
        @DisplayName("Token JWT com assinatura trocada: validateToken retorna false")
        void tokenAssinaturaTrocada_falso() {
            criarUser("user-token-true", "tokentrue@test.com",
                    Map.of(ACADEMIA_A, Set.of(roleFullAccessId)));
            String tokenValido = gerarToken("user-token-true");
            String tokenCorrompido = tokenValido.substring(0, tokenValido.length() - 5) + "XXXXX";
            assertThat(jwtTokenProvider.validateToken(tokenCorrompido)).isFalse();
        }

        @Test
        @DisplayName("Dois usuarios com permissoes diferentes na mesma academia: isolamento")
        void doisUsuariosMesmaAcademia_isolamento() {
            criarUser("admin-mesma-acad", "adminma@test.com",
                    Map.of(ACADEMIA_A, Set.of(roleFullAccessId)));
            criarUser("limited-mesma-acad", "limitedma@test.com",
                    Map.of(ACADEMIA_A, Set.of(roleEmployeeLimitedId)));

            String tokenAdmin = gerarToken("admin-mesma-acad");
            String tokenLimited = gerarToken("limited-mesma-acad");

            assertThat(nomesAutoridades(extrairAutoridades(tokenAdmin, ACADEMIA_A)))
                    .containsAll(nomesPermissoes(Permissao.values()));
            assertThat(nomesAutoridades(extrairAutoridades(tokenLimited, ACADEMIA_A)))
                    .containsExactlyInAnyOrder(Permissao.CLIENTES_LER.name(),
                            Permissao.AVALIACOES_LER.name());
        }
    }

    @Nested
    @DisplayName("Validacao do CustomUserDetailsService")
    class CustomUserDetailsServiceValidation {

        @Test
        @DisplayName("loadUserByUsername retorna CustomUserDetails com academiaPermissoes")
        void loadUserByUsername_retornaAcademiaPermissoes() {
            criarUser("user-full", "full@test.com",
                    Map.of(ACADEMIA_A, Set.of(roleFullAccessId)));

            var userDetails = userDetailsService.loadUserByUsername("user-full");
            assertThat(userDetails).isInstanceOf(CustomUserDetails.class);

            var cud = (CustomUserDetails) userDetails;
            assertThat(cud.getAcademiaPermissoes()).containsKey(ACADEMIA_A);
            assertThat(cud.getAcademiaPermissoes().get(ACADEMIA_A))
                    .containsAll(Set.of(Permissao.values()));
        }

        @Test
        @DisplayName("loadUserByUsername com academiaRoles nulo: academiaPermissoes vazio")
        void loadUserByUsername_academiaRolesNulo() {
            var doc = new UserDocument();
            doc.setUserName("user-null-roles");
            doc.setEmail("nullroles@test.com");
            doc.setPassword("$2a$12$dummy");
            doc.setActive(true);
            doc.setAcademiaRoles(null);
            userRepository.save(doc);

            assertThat(userRepository.findByEmail("nullroles@test.com"))
                    .describedAs("Usuario deve existir apos save")
                    .isPresent();

            var userDetails = userDetailsService.loadUserByUsername("nullroles@test.com");
            var cud = (CustomUserDetails) userDetails;
            assertThat(cud.getAcademiaPermissoes()).isNotNull().isEmpty();
            assertThat(userDetails.getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("loadUserByUsername com role que tem subset: permissoes corretas")
        void loadUserByUsername_roleComSubset() {
            criarUser("user-subset", "subset@test.com",
                    Map.of(ACADEMIA_A, Set.of(roleEmployeeLimitedId)));

            var userDetails = userDetailsService.loadUserByUsername("user-subset");
            var cud = (CustomUserDetails) userDetails;

            Set<Permissao> expected = Set.of(Permissao.CLIENTES_LER, Permissao.AVALIACOES_LER);
            assertThat(cud.getAcademiaPermissoes().get(ACADEMIA_A))
                    .containsExactlyInAnyOrderElementsOf(expected);
        }
    }

    @Nested
    @DisplayName("Validacao do JwtTokenProvider")
    class JwtTokenProviderValidation {

        @Test
        @DisplayName("generateToken produz token valido")
        void generateToken_produzTokenValido() {
            criarUser("token-test", "tokentest@test.com",
                    Map.of(ACADEMIA_A, Set.of(roleFullAccessId)));
            String token = gerarToken("token-test");

            assertThat(token).isNotBlank();
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("getAuthentication sem X-Academia-Id retorna authorities vazias (fail-closed)")
        void getAuthentication_semAcademiaId_semAutoridades() {
            criarUser("auth-uniao", "authuniao@test.com",
                    Map.of(ACADEMIA_A, Set.of(roleFullAccessId)));
            String token = gerarToken("auth-uniao");

            var authorities = extrairAutoridades(token, null);

            assertThat(authorities).isEmpty();
        }
    }
}
