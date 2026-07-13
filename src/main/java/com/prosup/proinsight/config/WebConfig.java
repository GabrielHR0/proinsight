package com.prosup.proinsight.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComponent;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComponentMixIn;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/v1",
                HandlerTypePredicate.forBasePackage("com.prosup.proinsight.api.controller.api.v1"));
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer persistedComponentMixIn() {
        return builder -> builder.mixIn(PersistedComponent.class, PersistedComponentMixIn.class);
    }
}
