package com.prosup.proinsight.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoUserDataRepository extends MongoRepository<UserDocument, String> {

    // Spring Data provides basic CRUD; custom queries can be added here

}

