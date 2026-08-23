package com.prosup.proinsight.service;

import com.prosup.proinsight.AbstractIntegrationTest;
import com.prosup.proinsight.api.dto.request.RegisterRequest;
import com.prosup.proinsight.bootstrap.RoleInitializer;
import com.prosup.proinsight.config.JwtTokenProvider;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração E2E que provam que os 3 fluxos de autorização
 * funcionam de ponta a ponta com MongoDB real:
 *
 * Flow 1: Personal User — registra sem academia → academia pessoal criada
 * Flow 2: Academia Owner — registra com academia → academia nomeada criada
 * Flow 3: Invited User — admin cria user → academiaIds sincronizado
 *
 * Cada fluxo valida:
 * - Persistência no MongoDB (UserDocument + AcademiaDocument)
 * - Resolução de permissões via CustomUserDetailsService
 * - Scoping de dados via TenantContext + X-Academia-Id
 * - Criação e listagem de clientes no escopo da academia
 */
@DisplayName("E2E: Todos os 3 fluxos de autorização com MongoDB real")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthorizationFlowsIT extends AbstractIntegrationTest {

    @Autowired private RegistrationService registrationService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private AcademiaRepository academiaRepository;
    @Autowired private com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository clienteRepository;
    @Autowired private CustomUserDetailsService userDetailsService;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private ClienteService clienteService;

    @BeforeAll
    void cleanDatabase() {
        clienteRepository.deleteAll();
        userRepository.deleteAll();
        academiaRepository.deleteAll();
    }

    /**
     * Helper: simula o que os filtros fazem — resolve permissões e seta TenantContext
     * a partir de um academiaId específico.
     */
    private void simulateFilterChain(String userId, String academiaId) {
        var user = userRepository.findById(userId).orElseThrow();
        var domainUser = com.prosup.proinsight.infrastructure.persistence.mapper.UserMapper.toDomain(user);
        CustomUserDetails userDetails = userDetailsService.toUserDetails(domainUser);

        // Simula JwtAuthenticationFilter.refreshAuthorities(): resolve authorities do academiaId
        var permissaoMap = userDetails.getAcademiaPermissoes();
        Set<String> effectivePerms = Set.of();
        if (permissaoMap != null && permissaoMap.containsKey(academiaId)) {
            effectivePerms = permissaoMap.get(academiaId).stream()
                    .map(Enum::name).collect(java.util.stream.Collectors.toSet());
        }
        var authorities = effectivePerms.stream()
                .map(p -> new org.springframework.security.core.authority.SimpleGrantedAuthority(p))
                .toList();
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Simula TenantContextFilter
        com.prosup.proinsight.config.TenantContext.setInHttpRequest(true);
        com.prosup.proinsight.config.TenantContext.setAcademiaId(academiaId);
    }

    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        com.prosup.proinsight.config.TenantContext.clear();
    }

    // ============================================================
    // FLOW 1: Personal User (sem academia)
    // ============================================================
    @Nested
    @DisplayName("Flow 1: Personal User — registra sem academia → academia pessoal criada")
    class PersonalUserFlow {

        @Test
        @DisplayName("registro cria AcademiaDocument real com nome 'Personal - {userName}'")
        void registroCriaAcademiaPessoal() {
            var request = new RegisterRequest();
            request.setUserName("personal_" + UUID.randomUUID().toString().substring(0, 6));
            request.setEmail("personal_" + UUID.randomUUID().toString().substring(0, 6) + "@test.com");
            request.setPassword("senha12345");
            request.setCref("CREF-PER-" + UUID.randomUUID().toString().substring(0, 4));
            request.setCpf("12345678901");

            var response = registrationService.register(request);

            // Academia foi criada
            assertThat(response.academiaPermissoes()).isNotEmpty();
            String academiaId = response.academiaPermissoes().keySet().iterator().next();

            var academia = academiaRepository.findById(academiaId).orElseThrow();
            assertThat(academia.getNomeFantasia()).startsWith("Personal - ");
            assertThat(academia.getOwnerId()).isEqualTo(response.userId());

            // User no MongoDB tem academiaIds setado
            var userDoc = userRepository.findById(response.userId()).orElseThrow();
            assertThat(userDoc.getAcademiaIds()).contains(academiaId);
            assertThat(userDoc.getAcademiaRoles()).containsKey(academiaId);
            assertThat(userDoc.getAcademiaRoles().get(academiaId))
                    .contains(RoleInitializer.ROLE_ADMIN_ID);
        }

        @Test
        @DisplayName("personal user pode criar e listar clientes no escopo da sua academia")
        void personalUserCriaEListaClientes() {
            var suf = UUID.randomUUID().toString().substring(0, 6);
            var request = new RegisterRequest();
            request.setUserName("personal_crud_" + suf);
            request.setEmail("personal_crud_" + suf + "@test.com");
            request.setPassword("senha12345");
            request.setCref("CREF-PC-" + suf);
            request.setCpf("11122233344");

            var reg = registrationService.register(request);
            String academiaId = reg.academiaPermissoes().keySet().iterator().next();

            try {
                simulateFilterChain(reg.userId(), academiaId);

                // Cria cliente
                var clienteRequest = new com.prosup.proinsight.api.dto.request.ClienteRequest();
                clienteRequest.setFullName("Cliente Personal");
                clienteRequest.setAcademiaId(academiaId);
                var created = clienteService.create(clienteRequest);

                assertThat(created).isNotNull();
                assertThat(created.id()).isNotBlank();

                // Lista clientes — deve retornar o cliente criado
                var clientes = clienteService.listAll();
                assertThat(clientes).hasSize(1);
                assertThat(clientes.get(0).fullName()).isEqualTo("Cliente Personal");
            } finally {
                clearSecurityContext();
            }
        }

        @Test
        @DisplayName("personal user NÃO pode acessar dados de outra academia")
        void personalUserNaoAcessaOutraAcademia() {
            var suf1 = UUID.randomUUID().toString().substring(0, 6);
            var suf2 = UUID.randomUUID().toString().substring(0, 6);

            // Registra user A
            var reqA = new RegisterRequest();
            reqA.setUserName("isolated_a_" + suf1);
            reqA.setEmail("isolated_a_" + suf1 + "@test.com");
            reqA.setPassword("senha12345");
            reqA.setCref("CREF-IA-" + suf1);
            reqA.setCpf("111111111" + suf1);
            var regA = registrationService.register(reqA);
            String academiaA = regA.academiaPermissoes().keySet().iterator().next();

            // Registra user B
            var reqB = new RegisterRequest();
            reqB.setUserName("isolated_b_" + suf2);
            reqB.setEmail("isolated_b_" + suf2 + "@test.com");
            reqB.setPassword("senha12345");
            reqB.setCref("CREF-IB-" + suf2);
            reqB.setCpf("222222222" + suf2);
            var regB = registrationService.register(reqB);
            String academiaB = regB.academiaPermissoes().keySet().iterator().next();

            try {
                // User A cria cliente na sua academia
                simulateFilterChain(regA.userId(), academiaA);
                var clienteRequest = new com.prosup.proinsight.api.dto.request.ClienteRequest();
                clienteRequest.setFullName("Cliente de A");
                clienteRequest.setAcademiaId(academiaA);
                clienteService.create(clienteRequest);

                // User A tenta listar clientes usando X-Academia-Id de B
                clearSecurityContext();
                simulateFilterChain(regA.userId(), academiaB);

                // Deve retornar vazio (isolamento por academia)
                var clientes = clienteService.listAll();
                assertThat(clientes).isEmpty();
            } finally {
                clearSecurityContext();
            }
        }
    }

    // ============================================================
    // FLOW 2: Academia Owner (com academia)
    // ============================================================
    @Nested
    @DisplayName("Flow 2: Academia Owner — registra com academia → academia nomeada criada")
    class AcademiaOwnerFlow {

        @Test
        @DisplayName("registro cria AcademiaDocument com nome e dados informados")
        void registroCriaAcademiaComDadosInformados() {
            var suf = UUID.randomUUID().toString().substring(0, 6);
            var request = new RegisterRequest();
            request.setUserName("owner_" + suf);
            request.setEmail("owner_" + suf + "@test.com");
            request.setPassword("senha12345");
            request.setCref("CREF-OW-" + suf);
            request.setCpf("99988877766");
            request.setAcademiaNome("GymPower " + suf);
            request.setRazaoSocial("GymPower LTDA");
            request.setCnpj("12345678000199");

            var response = registrationService.register(request);

            String academiaId = response.academiaPermissoes().keySet().iterator().next();
            var academia = academiaRepository.findById(academiaId).orElseThrow();
            assertThat(academia.getNomeFantasia()).isEqualTo("GymPower " + suf);
            assertThat(academia.getRazaoSocial()).isEqualTo("GymPower LTDA");
            assertThat(academia.getOwnerId()).isEqualTo(response.userId());

            var userDoc = userRepository.findById(response.userId()).orElseThrow();
            assertThat(userDoc.getAcademiaIds()).contains(academiaId);
        }

        @Test
        @DisplayName("owner pode criar e listar clientes no escopo da sua academia")
        void ownerCriaEListaClientes() {
            var suf = UUID.randomUUID().toString().substring(0, 6);
            var request = new RegisterRequest();
            request.setUserName("owner_crud_" + suf);
            request.setEmail("owner_crud_" + suf + "@test.com");
            request.setPassword("senha12345");
            request.setCref("CREF-OC-" + suf);
            request.setCpf("33344455566");
            request.setAcademiaNome("Academia CRUD " + suf);

            var reg = registrationService.register(request);
            String academiaId = reg.academiaPermissoes().keySet().iterator().next();

            try {
                simulateFilterChain(reg.userId(), academiaId);

                var clienteRequest = new com.prosup.proinsight.api.dto.request.ClienteRequest();
                clienteRequest.setFullName("Cliente Owner");
                clienteRequest.setAcademiaId(academiaId);
                var created = clienteService.create(clienteRequest);
                assertThat(created.id()).isNotBlank();

                var clientes = clienteService.listAll();
                assertThat(clientes).hasSize(1);
                assertThat(clientes.get(0).fullName()).isEqualTo("Cliente Owner");
            } finally {
                clearSecurityContext();
            }
        }
    }

    // ============================================================
    // FLOW 3: Invited User (convidado por academia)
    // ============================================================
    @Nested
    @DisplayName("Flow 3: Invited User — admin cria user convidado → funciona na academia")
    class InvitedUserFlow {

        @Test
        @DisplayName("admin cria academia, convida user, user convidado pode acessar clientes")
        void adminCriaEConvidadoAcessa() {
            // Passo 1: Admin registra academia
            var adminSuf = UUID.randomUUID().toString().substring(0, 6);
            var adminReq = new RegisterRequest();
            adminReq.setUserName("admin_invite_" + adminSuf);
            adminReq.setEmail("admin_invite_" + adminSuf + "@test.com");
            adminReq.setPassword("senha12345");
            adminReq.setCref("CREF-AD-" + adminSuf);
            adminReq.setCpf("111111111" + adminSuf);
            adminReq.setAcademiaNome("Academia Convite " + adminSuf);

            var adminReg = registrationService.register(adminReq);
            String academiaId = adminReg.academiaPermissoes().keySet().iterator().next();

            // Passo 2: Admin convida user para a academia
            var invitedSuf = UUID.randomUUID().toString().substring(0, 6);
            var academiaRoles = Map.of(academiaId, Set.of(RoleInitializer.ROLE_EMPLOYEE_ID));
            var invitedUser = userService.register(
                    "trainer_" + invitedSuf,
                    "trainer_" + invitedSuf + "@test.com",
                    "senha12345",
                    academiaRoles,
                    "CREF-TV-" + invitedSuf,
                    "22233344455");

            // Passo 3: Verifica que o user convidado tem academiaIds setado
            var invitedDoc = userRepository.findById(invitedUser.getId()).orElseThrow();
            assertThat(invitedDoc.getAcademiaIds()).contains(academiaId);
            assertThat(invitedDoc.getAcademiaRoles()).containsKey(academiaId);
            assertThat(invitedDoc.getCref()).isEqualTo("CREF-TV-" + invitedSuf);
            assertThat(invitedDoc.getCpf()).isEqualTo("22233344455");

            // Passo 4: Admin cria cliente na academia
            try {
                simulateFilterChain(adminReg.userId(), academiaId);
                var clienteRequest = new com.prosup.proinsight.api.dto.request.ClienteRequest();
                clienteRequest.setFullName("Cliente Convidado Test");
                clienteRequest.setAcademiaId(academiaId);
                clienteService.create(clienteRequest);
            } finally {
                clearSecurityContext();
            }

            // Passo 5: User convidado acessa a mesma academia — encontra o cliente
            try {
                simulateFilterChain(invitedUser.getId(), academiaId);
                var clientes = clienteService.listAll();
                assertThat(clientes).hasSize(1);
                assertThat(clientes.get(0).fullName()).isEqualTo("Cliente Convidado Test");
            } finally {
                clearSecurityContext();
            }
        }

        @Test
        @DisplayName("user convidado NÃO pode acessar outra academia")
        void convidadoNaoAcessaOutraAcademia() {
            var suf = UUID.randomUUID().toString().substring(0, 6);

            // Admin cria academia A
            var adminReq = new RegisterRequest();
            adminReq.setUserName("admin_iso_" + suf);
            adminReq.setEmail("admin_iso_" + suf + "@test.com");
            adminReq.setPassword("senha12345");
            adminReq.setCref("CREF-AI-" + suf);
            adminReq.setCpf("111111111" + suf);
            adminReq.setAcademiaNome("Academia ISO A " + suf);
            var adminReg = registrationService.register(adminReq);
            String academiaA = adminReg.academiaPermissoes().keySet().iterator().next();

            // Admin cria academia B
            var adminReq2 = new RegisterRequest();
            adminReq2.setUserName("admin_iso2_" + suf);
            adminReq2.setEmail("admin_iso2_" + suf + "@test.com");
            adminReq2.setPassword("senha12345");
            adminReq2.setCref("CREF-A2-" + suf);
            adminReq2.setCpf("222222222" + suf);
            adminReq2.setAcademiaNome("Academia ISO B " + suf);
            var adminReg2 = registrationService.register(adminReq2);
            String academiaB = adminReg2.academiaPermissoes().keySet().iterator().next();

            // Admin A convida user apenas para academia A
            var trainerSuf = UUID.randomUUID().toString().substring(0, 6);
            var invitedUser = userService.register(
                    "trainer_iso_" + trainerSuf,
                    "trainer_iso_" + trainerSuf + "@test.com",
                    "senha12345",
                    Map.of(academiaA, Set.of(RoleInitializer.ROLE_EMPLOYEE_ID)),
                    null, null);

            // Admin A cria cliente
            try {
                simulateFilterChain(adminReg.userId(), academiaA);
                var clienteRequest = new com.prosup.proinsight.api.dto.request.ClienteRequest();
                clienteRequest.setFullName("Cliente A");
                clienteRequest.setAcademiaId(academiaA);
                clienteService.create(clienteRequest);
            } finally {
                clearSecurityContext();
            }

            // User convidado acessa academia A — encontra cliente
            try {
                simulateFilterChain(invitedUser.getId(), academiaA);
                var clientesA = clienteService.listAll();
                assertThat(clientesA).hasSize(1);
            } finally {
                clearSecurityContext();
            }

            // User convidado tenta acessar academia B — vazio (não tem acesso)
            try {
                simulateFilterChain(invitedUser.getId(), academiaB);
                var clientesB = clienteService.listAll();
                assertThat(clientesB).isEmpty();
            } finally {
                clearSecurityContext();
            }
        }
    }
}
