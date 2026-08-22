package com.prosup.proinsight.infrastructure.persistence.repository;

import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProtocoloAvaliacaoRepository extends MongoRepository<ProtocoloAvaliacaoDocument, String> {

    Optional<ProtocoloAvaliacaoDocument> findFirstByCategoria(String categoria);
}
