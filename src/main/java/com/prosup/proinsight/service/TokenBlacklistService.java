package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.RevokedTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.RevokedTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenBlacklistService {

    private final RevokedTokenRepository repository;

    public TokenBlacklistService(RevokedTokenRepository repository) {
        this.repository = repository;
    }

    public void revoke(String jti, String userId, Instant expiresAt) {
        if (jti == null || jti.isBlank()) return;
        repository.save(new RevokedTokenDocument(jti, userId, expiresAt));
    }

    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) return false;
        return repository.existsById(jti);
    }
}
