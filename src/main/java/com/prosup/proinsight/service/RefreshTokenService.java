package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.RefreshTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository repository,
                               @Value("${jwt.refresh-expiration:604800000}") long refreshExpirationMs) {
        this.repository = repository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public RefreshTokenDocument createRefreshToken(String userId) {
        var token = new RefreshTokenDocument(
                UUID.randomUUID().toString(),
                userId,
                Instant.now().plusMillis(refreshExpirationMs));
        return repository.save(token);
    }

    public RefreshTokenDocument validateAndRevoke(String tokenId) {
        var doc = repository.findByIdAndRevokedFalse(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido ou já revogado"));

        if (!doc.isValid()) {
            doc.setRevoked(true);
            repository.save(doc);
            throw new IllegalArgumentException("Refresh token expirado");
        }

        doc.setRevoked(true);
        repository.save(doc);

        return doc;
    }

    public void revokeAllByUserId(String userId) {
        repository.deleteByUserId(userId);
    }
}
