package com.prosup.proinsight.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoAvaliadorDataRepository extends MongoRepository<AvaliadorDocument, String> {

	java.util.Optional<AvaliadorDocument> findByUserId(String userId);

}

