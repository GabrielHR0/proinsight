package com.prosup.proinsight.service;

import com.prosup.proinsight.AbstractIntegrationTest;
import com.prosup.proinsight.api.dto.request.ClienteRequest;
import com.prosup.proinsight.api.dto.request.RegisterRequest;
import com.prosup.proinsight.bootstrap.RoleInitializer;
import com.prosup.proinsight.config.JwtTokenProvider;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.UserMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * TESTES DE SISTEMA — validação completa de autorização e isolamento de dados.
 *
 * Diferente dos testes unitários e de integração básicos, estes testes validam
 * a cadeia completa: registro → persistência MongoDB → resolução de permissões
 * → TenantContext → Service → isolamento de dados.
 *
 * Cenários:
 * 1. Multi-Personal-User Isolation — 3+ users personal, cada um isolado
 * 2. Role-Based Access Control — admin/employee/evaluator na mesma academia
 * 3. Multi-Academia User — user em 2 academias com papéis diferentes
 * 4. Cross-Academia Attacks — tentativas de acesso cruzado
 * 5. Client Move Blocking — impedir movimentação de cliente entre academias
 */
@DisplayName("System Tests: Autorização e Isolamento de Dados")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SystemIT extends AbstractIntegrationTest {

    @Autowired private RegistrationService registrationService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private AcademiaRepository academiaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private CustomUserDetailsService userDetailsService;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private ClienteService clienteService;

    @BeforeAll
    void cleanDatabase() {
        clienteRepository.deleteAll();
        userRepository.deleteAll();
        academiaRepository.deleteAll();
    }

    // ========================================================================
    // HELPERS
    // ========================================================================

    /**
     * Simula a cadeia completa de filtros HTTP:
     * JwtAuthenticationFilter → TenantContextFilter
     */
    private void simulateFilterChain(String userId, String academiaId) {
        var user = userRepository.findById(userId).orElseThrow();
        var domainUser = UserMapper.toDomain(user);
        CustomUserDetails userDetails = userDetailsService.toUserDetails(domainUser);

        var permissaoMap = userDetails.getAcademiaPermissoes();
        Set<String> effectivePerms = Set.of();
        if (permissaoMap != null && permissaoMap.containsKey(academiaId)) {
            effectivePerms = permissaoMap.get(academiaId).stream()
                    .map(Enum::name).collect(Collectors.toSet());
        }
        var authorities = effectivePerms.stream()
                .map(p -> new SimpleGrantedAuthority(p))
                .toList();
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        com.prosup.proinsight.config.TenantContext.setInHttpRequest(true);
        com.prosup.proinsight.config.TenantContext.setAcademiaId(academiaId);
    }

    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        com.prosup.proinsight.config.TenantContext.clear();
    }

    /**
     * Verifica que a operação é bloqueada — AccessDeniedException (fetchScoped)
     * ou NoSuchElementException (@ScopedByAcademia filtra antes no MongoDB).
     * Ambos provam que o isolamento está funcionando.
     */
    private void assertAccessBlocked(Executable executable) {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> executable.execute())
                .isInstanceOfAny(AccessDeniedException.class, java.util.NoSuchElementException.class);
    }

    private String s() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    private RegisterRequest buildPersonalReq(String suffix) {
        var req = new RegisterRequest();
        req.setUserName("u_" + suffix);
        req.setEmail("u_" + suffix + "@sys.com");
        req.setPassword("senha12345");
        req.setCref("CREF-" + suffix);
        req.setCpf("10000000" + suffix);
        return req;
    }

    private RegisterRequest buildOwnerReq(String suffix, String academiaNome) {
        var req = buildPersonalReq(suffix);
        req.setAcademiaNome(academiaNome);
        req.setRazaoSocial("Razao " + suffix);
        req.setCnpj("123456780001" + suffix.substring(0, 2));
        return req;
    }

    private ClienteRequest buildClienteReq(String fullName, String academiaId) {
        var req = new ClienteRequest();
        req.setFullName(fullName);
        req.setEmail(fullName.toLowerCase().replace(" ", ".") + "@cli.com");
        req.setPhone("1199999" + s());
        req.setCpf("30000000" + s());
        req.setDataNascimento(LocalDate.of(1990, 5, 15));
        req.setSexo(Sexo.MASCULINO);
        req.setAcademiaId(academiaId);
        return req;
    }

    private String createCliente(String name, String academiaId, String userId) {
        try {
            simulateFilterChain(userId, academiaId);
            var req = buildClienteReq(name, academiaId);
            var resp = clienteService.create(req);
            return resp.id();
        } finally {
            clearSecurityContext();
        }
    }

    private String registerPersonal(String suffix) {
        var reg = registrationService.register(buildPersonalReq(suffix));
        return reg.userId();
    }

    private String registerOwner(String suffix, String academiaNome) {
        var reg = registrationService.register(buildOwnerReq(suffix, academiaNome));
        return reg.userId();
    }

    private String inviteUser(String suffix, String academiaId, String roleId) {
        var user = userService.register(
                "inv_" + suffix,
                "inv_" + suffix + "@sys.com",
                "senha12345",
                Map.of(academiaId, Set.of(roleId)),
                "CREF-INV-" + suffix,
                "20000000" + suffix);
        return user.getId();
    }

    private String getAcademiaId(String userId) {
        var doc = userRepository.findById(userId).orElseThrow();
        return doc.getAcademiaIds().iterator().next();
    }

    // ========================================================================
    // CENÁRIO 1: Multi-Personal-User Isolation
    // ========================================================================
    @Nested
    @DisplayName("1. Multi-Personal-User Isolation — users personal isolados")
    class MultiPersonalUserIsolation {

        @Test
        @DisplayName("3 personal users criam clientes e cada um só vê os seus")
        void cadaUmSoVeOsSeus() {
            var s1 = s(); var s2 = s(); var s3 = s();
            String uA = registerPersonal("a_" + s1);
            String uB = registerPersonal("b_" + s2);
            String uc = registerPersonal("c_" + s3);
            String acadA = getAcademiaId(uA);
            String acadB = getAcademiaId(uB);
            String acadC = getAcademiaId(uc);

            String cliA1 = createCliente("Cliente A1", acadA, uA);
            String cliA2 = createCliente("Cliente A2", acadA, uA);
            String cliB1 = createCliente("Cliente B1", acadB, uB);
            String cliC1 = createCliente("Cliente C1", acadC, uc);
            String cliC2 = createCliente("Cliente C2", acadC, uc);
            String cliC3 = createCliente("Cliente C3", acadC, uc);

            try {
                simulateFilterChain(uA, acadA);
                assertThat(clienteService.listAll()).hasSize(2);
                assertThat(clienteService.listAll().stream().map(c -> c.id()).toList())
                        .containsExactlyInAnyOrder(cliA1, cliA2);
            } finally { clearSecurityContext(); }

            try {
                simulateFilterChain(uB, acadB);
                assertThat(clienteService.listAll()).hasSize(1);
                assertThat(clienteService.listAll().get(0).id()).isEqualTo(cliB1);
            } finally { clearSecurityContext(); }

            try {
                simulateFilterChain(uc, acadC);
                assertThat(clienteService.listAll()).hasSize(3);
                assertThat(clienteService.listAll().stream().map(c -> c.id()).toList())
                        .containsExactlyInAnyOrder(cliC1, cliC2, cliC3);
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("user A não encontra cliente de user B por ID")
        void naoEncontraClientePorId() {
            var s1 = s(); var s2 = s();
            String uA = registerPersonal("a_" + s1);
            String uB = registerPersonal("b_" + s2);
            String acadA = getAcademiaId(uA);
            String acadB = getAcademiaId(uB);

            String cliB = createCliente("Cliente B", acadB, uB);

            try {
                simulateFilterChain(uA, acadA);
                // @ScopedByAcademia filtra no MongoDB → NoSuchElementException
                // ou fetchScoped lança AccessDeniedException. Ambos provam isolamento.
                assertAccessBlocked(() -> clienteService.findById(cliB));
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("user A não consegue atualizar cliente de user B")
        void naoAtualizaClienteDeOutro() {
            var s1 = s(); var s2 = s();
            String uA = registerPersonal("a_" + s1);
            String uB = registerPersonal("b_" + s2);
            String acadA = getAcademiaId(uA);
            String acadB = getAcademiaId(uB);

            String cliB = createCliente("Cliente B", acadB, uB);

            try {
                simulateFilterChain(uA, acadA);
                var updateReq = buildClienteReq("HACKED", acadA);
                assertAccessBlocked(() -> clienteService.update(cliB, updateReq));
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("update de cliente de A não afeta listagem de B")
        void updateDeAaNaoAfetaB() {
            var s1 = s(); var s2 = s();
            String uA = registerPersonal("a_" + s1);
            String uB = registerPersonal("b_" + s2);
            String acadA = getAcademiaId(uA);
            String acadB = getAcademiaId(uB);

            String cliA = createCliente("Cliente A", acadA, uA);
            createCliente("Cliente B", acadB, uB);

            try {
                simulateFilterChain(uA, acadA);
                var updateReq = buildClienteReq("Cliente A Updated", acadA);
                clienteService.update(cliA, updateReq);
            } finally { clearSecurityContext(); }

            try {
                simulateFilterChain(uB, acadB);
                var clientesB = clienteService.listAll();
                assertThat(clientesB).hasSize(1);
                assertThat(clientesB.get(0).fullName()).isEqualTo("Cliente B");
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("4 personal users isolados — ninguém se enxerga")
        void quatroUsersIsolados() {
            String[] users = new String[4];
            String[] acads = new String[4];
            for (int i = 0; i < 4; i++) {
                users[i] = registerPersonal("x" + i + "_" + s());
                acads[i] = getAcademiaId(users[i]);
                createCliente("Cliente " + i, acads[i], users[i]);
            }

            for (int i = 0; i < 4; i++) {
                try {
                    simulateFilterChain(users[i], acads[i]);
                    var clientes = clienteService.listAll();
                    assertThat(clientes).hasSize(1);
                    assertThat(clientes.get(0).fullName()).isEqualTo("Cliente " + i);
                } finally { clearSecurityContext(); }
            }

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (i == j) continue;
                    try {
                        simulateFilterChain(users[i], acads[i]);
                        final String cliId = clienteRepository.findByAcademiaId(acads[j])
                                .stream().findFirst().map(ClienteDocument::getId).orElse(null);
                        if (cliId != null) {
                            assertAccessBlocked(() -> clienteService.findById(cliId));
                        }
                    } finally { clearSecurityContext(); }
                }
            }
        }

        @Test
        @DisplayName("academia de cada personal user tem nome 'Personal - {userName}' e ownerId correto")
        void academiaPersonalTemNomeCorreto() {
            var s1 = s(); var s2 = s();
            String uA = registerPersonal("alpha_" + s1);
            String uB = registerPersonal("beta_" + s2);
            String acadA = getAcademiaId(uA);
            String acadB = getAcademiaId(uB);

            var acadDocA = academiaRepository.findById(acadA).orElseThrow();
            var acadDocB = academiaRepository.findById(acadB).orElseThrow();

            assertThat(acadDocA.getNomeFantasia()).startsWith("Personal - ");
            assertThat(acadDocA.getOwnerId()).isEqualTo(uA);
            assertThat(acadDocB.getNomeFantasia()).startsWith("Personal - ");
            assertThat(acadDocB.getOwnerId()).isEqualTo(uB);
        }

        @Test
        @DisplayName("academiaIds de cada user contém exatamente sua academia")
        void academiaIdsCorretos() {
            var s1 = s(); var s2 = s();
            String uA = registerPersonal("p_" + s1);
            String uB = registerPersonal("q_" + s2);
            String acadA = getAcademiaId(uA);
            String acadB = getAcademiaId(uB);

            var docA = userRepository.findById(uA).orElseThrow();
            var docB = userRepository.findById(uB).orElseThrow();

            assertThat(docA.getAcademiaIds()).containsExactly(acadA);
            assertThat(docB.getAcademiaIds()).containsExactly(acadB);
            assertThat(docA.getAcademiaIds()).doesNotContain(acadB);
            assertThat(docB.getAcademiaIds()).doesNotContain(acadA);
        }
    }

    // ========================================================================
    // CENÁRIO 2: Role-Based Access Control
    // ========================================================================
    @Nested
    @DisplayName("2. Role-Based Access Control — admin/employee/evaluator na mesma academia")
    class RoleBasedAccessControl {

        @Test
        @DisplayName("admin cria academia, convida employee e evaluator — dados corretos no MongoDB")
        void adminCriaEConvida() {
            String suf = s();
            String adminId = registerOwner("adm_" + suf, "Academia RBAC " + suf);
            String acadId = getAcademiaId(adminId);

            String empId = inviteUser("emp_" + suf, acadId, RoleInitializer.ROLE_EMPLOYEE_ID);
            String evaId = inviteUser("eva_" + suf, acadId, RoleInitializer.ROLE_EVALUATOR_ID);

            var empDoc = userRepository.findById(empId).orElseThrow();
            var evaDoc = userRepository.findById(evaId).orElseThrow();

            assertThat(empDoc.getAcademiaIds()).contains(acadId);
            assertThat(empDoc.getAcademiaRoles()).containsKey(acadId);
            assertThat(empDoc.getAcademiaRoles().get(acadId)).contains(RoleInitializer.ROLE_EMPLOYEE_ID);

            assertThat(evaDoc.getAcademiaIds()).contains(acadId);
            assertThat(evaDoc.getAcademiaRoles()).containsKey(acadId);
            assertThat(evaDoc.getAcademiaRoles().get(acadId)).contains(RoleInitializer.ROLE_EVALUATOR_ID);
        }

        @Test
        @DisplayName("admin pode criar cliente — employee e evaluator veem")
        void adminCriaEmployeeEVevaluatorVem() {
            String suf = s();
            String adminId = registerOwner("adm_" + suf, "Academia " + suf);
            String acadId = getAcademiaId(adminId);
            String empId = inviteUser("emp_" + suf, acadId, RoleInitializer.ROLE_EMPLOYEE_ID);
            String evaId = inviteUser("eva_" + suf, acadId, RoleInitializer.ROLE_EVALUATOR_ID);

            String cliId = createCliente("Cliente Admin", acadId, adminId);

            try {
                simulateFilterChain(empId, acadId);
                assertThat(clienteService.listAll()).hasSize(1);
                assertThat(clienteService.listAll().get(0).id()).isEqualTo(cliId);
            } finally { clearSecurityContext(); }

            try {
                simulateFilterChain(evaId, acadId);
                assertThat(clienteService.listAll()).hasSize(1);
                assertThat(clienteService.listAll().get(0).id()).isEqualTo(cliId);
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("employee pode criar cliente — admin e evaluator veem")
        void employeeCriaAdminEVevaluatorVem() {
            String suf = s();
            String adminId = registerOwner("adm_" + suf, "Academia " + suf);
            String acadId = getAcademiaId(adminId);
            String empId = inviteUser("emp_" + suf, acadId, RoleInitializer.ROLE_EMPLOYEE_ID);
            String evaId = inviteUser("eva_" + suf, acadId, RoleInitializer.ROLE_EVALUATOR_ID);

            String cliId = createCliente("Cliente Employee", acadId, empId);

            try {
                simulateFilterChain(adminId, acadId);
                assertThat(clienteService.listAll()).hasSize(1);
                assertThat(clienteService.listAll().get(0).id()).isEqualTo(cliId);
            } finally { clearSecurityContext(); }

            try {
                simulateFilterChain(evaId, acadId);
                assertThat(clienteService.listAll()).hasSize(1);
                assertThat(clienteService.listAll().get(0).id()).isEqualTo(cliId);
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("evaluator NÃO tem permissão CLIENTES_CRIAR")
        void evaluatorNaoTemClientesCriar() {
            String suf = s();
            String adminId = registerOwner("adm_" + suf, "Academia " + suf);
            String acadId = getAcademiaId(adminId);
            String evaId = inviteUser("eva_" + suf, acadId, RoleInitializer.ROLE_EVALUATOR_ID);

            try {
                simulateFilterChain(evaId, acadId);
                var auth = SecurityContextHolder.getContext().getAuthentication();
                var permNames = auth.getAuthorities().stream()
                        .map(a -> a.getAuthority()).toList();
                assertThat(permNames).doesNotContain("CLIENTES_CRIAR");
                assertThat(permNames).doesNotContain("CLIENTES_ATUALIZAR");
                assertThat(permNames).doesNotContain("USUARIOS_CRIAR");
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("employee NÃO tem permissão USUARIOS_CRIAR")
        void employeeNaoTemUsuariosCriar() {
            String suf = s();
            String adminId = registerOwner("adm_" + suf, "Academia " + suf);
            String acadId = getAcademiaId(adminId);
            String empId = inviteUser("emp_" + suf, acadId, RoleInitializer.ROLE_EMPLOYEE_ID);

            try {
                simulateFilterChain(empId, acadId);
                var auth = SecurityContextHolder.getContext().getAuthentication();
                var permNames = auth.getAuthorities().stream()
                        .map(a -> a.getAuthority()).toList();
                assertThat(permNames).doesNotContain("USUARIOS_CRIAR");
                assertThat(permNames).doesNotContain("USUARIOS_LER");
                assertThat(permNames).doesNotContain("ACADEMIAS_ATUALIZAR");
                assertThat(permNames).doesNotContain("ACADEMIAS_CRIAR");
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("employee NÃO pode atualizar academia — falta ACADEMIAS_ATUALIZAR")
        void employeeNaoAtualizaAcademia() {
            String suf = s();
            String adminId = registerOwner("adm_" + suf, "Academia " + suf);
            String acadId = getAcademiaId(adminId);
            String empId = inviteUser("emp_" + suf, acadId, RoleInitializer.ROLE_EMPLOYEE_ID);

            try {
                simulateFilterChain(empId, acadId);
                var auth = SecurityContextHolder.getContext().getAuthentication();
                var permNames = auth.getAuthorities().stream()
                        .map(a -> a.getAuthority()).toList();
                assertThat(permNames).doesNotContain("ACADEMIAS_ATUALIZAR");
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("admin tem 21 permissões, employee tem 8, evaluator tem 5")
        void contagemPermissoesPorRole() {
            String suf = s();
            String adminId = registerOwner("adm_" + suf, "Academia " + suf);
            String acadId = getAcademiaId(adminId);
            String empId = inviteUser("emp_" + suf, acadId, RoleInitializer.ROLE_EMPLOYEE_ID);
            String evaId = inviteUser("eva_" + suf, acadId, RoleInitializer.ROLE_EVALUATOR_ID);

            try {
                simulateFilterChain(adminId, acadId);
                var auth = SecurityContextHolder.getContext().getAuthentication();
                assertThat(auth.getAuthorities()).hasSize(21);
            } finally { clearSecurityContext(); }

            try {
                simulateFilterChain(empId, acadId);
                var auth = SecurityContextHolder.getContext().getAuthentication();
                assertThat(auth.getAuthorities()).hasSize(8);
            } finally { clearSecurityContext(); }

            try {
                simulateFilterChain(evaId, acadId);
                var auth = SecurityContextHolder.getContext().getAuthentication();
                assertThat(auth.getAuthorities()).hasSize(5);
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("employee tem PROTOCOLOS_LER mas não USUARIOS_CRIAR")
        void employeeTemProtocolosLerMasNaoUsuariosCriar() {
            String suf = s();
            String adminId = registerOwner("adm_" + suf, "Academia " + suf);
            String acadId = getAcademiaId(adminId);
            String empId = inviteUser("emp_" + suf, acadId, RoleInitializer.ROLE_EMPLOYEE_ID);

            try {
                simulateFilterChain(empId, acadId);
                var auth = SecurityContextHolder.getContext().getAuthentication();
                var permNames = auth.getAuthorities().stream()
                        .map(a -> a.getAuthority()).toList();
                assertThat(permNames).contains("PROTOCOLOS_LER");
                assertThat(permNames).contains("CLIENTES_CRIAR");
                assertThat(permNames).contains("CLIENTES_LER");
                assertThat(permNames).contains("AVALIACOES_CRIAR");
                assertThat(permNames).doesNotContain("USUARIOS_CRIAR");
                assertThat(permNames).doesNotContain("USUARIOS_LER");
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("evaluator tem AVALIACOES_CRIAR mas não CLIENTES_CRIAR")
        void evaluatorTemAvaliacoesCriarMasNaoClientesCriar() {
            String suf = s();
            String adminId = registerOwner("adm_" + suf, "Academia " + suf);
            String acadId = getAcademiaId(adminId);
            String evaId = inviteUser("eva_" + suf, acadId, RoleInitializer.ROLE_EVALUATOR_ID);

            try {
                simulateFilterChain(evaId, acadId);
                var auth = SecurityContextHolder.getContext().getAuthentication();
                var permNames = auth.getAuthorities().stream()
                        .map(a -> a.getAuthority()).toList();
                assertThat(permNames).contains("AVALIACOES_CRIAR");
                assertThat(permNames).contains("AVALIACOES_LER");
                assertThat(permNames).contains("CLIENTES_LER");
                assertThat(permNames).doesNotContain("CLIENTES_CRIAR");
                assertThat(permNames).doesNotContain("CLIENTES_ATUALIZAR");
                assertThat(permNames).doesNotContain("USUARIOS_CRIAR");
            } finally { clearSecurityContext(); }
        }
    }

    // ========================================================================
    // CENÁRIO 3: Multi-Academia User
    // ========================================================================
    @Nested
    @DisplayName("3. Multi-Academia User — user em 2 academias com papéis diferentes")
    class MultiAcademiaUser {

        @Test
        @DisplayName("user é admin em academia A e employee em academia B")
        void userEmDuasAcademias() {
            String suf = s();
            String adminA = registerOwner("ownerA_" + suf, "Academia A " + suf);
            String acadA = getAcademiaId(adminA);
            String adminB = registerOwner("ownerB_" + suf, "Academia B " + suf);
            String acadB = getAcademiaId(adminB);

            var multiUser = userService.register(
                    "multi_" + suf, "multi_" + suf + "@sys.com", "senha12345",
                    Map.of(acadA, Set.of(RoleInitializer.ROLE_ADMIN_ID),
                           acadB, Set.of(RoleInitializer.ROLE_EMPLOYEE_ID)),
                    "CREF-MU-" + suf, "30000000" + suf);

            var doc = userRepository.findById(multiUser.getId()).orElseThrow();
            assertThat(doc.getAcademiaIds()).containsExactlyInAnyOrder(acadA, acadB);
        }

        @Test
        @DisplayName("em academia A (admin): pode criar clientes")
        void emAcademiaAPodeCriar() {
            String suf = s();
            String adminA = registerOwner("ownerA_" + suf, "Academia A " + suf);
            String acadA = getAcademiaId(adminA);
            String adminB = registerOwner("ownerB_" + suf, "Academia B " + suf);
            String acadB = getAcademiaId(adminB);

            var multiUser = userService.register(
                    "multi_" + suf, "multi_" + suf + "@sys.com", "senha12345",
                    Map.of(acadA, Set.of(RoleInitializer.ROLE_ADMIN_ID),
                           acadB, Set.of(RoleInitializer.ROLE_EMPLOYEE_ID)),
                    "CREF-MU-" + suf, "30000000" + suf);
            String muId = multiUser.getId();

            try {
                simulateFilterChain(muId, acadA);
                var auth = SecurityContextHolder.getContext().getAuthentication();
                var permNames = auth.getAuthorities().stream()
                        .map(a -> a.getAuthority()).toList();
                assertThat(permNames).contains("CLIENTES_CRIAR");
                assertThat(permNames).contains("USUARIOS_CRIAR");
                assertThat(permNames).contains("ACADEMIAS_ATUALIZAR");
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("em academia B (employee): pode ler mas tem menos permissões")
        void emAcademiaBMenosPermissoes() {
            String suf = s();
            String adminA = registerOwner("ownerA_" + suf, "Academia A " + suf);
            String acadA = getAcademiaId(adminA);
            String adminB = registerOwner("ownerB_" + suf, "Academia B " + suf);
            String acadB = getAcademiaId(adminB);

            var multiUser = userService.register(
                    "multi_" + suf, "multi_" + suf + "@sys.com", "senha12345",
                    Map.of(acadA, Set.of(RoleInitializer.ROLE_ADMIN_ID),
                           acadB, Set.of(RoleInitializer.ROLE_EMPLOYEE_ID)),
                    "CREF-MU-" + suf, "30000000" + suf);
            String muId = multiUser.getId();

            try {
                simulateFilterChain(muId, acadB);
                var auth = SecurityContextHolder.getContext().getAuthentication();
                var permNames = auth.getAuthorities().stream()
                        .map(a -> a.getAuthority()).toList();
                assertThat(permNames).contains("CLIENTES_CRIAR");
                assertThat(permNames).contains("CLIENTES_LER");
                assertThat(permNames).doesNotContain("USUARIOS_CRIAR");
                assertThat(permNames).doesNotContain("ACADEMIAS_ATUALIZAR");
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("admin de academia A não enxerga dados da academia B")
        void adminDeANaoEnxergaB() {
            String suf = s();
            String adminA = registerOwner("ownerA_" + suf, "Academia A " + suf);
            String acadA = getAcademiaId(adminA);
            String adminB = registerOwner("ownerB_" + suf, "Academia B " + suf);
            String acadB = getAcademiaId(adminB);

            createCliente("Cliente B", acadB, adminB);

            try {
                simulateFilterChain(adminA, acadA);
                assertThat(clienteService.listAll()).isEmpty();
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("multi-academia user cria cliente em A — não aparece em B")
        void criaEmANaoApareceEmB() {
            String suf = s();
            String adminA = registerOwner("ownerA_" + suf, "Academia A " + suf);
            String acadA = getAcademiaId(adminA);
            String adminB = registerOwner("ownerB_" + suf, "Academia B " + suf);
            String acadB = getAcademiaId(adminB);

            var multiUser = userService.register(
                    "multi_" + suf, "multi_" + suf + "@sys.com", "senha12345",
                    Map.of(acadA, Set.of(RoleInitializer.ROLE_ADMIN_ID),
                           acadB, Set.of(RoleInitializer.ROLE_EMPLOYEE_ID)),
                    "CREF-MU-" + suf, "30000000" + suf);
            String muId = multiUser.getId();

            try {
                simulateFilterChain(muId, acadA);
                clienteService.create(buildClienteReq("Cliente A", acadA));
            } finally { clearSecurityContext(); }

            try {
                simulateFilterChain(muId, acadB);
                assertThat(clienteService.listAll()).isEmpty();
            } finally { clearSecurityContext(); }
        }
    }

    // ========================================================================
    // CENÁRIO 4: Cross-Academia Attacks
    // ========================================================================
    @Nested
    @DisplayName("4. Cross-Academia Attacks — tentativas de acesso cruzado")
    class CrossAcademiaAttacks {

        @Test
        @DisplayName("admin A cria cliente → tenta ler de B → vazio")
        void tentaLerDeOutraAcademia() {
            String s1 = s(); String s2 = s();
            String uA = registerOwner("a_" + s1, "Academia X " + s1);
            String acadA = getAcademiaId(uA);
            String uB = registerOwner("b_" + s2, "Academia Y " + s2);
            String acadB = getAcademiaId(uB);

            createCliente("Victim", acadA, uA);

            try {
                simulateFilterChain(uA, acadB);
                assertThat(clienteService.listAll()).isEmpty();
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("admin A tenta atualizar cliente de B → bloqueado pelo scoping")
        void tentaAtualizarDeOutraAcademia() {
            String s1 = s(); String s2 = s();
            String uA = registerOwner("a_" + s1, "Academia X " + s1);
            String acadA = getAcademiaId(uA);
            String uB = registerOwner("b_" + s2, "Academia Y " + s2);
            String acadB = getAcademiaId(uB);

            String cliB = createCliente("Victim B", acadB, uB);

            try {
                simulateFilterChain(uA, acadA);
                var req = buildClienteReq("HACKED", acadA);
                // @ScopedByAcademia filtra findById antes de fetchScoped → NoSuchElementException
                assertAccessBlocked(() -> clienteService.update(cliB, req));
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("user convidado só para academia A → não tem permissões em B → findById bloqueado")
        void convidadoNaoAcessaB() {
            String suf = s();
            String adminA = registerOwner("admA_" + suf, "Academia A " + suf);
            String acadA = getAcademiaId(adminA);
            String adminB = registerOwner("admB_" + suf, "Academia B " + suf);
            String acadB = getAcademiaId(adminB);

            String empId = inviteUser("emp_" + suf, acadA, RoleInitializer.ROLE_EMPLOYEE_ID);
            String cliB = createCliente("Cliente B", acadB, adminB);

            // Prova 1: academiaPermissoes do employee NÃO contém acadB
            // (TenantContextFilter real bloquearia com 403)
            try {
                simulateFilterChain(empId, acadA);
                var userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();
                assertThat(userDetails.getAcademiaPermissoes()).doesNotContainKey(acadB);
            } finally { clearSecurityContext(); }

            // Prova 2: findById de cliente de B é bloqueado pelo @ScopedByAcademia
            try {
                simulateFilterChain(empId, acadA);
                assertAccessBlocked(() -> clienteService.findById(cliB));
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("4 academias isoladas — cada user só vê seus clientes, findById cross bloqueado")
        void quatroAcademiasIsoladas() {
            String[] owners = new String[4];
            String[] acads = new String[4];
            String[] clients = new String[4];

            for (int i = 0; i < 4; i++) {
                owners[i] = registerOwner("own" + i + "_" + s(), "Academia " + i);
                acads[i] = getAcademiaId(owners[i]);
                clients[i] = createCliente("Cliente " + i, acads[i], owners[i]);
            }

            // Cada user com seu TenantContext vê exatamente 1 cliente
            for (int i = 0; i < 4; i++) {
                try {
                    simulateFilterChain(owners[i], acads[i]);
                    var lista = clienteService.listAll();
                    assertThat(lista).hasSize(1);
                    assertThat(lista.get(0).id()).isEqualTo(clients[i]);
                } finally { clearSecurityContext(); }
            }

            // findById cross-academia é bloqueado
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (i == j) continue;
                    try {
                        simulateFilterChain(owners[i], acads[i]);
                        final String targetClientId = clients[j];
                        assertAccessBlocked(() -> clienteService.findById(targetClientId));
                    } finally { clearSecurityContext(); }
                }
            }
        }

        @Test
        @DisplayName("múltiplos users na mesma academia veem os mesmos clientes")
        void usersMesmaAcademiaVemMesmosClientes() {
            String suf = s();
            String admin = registerOwner("adm_" + suf, "Academia Shared " + suf);
            String acadId = getAcademiaId(admin);
            String emp1 = inviteUser("e1_" + suf, acadId, RoleInitializer.ROLE_EMPLOYEE_ID);
            String emp2 = inviteUser("e2_" + suf, acadId, RoleInitializer.ROLE_EMPLOYEE_ID);

            String c1 = createCliente("Cliente 1", acadId, admin);
            String c2 = createCliente("Cliente 2", acadId, admin);

            for (String uid : new String[]{admin, emp1, emp2}) {
                try {
                    simulateFilterChain(uid, acadId);
                    var lista = clienteService.listAll();
                    assertThat(lista).hasSize(2);
                    assertThat(lista.stream().map(c -> c.id()).toList())
                            .containsExactlyInAnyOrder(c1, c2);
                } finally { clearSecurityContext(); }
            }
        }
    }

    // ========================================================================
    // CENÁRIO 5: Client Move Blocking
    // ========================================================================
    @Nested
    @DisplayName("5. Client Move Blocking — impedir movimentação de cliente entre academias")
    class ClientMoveBlocking {

        @Test
        @DisplayName("update de cliente com academiaId diferente → bloqueado pelo scoping")
        void naoMoveParaOutraAcademia() {
            String s1 = s(); String s2 = s();
            String uA = registerOwner("a_" + s1, "Academia A " + s1);
            String acadA = getAcademiaId(uA);
            String uB = registerOwner("b_" + s2, "Academia B " + s2);
            String acadB = getAcademiaId(uB);

            String cliId = createCliente("Victim", acadA, uA);

            try {
                simulateFilterChain(uA, acadA);
                var req = buildClienteReq("Victim Moved", acadB);
                // @ScopedByAcademia filtra findById antes de fetchScoped → NoSuchElementException
                assertAccessBlocked(() -> clienteService.update(cliId, req));
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("update de cliente mantendo mesma academiaId → funciona")
        void updateMesmaAcademiaFunciona() {
            String suf = s();
            String admin = registerOwner("adm_" + suf, "Academia " + suf);
            String acadId = getAcademiaId(admin);
            String cliId = createCliente("Original", acadId, admin);

            try {
                simulateFilterChain(admin, acadId);
                var req = buildClienteReq("Updated", acadId);
                var resp = clienteService.update(cliId, req);
                assertThat(resp.fullName()).isEqualTo("Updated");
                assertThat(resp.id()).isEqualTo(cliId);
            } finally { clearSecurityContext(); }

            try {
                simulateFilterChain(admin, acadId);
                var clientes = clienteService.listAll();
                assertThat(clientes).hasSize(1);
                assertThat(clientes.get(0).fullName()).isEqualTo("Updated");
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("create de cliente com academiaId válido → funciona")
        void createComAcademiaIdValidoFunciona() {
            String suf = s();
            String admin = registerOwner("adm_" + suf, "Academia " + suf);
            String acadId = getAcademiaId(admin);

            try {
                simulateFilterChain(admin, acadId);
                var req = buildClienteReq("Novo Cliente", acadId);
                var resp = clienteService.create(req);
                assertThat(resp.id()).isNotBlank();
                assertThat(resp.fullName()).isEqualTo("Novo Cliente");
            } finally { clearSecurityContext(); }
        }

        @Test
        @DisplayName("create de cliente com academiaId inexistente → NoSuchElementException")
        void createComAcademiaIdInexistente() {
            String suf = s();
            String admin = registerOwner("adm_" + suf, "Academia " + suf);
            String acadId = getAcademiaId(admin);

            try {
                simulateFilterChain(admin, acadId);
                var req = buildClienteReq("Fantasma", "academia_inexistente_" + suf);
                assertThatThrownBy(() -> clienteService.create(req))
                        .isInstanceOf(java.util.NoSuchElementException.class);
            } finally { clearSecurityContext(); }
        }
    }
}
