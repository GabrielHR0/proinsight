package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.RefreshTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;
    @Mock
    private MongoTemplate mongoTemplate;

    private RefreshTokenService service() {
        return new RefreshTokenService(repository, mongoTemplate, 604_800_000L);
    }

    @Test
    @DisplayName("createRefreshToken persiste apenas o hash e devolve o token puro")
    void createRefreshTokenStoresOnlyHash() {
        when(repository.save(any(RefreshTokenDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String rawToken = service().createRefreshToken("user-1");

        ArgumentCaptor<RefreshTokenDocument> captor =
                ArgumentCaptor.forClass(RefreshTokenDocument.class);
        verify(repository).save(captor.capture());

        RefreshTokenDocument saved = captor.getValue();
        assertThat(rawToken).isNotBlank().hasSize(36);
        assertThat(saved.getId()).isEqualTo(sha256(rawToken));
        assertThat(saved.getId()).isNotEqualTo(rawToken);
        assertThat(saved.getUserId()).isEqualTo("user-1");
    }

    @Test
    @DisplayName("validateAndRevoke busca pelo hash, não pelo token puro")
    void validateAndRevokeLooksUpByHash() {
        String rawToken = "raw-refresh-token";
        RefreshTokenDocument document = new RefreshTokenDocument(
                sha256(rawToken), "user-1", Instant.now().plusSeconds(60));
        when(mongoTemplate.findAndModify(
                any(Query.class), any(UpdateDefinition.class),
                any(FindAndModifyOptions.class), eq(RefreshTokenDocument.class)))
                .thenReturn(document);

        RefreshTokenDocument result = service().validateAndRevoke(rawToken);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).findAndModify(
                queryCaptor.capture(), any(UpdateDefinition.class),
                any(FindAndModifyOptions.class), eq(RefreshTokenDocument.class));

        assertThat(result).isSameAs(document);
        assertThat(queryCaptor.getValue().getQueryObject().get("_id"))
                .isEqualTo(sha256(rawToken));
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
