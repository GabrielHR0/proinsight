package com.prosup.proinsight.infrastructure.persistence.repository;

import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends MongoRepository<ClienteDocument, String> {
    List<ClienteDocument> findByAcademiaId(String academiaId);
    List<ClienteDocument> findByAvaliadorId(String avaliadorId);
}
