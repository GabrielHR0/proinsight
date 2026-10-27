package com.prosup.proinsight.config;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

/**
 * Habilita transações multi-documento do MongoDB.
 *
 * Requer uma topologia replica set (Atlas em produção ou o compose local).
 * Sem o manager, as anotações {@code @Transactional} existentes são ignoradas.
 */
@Configuration
public class MongoTransactionConfig {

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory databaseFactory) {
        TransactionOptions options = TransactionOptions.builder()
                .readConcern(ReadConcern.MAJORITY)
                .writeConcern(WriteConcern.MAJORITY)
                .readPreference(ReadPreference.primary())
                .build();
        return new MongoTransactionManager(databaseFactory, options);
    }
}
