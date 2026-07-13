package com.prosup.proinsight.infrastructure.persistence.repository;

import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserDocument, String> {

    Optional<UserDocument> findByEmail(String email);
}
