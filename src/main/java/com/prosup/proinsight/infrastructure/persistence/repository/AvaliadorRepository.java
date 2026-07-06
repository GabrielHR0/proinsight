package com.prosup.proinsight.infrastructure.persistence.repository;

import com.prosup.proinsight.infrastructure.persistence.document.AvaliadorDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvaliadorRepository extends MongoRepository<AvaliadorDocument, String> {

	java.util.Optional<AvaliadorDocument> findByUserId(String userId);

}
