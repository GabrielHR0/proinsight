package com.prosup.proinsight.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoPermissionDataRepository extends MongoRepository<PermissionDocument, String> {

}

