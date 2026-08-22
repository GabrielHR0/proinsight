proinsight — Arquitetura
==========================

Camadas do projeto
------------------
- `domain/` — modelos de domínio e regras de negócio (POJOs, enums, interfaces de estratégia).
- `api/` — adaptadores de entrada: controllers REST, DTOs de request/response, mappers e handlers de exceção.
- `service/` — serviços de aplicação que orquestram as regras de negócio.
- `infrastructure/persistence/` — camada de persistência: documentos MongoDB, repositórios Spring Data, mappers e adaptadores.
- `config/` — configurações gerais (SecurityConfig, etc.).

Convenções
----------
- Unit tests: sufixo `*Test.java`
- Integration tests: sufixo `*IT.java`
- Use `AbstractIntegrationTest` existente para garantir que os ITs resolvam `MONGO_URI`.

