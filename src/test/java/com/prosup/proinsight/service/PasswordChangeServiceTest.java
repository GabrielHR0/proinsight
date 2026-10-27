package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import com.prosup.proinsight.infrastructure.transaction.MongoTransactionExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordChangeServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MongoTransactionExecutor transactionExecutor;

    private PasswordChangeService service;

    @BeforeEach
    void setUp() {
        lenient().when(transactionExecutor.execute(any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
        service = new PasswordChangeService(
                userRepository, refreshTokenService, passwordEncoder, transactionExecutor);
    }

    @Test
    @DisplayName("changes the password and revokes refresh tokens inside the transaction")
    void changesPasswordSuccessfully() {
        UserDocument user = new UserDocument();
        user.setId("u1");
        user.setPassword("old-hash");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        service.changePassword("u1", "current", "new-password");

        assertThat(user.getPassword()).isEqualTo("new-hash");
        verify(userRepository).save(user);
        verify(refreshTokenService).revokeAllByUserId("u1");
    }

    @Test
    @DisplayName("rejects an incorrect current password without opening a transaction")
    void rejectsWrongCurrentPassword() {
        UserDocument user = new UserDocument();
        user.setId("u1");
        user.setPassword("old-hash");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword("u1", "wrong", "new-password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Senha atual incorreta");

        verify(transactionExecutor, never()).execute(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("rejects an unknown user")
    void rejectsUnknownUser() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changePassword("missing", "current", "new-password"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Usuário não encontrado");

        verify(transactionExecutor, never()).execute(any());
    }
}
