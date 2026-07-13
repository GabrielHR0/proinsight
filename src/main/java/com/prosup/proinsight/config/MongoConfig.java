package com.prosup.proinsight.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComponentReadConverter;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComponentWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@EnableMongoAuditing

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions(ObjectMapper objectMapper) {
        return MongoCustomConversions.create(adapter -> {
            adapter.registerConverter(new PersistedComponentReadConverter(objectMapper));
            adapter.registerConverter(new PersistedComponentWriteConverter(objectMapper));
        });
    }
}
