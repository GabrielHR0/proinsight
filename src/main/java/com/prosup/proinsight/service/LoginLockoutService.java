package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

@Service
public class LoginLockoutService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;

    public LoginLockoutService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private void withUser(String login, Consumer<UserDocument> action) {
        userRepository.findByEmail(login)
                .or(() -> userRepository.findByUserName(login))
                .ifPresent(action);
    }

    public void checkLockout(String login) {
        withUser(login, doc -> {
            if (doc.getLockedUntil() != null && doc.getLockedUntil().isAfter(Instant.now())) {
                long minutes = Duration.between(Instant.now(), doc.getLockedUntil()).toMinutes();
                throw new LockedException(
                    "Conta temporariamente bloqueada. Tente novamente em " + Math.max(1, minutes) + " minuto(s).");
            }
        });
    }

    public void recordFailedAttempt(String login) {
        withUser(login, doc -> {
            int attempts = (doc.getFailedLoginAttempts() == null ? 0 : doc.getFailedLoginAttempts()) + 1;
            doc.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_ATTEMPTS) {
                doc.setLockedUntil(Instant.now().plus(LOCKOUT_DURATION));
            }
            userRepository.save(doc);
        });
    }

    public void resetAttempts(String login) {
        withUser(login, doc -> {
            doc.setFailedLoginAttempts(0);
            doc.setLockedUntil(null);
            userRepository.save(doc);
        });
    }
}
