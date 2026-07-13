# Arquitetura do Proinsight

> Documentação técnica detalhada da arquitetura, camadas, padrões de design e decisões técnicas.

---

## Sumário

1. [Visão Geral](#1-visão-geral)
2. [Stack Tecnológica](#2-stack-tecnológica)
3. [Arquitetura em Camadas](#3-arquitetura-em-camadas)
4. [Diagramas](#4-diagramas)
5. [Padrões de Design](#5-padrões-de-design)
6. [Persistência Polimórfica](#6-persistência-polimórfica)
7. [Fluxo de Requisição](#7-fluxo-de-requisição)
8. [Decisões Técnicas](#8-decisões-técnicas)

---

## 1. Visão Geral

**Proinsight** é um sistema de avaliações físicas para academias que permite:

- Cadastrar academias, avaliadores e clientes
- Realizar avaliações físicas (VO2Max, IMC, Bioimpedância)
- Classificar resultados usando árvores de tabelas compostas
- Gerenciar protocolos de avaliação (Cooper, Rockport, Esteira Incremental)

### Conceitos de Negócio

| Conceito | Descrição |
|----------|-----------|
| **Academia** | Estabelecimento físico com endereço e CNPJ |
| **Avaliador** | Profissional com CREF vinculado a uma academia |
| **Cliente** | Pessoa física com dados antropométricos, vinculada a um responsável |
| **Avaliação Física** | Registro completo de uma avaliação com medições e classificações |
| **Medição** | Dados brutos coletados (peso, altura, distância, tempo, etc.) |
| **Teste** | Cálculo específico (Cooper, Rockport, IMC) que gera um resultado numérico |
| **Classificação** | Árvore hierárquica que mapeia resultados em níveis (Ótimo, Bom, Regular, etc.) |

---

## 2. Stack Tecnológica

| Camada | Tecnologia | Versão | Justificativa |
|--------|------------|--------|---------------|
| **Linguagem** | Java | 21 | LTS, records, pattern matching, virtual threads |
| **Framework** | Spring Boot | 3.2.4 | Ecossistema maduro, auto-configuração, dependência gestionada |
| **Banco de Dados** | MongoDB | 6.0 | Schema flexível para dados hierárquicos, JSON nativo |
| **Build** | Maven | - | Wrapper integrado, plugins maduros |
| **Segurança** | Spring Security | 7.0.2 | Codificação de senhas BCrypt (strength 12) |
| **Monitoramento** | Actuator + Micrometer + Prometheus | - | Métricas de aplicação, health checks |
| **Logs** | Logstash Logback Encoder | 8.0 | Logs em JSON para agregadores (ELK, Datadog) |
| **Testes** | JUnit 5 + Mockito + Spring Boot Test | - | Testes unitários e de integração |

### Por que MongoDB e não relational?

| Critério | MongoDB | PostgreSQL/MySQL |
|----------|---------|------------------|
| **Schema hierárquico** | Nativo (subdocumentos, arrays) | Precisa de junction tables ou JSONB |
| **Polimorfismo** | `_class` field + conversores | Discriminator column + JOINs |
| **Flexibilidade** | Adiciona campos sem ALTER TABLE | Migrations obrigatórias |
| **Performance leitura** | Dados denormalizados, uma query | JOINs complexos |
| **Consistência** | Eventual (write concerns configuráveis) | ACID garantido |

**Decisão**: Para o domínio de avaliações com árvores de classificação polimórficas, MongoDB é mais natural. A hierarquia `TabelaVo2Max → TabelaSexo → TabelaIdade → NivelVo2Max` é um documento único, não 4 tabelas com JOINs.

---

## 3. Arquitetura em Camadas

O projeto segue uma arquitetura **hexagonal simplificada** (ports & adapters leve):

```
┌─────────────────────────────────────────────────────────────────┐
│                        API (Entrada)                            │
│  Controllers → DTOs → Mappers → Handlers                       │
├─────────────────────────────────────────────────────────────────┤
│                      SERVICE (Orquestração)                     │
│  AvaliacaoService → Handlers (IMC, VO2Max, Listagem)            │
├─────────────────────────────────────────────────────────────────┤
│                       DOMAIN (Núcleo)                           │
│  Models → Strategies → Composite Tree → Testes                  │
├─────────────────────────────────────────────────────────────────┤
│                 INFRASTRUCTURE (Saída/Persistência)             │
│  Documents → Repositories → Mappers → Adapters                  │
├─────────────────────────────────────────────────────────────────┤
│                        CONFIG                                    │
│  MongoConfig, WebConfig, SecurityConfig, Properties             │
├─────────────────────────────────────────────────────────────────┤
│                       BOOTSTRAP                                  │
│  Initializers (seeding de dados)                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 3.1 Domain Layer (Núcleo)

**Localização**: `com.prosup.proinsight.domain`

**Responsabilidade**: Definir regras de negócio, modelos de dados e interfaces. **NÃO depende de nenhuma outra camada.**

```
domain/
├── model/
│   ├── User.java                 → Usuário do sistema (email, password, roles)
│   ├── Role.java                 → Papel com conjunto de permissões
│   ├── Permission.java           → Permissão (recurso + ação)
│   ├── Cliente.java              → Cliente com responsável polimórfico
│   ├── Avaliador.java            → Profissional (CREF, userId, academiaId)
│   ├── Academia.java             → Estabelecimento (CNPJ, endereço)
│   ├── Endereco.java             → Value Object (rua, número, cidade, estado, CEP)
│   ├── AvaliacaoFisica.java      → Avaliação completa (protocolo, medições)
│   ├── TabelaClassificacao.java  → Árvore de classificação (raiz Composite)
│   ├── Medicao.java              → Abstrata: dados brutos da medição
│   ├── MedicaoVo2Max.java        → VO2Max: distância, tempo, FC, peso
│   ├── MedicaoImc.java           → IMC: peso, altura
│   └── DadosAvaliacao.java       → Bag de dados (Map<String, Object>)
│
├── model/teste/
│   ├── Teste.java                → Interface: gerarCodigo(), getCriterio()
│   ├── TesteVo2Max.java          → Abstrata: calcularVo2Max() template method
│   ├── TesteVo2MaxCooper.java    → Fórmula: (distância - 504.9) / 44.73
│   ├── TesteVo2MaxRockport.java  → Fórmula: 132.853 - (0.0769 * peso) - ...
│   ├── TesteVo2MaxEsteiraIncremental.java → Fórmula: (0.2 * velocidade) + 3.5
│   └── TesteImc.java             → Fórmula: peso / altura²
│
├── model/composite/
│   ├── Component.java            → Interface: classificar(), classificarComTeste()
│   ├── Composite.java            → Abstrata: filhos + delegação
│   └── Leaf.java                 → Abstrata: retorna self
│
├── model/composite/classes/
│   ├── NivelVo2Max.java          → Folha: classificação + faixa min/max
│   └── NivelForca.java           → Folha: nível + classificação
│
├── model/composite/tabelas/
│   ├── TabelaClassificacaoGenerica.java → Composite genérico
│   ├── TabelaVo2Max.java         → Filtra por protocolo
│   ├── TabelaSexo.java           → Filtra por sexo (MASCULINO/FEMININO)
│   ├── TabelaIdade.java          → Filtra por faixa etária
│   └── TabelaEquipamento.java    → Filtra por equipamento
│
├── strategy/
│   ├── AvaliacaoStrategy.java    → Interface: avaliar(Context) → Leaf
│   ├── StrategyFor.java          → Anotação: @StrategyFor("IMC")
│   ├── StrategyRegistry.java     → Registry: auto-descobre strategies
│   ├── AvaliacaoContext.java     → Interface: contexto da avaliação
│   ├── AvaliacaoImc.java         → Strategy: classificação IMC
│   ├── AvaliacaoImcContext.java   → Contexto IMC
│   ├── AvaliacaoImcContextBuilder.java → Builder com validação
│   ├── AvaliacaoVo2Max.java      → Strategy: classificação VO2Max
│   ├── AvaliacaoVo2MaxAdaptado.java → VO2Max adaptado (mesma lógica)
│   ├── AvaliacaoVo2MaxContext.java → Contexto VO2Max
│   └── AvaliacaoVo2MaxContextBuilder.java → Builder com validação
│
└── enums/
    ├── Sexo.java                 → MASCULINO, FEMININO
    ├── ProtocoloVo2Max.java      → COOPER, ROCKPORT, ESTEIRA, ESTEIRA_INCREMENTAL
    ├── TipoLimite.java           → INCLUSIVO, EXCLUSIVO
    ├── MedicaoTipo.java          → IMC, VO2_MAX, BIOIMPEDANCIA
    ├── ResponsavelType.java      → ACADEMIA, AVALIADOR
    ├── Unidade.java              → KG, METRO
    └── Equipamento.java          → (placeholder)
```

**Decisões técnicas no Domain**:

1. **Interfaces vs Classes Abstratas**: `Component`, `Teste`, `AvaliacaoStrategy` são interfaces porque definem contratos sem estado. `Composite`, `Leaf`, `TesteVo2Max` são classes abstratas porque compartilham implementação base.

2. **Value Objects imutáveis**: `Endereco`, `DadosAvaliacao` são imutáveis (sem setters). Garante thread-safety e facilita testes.

3. **Enums para domínios finitos**: `Sexo`, `ProtocoloVo2Max`, `MedicaoTipo` são enums porque o conjunto de valores é fixo e conhecido.

4. **`DadosAvaliacao` como Map**: Flexibilidade para dados variáveis (idade, sexo, peso) sem criar classes específicas para cada combinação. Trade-off: perde type-safety, ganha flexibilidade.

---

### 3.2 API Layer (Entrada)

**Localização**: `com.prosup.proinsight.api`

**Responsabilidade**: Receber requisições HTTP, validar entrada, chamar services, retornar respostas.

```
api/
├── controller/api/v1/
│   ├── AvaliacaoController.java        → POST /avaliacoes/vo2max
│   ├── AvaliacaoImcController.java     → POST /avaliacoes/imc
│   ├── AvaliadorController.java        → POST /avaliadores
│   ├── AcademiaController.java         → POST /academias
│   ├── ClienteController.java          → CRUD /clientes + /clientes/{id}/avaliacoes
│   ├── UserController.java             → POST /users
│   └── TabelasClassificacaoController.java → GET /tabelas_classificacao
│
├── dto/request/
│   ├── AvaliacaoVo2MaxRequest.java     → Dados para avaliação VO2Max
│   ├── AvaliacaoImcRequest.java        → Record: peso, altura, protocolo
│   ├── TesteVo2MaxDto.java             → Dados de um teste específico
│   ├── MedicaoVo2MaxDto.java           → Lista de testes
│   ├── ClienteRequest.java             → Dados do cliente
│   ├── AvaliadorRequest.java           → Dados do avaliador
│   ├── AcademiaRequest.java            → Dados da academia
│   ├── UserRequest.java                → Dados do usuário
│   ├── RoleRequest.java                → Dados da role
│   ├── PermissionRequest.java          → Dados da permissão
│   └── ResponsavelRequest.java         → CREF do responsável
│
├── dto/response/
│   ├── AvaliacaoVo2MaxResponse.java    → Resposta VO2Max com classificação
│   ├── AvaliacaoImcResponse.java       → Resposta IMC com extras
│   ├── AvaliacaoListaResponse.java     → Item da lista de avaliações
│   ├── ClassificacaoVo2Max.java        → Nome + descrição + valor
│   ├── ClienteResponse.java            → Dados do cliente
│   ├── AvaliadorResponse.java          → Dados do avaliador
│   ├── AcademiaResponse.java           → Dados da academia
│   ├── UserResponse.java               → Dados do usuário (sem senha!)
│   ├── RoleResponse.java               → Role com permissões
│   ├── PermissionResponse.java         → Permissão
│   ├── ResponsavelResponse.java        → ID + CREF
│   └── TabelaClassificacaoResponse.java → Tabela completa
│
├── handler/
│   └── GlobalExceptionHandler.java     → RFC 7807 Problem Details
│
└── mapper/
    └── (mappers de response)
```

**Decisões técnicas na API**:

1. **Records para DTOs simples**: `ClienteResponse`, `UserResponse` são records porque são imutáveis e têm apenas getters. Trade-off: sem validação em construction time.

2. **Classes para DTOs complexos**: `AvaliacaoVo2MaxResponse` tem lógica de construção, então é classe.

3. **API versionada (`/api/v1/`)**: Permite evolução sem quebrar clientes existentes. Controllers ficam em `api/v1/` e são prefixados via `WebConfig`.

4. **RFC 7807 Problem Details**: `GlobalExceptionHandler` retorna erros em formato padronizado (`type`, `title`, `status`, `detail`, `instance`). Mais informativo que `{ "error": "message" }`.

5. **`UserResponse` sem senha**: Nunca expor hash de senha em responses. O `UserController` deveria usar `UserResponse` em vez de `User` (dívida técnica #5).

---

### 3.3 Service Layer (Orquestração)

**Localização**: `com.prosup.proinsight.service`

**Responsabilidade**: Orquestrar fluxos de negócio, coordenar domínio e persistência.

```
service/
├── ClienteService.java              → CRUD com validação de responsável
├── AcademiaService.java             → Criação com validação de usuário
├── AvaliadorService.java            → Criação com validação de usuário/academia
├── UserService.java                 → Registro com BCrypt + validação de roles
├── TabelasClassificacaoService.java → Queries de tabelas
├── AvaliacaoService.java            → Validação de cliente/avaliador
│
└── handler/
    ├── AvaliacaoImcHandler.java     → Pipeline completo IMC
    ├── AvaliacaoVo2MaxHandler.java  → Pipeline completo VO2Max
    └── ListagemAvaliacaoHandler.java → Listagem com conversão
```

**Decisão: Handlers vs Services monolíticos**

O projeto separa a lógica de orquestração em **handlers** (`AvaliacaoImcHandler`, `AvaliacaoVo2MaxHandler`) em vez de colocar tudo no `AvaliacaoService`. Razões:

1. **Single Responsibility**: Cada handler cuida de um fluxo específico (VO2Max vs IMC)
2. **Testabilidade**: Handlers menores são mais fáceis de mockar
3. **Extensibilidade**: Adicionar nova avaliação = novo handler + nova strategy
4. **Legibilidade**: Código linear de pipeline (load → create → build → execute → save → return)

**Fluxo típico de um Handler**:

```
1. Carregar protocolo do banco
2. Criar teste específico (Cooper/Rockport/etc)
3. Criar medição com o teste
4. Carregar tabela de classificação
5. Construir contexto (Builder com validação)
6. Executar strategy (classificar)
7. Salvar avaliação
8. Converter para response
```

---

### 3.4 Infrastructure Layer (Persistência)

**Localização**: `com.prosup.proinsight.infrastructure.persistence`

**Responsabilidade**: Persistir e recuperar dados do MongoDB.

```
infrastructure/persistence/
├── document/
│   ├── UserDocument.java                 → Coleção: users
│   ├── RoleDocument.java                 → Coleção: roles
│   ├── PermissionDocument.java           → Coleção: permissions
│   ├── ClienteDocument.java              → Coleção: clientes (index: responsavelId)
│   ├── AvaliadorDocument.java            → Coleção: avaliadores (unique: CPF)
│   ├── AcademiaDocument.java             → Coleção: academias (unique: CNPJ)
│   ├── AvaliacaoFisicaDocument.java      → Coleção: avaliacoesFisicas
│   ├── TabelaClassificacaoDocument.java  → Coleção: tabelasClassificacao
│   ├── ProtocoloAvaliacaoDocument.java   → Coleção: protocolos
│   ├── MedicaoDocument.java             → Abstrata: base para medições
│   ├── MedicaoVo2MaxDocument.java        → VO2Max com classificação
│   ├── MedicaoImcDocument.java           → IMC com classificação
│   ├── MedicaoBioimpedanciaDocument.java → Bioimpedância
│   │
│   └── composite/                        → Persistência polimórfica
│       ├── PersistedComponent.java       → Interface raiz
│       ├── PersistedComposite.java       → Abstrata: com lista de filhos
│       ├── PersistedLeaf.java            → Abstrata: folha
│       ├── PersistedComponentMixIn.java  → Jackson @JsonTypeInfo/@JsonSubTypes
│       ├── PersistedComponentReadConverter.java  → @ReadingConverter
│       ├── PersistedComponentWriteConverter.java → @WritingConverter
│       ├── PersistedTabelaVo2Max.java    → Filtra por protocolo
│       ├── PersistedTabelaSexo.java      → Filtra por sexo
│       ├── PersistedTabelaIdade.java     → Filtra por idade
│       ├── PersistedTabelaEquipamento.java → Filtra por equipamento
│       ├── PersistedTabelaClassificacaoGenerica.java → Genérico
│       ├── PersistedNivelVo2Max.java     → Folha VO2Max
│       └── PersistedNivelForca.java      → Folha força
│
├── repository/
│   ├── UserRepository.java               → MongoRepository<UserDocument, String>
│   ├── RoleRepository.java               → MongoRepository<RoleDocument, String>
│   ├── PermissionRepository.java         → MongoRepository<PermissionDocument, String>
│   ├── ClienteRepository.java            → + findByResponsavelId()
│   ├── AvaliadorRepository.java          → + findByUserId()
│   ├── AcademiaRepository.java           → + findByUserId()
│   ├── AvaliacaoFisicaRepository.java    → + findByClienteId()
│   ├── TabelaClassificacaoRepository.java → Crud básico
│   └── ProtocoloAvaliacaoRepository.java → Crud básico
│
├── mapper/
│   ├── AvaliacaoFisicaMapper.java        → Domain ↔ Document (polimórfico)
│   ├── AvaliacaoVo2MaxDtoMapper.java     → DTO → Domain
│   ├── TesteVo2MaxMapperRegistry.java    → Protocolo → Converter
│   ├── TabelaClassificacaoMapper.java    → Document → Domain
│   ├── PersistedComponentMapper.java     → Composite ↔ Persisted
│   ├── PersistedComponentRegistry.java   → Bidirecional (7 tipos)
│   ├── AvaliadorMapper.java              → Request/Document/Domain/Response
│   ├── AcademiaMapper.java               → Request/Document/Response
│   └── ClienteMapper.java                → Request/Document/Domain/Response
│
└── adapter/
    └── AvaliadorMongoRepositoryAdapter.java → Legacy wrapper
```

**Decisões técnicas na Infrastructure**:

1. **Documents ≠ Domain Models**: `ClienteDocument` ≠ `Cliente`. Documents têm anotações MongoDB (`@Document`, `@Indexed`), models são POJOs puros. Isso mantém o domínio limpo de infraestrutura.

2. **Mappers manuais vs MapStruct**: O projeto usa mappers manuais porque:
   - Lógica de conversão polimórfica (VO2Max vs IMC) é complexa
   - MapStruct não lida bem com hierarquias polimórficas
   - Trade-off: mais código, mais controle

3. **`@Indexed` em campos de busca**: `ClienteDocument` tem `@Indexed` em `responsavelId` para queries eficientes. Sem índice, `findByResponsavelId()` faria scan completo.

4. **Conversores customizados para polimorfismo**: `PersistedComponentReadConverter`/`WriteConverter` bypassam o `MappingMongoConverter` padrão porque ele não consegue resolver subtipos polimórficos. Ver seção 6.

---

### 3.5 Config Layer

**Localização**: `com.prosup.proinsight.config`

```
config/
├── MongoConfig.java              → Conversores customizados + auditing
├── WebConfig.java                → Prefixo /api/v1 + Jackson MixIn
├── SecurityConfig.java           → BCrypt bean (strength 12)
└── properties/
    └── TabelaClassificacaoProperties.java → IDs das tabelas
```

**Decisões técnicas**:

1. **`MongoConfig`**: Registra `PersistedComponentReadConverter` e `WriteConverter` como `MongoCustomConversions`. Esses conversores têm prioridade sobre o mecanismo padrão do Spring Data.

2. **`WebConfig`**: Dois papéis:
   - `WebMvcConfigurer.addInterceptors()`: adiciona prefixo `/api/v1` para todos os controllers
   - `ObjectMapper configurator`: registra `PersistedComponentMixIn` para serialização Jackson

3. **`SecurityConfig`**: Apenas expõe o bean `BCryptPasswordEncoder`. Não configura autenticação/autorização (a cargo do Spring Security auto-config).

4. **Properties externas**: `TabelaClassificacaoProperties` usa `@ConfigurationProperties` para mapear IDs de tabelas do `application.properties`. Evita hardcoded strings.

---

### 3.6 Bootstrap Layer

**Localização**: `com.prosup.proinsight.bootstrap`

```
bootstrap/
├── TabelaClassificacaoInitializer.java  → CommandLineRunner
└── ProtocoloAvaliacaoInitializer.java   → CommandLineRunner
```

**Responsabilidade**: Seeding de dados iniciais (tabelas de classificação e protocolos).

**Decisão**: Usar `CommandLineRunner` em vez de scripts SQL/MongoDB porque:
- Seeding é parte da aplicação, não do banco
- Pode usar lógica Java (construção da árvore composite)
- Executa apenas uma vez (com `@ConditionalOnMissingBean` ou verificação de existência)

---

## 4. Diagramas

### 4.1 Diagrama de Camadas

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              CLIENTE                                    │
│                           (Browser/App)                                 │
└─────────────────────────────┬───────────────────────────────────────────┘
                              │ HTTP/JSON
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         API LAYER                                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐         │
│  │   Controllers   │  │      DTOs       │  │    Exception    │         │
│  │   (REST)        │  │  (Request/      │  │    Handler      │         │
│  │                 │  │   Response)     │  │  (RFC 7807)     │         │
│  └────────┬────────┘  └────────┬────────┘  └─────────────────┘         │
│           │                    │                                        │
│           │    ┌───────────────┘                                        │
│           │    │ Mappers                                                 │
│           ▼    ▼                                                        │
│  ┌─────────────────┐                                                   │
│  │    Handlers     │ ← Orquestram o fluxo completo                     │
│  │ (IMC, VO2Max)   │                                                   │
│  └────────┬────────┘                                                   │
└───────────┼─────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       SERVICE LAYER                                      │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐         │
│  │ ClienteService  │  │ AcademiaService │  │ AvaliadorService│         │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘         │
│  ┌─────────────────┐  ┌─────────────────┐                              │
│  │ UserService     │  │AvaliacaoService │                              │
│  └─────────────────┘  └─────────────────┘                              │
└───────────┬─────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        DOMAIN LAYER                                      │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐         │
│  │     Models      │  │   Strategies    │  │   Composite     │         │
│  │ (User, Cliente, │  │ (IMC, VO2Max,   │  │   Tree          │         │
│  │  Avaliacao...)  │  │  Adaptado)      │  │ (Tabela→Nivel)  │         │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘         │
│  ┌─────────────────┐  ┌─────────────────┐                              │
│  │    Testes       │  │    Enums        │                              │
│  │ (Cooper, Rock)  │  │ (Sexo, Proto)   │                              │
│  └─────────────────┘  └─────────────────┘                              │
└───────────┬─────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                                  │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐         │
│  │   Documents     │  │  Repositories   │  │    Mappers      │         │
│  │ (MongoDB BSON)  │  │ (Spring Data)   │  │ (Domain↔Doc)    │         │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘         │
│  ┌─────────────────┐  ┌─────────────────┐                              │
│  │   Composite     │  │   Converters    │                              │
│  │   Persisted     │  │ (Read/Write)    │                              │
│  └─────────────────┘  └─────────────────┘                              │
└───────────┬─────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          MONGODB                                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │
│  │  users      │ │  clientes   │ │ avaliacoes  │ │ tabelas     │       │
│  │             │ │             │ │  Fisicas    │ │Classificacao│       │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘       │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4.2 Fluxo de uma Avaliação VO2Max

```
┌──────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────┐
│ Request  │────▶│ Controller   │────▶│   Handler    │────▶│  Domain  │
│ JSON     │     │ Valida DTO   │     │ Orquestra    │     │  Rules   │
└──────────┘     └──────────────┘     └──────────────┘     └──────────┘
                       │                     │                    │
                       │                     │                    │
                       ▼                     ▼                    ▼
                 ┌──────────┐         ┌──────────┐         ┌──────────┐
                 │ Protocolo│         │ Teste    │         │ Strategy │
                 │ Repository│        │ (Cooper) │         │ Registry │
                 └──────────┘         └──────────┘         └──────────┘
                       │                     │                    │
                       │                     ▼                    │
                       │              ┌──────────┐               │
                       │              │ Medição  │               │
                       │              │ VO2Max   │               │
                       │              └──────────┘               │
                       │                                          │
                       ▼                                          ▼
                 ┌──────────┐                              ┌──────────┐
                 │ Tabela   │                              │ Avaliar  │
                 │Classific.│                              │ (Strategy)│
                 └──────────┘                              └──────────┘
                                                                │
                                                                ▼
                                                          ┌──────────┐
                                                          │  Leaf    │
                                                          │ (Nível)  │
                                                          └──────────┘
```

### 4.3 Árvore Composite de Classificação

```
TabelaClassificacao (raiz)
│
└── TabelaVo2Max (filtra por protocolo: COOPER)
    │
    ├── TabelaSexo (MASCULINO)
    │   │
    │   ├── TabelaIdade (20-29 anos)
    │   │   │
    │   │   ├── NivelVo2Max (RUIM, min=0, max=24.9)
    │   │   ├── NivelVo2Max (ABAIXO, min=25.0, max=33.8)
    │   │   ├── NivelVo2Max (RAZOAVEL, min=33.9, max=38.1)
    │   │   ├── NivelVo2Max (BOM, min=38.2, max=43.8)
    │   │   └── NivelVo2Max (OTIMO, min=43.9, max=∞)
    │   │
    │   └── TabelaIdade (30-39 anos)
    │       └── ...
    │
    └── TabelaSexo (FEMININO)
        └── ...
```

### 4.4 Padrão Strategy

```
                    ┌─────────────────────┐
                    │  AvaliacaoStrategy  │
                    │     <<interface>>   │
                    │                     │
                    │ +avaliar(Context)   │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
    ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
    │ AvaliacaoImc    │ │AvaliacaoVo2Max  │ │AvaliacaoVo2Max  │
    │ @StrategyFor    │ │@StrategyFor     │ │Adaptado         │
    │ ("IMC")         │ │("VO2_MAX")      │ │@StrategyFor     │
    │                 │ │                 │ │("VO2_MAX_ADAPT")│
    └─────────────────┘ └─────────────────┘ └─────────────────┘

Uso:
  StrategyRegistry.resolve("IMC") → AvaliacaoImc instance
  strategy.avaliar(context) → Leaf (classificação)
```

---

## 5. Padrões de Design

### 5.1 Strategy Pattern

**Onde**: `domain/strategy/`

**Problema**: Diferentes tipos de avaliação (IMC, VO2Max) têm lógicas de classificação diferentes, mas o fluxo orquestratório é o mesmo.

**Solução**: Interface `AvaliacaoStrategy` com implementações específicas, descobertas automaticamente via `@StrategyFor`.

**Benefícios**:
- Adicionar nova avaliação = criar nova classe com `@StrategyFor`
- Teste unitário isolado de cada strategy
- Desacoplamento entre orquestração e lógica de negócio

**Código chave**:

```java
// Interface
public interface AvaliacaoStrategy {
    Leaf avaliar(AvaliacaoContext context);
}

// Implementação
@StrategyFor("IMC")
public class AvaliacaoImc implements AvaliacaoStrategy {
    @Override
    public Leaf avaliar(AvaliacaoContext context) {
        Double imc = calcularImc(context.getMedicao());
        return context.getTabela().classificarComTeste(
            new TesteImc(imc), context.getDadosAvaliacao()
        );
    }
}

// Registry (auto-descoberta)
@Component
public class StrategyRegistry {
    private final Map<String, AvaliacaoStrategy> strategies = new HashMap<>();
    
    @Autowired
    public StrategyRegistry(List<AvaliacaoStrategy> allStrategies) {
        for (AvaliacaoStrategy s : allStrategies) {
            StrategyFor annotation = s.getClass().getAnnotation(StrategyFor.class);
            strategies.put(annotation.value(), s);
        }
    }
    
    public AvaliacaoStrategy resolve(String key) {
        return strategies.get(key);
    }
}
```

**Por que não enum com switch?**
- Enums são fechados: adicionar tipo requer modificar código existente
- Strategy com registry é aberto: adicionar tipo = adicionar classe
- Testabilidade: cada strategy é testada isoladamente

---

### 5.2 Composite Pattern

**Onde**: `domain/model/composite/`

**Problema**: Tabelas de classificação são árvores hierárquicas (Tabela → Sexo → Idade → Nível). Cada nó tem comportamento diferente mas a interface é a mesma.

**Solução**: Interface `Component` com `Composite` (nó com filhos) e `Leaf` (nó folha).

```
Component (interface)
├── classificar() → Component
├── classificarComTeste(Teste, DadosAvaliacao) → Leaf
│
├── Composite (abstrata)
│   ├── componentes: List<Component>
│   ├── addComponente(Component)
│   └── classificar() → delega para filhos
│
└── Leaf (abstrata)
    └── classificar() → retorna self
```

**Por que não apenas classes concretas?**
- Polimorfismo: código que usa `Component` não precisa saber se é `Tabela` ou `Nivel`
- Recursividade: `classificar()` navega a árvore uniformemente
- Extensibilidade: adicionar novo tipo de tabela = nova subclasse de `Composite`

**Mapeamento para persistência**:

| Domain | Persisted | Anotação |
|--------|-----------|----------|
| `Component` | `PersistedComponent` | Interface |
| `Composite` | `PersistedComposite` | Abstrata |
| `Leaf` | `PersistedLeaf` | Abstrata |
| `TabelaVo2Max` | `PersistedTabelaVo2Max` | `@TypeAlias("persistedTabelaVo2Max")` |
| `NivelVo2Max` | `PersistedNivelVo2Max` | `@TypeAlias("persistedNivelVo2Max")` |

---

### 5.3 Handler Pattern

**Onde**: `service/handler/`

**Problema**: Orquestrar um fluxo de avaliação envolve many steps (load protocol → create test → create measurement → load table → build context → execute strategy → save → return response). Colocar tudo em um Service method monolítico dificulta teste e manutenção.

**Solução**: Handlers dedicados que encapsulam o pipeline completo.

```java
@Component
public class AvaliacaoVo2MaxHandler {
    
    public AvaliacaoVo2MaxResponse executar(AvaliacaoVo2MaxRequest request) {
        // 1. Carregar protocolo
        ProtocoloAvaliacaoDocument protocolo = protocoloRepo.findById(request.protocoloId());
        
        // 2. Criar teste específico
        TesteVo2Max teste = testeMapperRegistry.resolve(protocolo.getProtocolo())
            .mapear(request.medicao());
        
        // 3. Criar medição
        MedicaoVo2Max medicao = new MedicaoVo2Max(protocolo, teste);
        
        // 4. Carregar tabela
        TabelaClassificacao tabela = tabelaService.find(request.tabelaId());
        
        // 5. Construir contexto (com validação)
        AvaliacaoVo2MaxContext context = new AvaliacaoVo2MaxContextBuilder()
            .comMedicao(medicao)
            .comTabela(tabela)
            .comDadosAvaliacao(request.dados())
            .build();
        
        // 6. Executar strategy
        AvaliacaoStrategy strategy = registry.resolve("VO2_MAX");
        Leaf resultado = strategy.avaliar(context);
        
        // 7. Salvar
        avaliacaoRepo.save(document);
        
        // 8. Converter para response
        return converterParaResponse(resultado, medicao);
    }
}
```

**Benefícios**:
- Fluxo linear e legível
- Cada step é testável
- Handlers reutilizam services existentes

---

### 5.4 Registry Pattern

**Onde**: `TesteVo2MaxMapperRegistry`, `StrategyRegistry`, `PersistedComponentRegistry`

**Problema**: Mapear tipos para implementações (Protocolo → Mapper, Strategy Key → Strategy, Type → Converter) sem usar switch/if-else chains.

**Solução**: Mapas estáticos/dinâmicos com lookup O(1).

```java
@Component
public class TesteVo2MaxMapperRegistry {
    private final Map<ProtocoloVo2Max, Function<MedicaoVo2MaxDto, TesteVo2Max>> registry = Map.of(
        ProtocoloVo2Max.COOPER, this::mapearCooper,
        ProtocoloVo2Max.ROCKPORT, this::mapearRockport,
        ProtocoloVo2Max.ESTEIRA_INCREMENTAL, this::mapearEsteiraIncremental
    );
    
    public Function<MedicaoVo2MaxDto, TesteVo2Max> resolve(ProtocoloVo2Max protocolo) {
        return registry.get(protocolo);
    }
}
```

**Por que não switch?**
- Adicionar caso requer modificar código existente (viola Open/Closed)
- Registry é declarativo: novo caso = nova entrada no mapa
- Mais fácil de testar (cada mapper é unitário)

---

### 5.5 Mapper Pattern

**Onde**: `infrastructure/persistence/mapper/`

**Problema**: Converter entre Domain Models, Documents, DTOs e Responses. Cada camada tem sua representação.

**Solução**: Mappers dedicados com conversão bidirecional.

```
Request (DTO) → Domain → Document → MongoDB
                  ↑         ↑
               Mapper    Mapper
                  │         │
                  ▼         ▼
Response (DTO) ← Domain ← Document ← MongoDB
```

**Decisão: Manuais vs MapStruct**

| Critério | Manuais | MapStruct |
|----------|---------|-----------|
| **Controle** | Total | Limitado |
| **Polimorfismo** | Simples | Complexo |
| **Código** | Mais | Menos |
| **Manutenção** | Mais trabalho | Automático |

**Escolha**: Manuais, porque a lógica de conversão polimórfica (VO2Max vs IMC) é complexa demais para anotações.

---

## 6. Persistência Polimórfica

### 6.1 O Problema

O `MappingMongoConverter` do Spring Data não consegue resolver subtipos polimórficos na leitura:

```
MongoDB: { _class: "persistedTabelaVo2Max", ... }
           ↓
Spring Data: Class.forName("persistedTabelaVo2Max") → falha!
           ↓
Fallback: PersistedComponent (interface) → não instanciável
           ↓
Erro: MappingInstantiationException
```

### 6.2 A Solução

Usar conversores customizados que bypassam o mecanismo padrão:

```
┌─────────────────────────────────────────────────────────────┐
│                    LEITURA                                    │
│                                                               │
│  MongoDB Document                                            │
│       ↓                                                       │
│  PersistedComponentReadConverter (@ReadingConverter)          │
│       ↓                                                       │
│  document.toJson() → JSON string                             │
│       ↓                                                       │
│  ObjectMapper.readValue(json, PersistedComponent.class)      │
│       ↓                                                       │
│  PersistedComponentMixIn detecta _class                      │
│       ↓                                                       │
│  @JsonSubTypes resolve → PersistedTabelaVo2Max              │
│       ↓                                                       │
│  Jackson desserializa recursivamente toda a árvore           │
└─────────────────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────────────────┐
│                    ESCRITA                                    │
│                                                               │
│  PersistedComponent (árvore em memória)                      │
│       ↓                                                       │
│  PersistedComponentWriteConverter (@WritingConverter)        │
│       ↓                                                       │
│  objectMapper.writeValueAsString(component)                  │
│       ↓                                                       │
│  PersistedComponentMixIn adiciona _class                     │
│       ↓                                                       │
│  Jackson serializa recursivamente toda a árvore              │
│       ↓                                                       │
│  Document.parse(json) → MongoDB Document                     │
└─────────────────────────────────────────────────────────────┘
```

### 6.3 Componentes

| Componente | Função |
|------------|--------|
| `PersistedComponentMixIn` | `@JsonTypeInfo(property="_class")` + `@JsonSubTypes` para 7 tipos |
| `PersistedComponentReadConverter` | `Document` → `PersistedComponent` via ObjectMapper |
| `PersistedComponentWriteConverter` | `PersistedComponent` → `Document` via ObjectMapper |
| `MongoConfig` | Registra os conversores como `MongoCustomConversions` |

### 6.4 Documento no MongoDB

```javascript
{
  _id: "classificacao_cooper_12min",
  _class: "tabelaClassificacao",           // @TypeAlias do documento raiz
  nome: "Classificação Cooper 12 min",
  raiz: {
    _class: "persistedTabelaVo2Max",       // @JsonSubTypes name
    protocolo: "COOPER",
    componentes: [{
      _class: "persistedTabelaSexo",
      sexo: "MASCULINO",
      componentes: [{
        _class: "persistedTabelaIdade",
        idadeMin: 20,
        idadeMax: 29,
        componentes: [
          { _class: "persistedNivelVo2Max", classificacao: "RUIM", min: 0, max: 24.9 },
          { _class: "persistedNivelVo2Max", classificacao: "OTIMO", min: 43.9, max: null }
        ]
      }]
    }]
  }
}
```

---

## 7. Fluxo de Requisição

### 7.1 Avaliação VO2Max (Cooper)

```
1. POST /api/v1/avaliacoes/vo2max
   Body: { clienteId, avaliadorId, tabelaId, medicao: { distancia: 2400, tempo: 720 } }

2. AvaliacaoController.avaliarVo2Max(request)
   → Chama AvaliacaoVo2MaxHandler.executar(request)

3. Handler: ProtocoloAvaliacaoRepository.findById("protocolo_cooper")
   → Retorna: { id: "protocolo_cooper", protocolo: "COOPER", strategyKey: "VO2_MAX" }

4. Handler: TesteVo2MaxMapperRegistry.resolve(COOPER) → mapearCooper()
   → Cria: TesteVo2MaxCooper(distancia=2400, tempo=720)

5. Handler: Cria MedicaoVo2Max(protocolo, teste)

6. Handler: TabelasClassificacaoService.find("classificacao_cooper_12min")
   → Retorna árvore TabelaVo2Max → TabelaSexo → TabelaIdade → NivelVo2Max

7. Handler: AvaliacaoVo2MaxContextBuilder.build()
   → Valida: medicao ≠ null, tabela ≠ null, idade > 0, sexo ≠ null

8. Handler: StrategyRegistry.resolve("VO2_MAX") → AvaliacaoVo2Max
   → strategy.avaliar(context)

9. Strategy: TabelaVo2Max.classificarComTeste(teste, dados)
   → Filtra por protocolo COOPER ✓
   → Filtra por sexo MASCULINO ✓
   → Filtra por idade 25 ∈ [20, 29] ✓
   → Retorna NivelVo2Max(classificacao="OTIMO", min=43.9, max=null)

10. Handler: Salva AvaliacaoFisicaDocument no MongoDB

11. Handler: Converte para AvaliacaoVo2MaxResponse
    → { classificacao: "OTIMO", descricao: "...", valorVo2Max: 45.2 }

12. Controller: Retorna 200 OK com response
```

---

## 8. Decisões Técnicas

### 8.1 Resumo de Decisões

| Decisão | Alternativa | Motivo |
|---------|-------------|--------|
| MongoDB | PostgreSQL | Schema hierárquico natural, polimorfismo via `_class` |
| Records para DTOs | Classes | Imutabilidade, less boilerplate |
| Mappers manuais | MapStruct | Controle total sobre conversão polimórfica |
| Handlers dedicados | Services monolíticos | Single Responsibility, testabilidade |
| Strategy + Registry | Switch/enum | Open/Closed, extensibilidade |
| Composite Pattern | Classes avulsas | Polimorfismo recursivo na árvore |
| RFC 7807 Problem Details | `{ "error": "msg" }` | Padrão REST, mais informativo |
| BCrypt strength 12 | SHA-256 | One-way hash com salt, resistente a rainbow tables |
| Logs JSON | Logs texto | Agregação em ELK/Datadog, query estruturada |
| API versionada | Sem versionamento | Evolução sem breaking changes |
| CommandLineRunner | Scripts SQL | Seeding com lógica Java, versionado no código |

### 8.2 Trade-offs Conhecidos

| Trade-off | Lado bom | Lado ruim |
|-----------|----------|-----------|
| MongoDB sem transactions | Performance, flexibilidade | Consistência eventual |
| Mappers manuais | Controle total | Mais código para manter |
| `DadosAvaliacao` como Map | Flexibilidade | Perde type-safety |
| Handlers menores | Testabilidade | Mais classes |
| Sem autenticação | Simples para dev | Precisa implementar em produção |

---

*Documentado em: 2026-07-10*
*Versão: 1.0*