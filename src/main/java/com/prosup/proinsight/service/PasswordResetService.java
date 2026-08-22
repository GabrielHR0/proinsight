package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.PasswordResetTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.PasswordResetTokenRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.NoSuchElementException;

/**
 * Fluxo de "esqueci minha senha": gera um token aleatório de uso único
 * (armazenado apenas como hash SHA-256, como um refresh token), com TTL
 * via índice MongoDB. O token em texto puro é devolvido ao chamador para
 * envio por e-mail; o endpoint sempre responde 200 para não vazar a
 * existência de contas.
 */
@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final long resetTokenTtlMs;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                RefreshTokenService refreshTokenService,
                                PasswordEncoder passwordEncoder,
                                @Value("${security.password-reset-token-ttl-ms:900000}") long resetTokenTtlMs) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.resetTokenTtlMs = resetTokenTtlMs;
    }

    public String createResetToken(String email) {
        UserDocument user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !user.isActive()) {
            return null;
        }
        tokenRepository.deleteByUserId(user.getId());

        byte[] raw = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(raw);
        String rawToken = HexFormat.of().formatHex(raw);

        tokenRepository.save(new PasswordResetTokenDocument(
                sha256(rawToken),
                user.getId(),
                Instant.now().plusMillis(resetTokenTtlMs)));

        return rawToken;
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetTokenDocument token = tokenRepository.findByTokenHashAndUsedFalse(sha256(rawToken))
                .orElseThrow(() -> new NoSuchElementException("Token inválido ou expirado"));

        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new NoSuchElementException("Token inválido ou expirado");
        }

        token.setUsed(true);
        tokenRepository.save(token);

        UserDocument user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        refreshTokenService.revokeAllByUserId(user.getId());
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
