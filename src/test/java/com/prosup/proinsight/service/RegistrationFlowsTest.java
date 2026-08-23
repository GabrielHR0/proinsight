package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.request.RegisterRequest;
import com.prosup.proinsight.bootstrap.RoleInitializer;
import com.prosup.proinsight.config.JwtTokenProvider;
import com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument;
import com.prosup.proinsight.infrastructure.persistence.document.RefreshTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de validação dos 3 fluxos de autorização:
 * 1. Personal user (sem academia) — registra sem nome de academia
 * 2. Academia owner (com academia) — registra com nome de academia
 * 3. Invited user (convidado) — criado por admin via UserService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegistrationFlowsTest {

    @Mock private UserRepository userRepository;
    @Mock private AcademiaRepository academiaRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private CustomUserDetailsService userDetailsService;

    @InjectMocks private RegistrationService registrationService;

    private UserDocument savedUser;

    @BeforeEach
    void setUp() {
        savedUser = new UserDocument();
        savedUser.setId("user-123");
        savedUser.setUserName("testuser");
        savedUser.setEmail("test@test.com");
        savedUser.setPassword("hashed-pw");
        savedUser.setAcademiaIds(new HashSet<>());
        savedUser.setAcademiaRoles(new HashMap<>());

        when(passwordEncoder.encode(any())).thenReturn("hashed-pw");
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByUserName(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserDocument.class))).thenReturn(savedUser);

        var refreshTokenDoc = new RefreshTokenDocument();
        refreshTokenDoc.setId("refresh-token-123");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshTokenDoc);
        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt-token-xyz");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        var userDetails = new com.prosup.proinsight.domain.model.CustomUserDetails(
                new com.prosup.proinsight.domain.model.User(
                        "user-123", "testuser", "test@test.com", "hashed-pw",
                        new HashMap<>(), true, 0, null, new HashSet<>(),
                        null, null, null, null),
                List.of(new SimpleGrantedAuthority("CLIENTES_LER")),
                Map.of()
        );
        when(userDetailsService.toUserDetails(any())).thenReturn(userDetails);
    }

    private RegisterRequest buildRequest(String userName, String academiaNome) {
        var request = new RegisterRequest();
        request.setUserName(userName);
        request.setEmail(userName + "@test.com");
        request.setPassword("senha12345");
        request.setCref("CREF-001");
        request.setCpf("11122233344");
        if (academiaNome != null) {
            request.setAcademiaNome(academiaNome);
        }
        return request;
    }

    // ============================================================
    // FLOW 1: Personal User (sem academia)
    // ============================================================
    @Nested
    @DisplayName("Flow 1: Personal User (sem academia)")
    class PersonalUserFlow {

        @Test
        @DisplayName("deve criar AcademiaDocument com nome 'Personal - {userName}'")
        void deveCriarAcademiaPessoal() {
            var academiaDoc = new AcademiaDocument();
            academiaDoc.setId("personal-acad-001");
            when(academiaRepository.save(any())).thenReturn(academiaDoc);

            var request = buildRequest("personal_user", null);
            registrationService.register(request);

            var academiaCaptor = ArgumentCaptor.forClass(AcademiaDocument.class);
            verify(academiaRepository).save(academiaCaptor.capture());
            var savedAcademia = academiaCaptor.getValue();

            assertThat(savedAcademia.getNomeFantasia()).isEqualTo("Personal - personal_user");
            assertThat(savedAcademia.getOwnerId()).isEqualTo("user-123");
        }

        @Test
        @DisplayName("deve setar academiaIds no UserDocument com o ID da academia pessoal")
        void deveSetarAcademiaIdsNoUser() {
            var academiaDoc = new AcademiaDocument();
            academiaDoc.setId("personal-acad-001");
            when(academiaRepository.save(any())).thenReturn(academiaDoc);

            var request = buildRequest("personal_user", null);
            registrationService.register(request);

            var userCaptor = ArgumentCaptor.forClass(UserDocument.class);
            verify(userRepository, times(2)).save(userCaptor.capture());

            var secondSave = userCaptor.getAllValues().get(1);
            assertThat(secondSave.getAcademiaIds()).contains("personal-acad-001");
        }

        @Test
        @DisplayName("deve setar academiaRoles com role_admin na academia pessoal")
        void deveSetarAcademiaRolesComAdmin() {
            var academiaDoc = new AcademiaDocument();
            academiaDoc.setId("personal-acad-001");
            when(academiaRepository.save(any())).thenReturn(academiaDoc);

            var request = buildRequest("personal_user", null);
            registrationService.register(request);

            var userCaptor = ArgumentCaptor.forClass(UserDocument.class);
            verify(userRepository, times(2)).save(userCaptor.capture());

            var secondSave = userCaptor.getAllValues().get(1);
            assertThat(secondSave.getAcademiaRoles()).containsKey("personal-acad-001");
            assertThat(secondSave.getAcademiaRoles().get("personal-acad-001"))
                    .contains(RoleInitializer.ROLE_ADMIN_ID);
        }

        @Test
        @DisplayName("academiaPermissoes na resposta deve conter a academiaId como key")
        void deveRetornarPermissoesComAcademiaId() {
            var academiaDoc = new AcademiaDocument();
            academiaDoc.setId("personal-acad-001");
            when(academiaRepository.save(any())).thenReturn(academiaDoc);

            var request = buildRequest("personal_user", null);
            var response = registrationService.register(request);

            assertThat(response.academiaPermissoes()).containsKey("personal-acad-001");
            assertThat(response.academiaPermissoes().get("personal-acad-001")).isNotEmpty();
        }

        @Test
        @DisplayName("cref e cpf devem ser persistidos")
        void devePersistirCrefECpf() {
            var academiaDoc = new AcademiaDocument();
            academiaDoc.setId("personal-acad-001");
            when(academiaRepository.save(any())).thenReturn(academiaDoc);

            var request = buildRequest("personal_user", null);
            registrationService.register(request);

            var userCaptor = ArgumentCaptor.forClass(UserDocument.class);
            verify(userRepository, times(2)).save(userCaptor.capture());

            var firstSave = userCaptor.getAllValues().get(0);
            assertThat(firstSave.getCref()).isEqualTo("CREF-001");
            assertThat(firstSave.getCpf()).isEqualTo("11122233344");
        }
    }

    // ============================================================
    // FLOW 2: Academia Owner (com academia)
    // ============================================================
    @Nested
    @DisplayName("Flow 2: Academia Owner (com academia)")
    class AcademiaOwnerFlow {

        @Test
        @DisplayName("deve criar AcademiaDocument com dados informados pelo user")
        void deveCriarAcademiaComDadosInformados() {
            var academiaDoc = new AcademiaDocument();
            academiaDoc.setId("academia-xyz");
            when(academiaRepository.save(any())).thenReturn(academiaDoc);

            var request = buildRequest("gym_owner", "GymPower");
            request.setRazaoSocial("GymPower LTDA");
            request.setCnpj("12345678000199");
            request.setTelefone("11999998888");
            registrationService.register(request);

            var academiaCaptor = ArgumentCaptor.forClass(AcademiaDocument.class);
            verify(academiaRepository).save(academiaCaptor.capture());
            var savedAcademia = academiaCaptor.getValue();

            assertThat(savedAcademia.getNomeFantasia()).isEqualTo("GymPower");
            assertThat(savedAcademia.getRazaoSocial()).isEqualTo("GymPower LTDA");
            assertThat(savedAcademia.getCnpj()).isEqualTo("12345678000199");
            assertThat(savedAcademia.getOwnerId()).isEqualTo("user-123");
        }

        @Test
        @DisplayName("deve setar academiaIds no UserDocument")
        void deveSetarAcademiaIdsNoUser() {
            var academiaDoc = new AcademiaDocument();
            academiaDoc.setId("academia-xyz");
            when(academiaRepository.save(any())).thenReturn(academiaDoc);

            var request = buildRequest("gym_owner", "GymPower");
            registrationService.register(request);

            var userCaptor = ArgumentCaptor.forClass(UserDocument.class);
            verify(userRepository, times(2)).save(userCaptor.capture());

            var secondSave = userCaptor.getAllValues().get(1);
            assertThat(secondSave.getAcademiaIds()).contains("academia-xyz");
        }

        @Test
        @DisplayName("deve setar academiaRoles com role_admin")
        void deveSetarAcademiaRolesComAdmin() {
            var academiaDoc = new AcademiaDocument();
            academiaDoc.setId("academia-xyz");
            when(academiaRepository.save(any())).thenReturn(academiaDoc);

            var request = buildRequest("gym_owner", "GymPower");
            registrationService.register(request);

            var userCaptor = ArgumentCaptor.forClass(UserDocument.class);
            verify(userRepository, times(2)).save(userCaptor.capture());

            var secondSave = userCaptor.getAllValues().get(1);
            assertThat(secondSave.getAcademiaRoles()).containsKey("academia-xyz");
            assertThat(secondSave.getAcademiaRoles().get("academia-xyz"))
                    .contains(RoleInitializer.ROLE_ADMIN_ID);
        }

        @Test
        @DisplayName("academiaPermissoes na resposta deve conter a academiaId como key")
        void deveRetornarPermissoesComAcademiaId() {
            var academiaDoc = new AcademiaDocument();
            academiaDoc.setId("academia-xyz");
            when(academiaRepository.save(any())).thenReturn(academiaDoc);

            var request = buildRequest("gym_owner", "GymPower");
            var response = registrationService.register(request);

            assertThat(response.academiaPermissoes()).containsKey("academia-xyz");
            assertThat(response.academiaPermissoes().get("academia-xyz")).isNotEmpty();
        }
    }

    // ============================================================
    // FLOW 3: Invited User (convidado) — tested via UserService
    // ============================================================
    @Nested
    @DisplayName("Flow 3: Invited User (via UserService)")
    class InvitedUserFlow {

        @Test
        @DisplayName("UserService.register deve setar academiaIds no UserDocument")
        void deveSetarAcademiaIdsParaUserConvidado() {
            var academiaRoles = Map.of(
                "existing-academia-id", Set.of(RoleInitializer.ROLE_EMPLOYEE_ID)
            );

            var mockRoleRepo = mock(com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository.class);
            var mockUserRepo = mock(com.prosup.proinsight.infrastructure.persistence.repository.UserRepository.class);
            var mockPasswordEncoder = mock(PasswordEncoder.class);

            when(mockRoleRepo.existsById(any())).thenReturn(true);
            when(mockPasswordEncoder.encode(any())).thenReturn("hashed-pw");

            var docToReturn = new UserDocument();
            docToReturn.setId("invited-user-456");
            when(mockUserRepo.save(any(UserDocument.class))).thenAnswer(inv -> {
                UserDocument arg = inv.getArgument(0);
                arg.setId("invited-user-456");
                return arg;
            });

            var userService = new com.prosup.proinsight.service.UserService(
                    mockUserRepo, mockRoleRepo, mockPasswordEncoder);

            var user = userService.register(
                    "trainer_jane", "jane@gym.com", "password123",
                    academiaRoles, null, null);

            var userCaptor = ArgumentCaptor.forClass(UserDocument.class);
            verify(mockUserRepo).save(userCaptor.capture());
            var savedDoc = userCaptor.getValue();

            assertThat(savedDoc.getAcademiaIds()).contains("existing-academia-id");
            assertThat(savedDoc.getAcademiaRoles()).containsKey("existing-academia-id");
        }

        @Test
        @DisplayName("UserService.register deve setar cref e cpf quando fornecidos")
        void deveSetarCrefECpfParaUserConvidado() {
            var academiaRoles = Map.of(
                "existing-academia-id", Set.of(RoleInitializer.ROLE_EMPLOYEE_ID)
            );

            var mockRoleRepo = mock(com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository.class);
            var mockUserRepo = mock(com.prosup.proinsight.infrastructure.persistence.repository.UserRepository.class);
            var mockPasswordEncoder = mock(PasswordEncoder.class);

            when(mockRoleRepo.existsById(any())).thenReturn(true);
            when(mockPasswordEncoder.encode(any())).thenReturn("hashed-pw");

            var docToReturn = new UserDocument();
            docToReturn.setId("invited-user-456");
            when(mockUserRepo.save(any(UserDocument.class))).thenAnswer(inv -> {
                UserDocument arg = inv.getArgument(0);
                arg.setId("invited-user-456");
                return arg;
            });

            var userService = new com.prosup.proinsight.service.UserService(
                    mockUserRepo, mockRoleRepo, mockPasswordEncoder);

            var user = userService.register(
                    "trainer_jane", "jane@gym.com", "password123",
                    academiaRoles, "CREF-999", "55566677788");

            var userCaptor = ArgumentCaptor.forClass(UserDocument.class);
            verify(mockUserRepo).save(userCaptor.capture());
            var savedDoc = userCaptor.getValue();

            assertThat(savedDoc.getCref()).isEqualTo("CREF-999");
            assertThat(savedDoc.getCpf()).isEqualTo("55566677788");
        }

        @Test
        @DisplayName("UserService.register deve setar academiaIds para múltiplas academias")
        void deveSetarAcademiaIdsMultiplasAcademias() {
            var academiaRoles = Map.of(
                "academia-a", Set.of(RoleInitializer.ROLE_ADMIN_ID),
                "academia-b", Set.of(RoleInitializer.ROLE_EMPLOYEE_ID)
            );

            var mockRoleRepo = mock(com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository.class);
            var mockUserRepo = mock(com.prosup.proinsight.infrastructure.persistence.repository.UserRepository.class);
            var mockPasswordEncoder = mock(PasswordEncoder.class);

            when(mockRoleRepo.existsById(any())).thenReturn(true);
            when(mockPasswordEncoder.encode(any())).thenReturn("hashed-pw");

            when(mockUserRepo.save(any(UserDocument.class))).thenAnswer(inv -> {
                UserDocument arg = inv.getArgument(0);
                arg.setId("multi-acad-user");
                return arg;
            });

            var userService = new com.prosup.proinsight.service.UserService(
                    mockUserRepo, mockRoleRepo, mockPasswordEncoder);

            userService.register(
                    "multi_user", "multi@test.com", "password123",
                    academiaRoles, null, null);

            var userCaptor = ArgumentCaptor.forClass(UserDocument.class);
            verify(mockUserRepo).save(userCaptor.capture());
            var savedDoc = userCaptor.getValue();

            assertThat(savedDoc.getAcademiaIds()).containsExactlyInAnyOrder("academia-a", "academia-b");
            assertThat(savedDoc.getAcademiaRoles()).containsKey("academia-a");
            assertThat(savedDoc.getAcademiaRoles()).containsKey("academia-b");
        }

        @Test
        @DisplayName("UserService.register deve rejeitar role inexistente")
        void deveRejeitarRoleInexistente() {
            var academiaRoles = Map.of(
                "existing-academia-id", Set.of("role_inexistente")
            );

            var mockRoleRepo = mock(com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository.class);
            var mockUserRepo = mock(com.prosup.proinsight.infrastructure.persistence.repository.UserRepository.class);
            var mockPasswordEncoder = mock(PasswordEncoder.class);

            when(mockRoleRepo.existsById("role_inexistente")).thenReturn(false);

            var userService = new com.prosup.proinsight.service.UserService(
                    mockUserRepo, mockRoleRepo, mockPasswordEncoder);

            var ex = org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.register(
                            "bad_user", "bad@test.com", "password123",
                            academiaRoles, null, null));

            assertThat(ex.getMessage()).contains("Role id not found: role_inexistente");
        }
    }
}
