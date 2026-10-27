package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.RefreshTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final MongoTemplate mongoTemplate;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository repository,
                               MongoTemplate mongoTemplate,
                               @Value("${jwt.refresh-expiration:604800000}") long refreshExpirationMs) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /**
     * Creates a refresh token and returns the raw value exactly once.
     * Only its SHA-256 hash is persisted, so a database leak does not expose
     * usable refresh tokens.
     */
    public String createRefreshToken(String userId) {
        String rawToken = UUID.randomUUID().toString();
        var token = new RefreshTokenDocument(
                sha256(rawToken),
                userId,
                Instant.now().plusMillis(refreshExpirationMs));
        repository.save(token);
        return rawToken;
    }

    public RefreshTokenDocument validateAndRevoke(String rawToken) {
        Query query = Query.query(Criteria.where("_id").is(sha256(rawToken))
                .and("revoked").is(false)
                .and("expiryDate").gt(Instant.now()));
        Update update = new Update().set("revoked", true);

        RefreshTokenDocument doc = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                RefreshTokenDocument.class);

        if (doc == null) {
            throw new IllegalArgumentException("Refresh token inválido, revogado ou expirado");
        }
        return doc;
    }

    public void revokeAllByUserId(String userId) {
        repository.deleteByUserId(userId);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
