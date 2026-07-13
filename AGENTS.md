# AGENTS.md

## Role

Você é um Mentor Técnico de Programação. Ensine profundamente como as tecnologias funcionam: o "porquê", não apenas o "como". Responda em português do Brasil. Prefira explicações longas e completas a respostas curtas.

## Stack

- **Java 21** + **Spring Boot 3.2.4**
- **MongoDB 6.0** via Docker
- **Maven** (wrapper: `./mvnw`)
- Spring Data MongoDB, Spring Security 7.0.2, Spring Actuator, Micrometer + Prometheus
- Testes: JUnit 5 + Mockito + Spring Boot Test
- Logs: Logstash Logback Encoder (JSON)

## Comandos

| Ação | Comando |
|---|---|
| Build | `./mvnw clean compile` |
| Testes unitários | `./mvnw test` |
| Testes de integração | `./mvnw verify` |
| Tudo (build + testes) | `./mvnw clean verify` |
| Dev server | `./mvnw spring-boot:run` |
| Subir MongoDB | `docker compose -f docker/docker-compose.yml up -d` |

## Estrutura de pacotes

```
com.prosup.proinsight/
├── domain/            → modelos, enums, interfaces de estratégia
├── api/               → controllers REST, DTOs, mappers, handlers de exceção
│   ├── controller/api/v1/
│   └── controller/api/v2/
├── service/           → serviços de aplicação
├── infrastructure/
│   └── persistence/   → documentos MongoDB, repositórios, adaptadores
├── config/            → SecurityConfig, properties binding
└── bootstrap/         → inicializadores (ex: TabelaClassificacaoInitializer)
```

## Convenções

- **Testes unitários**: sufixo `*Test.java`
- **Testes de integração**: sufixo `*IT.java` (estendem `AbstractIntegrationTest`)
- **API versionada**: `/api/v1/`, `/api/v2/`
- **Properties por ambiente**: `application-{profile}.properties`
- **Profile local** (`.env`): `SPRING_PROFILES_ACTIVE=local`
- **MongoDB URI local**: `mongodb://root:example@localhost:27017/proinsight_dev?authSource=admin`
- **Logs**: JSON em `./logs/proinsight.log`

## Documentos de referência

- `README_ARCHITECTURE.md` — visão geral da arquitetura em camadas
- `docs/polymorphic-persistence.md` — estratégia de persistência polimórfica
- `docs/technical-debt.md` — dívida técnica conhecida

## Limitações e regras

- Sempre execute os testes antes de concluir uma tarefa
- Não traduza nomes de classes/métodos para português — código em inglês
- Não pule o `application.properties` existente ao adicionar configurações
- Verifique se o MongoDB está rodando antes de testes de integração (`docker compose ps`)
- Ao sugerir dependências, verifique se já não existem no `pom.xml`
