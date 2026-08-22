package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.PasswordResetTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.PasswordResetTokenRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetService service;

    private static final long TTL_MS = 900_000; // 15 minutos

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(tokenRepository, userRepository,
                refreshTokenService, passwordEncoder, TTL_MS);
    }

    @Test
    @DisplayName("createResetToken → email existente → devolve token puro e salva hash")
    void createToken_sucesso() {
        UserDocument user = new UserDocument();
        user.setId("u1");
        user.setEmail("user@test.com");
        user.setActive(true);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        String rawToken = service.createResetToken("user@test.com");

        assertThat(rawToken).isNotBlank();
        ArgumentCaptor<PasswordResetTokenDocument> captor =
                ArgumentCaptor.forClass(PasswordResetTokenDocument.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("u1");
        assertThat(captor.getValue().getTokenHash()).isNotEqualTo(rawToken);
    }

    @Test
    @DisplayName("createResetToken → email inexistente → devolve null (anti-enumeração)")
    void createToken_emailInexistente() {
        when(userRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());

        String result = service.createResetToken("x@test.com");

        assertThat(result).isNull();
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword → token válido → senha trocada e refresh tokens revogados")
    void resetPassword_sucesso() {
        UserDocument user = new UserDocument();
        user.setId("u1");
        user.setPassword("old-hash");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NovaSenha@123")).thenReturn("new-hash");

        PasswordResetTokenDocument token = new PasswordResetTokenDocument();
        token.setUserId("u1");
        token.setUsed(false);
        token.setExpiresAt(Instant.now().plusSeconds(600));
        when(tokenRepository.findByTokenHashAndUsedFalse(anyString())).thenReturn(Optional.of(token));

        service.resetPassword("any-token", "NovaSenha@123");

        assertThat(user.getPassword()).isEqualTo("new-hash");
        verify(userRepository).save(user);
        verify(refreshTokenService).revokeAllByUserId("u1");
    }

    @Test
    @DisplayName("resetPassword → token já utilizado → lança NoSuchElementException")
    void resetPassword_tokenJaUsado() {
        PasswordResetTokenDocument token = new PasswordResetTokenDocument();
        token.setUserId("u1");
        token.setUsed(true);
        token.setExpiresAt(Instant.now().plusSeconds(600));
        when(tokenRepository.findByTokenHashAndUsedFalse(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("any-token", "NovaSenha@123"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("resetPassword → token expirado → lança NoSuchElementException")
    void resetPassword_tokenExpirado() {
        PasswordResetTokenDocument token = new PasswordResetTokenDocument();
        token.setUserId("u1");
        token.setUsed(false);
        token.setExpiresAt(Instant.now().minusSeconds(10));
        when(tokenRepository.findByTokenHashAndUsedFalse(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("any-token", "NovaSenha@123"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("resetPassword → token inexistente → lança NoSuchElementException")
    void resetPassword_tokenInexistente() {
        when(tokenRepository.findByTokenHashAndUsedFalse(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword("bad-token", "NovaSenha@123"))
                .isInstanceOf(NoSuchElementException.class);
    }
}
