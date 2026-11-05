package com.prosup.proinsight.bootstrap;

import com.prosup.proinsight.infrastructure.persistence.document.PasswordResetTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.document.RefreshTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.document.RevokedTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Component
public class IndexInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(IndexInitializer.class);

    private static final Set<Class<?>> MANUALLY_MANAGED_DOCUMENTS = Set.of(
            UserDocument.class,
            RefreshTokenDocument.class,
            PasswordResetTokenDocument.class,
            RevokedTokenDocument.class);

    private final MongoTemplate mongoTemplate;

    public IndexInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        ensureUserIndexes();
        ensureAuthTokenIndexes();
        ensureRemainingDocumentIndexes();
    }

    private void ensureUserIndexes() {
        IndexOperations ops = mongoTemplate.indexOps("users");
        ensureUnique(ops, "userName", false);
        ensureUnique(ops, "email", false);
        ensureUnique(ops, "cref", true);
        ensureUnique(ops, "cpf", true);
        log.info("Unique indexes guaranteed on 'users' collection");
    }

    /**
     * Refresh tokens, revoked access tokens and password reset tokens are
     * temporary data. Without TTL indexes these collections grow forever:
     * every login creates a refresh token, every rotation keeps the previous
     * one revoked, and every logout stores a revoked access token until its
     * natural expiration. The indexes below are therefore part of the
     * correctness of the authentication flow, not just a performance detail.
     */
    private void ensureAuthTokenIndexes() {
        IndexOperations refreshTokens = indexOpsFor(RefreshTokenDocument.class);
        ensureSimple(refreshTokens, "userId");
        ensureTtl(refreshTokens, "expiryDate");

        IndexOperations resetTokens = indexOpsFor(PasswordResetTokenDocument.class);
        ensureUnique(resetTokens, "tokenHash", false);
        ensureSimple(resetTokens, "userId");
        ensureTtl(resetTokens, "expiresAt");

        IndexOperations revokedTokens = indexOpsFor(RevokedTokenDocument.class);
        ensureSimple(revokedTokens, "userId");
        ensureTtl(revokedTokens, "expiresAt");

        log.info("Authentication token indexes guaranteed");
    }

    /**
     * Creates the indexes declared by @Indexed/@CompoundIndex for every
     * document type that is not managed explicitly above. Spring Data's
     * auto-index creation stays disabled, so without this step those
     * annotations would be silently ignored (no TTL, no tenant indexes).
     */
    private void ensureRemainingDocumentIndexes() {
        var mappingContext = mongoTemplate.getConverter().getMappingContext();
        if (!(mappingContext instanceof MongoMappingContext mongoMappingContext)) {
            log.warn("MongoMappingContext unavailable; skipping annotated document indexes");
            return;
        }

        var resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
        for (var entity : mongoMappingContext.getPersistentEntities()) {
            Class<?> type = entity.getType();
            if (!entity.isAnnotationPresent(Document.class)
                    || MANUALLY_MANAGED_DOCUMENTS.contains(type)) {
                continue;
            }

            IndexOperations ops = mongoTemplate.indexOps(type);
            int ensured = 0;
            for (IndexDefinition definition : resolver.resolveIndexFor(type)) {
                ops.ensureIndex(definition);
                ensured++;
            }
            if (ensured > 0) {
                log.info("{} indexes guaranteed on '{}'", ensured,
                        mongoTemplate.getCollectionName(type));
            }
        }
    }

    private IndexOperations indexOpsFor(Class<?> documentType) {
        if (!mongoTemplate.collectionExists(documentType)) {
            mongoTemplate.createCollection(documentType);
            log.info("Collection '{}' created for index initialization",
                    mongoTemplate.getCollectionName(documentType));
        }
        return mongoTemplate.indexOps(documentType);
    }

    private void ensureSimple(IndexOperations ops, String field) {
        IndexInfo existing = findSingleFieldIndex(ops, field);
        if (existing != null) {
            log.info("Index already exists for '{}'", field);
            return;
        }
        ops.ensureIndex(new Index().on(field, Sort.Direction.ASC));
        log.info("Index created for '{}'", field);
    }

    private void ensureUnique(IndexOperations ops, String field, boolean sparse) {
        IndexInfo existing = findSingleFieldIndex(ops, field);
        if (existing != null && existing.isUnique() && existing.isSparse() == sparse) {
            log.info("Unique index already exists for '{}'", field);
            return;
        }
        if (existing != null) {
            ops.dropIndex(existing.getName());
            log.warn("Dropped incompatible index '{}' before recreating it as unique", field);
        }
        Index index = new Index().on(field, Sort.Direction.ASC).unique();
        if (sparse) {
            index.sparse();
        }
        ops.ensureIndex(index);
        log.info("Unique index created for '{}'", field);
    }

    /**
     * TTL indexes must be single-field indexes with expireAfterSeconds=0.
     * If a plain index already exists on the field, it is replaced: keeping
     * both would either fail with an index conflict or leave documents
     * forever. The TTL monitor then removes documents once the indexed date
     * is reached (MongoDB runs it approximately every 60 seconds).
     */
    private void ensureTtl(IndexOperations ops, String field) {
        IndexInfo existing = findSingleFieldIndex(ops, field);
        if (existing != null && existing.getExpireAfter().isPresent()) {
            log.info("TTL index already exists for '{}'", field);
            return;
        }
        if (existing != null) {
            ops.dropIndex(existing.getName());
            log.warn("Dropped non-TTL index '{}' before creating its TTL counterpart", field);
        }
        ops.ensureIndex(new Index().on(field, Sort.Direction.ASC).expire(Duration.ZERO));
        log.info("TTL index created for '{}'", field);
    }

    private IndexInfo findSingleFieldIndex(IndexOperations ops, String field) {
        List<IndexInfo> indexes = ops.getIndexInfo();
        for (IndexInfo info : indexes) {
            if (info.getIndexFields().size() == 1
                    && field.equals(info.getIndexFields().get(0).getKey())) {
                return info;
            }
        }
        return null;
    }
}
