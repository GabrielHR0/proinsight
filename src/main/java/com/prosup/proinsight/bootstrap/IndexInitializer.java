package com.prosup.proinsight.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IndexInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(IndexInitializer.class);

    private final MongoTemplate mongoTemplate;

    public IndexInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        garantirIndicesUsuarios();
    }

    private void garantirIndicesUsuarios() {
        IndexOperations ops = mongoTemplate.indexOps("users");
        garantirUnico(ops, "userName");
        garantirUnico(ops, "email");
        garantirUnicoSparse(ops, "cref");
        garantirUnicoSparse(ops, "cpf");
        log.info("Índices únicos garantidos na coleção 'users'");
    }

    private void garantirUnico(IndexOperations ops, String campo) {
        if (existeIndicePara(ops, campo)) {
            log.info("Índice único já existe para '{}'", campo);
            return;
        }
        ops.ensureIndex(new Index().on(campo, Sort.Direction.ASC).unique());
        log.info("Índice único criado para '{}'", campo);
    }

    private void garantirUnicoSparse(IndexOperations ops, String campo) {
        if (existeIndicePara(ops, campo)) {
            log.info("Índice único já existe para '{}'", campo);
            return;
        }
        ops.ensureIndex(new Index().on(campo, Sort.Direction.ASC).unique().sparse());
        log.info("Índice único sparse criado para '{}'", campo);
    }

    private boolean existeIndicePara(IndexOperations ops, String campo) {
        for (IndexInfo info : ops.getIndexInfo()) {
            if (info.getIndexFields().stream()
                    .anyMatch(f -> campo.equals(f.getKey()))) {
                return true;
            }
        }
        return false;
    }
}