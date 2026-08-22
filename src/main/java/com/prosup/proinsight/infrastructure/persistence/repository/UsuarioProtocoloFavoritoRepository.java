package com.prosup.proinsight.infrastructure.persistence.repository;

import com.prosup.proinsight.infrastructure.persistence.document.UsuarioProtocoloFavoritoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioProtocoloFavoritoRepository extends MongoRepository<UsuarioProtocoloFavoritoDocument, String> {

    List<UsuarioProtocoloFavoritoDocument> findByUserId(String userId);

    Optional<UsuarioProtocoloFavoritoDocument> findByUserIdAndProtocoloId(String userId, String protocoloId);

    void deleteByUserIdAndProtocoloId(String userId, String protocoloId);

    boolean existsByUserIdAndProtocoloId(String userId, String protocoloId);
}
