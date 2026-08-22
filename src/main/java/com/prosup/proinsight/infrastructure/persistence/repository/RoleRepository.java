package com.prosup.proinsight.infrastructure.persistence.repository;

import com.prosup.proinsight.infrastructure.persistence.document.RoleDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<RoleDocument, String> {

    Optional<RoleDocument> findByNome(String nome);

}
