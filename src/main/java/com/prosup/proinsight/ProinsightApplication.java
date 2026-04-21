package com.prosup.proinsight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the Proinsight Spring Boot application.
 */
@SpringBootApplication
@ConfigurationPropertiesScan("com.prosup.proinsight.config")
public class ProinsightApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProinsightApplication.class, args);
    }

}
