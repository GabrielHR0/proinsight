package com.prosup.proinsight.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security-related configuration beans.
 *
 * Exposes a PasswordEncoder (BCrypt) to be injected where needed (e.g. UserService).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // strength 12 is a reasonable default for BCrypt on modern hardware
        return new BCryptPasswordEncoder(12);
    }
}

