package com.prosup.proinsight.bootstrap;

import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.infrastructure.persistence.document.RoleDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@Profile("local")
public class DefaultUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultUserInitializer.class);

    static final String SUPER_ROLE_ID = "super_admin_role";
    static final String DEFAULT_ACADEMIA_ID = "academia_padrao";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final String defaultEmail;
    private final String defaultUsername;
    private final String defaultPassword;

    public DefaultUserInitializer(UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${default-user.email:}") String defaultEmail,
                                  @Value("${default-user.username:}") String defaultUsername,
                                  @Value("${default-user.password:}") String defaultPassword) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultEmail = defaultEmail;
        this.defaultUsername = defaultUsername;
        this.defaultPassword = defaultPassword;
    }

    @Override
    public void run(String... args) {
        if (defaultEmail.isBlank() || defaultPassword.isBlank()) {
            log.warn("default-user.email or default-user.password not configured, skipping user creation");
            return;
        }

        if (userRepository.findByEmail(defaultEmail).isPresent()) {
            log.info("Default admin user already exists, skipping creation");
            return;
        }

        if (!roleRepository.existsById(SUPER_ROLE_ID)) {
            roleRepository.save(new RoleDocument(
                    SUPER_ROLE_ID, "Super Administrador",
                    "Acesso total a todas as funcionalidades do sistema",
                    Set.of(Permissao.values())));
            log.info("Role 'Super Administrador' created with id={}", SUPER_ROLE_ID);
        }

        var doc = new UserDocument();
        doc.setEmail(defaultEmail);
        doc.setUserName(defaultUsername);
        doc.setPassword(passwordEncoder.encode(defaultPassword));
        doc.setActive(true);
        doc.setAcademiaRoles(Map.of(DEFAULT_ACADEMIA_ID, Set.of(SUPER_ROLE_ID)));
        doc.addAcademiaId(DEFAULT_ACADEMIA_ID);
        userRepository.save(doc);

        log.info("Default admin user created: email={}", defaultEmail);
    }
}
