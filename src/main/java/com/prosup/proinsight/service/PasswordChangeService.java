package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import com.prosup.proinsight.infrastructure.transaction.MongoTransactionExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class PasswordChangeService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final MongoTransactionExecutor transactionExecutor;

    public PasswordChangeService(UserRepository userRepository,
                                 RefreshTokenService refreshTokenService,
                                 PasswordEncoder passwordEncoder,
                                 MongoTransactionExecutor transactionExecutor) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.transactionExecutor = transactionExecutor;
    }

    public void changePassword(String userId, String currentPassword, String newPassword) {
        UserDocument user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        // BCrypt fora da transação; dentro dela ficam apenas as escritas.
        String newPasswordHash = passwordEncoder.encode(newPassword);

        transactionExecutor.execute(() -> {
            UserDocument current = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

            current.setPassword(newPasswordHash);
            current.setFailedLoginAttempts(0);
            current.setLockedUntil(null);
            userRepository.save(current);

            refreshTokenService.revokeAllByUserId(userId);
            return null;
        });
    }
}
