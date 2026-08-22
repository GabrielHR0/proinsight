package com.prosup.proinsight.infrastructure.persistence.repository;

import com.prosup.proinsight.infrastructure.persistence.document.PasswordResetTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetTokenDocument, String> {

    Optional<PasswordResetTokenDocument> findByTokenHashAndUsedFalse(String tokenHash);

    void deleteByUserId(String userId);
}
