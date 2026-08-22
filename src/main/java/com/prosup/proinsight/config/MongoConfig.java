package com.prosup.proinsight.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComponentReadConverter;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComponentWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableMongoAuditing
@EnableAspectJAutoProxy

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions(ObjectMapper objectMapper) {
        return MongoCustomConversions.create(adapter -> {
            adapter.registerConverter(new PersistedComponentReadConverter(objectMapper));
            adapter.registerConverter(new PersistedComponentWriteConverter(objectMapper));
        });
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setTaskDecorator(new TenantContextTaskDecorator());
        executor.setThreadNamePrefix("proinsight-async-");
        executor.initialize();
        return executor;
    }
}
