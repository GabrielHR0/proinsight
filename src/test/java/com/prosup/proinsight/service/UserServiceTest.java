package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.RoleDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    @DisplayName("valida roles distintas com uma única consulta ao repositório")
    void validaRolesComUmaConsulta() {
        var academiaRoles = Map.of(
                "academia-a", Set.of("r1", "r2"),
                "academia-b", Set.of("r1"));
        when(roleRepository.findAllById(Set.of("r1", "r2")))
                .thenReturn(List.of(role("r1"), role("r2")));
        when(passwordEncoder.encode("senha")).thenReturn("hash");
        when(userRepository.save(any(UserDocument.class))).thenAnswer(inv -> {
            UserDocument doc = inv.getArgument(0);
            doc.setId("user-1");
            return doc;
        });

        var user = service.register(
                "trainer", "trainer@test.com", "senha",
                academiaRoles, null, null);

        assertThat(user.getId()).isEqualTo("user-1");
        assertThat(user.getAcademiaRoles()).containsKeys("academia-a", "academia-b");
        verify(roleRepository, times(1)).findAllById(any());
        verify(roleRepository, never()).existsById(any());
    }

    @Test
    @DisplayName("rejeita role ausente após consulta em lote")
    void rejeitaRoleAusente() {
        when(roleRepository.findAllById(any())).thenReturn(List.of(role("r1")));

        assertThatThrownBy(() -> service.register(
                "trainer", "trainer@test.com", "senha",
                Map.of("academia-a", Set.of("r1", "r2")), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role id not found: r2");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("não consulta roles quando o conjunto é vazio")
    void naoConsultaQuandoNaoHaRoles() {
        when(passwordEncoder.encode("senha")).thenReturn("hash");
        when(userRepository.save(any(UserDocument.class))).thenAnswer(inv -> {
            UserDocument doc = inv.getArgument(0);
            doc.setId("user-2");
            return doc;
        });

        service.register(
                "trainer", "trainer@test.com", "senha",
                Map.of("academia-a", Set.of()), null, null);

        verify(roleRepository, never()).findAllById(any());
        verify(roleRepository, never()).existsById(any());
    }

    private static RoleDocument role(String id) {
        return new RoleDocument(id, id, null, Set.of());
    }
}
