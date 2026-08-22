package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.request.RegisterRequest;
import com.prosup.proinsight.bootstrap.RoleInitializer;
import com.prosup.proinsight.config.JwtTokenProvider;
import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.infrastructure.persistence.document.RefreshTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

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
        savedUser.setId("user-abc-123");
        savedUser.setUserName("personaluser");
        savedUser.setEmail("personal@test.com");
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

        var userDetails = new CustomUserDetails(
                new User("user-abc-123", "personaluser", "personal@test.com", "hashed-pw",
                        new HashMap<>(), true, 0, null, new HashSet<>(), null, null, null, null),
                List.of(new SimpleGrantedAuthority("CLIENTES_LER")),
                Map.of()
        );
        when(userDetailsService.toUserDetails(any())).thenReturn(userDetails);
    }

    @Test
    @DisplayName("registro SEM academia: deve criar AcademiaDocument pessoal")
    void registroSemAcademia_criaAcademiaPessoal() {
        var academiaDoc = new com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument();
        academiaDoc.setId("personal-academia-xyz");
        when(academiaRepository.save(any())).thenReturn(academiaDoc);

        var request = new RegisterRequest();
        request.setUserName("personaluser");
        request.setEmail("personal@test.com");
        request.setPassword("senha12345");
        request.setCref("CREF-001");
        request.setCpf("11122233344");

        registrationService.register(request);

        verify(academiaRepository).save(any());
    }

    @Test
    @DisplayName("registro SEM academia: deve usar academiaId como chave em academiaRoles")
    void registroSemAcademia_usaAcademiaIdComoChave() {
        var academiaDoc = new com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument();
        academiaDoc.setId("personal-academia-xyz");
        when(academiaRepository.save(any())).thenReturn(academiaDoc);

        var request = new RegisterRequest();
        request.setUserName("personaluser");
        request.setEmail("personal@test.com");
        request.setPassword("senha12345");
        request.setCref("CREF-001");
        request.setCpf("11122233344");

        registrationService.register(request);

        var userCaptor = ArgumentCaptor.forClass(UserDocument.class);
        verify(userRepository, times(2)).save(userCaptor.capture());

        var secondSave = userCaptor.getAllValues().get(1);
        assertThat(secondSave.getAcademiaRoles()).containsKey("personal-academia-xyz");
        assertThat(secondSave.getAcademiaRoles().get("personal-academia-xyz"))
                .contains(RoleInitializer.ROLE_ADMIN_ID);
        assertThat(secondSave.getAcademiaIds()).contains("personal-academia-xyz");
    }

    @Test
    @DisplayName("registro SEM academia: academiaPermissoes deve ter academiaId como chave")
    void registroSemAcademia_permissoesComAcademiaId() {
        var academiaDoc = new com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument();
        academiaDoc.setId("personal-academia-xyz");
        when(academiaRepository.save(any())).thenReturn(academiaDoc);

        var request = new RegisterRequest();
        request.setUserName("personaluser");
        request.setEmail("personal@test.com");
        request.setPassword("senha12345");
        request.setCref("CREF-001");
        request.setCpf("11122233344");

        var response = registrationService.register(request);

        assertThat(response.academiaPermissoes()).containsKey("personal-academia-xyz");
        assertThat(response.academiaPermissoes().get("personal-academia-xyz")).isNotEmpty();
    }

    @Test
    @DisplayName("registro COM academia: deve criar AcademiaDocument")
    void registroComAcademia_criaAcademia() {
        var academiaDoc = new com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument();
        academiaDoc.setId("academia-xyz");
        when(academiaRepository.save(any())).thenReturn(academiaDoc);

        var request = new RegisterRequest();
        request.setUserName("owneruser");
        request.setEmail("owner@test.com");
        request.setPassword("senha12345");
        request.setCref("CREF-002");
        request.setCpf("22233344455");
        request.setAcademiaNome("Academia Teste");

        registrationService.register(request);

        verify(academiaRepository).save(any());
    }

    @Test
    @DisplayName("registro COM academia: academiaPermissoes deve ter academiaId como chave")
    void registroComAcademia_permissoesComAcademiaId() {
        var academiaDoc = new com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument();
        academiaDoc.setId("academia-xyz");
        when(academiaRepository.save(any())).thenReturn(academiaDoc);

        var userDetails = new CustomUserDetails(
                new User("user-abc-123", "owneruser", "owner@test.com", "hashed-pw",
                        Map.of("academia-xyz", Set.of(RoleInitializer.ROLE_ADMIN_ID)),
                        true, 0, null, Set.of("academia-xyz"), null, null, null, null),
                List.of(new SimpleGrantedAuthority("CLIENTES_LER")),
                Map.of("academia-xyz", Set.of(Permissao.CLIENTES_LER))
        );
        when(userDetailsService.toUserDetails(any())).thenReturn(userDetails);

        var request = new RegisterRequest();
        request.setUserName("owneruser");
        request.setEmail("owner@test.com");
        request.setPassword("senha12345");
        request.setCref("CREF-002");
        request.setCpf("22233344455");
        request.setAcademiaNome("Academia Teste");

        var response = registrationService.register(request);

        assertThat(response.academiaPermissoes()).containsKey("academia-xyz");
    }

    @Test
    @DisplayName("registro: cref e cpf devem ser persistidos no UserDocument")
    void registro_persisteCrefECpf() {
        var academiaDoc = new com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument();
        academiaDoc.setId("personal-academia-xyz");
        when(academiaRepository.save(any())).thenReturn(academiaDoc);

        var request = new RegisterRequest();
        request.setUserName("avaliadoruser");
        request.setEmail("avaliador@test.com");
        request.setPassword("senha12345");
        request.setCref("  CREF-123  ");
        request.setCpf(" 111.222.333-44 ");

        registrationService.register(request);

        var userCaptor = ArgumentCaptor.forClass(UserDocument.class);
        verify(userRepository, times(2)).save(userCaptor.capture());

        var primeiroSave = userCaptor.getAllValues().get(0);
        assertThat(primeiroSave.getCref()).isEqualTo("CREF-123");
        assertThat(primeiroSave.getCpf()).isEqualTo("111.222.333-44");
    }
}
