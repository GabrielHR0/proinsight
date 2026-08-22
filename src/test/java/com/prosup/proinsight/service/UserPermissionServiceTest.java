package com.prosup.proinsight.service;

import com.prosup.proinsight.domain.enums.Permissao;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPermissionServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    private UserPermissionService service;

    private static final String ROLE_ADMIN_ID = "role-admin";
    private static final String ROLE_READER_ID = "role-reader";

    @BeforeEach
    void setUp() {
        service = new UserPermissionService(userRepository, roleRepository);
    }

    private UserDocument buildUser(Map<String, Set<String>> academiaRoles, boolean active) {
        UserDocument user = new UserDocument();
        user.setId("user-1");
        user.setEmail("admin@test.com");
        user.setActive(active);
        user.setAcademiaRoles(academiaRoles);
        return user;
    }

    @Test
    @DisplayName("Permissões agrupadas por academia, com cache em segunda chamada")
    void permissoesPorAcademia() {
        UserDocument user = buildUser(Map.of(
                "academia-a", Set.of(ROLE_ADMIN_ID),
                "academia-b", Set.of(ROLE_READER_ID)), true);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(roleRepository.findAllById(Set.of(ROLE_ADMIN_ID, ROLE_READER_ID))).thenReturn(List.of(
                new RoleDocument(ROLE_ADMIN_ID, "admin", null,
                        Set.of(Permissao.CLIENTES_CRIAR, Permissao.CLIENTES_LER)),
                new RoleDocument(ROLE_READER_ID, "reader", null,
                        Set.of(Permissao.CLIENTES_LER))));

        var result = service.loadAcademiaPermissoes("user-1");

        assertThat(result.get("academia-a"))
                .containsExactlyInAnyOrder(Permissao.CLIENTES_CRIAR, Permissao.CLIENTES_LER);
        assertThat(result.get("academia-b")).containsExactly(Permissao.CLIENTES_LER);
    }

    @Test
    @DisplayName("evict recarrega do banco (resultado segue correto após invalidar)")
    void evictRecarrega() {
        UserDocument user = buildUser(Map.of(
                "academia-a", Set.of(ROLE_ADMIN_ID)), true);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(roleRepository.findAllById(Set.of(ROLE_ADMIN_ID))).thenReturn(List.of(
                new RoleDocument(ROLE_ADMIN_ID, "admin", null,
                        Set.of(Permissao.CLIENTES_CRIAR, Permissao.CLIENTES_LER))));

        service.loadAcademiaPermissoes("user-1");
        service.evict("user-1");

        var result = service.loadAcademiaPermissoes("user-1");

        assertThat(result.get("academia-a"))
                .containsExactlyInAnyOrder(Permissao.CLIENTES_CRIAR, Permissao.CLIENTES_LER);
    }

    @Test
    @DisplayName("Usuário inexistente lança UsernameNotFoundException")
    void usuarioInexistente() {
        when(userRepository.findById("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadAcademiaPermissoes("nao-existe"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Usuário desativado lança UsernameNotFoundException (fail-closed)")
    void usuarioDesativado() {
        UserDocument user = buildUser(Map.of("academia-a", Set.of(ROLE_ADMIN_ID)), false);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.loadAcademiaPermissoes("user-1"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Usuário sem roles não lança e retorna mapa vazio")
    void usuarioSemRoles() {
        UserDocument user = buildUser(null, true);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        var result = service.loadAcademiaPermissoes("user-1");

        assertThat(result).isEmpty();
    }
}
