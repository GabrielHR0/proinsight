package com.prosup.proinsight.infrastructure.persistence.repository;

import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProtocoloAvaliacaoRepository extends MongoRepository<ProtocoloAvaliacaoDocument, String> {

    Optional<ProtocoloAvaliacaoDocument> findFirstByCategoria(String categoria);

    /**
     * Carrega somente os campos usados pelo resumo do hub.
     * Evita trazer mapas de tabelas e textos longos do documento completo.
     */
    @Query(
            value = "{}",
            fields = "{ 'nome': 1, 'categoria': 1, 'padrao': 1, 'descricao': 1, 'unidadeMedida': 1 }",
            sort = "{ 'categoria': 1, 'nome': 1 }"
    )
    List<ProtocoloAvaliacaoDocument> findAllResumos();

    @Query(
            value = "{ '_id': { $in: ?0 } }",
            fields = "{ 'nome': 1, 'categoria': 1, 'padrao': 1, 'descricao': 1, 'unidadeMedida': 1 }",
            sort = "{ 'categoria': 1, 'nome': 1 }"
    )
    List<ProtocoloAvaliacaoDocument> findResumosByIdIn(List<String> ids);
}
