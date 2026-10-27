package com.prosup.proinsight.infrastructure.persistence.repository;

import com.prosup.proinsight.infrastructure.persistence.document.RefreshTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshTokenDocument, String> {

    void deleteByUserId(String userId);
}
