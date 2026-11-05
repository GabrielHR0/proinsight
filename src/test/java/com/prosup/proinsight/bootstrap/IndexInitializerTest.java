package com.prosup.proinsight.bootstrap;

import com.prosup.proinsight.infrastructure.persistence.document.PasswordResetTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.document.RefreshTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.document.RevokedTokenDocument;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexInitializerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private MongoConverter mongoConverter;

    @Mock
    private IndexOperations userIndexes;

    @Mock
    private IndexOperations refreshTokenIndexes;

    @Mock
    private IndexOperations resetTokenIndexes;

    @Mock
    private IndexOperations revokedTokenIndexes;

    private IndexInitializer initializer;

    @BeforeEach
    void setUp() {
        var mappingContext = new MongoMappingContext();
        when(mongoTemplate.getConverter()).thenReturn(mongoConverter);
        doReturn(mappingContext).when(mongoConverter).getMappingContext();
        when(mongoTemplate.indexOps("users")).thenReturn(userIndexes);
        when(mongoTemplate.collectionExists(RefreshTokenDocument.class)).thenReturn(true);
        when(mongoTemplate.collectionExists(PasswordResetTokenDocument.class)).thenReturn(true);
        when(mongoTemplate.collectionExists(RevokedTokenDocument.class)).thenReturn(true);
        when(mongoTemplate.indexOps(RefreshTokenDocument.class)).thenReturn(refreshTokenIndexes);
        when(mongoTemplate.indexOps(PasswordResetTokenDocument.class)).thenReturn(resetTokenIndexes);
        when(mongoTemplate.indexOps(RevokedTokenDocument.class)).thenReturn(revokedTokenIndexes);

        initializer = new IndexInitializer(mongoTemplate);
    }

    @Test
    @DisplayName("cria índices TTL para refresh, reset e access tokens revogados")
    void createsTtlIndexesForAllTemporaryAuthTokens() {
        stubEmptyIndexes();
        when(refreshTokenIndexes.getIndexInfo()).thenReturn(List.of());

        initializer.run();

        assertThat(capturedIndexes(refreshTokenIndexes))
                .anySatisfy(index -> assertTtl(index, "expiryDate"));
        assertThat(capturedIndexes(resetTokenIndexes))
                .anySatisfy(index -> assertTtl(index, "expiresAt"));
        assertThat(capturedIndexes(revokedTokenIndexes))
                .anySatisfy(index -> assertTtl(index, "expiresAt"));
    }

    @Test
    @DisplayName("cria índice único para o hash do token de reset")
    void createsUniqueIndexForPasswordResetTokenHash() {
        stubEmptyIndexes();
        when(refreshTokenIndexes.getIndexInfo()).thenReturn(List.of());

        initializer.run();

        Index tokenHash = capturedIndexes(resetTokenIndexes).stream()
                .filter(index -> index.getIndexKeys().containsKey("tokenHash"))
                .findFirst()
                .orElseThrow();

        assertThat(tokenHash.getIndexOptions()).containsEntry("unique", true);
    }

    @Test
    @DisplayName("não recria índice TTL já existente")
    void doesNotRecreateExistingTtlIndex() {
        stubEmptyIndexes();
        IndexInfo ttl = IndexInfo.indexInfoOf(new Document("v", 2)
                .append("key", new Document("expiryDate", 1))
                .append("name", "expiryDate_ttl")
                .append("expireAfterSeconds", 0L));
        when(refreshTokenIndexes.getIndexInfo()).thenReturn(List.of(ttl));

        initializer.run();

        assertThat(capturedIndexes(refreshTokenIndexes))
                .noneMatch(index -> index.getIndexKeys().containsKey("expiryDate"));
        verify(refreshTokenIndexes, never()).dropIndex(any());
    }

    private void stubEmptyIndexes() {
        when(userIndexes.getIndexInfo()).thenReturn(List.of());
        when(resetTokenIndexes.getIndexInfo()).thenReturn(List.of());
        when(revokedTokenIndexes.getIndexInfo()).thenReturn(List.of());
    }

    private List<Index> capturedIndexes(IndexOperations operations) {
        ArgumentCaptor<Index> captor = ArgumentCaptor.forClass(Index.class);
        verify(operations, atLeastOnce()).ensureIndex(captor.capture());
        return captor.getAllValues();
    }

    private void assertTtl(Index index, String field) {
        assertThat(index.getIndexKeys()).containsKey(field);
        assertThat(((Number) index.getIndexOptions().get("expireAfterSeconds")).longValue())
                .isZero();
    }
}
