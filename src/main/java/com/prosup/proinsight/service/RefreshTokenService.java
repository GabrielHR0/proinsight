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

import java.time.Instant;
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

    public RefreshTokenDocument createRefreshToken(String userId) {
        var token = new RefreshTokenDocument(
                UUID.randomUUID().toString(),
                userId,
                Instant.now().plusMillis(refreshExpirationMs));
        return repository.save(token);
    }

    public RefreshTokenDocument validateAndRevoke(String tokenId) {
        Query query = Query.query(Criteria.where("_id").is(tokenId)
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
}
