package com.prosup.proinsight.infrastructure.persistence;

import com.prosup.proinsight.AbstractIntegrationTest;
import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.composite.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PolymorphicRaizPersistenceIT extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PolymorphicRaizPersistenceIT.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoMappingContext mappingContext;

    private String savedId;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(TabelaClassificacaoDocument.class);

        PersistedTabelaVo2Max raiz = new PersistedTabelaVo2Max(ProtocoloVo2Max.COOPER);
        PersistedTabelaSexo filho = new PersistedTabelaSexo(Sexo.MASCULINO);
        raiz.addComponente(filho);

        TabelaClassificacaoDocument doc = new TabelaClassificacaoDocument();
        doc.setNome("test-polymorphic");
        doc.setRaiz(raiz);

        TabelaClassificacaoDocument saved = mongoTemplate.save(doc);
        savedId = saved.getId();

        log.info("Saved document with id={}", savedId);

        // --- DEBUG: dump registered entities ---
        log.info("=== Registered persistent entities ===");
        for (MongoPersistentEntity<?> entity : mappingContext.getPersistentEntities()) {
            log.info("  entity type={}, alias={}", entity.getType().getSimpleName(),
                    entity.getTypeAlias() != null ? entity.getTypeAlias() : "NONE");
        }
    }

    @Test
    void shouldPersistAndRetrievePolymorphicRaiz() {
        // --- ACT: read via MongoTemplate with entity mapping ---
        TabelaClassificacaoDocument loaded = mongoTemplate.findById(
                savedId, TabelaClassificacaoDocument.class);

        // --- ASSERT ---
        assertThat(loaded).isNotNull();
        assertThat(loaded.getRaiz()).isNotNull();
        assertThat(loaded.getRaiz()).isInstanceOfSatisfying(PersistedTabelaVo2Max.class, r -> {
            assertThat(r.getComponentes()).hasSize(1);
            assertThat(r.getComponentes().get(0)).isInstanceOf(PersistedTabelaSexo.class);
        });

        log.info("SUCCESS: raiz={}, filho={}",
                loaded.getRaiz().getClass().getSimpleName(),
                ((PersistedComposite) loaded.getRaiz()).getComponentes().get(0).getClass().getSimpleName());
    }
}
