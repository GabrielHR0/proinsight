package com.prosup.proinsight.infrastructure.persistence.repository;

import com.prosup.proinsight.infrastructure.persistence.document.AvaliacaoFisicaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvaliacaoFisicaRepository extends MongoRepository<AvaliacaoFisicaDocument, String> {
    Optional<AvaliacaoFisicaDocument> findFirstByClienteIdAndProtocoloIdOrderByCreatedAtDesc(String clienteId, String protocoloId);
}
