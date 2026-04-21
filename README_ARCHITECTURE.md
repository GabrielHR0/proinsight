Clean Architecture — proinsight
=================================

Visão geral
-----------
Este arquivo descreve a organização inicial do projeto usando Clean Architecture (adaptada para um serviço Spring Boot com MongoDB).

Camadas principais criadas
- domain: entidades de domínio e portas (interfaces) que representam regras de negócio.
- application: casos de uso (use-cases) e serviços que orquestram regras de negócio usando portas.
- adapter/in/web: adaptadores de entrada — controllers REST que expõem endpoints e chamam use-cases.
- adapter/out/persistence: adaptadores de saída — implementação das portas de persistência usando Spring Data (MongoDB).

Como usar
---------
- Coloque regras de negócio puras em `domain` (POJOs, interfaces). Evite referências a frameworks.
- Implemente **ports** (interfaces) em `adapter/out`.
- Exponha casos de uso via controllers em `adapter/in/web`.

Convenições
-----------
- Unit tests: sufixo `*Test.java`
- Integration tests: sufixo `*IT.java`
- Use `AbstractIntegrationTest` existente para garantir que os ITs resolvam `MONGO_URI`.

Pronto para desenvolvimento
--------------------------
Os arquivos criados aqui são esqueletos (classes minimais) para você começar a implementar a lógica.

Bom desenvolvimento.

