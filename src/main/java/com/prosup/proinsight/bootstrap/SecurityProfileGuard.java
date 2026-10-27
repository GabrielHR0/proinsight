package com.prosup.proinsight.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

/**
 * Fails fast when the local profile is used against a remote database.
 *
 * <p>The local profile creates a default SUPER_ADMIN user. Allowing that
 * profile to run against a production database would silently provision a
 * privileged account with bootstrap credentials, so this guard aborts startup
 * before the initializer can execute.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityProfileGuard implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SecurityProfileGuard.class);

    private final Environment environment;
    private final String mongoUri;

    public SecurityProfileGuard(Environment environment,
                                @Value("${spring.data.mongodb.uri:}") String mongoUri) {
        this.environment = environment;
        this.mongoUri = mongoUri;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean localProfile = Arrays.stream(environment.getActiveProfiles())
                .anyMatch("local"::equalsIgnoreCase);

        if (!localProfile) {
            return;
        }

        if (!isLocalMongoUri(mongoUri)) {
            throw new IllegalStateException(
                    "Refusing to start with the 'local' profile against a non-local MongoDB URI. "
                            + "Use SPRING_PROFILES_ACTIVE=prod for remote databases.");
        }

        log.info("Local profile active with a local MongoDB URI");
    }

    private static boolean isLocalMongoUri(String uri) {
        if (uri == null || uri.isBlank()) {
            return false;
        }
        String normalized = uri.toLowerCase(Locale.ROOT);
        return normalized.contains("localhost")
                || normalized.contains("127.0.0.1")
                || normalized.contains("[::1]");
    }
}
