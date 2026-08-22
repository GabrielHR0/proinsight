package com.prosup.proinsight.bootstrap;

import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.infrastructure.persistence.document.RoleDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RoleInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleInitializer.class);

    public static final String ROLE_ADMIN_ID = "role_admin";
    public static final String ROLE_EMPLOYEE_ID = "role_employee";
    public static final String ROLE_EVALUATOR_ID = "role_evaluator";

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        createIfNotExists(ROLE_ADMIN_ID, "admin",
                "Administrador da academia - acesso total exceto SUPER_ADMIN",
                adminPermissions());

        createIfNotExists(ROLE_EMPLOYEE_ID, "employee",
                "Funcionário da academia - acesso limitado a clientes e avaliações",
                employeePermissions());

        createIfNotExists(ROLE_EVALUATOR_ID, "evaluator",
                "Avaliador - acesso a avaliações e leitura de clientes",
                evaluatorPermissions());

        log.info("Roles padrão inicializadas");
    }

    private void createIfNotExists(String id, String nome, String descricao, Set<Permissao> permissoes) {
        if (!roleRepository.existsById(id)) {
            roleRepository.save(new RoleDocument(id, nome, descricao, permissoes));
            log.info("Role '{}' criada com id={}", nome, id);
        }
    }

    private static Set<Permissao> adminPermissions() {
        return Set.of(
                Permissao.CLIENTES_CRIAR, Permissao.CLIENTES_LER, Permissao.CLIENTES_ATUALIZAR, Permissao.CLIENTES_EXCLUIR,
                Permissao.AVALIACOES_CRIAR, Permissao.AVALIACOES_LER, Permissao.AVALIACOES_ATUALIZAR, Permissao.AVALIACOES_EXCLUIR,
                Permissao.AVALIADORES_CRIAR, Permissao.AVALIADORES_LER, Permissao.AVALIADORES_ATUALIZAR,
                Permissao.PROTOCOLOS_LER,
                Permissao.USUARIOS_CRIAR, Permissao.USUARIOS_LER, Permissao.USUARIOS_ATUALIZAR, Permissao.USUARIOS_EXCLUIR,
                Permissao.ACADEMIAS_CRIAR, Permissao.ACADEMIAS_LER, Permissao.ACADEMIAS_ATUALIZAR,
                Permissao.RELATORIOS_LER, Permissao.RELATORIOS_EXPORTAR
        );
    }

    private static Set<Permissao> employeePermissions() {
        return Set.of(
                Permissao.CLIENTES_CRIAR, Permissao.CLIENTES_LER, Permissao.CLIENTES_ATUALIZAR,
                Permissao.AVALIACOES_CRIAR, Permissao.AVALIACOES_LER, Permissao.AVALIACOES_ATUALIZAR,
                Permissao.PROTOCOLOS_LER,
                Permissao.RELATORIOS_LER
        );
    }

    private static Set<Permissao> evaluatorPermissions() {
        return Set.of(
                Permissao.CLIENTES_LER,
                Permissao.AVALIACOES_CRIAR, Permissao.AVALIACOES_LER, Permissao.AVALIACOES_ATUALIZAR,
                Permissao.PROTOCOLOS_LER
        );
    }
}
