package com.prosup.proinsight.infrastructure.persistence.repository;

import com.prosup.proinsight.infrastructure.persistence.document.ApiKeyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends MongoRepository<ApiKeyDocument, String> {

    Optional<ApiKeyDocument> findByKeyHashAndActiveTrue(String keyHash);
}
