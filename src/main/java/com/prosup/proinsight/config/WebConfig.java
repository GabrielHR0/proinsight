package com.prosup.proinsight.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComponent;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComponentMixIn;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public WebConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/v1",
                HandlerTypePredicate.forBasePackage("com.prosup.proinsight.api.controller.api.v1"));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var origins = corsProperties.allowedOrigins();
        if (origins != null && !origins.isEmpty()) {
            registry.addMapping("/**")
                    .allowedOrigins(origins.toArray(new String[0]))
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
        }
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer persistedComponentMixIn() {
        return builder -> builder.mixIn(PersistedComponent.class, PersistedComponentMixIn.class);
    }
}
