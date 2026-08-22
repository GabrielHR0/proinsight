# Sprint Planning — Proinsight

> Planejamento de sprints baseado no estado atual do projeto.
> Cada task é curta, acionável e com critério de aceite claro.
>
> **Status:** ✅ Concluído | ❌ Pendente | 🔄 Em andamento
>
> Atualizado: 2026-07-17

---

## Visão Geral das Sprints

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         ROADMAP GERAL                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Sprint 1 (2-3 semanas)     Sprint 2 (3-4 semanas)    Sprint 3 (3-4 semanas)│
│  ─────────────────────      ──────────────────────    ──────────────────── │
│  Cadastros + Avaliações     Segurança + Estudo        Multi-Tenancy         │
│  + Dívida Técnica           Profundo                  + Isolamento          │
│                                                                             │
│  Foco:                       Foco:                     Foco:                 │
│  • CRUD completo             • Autenticação            • Isolamento por      │
│  • Endpoints funcionando     • Autorização               academia/avaliador  │
│  • Dívida técnica crítica    • Filters                 • Queries seguras     │
│  • Testes passando           • JWT/OAuth               • Estudo de tenants   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## SPRINT 1: Cadastros, Avaliações e Dívida Técnica

**Objetivo**: Fechar a base do sistema — endpoints funcionando, dívida técnica crítica resolvida, testes passando.

**Duração estimada**: 2-3 semanas

---

### Fase 1.1: Corrigir Bugs Críticos (Dívida Técnica)

> Esses bugs impedem o funcionamento correto. Resolver ANTES de tudo.

| # | Task | Arquivo | Descrição | Critério de Aceite |
|---|------|---------|-----------|-------------------|
| ✅ 1.1.1 | **Corrigir `Role.setId()`** | `domain/model/Role.java:42-43` | Setter vazio: `setId(String id) {}`. Deveria ser `this.id = id;` | Role carrega ID corretamente do banco |
| ✅ 1.1.2 | **Corrigir `UserController.register()`** | `api/controller/UserController.java:36-38` | Retorna `User` (com password). Usar `UserResponse` | Response nunca expõe hash de senha |
| ✅ 1.1.3 | **Adicionar `findByEmail` ao `UserService`** | `service/UserService.java:100-104` | `findAll().stream().filter()` → query indexada | Adicionar `findByEmail()` no `UserRepository` |
| ✅ 1.1.4 | **Adicionar `findByUserId` ao `AcademiaService`** | `service/AcademiaService.java:31` | `findAll().stream().filter()` → query indexada | Usar query do repository |

---

### Fase 1.2: Completar Endpoints de Cadastro

> Garantir que todos os CRUDs funcionam com validação e testes.

| # | Task | Descrição | Critério de Aceite |
|---|------|-----------|-------------------|
| ✅ 1.2.1 | **CRUD Academia completo** | Controller usa DTOs (`AcademiaRequest`/`AcademiaResponse`), não domain | POST/GET/PUT/DELETE funcionando |
| ✅ 1.2.2 | **CRUD Avaliador completo** | `cpf` já é mapeado em `AvaliadorMapper.toDocument()` (dívida #11 corrigida) | CPF persistido corretamente |
| ✅ 1.2.3 | **CRUD Cliente completo** | `responsavelId`/`responsavelType` já existem no `ClienteDocument` | Campos persistidos e query por responsavelId funcionando |
| ✅ 1.2.4 | **CRUD Usuários completo** | Usa `UserRequest` do pacote `api/dto/request/` (dívida #13 corrigida) | DTO consistente com padrão do projeto |
| ❌ 1.2.5 | **CRUD Roles/Permissions** | Criar endpoints básicos para gerenciar roles e permissões | POST/GET para roles e permissions |

---

### Fase 1.3: Completar Pipeline de Avaliações

> O core do sistema — avaliações precisam funcionar ponta a ponta.

| # | Task | Descrição | Critério de Aceite |
|---|------|-----------|-------------------|
| ✅ 1.3.1 | **`AvaliacaoService.salvarAvaliacao()`** | Service refatorado com `save()` que valida e persiste (dívida #10) | Avaliação salva no MongoDB |
| ✅ 1.3.2 | **Registrar ROCKPORT no `TesteVo2MaxMapperRegistry`** | COOPER, ROCKPORT e ESTEIRA_INCREMENTAL registrados (dívida #9) | Avaliação Rockport funciona sem erro |
| ✅ 1.3.3 | **Corrigir `AvaliacaoVo2MaxHandler.avaliar()` null-check** | `IllegalStateException` lançado quando resultado é null (dívida #14) | NPE evitado, erro claro retornado |
| ✅ 1.3.4 | **Corrigir `AvaliacaoVo2MaxHandler.converterParaResponse()`** | Extrai dados reais do `NivelVo2Max` (dívida #15) | Response com classificação real |
| ✅ 1.3.5 | **Testes de integração de avaliação** | `AvaliacaoIntegrationTest` com 8 testes cobrindo todos os protocolos | Testes passando com MongoDB real |
| ✅ 1.3.6 | **Teste de integração IMC** | Testes de fluxo completo, obesidade, abaixo do peso | Testes passando |

---

### Fase 1.4: Limpeza e Consistência

> Padronizar o código para manutenção futura.

| # | Task | Descrição | Critério de Aceite |
|---|------|-----------|-------------------|
| ✅ 1.4.1 | **`AcademiaController` usar DTOs** | Já usa `AcademiaRequest`/`AcademiaResponse` (dívida #12) | Controllers consistentes |
| ✅ 1.4.2 | **`AvaliacaoController` usar construtor** | Já usa construtor explícito (dívida #18) | Injeção de dependência consistente |
| ✅ 1.4.3 | **Remover plugins duplicados do POM** | POM limpo, cada plugin declarado uma vez (dívida #19) | POM limpo |
| ✅ 1.4.4 | **URLs placeholder no `GlobalExceptionHandler`** | Substituído por `proinsight://problems/*` (dívida #20) | URLs funcionais |
| ❌ 1.4.5 | **Testes vazios** | Implementar `AvaliadorControllerValidationTest` (dívida #16) ou remover | Testes com cobertura real |
| ❌ 1.4.6 | **Descomentar `ExampleRepositoryIT`** | Remover ou implementar (dívida #17) | Teste removido ou funcionando |
| ✅ 1.4.7 | **Remover validações dos Documents** | Validações ficam apenas nos DTOs Request | Documents sem `@NotBlank`, `@Email`, `@CPF` |

### Fase 1.5: Usuário com Múltiplas Academias (Modelo de Dados)

> Um usuário poder gerenciar mais de uma academia. Isso muda a relação User ↔ Academia de 1:1 para 1:N.

| # | Task | Descrição | Critério de Aceite |
|---|------|-----------|-------------------|
| ✅ 1.5.1 | **Mapear relacionamento atual** | Entendido: `AcademiaDocument.userId` era 1:1 | Documento com diagrama antes/depois |
| ✅ 1.5.2 | **Definir estratégia de relacionamento** | Opção A escolhida: `User.academiaIds: List<String>` (embed) + `Academia.ownerId` | Decisão documentada com trade-offs |
| ✅ 1.5.3 | **Refatorar `UserDocument`** | Adicionado `academiaIds: List<String>` + `avaliadorId: String` | Campo existe e é persistido |
| ✅ 1.5.4 | **Refatorar `User` domain** | Adicionado `academiaIds: List<String>` + `avaliadorId: String` | Modelo refatorado |
| ✅ 1.5.5 | **Atualizar `UserService`** | Usa `UserMapper` para mapear `academiaIds` e `avaliadorId` | Mapeamento funcionando |
| ✅ 1.5.6 | **Atualizar `AcademiaService`** | `AcademiaDocument.ownerId` referencia o dono | Vinculação funcionando |
| ✅ 1.5.7 | **Atualizar `UserController`** | `UserResponse` inclui `academiaIds` e `avaliadorId` | Response completa |
| ✅ 1.5.8 | **Atualizar queries de academia** | `AcademiaRepository.findByOwnerId()` + `findByOwnerIdIn()` | Queries funcionais |
| ✅ 1.5.9 | **Refatorar `ClienteDocument`** | Substituído `responsavelId`/`responsavelType` por `academiaId`/`avaliadorId` | Cliente isolado por academia |
| ✅ 1.5.10 | **Testes de múltiplas academias** | Testes de integração com 8 cenários | Testes passando |

---

### Fase 1.6: Validação de Campos da API

> Garantir que toda entrada da API é validada antes de chegar ao domínio. Dados inválidos devem ser rejeitados com erro claro e estruturado (RFC 7807).
>
> **Problema identificado:** Vários controllers não usam `@Valid` nos `@RequestBody`, e DTOs críticos como `AvaliacaoVo2MaxRequest` não têm anotações de validação. Isso significa que dados nulos, vazios ou mal formatados são aceitos silenciosamente.

| # | Task | Onde | Descrição | Critério de Aceite |
|---|------|------|-----------|-------------------|
| ✅ 1.6.1 | **Adicionar `@Valid` nos controllers** | `UserController`, `AcademiaController` (POST/PUT), `AvaliacaoController` | `@Valid @RequestBody` ativa o Jakarta Bean Validation | Request inválido retorna 400 com violations no corpo |
| ✅ 1.6.2 | **Validar `AvaliacaoVo2MaxRequest`** | `api/dto/request/AvaliacaoVo2MaxRequest.java` | Adicionar `@NotBlank` em IDs, `@NotNull`/`@Positive` em campos numéricos | DTO rejeita campos nulos/mal formatados |
| ✅ 1.6.3 | **Validar `MedicaoVo2MaxDto` e `TesteVo2MaxDto`** | `api/dto/request/MedicaoVo2MaxDto.java`, `TesteVo2MaxDto.java` | `@NotNull` em `medidoEm`, `@Valid` na lista de testes, `@NotNull` em `protocolo` | Nested DTOs validados em cascata |
| ✅ 1.6.4 | **Validar `AcademiaRequest.EnderecoRequest`** | `api/dto/request/AcademiaRequest.java` | `@NotBlank` em rua, cidade, estado, cep | Endereço inválido rejeitado |
| ✅ 1.6.5 | **Adicionar `@Validated` em controllers com `@RequestParam`** | `ProtocoloHubController` | `@Validated` na classe + `@NotBlank` em `userId` | Parâmetro vazio retorna 400 |
| ✅ 1.6.6 | **Adicionar validação em construtores de domínios core** | `User`, `Cliente`, `Academia` | Guard clauses: null check, blank check, tamanho mínimo | Domínio rejeita estado inválido mesmo construído diretamente |
| ✅ 1.6.7 | **Corrigir `RuntimeException` no `AvaliacaoVo2MaxHandler`** | `service/handler/AvaliacaoVo2MaxHandler.java` | Trocar por `NoSuchElementException` para consistência | Exceção consistente com o resto do código |
| ✅ 1.6.8 | **Remover validação redundante handlers vs services** | `AvaliacaoVo2MaxHandler`, `AvaliacaoImcHandler` | Handlers validam existência, services validam de novo. Manter só no service. | Validação em camada única (service) |
| ✅ 1.6.9 | **Padronizar mensagens de erro em português** | Todos os DTOs Request | `@NotBlank(message = "Campo obrigatório")`, `@Email(message = "E-mail inválido")` | Mensagens consistentes e em português |
| ✅ 1.6.10 | **Testes de validação** | `src/test/.../controller/AvaliacaoControllerValidationTest.java` | Testes que enviam payloads inválidos e assertem 400 com RFC 7807 | 6 testes: campos obrigatórios, negativo, múltiplos erros |

---

### Checklist de Entrega — Sprint 1

- [x] Todos os bugs críticos resolvidos (1.1.x)
- [x] CRUDs completos: Academia ✅, Avaliador ✅, Cliente ✅, User ✅, Role/Permission ❌ (sprint 2)
- [x] Pipeline de avaliação VO2Max (Cooper + Rockport + Esteira Incremental) funcionando
- [x] Pipeline de avaliação IMC funcionando
- [x] **Usuário pode ter múltiplas academias** (modelo + endpoints)
- [x] Testes unitários passando (`./mvnw test`)
- [x] Testes de integração passando (`./mvnw verify`)
- [x] Código padronizado (DTOs, construtores, sem plugins duplicados, mappers consistentes)
- [x] Validações apenas nos DTOs Request (removidas dos Documents)
- [x] URLs do GlobalExceptionHandler corrigidas (`proinsight://problems/*`)
- [x] Protocolos de avaliação com campos descritivos completos
- [x] **Validação de campos da API implementada (Fase 1.6)** — 10/10 tarefas concluídas
- [x] **Ativo field** adicionado ao Cliente (domain + document + DTO + frontend)
- [x] **Corrigido `ClienteController @RequestMapping`** (removido `/api/v1` duplicado com WebConfig)
- [x] **ProtocoloVo2Max renomeado para `Protocolo`** — enum geral para todos os tipos de avaliação

**Progresso Sprint 1: 100%** (1.2.5 Roles/Permissions movido para Sprint 2)

---

## SPRINT 2: Segurança + Estudo Profundo

**Objetivo**: Implementar autenticação/autorização e estudar segurança a fundo — não é sobre "colocar JWT", é sobre **entender** o fluxo completo de segurança em APIs REST.

**Duração estimada**: 3-4 semanas

**Filosofia**: Cada task inclui **o que estudar** antes de implementar. O aprendizado é o produto, a implementação é o bônus.

---

### Fase 2.1: Fundamentos de Segurança Web (Estudo)

> Estudar ANTES de escrever qualquer código. Entender o "porquê" antes do "como".

| # | Task | O que Estudar | Por que Importa | Recursos Sugeridos |
|---|------|---------------|-----------------|-------------------|
| 2.1.1 | **Autenticação vs Autorização** | Diferença entre "quem é você" e "o que você pode fazer" | São problemas diferentes com soluções diferentes | [Spring Security Architecture (ref. docs)](https://docs.spring.io/spring-security/reference/7.0/servlet/architecture.html) — entenda a arquitetura primeiro |
| 2.1.2 | **HTTP Statelessness** | Por que HTTP é stateless e como state é mantido (cookies, tokens, sessions) | Base para entender JWT, OAuth, sessions | [MDN: HTTP Authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication) + [MDN: Set-Cookie](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie) |
| 2.1.3 | **OWASP Top 10** | As 10 vulnerabilidades mais comuns (Injection, Broken Auth, XSS, etc.) | Saber O QUE proteger antes de como | [OWASP Top 10](https://owasp.org/www-project-top-ten/) — leia as descrições de cada item |
| 2.1.4 | **HTTPS e TLS** | Como TLS funciona, certificados, por que HTTP é inseguro | Proteção em trânsito é obrigatória | [Cloudflare Learning Center: TLS](https://www.cloudflare.com/learning/ssl/transport-layer-security-tls/) + [Let's Encrypt](https://letsencrypt.org/pt-br/how-it-works/) |
| 2.1.5 | **Password Hashing** | BCrypt, scrypt, Argon2 — por que NUNCA guardar senhas em texto plano | Já usamos BCrypt, mas precisamos entender POR QUÊ | [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) |

---

### Fase 2.2: Spring Security — Arquitetura (Estudo + Implementação)

> Entender como o Spring Security funciona POR BAIXO dos panos.

| # | Task | O que Estudar | Implementação | Critério de Aceite |
|---|------|---------------|---------------|-------------------|
| 2.2.1 | **SecurityFilterChain** | Como filtros são encadeados. Ordem de execução. `OncePerRequestFilter` | Criar `SecurityConfig` com filter chain | [Spring Security Architecture](https://docs.spring.io/spring-security/reference/7.0/servlet/architecture.html) + [SecurityFilterChain](https://docs.spring.io/spring-security/reference/7.0/servlet/configuration/java.html) |
| 2.2.2 | **AuthenticationManager** | Como autenticação é processada. `AuthenticationProvider`, `Authentication` object | Configurar `AuthenticationManager` bean | [Authentication Architecture](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/architecture.html) |
| 2.2.3 | **UserDetailsService** | Como carregar usuários do banco. `loadUserByUsername()` | Implementar `CustomUserDetailsService` | [UserDetailsService docs](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/passwords/user-details-service.html) |
| 2.2.4 | **PasswordEncoder** | BCrypt strength, por que 12 é bom, performance vs segurança | Já existe `SecurityConfig` — verificar se está correto | [PasswordEncoder docs](https://docs.spring.io/spring-security/reference/7.0/features/authentication/password-storage.html) |
| 2.2.5 | **@PreAuthorize / @Secured** | Method-level security. SpEL expressions | Habilitar `@EnableMethodSecurity` | [Method Security](https://docs.spring.io/spring-security/reference/7.0/servlet/authorization/method-security.html) |

---

### Fase 2.3: JWT — O Padrão de Autenticação (Estudo + Implementação)

> JWT é o padrão mais comum para APIs REST. Entender deeply.

| # | Task | O que Estudar | Implementação | Critério de Aceite |
| 2.3.1 | **O que é JWT** | Header.Payload.Signature. Por que é stateless. Formato Base64URL. Use [jwt.io](https://jwt.io) para decodificar/verificar tokens manualmente | Nenhum (estudo puro) | Consegue decodificar um JWT manualmente |
| 2.3.2 | **Fluxo de Login** | Client envia credenciais → Server valida → Gera JWT → Client armazena → Client envia em cada request | Criar `POST /auth/login` | Login retorna JWT |
| 2.3.3 | **JwtAuthenticationFilter** | `OncePerRequestFilter` que lê `Authorization: Bearer <token>` e valida. [Exemplo BearerTokenResolver](https://docs.spring.io/spring-security/reference/7.0/api/java/org/springframework/security/oauth2/server/resource/web/BearerTokenResolver.html) | Criar `JwtAuthenticationFilter` | Request com token é autenticado |
| 2.3.4 | **Geração e Validação de Token** | [`nimbus-jose-jwt`](https://connect2id.com/products/nimbus-jose-jwt) já incluso via `spring-boot-starter-oauth2-resource-server`. [Spring Security JWT docs](https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/resource-server/jwt.html) | Criar `JwtTokenProvider` | Token gerado e validado |
| 2.3.5 | **Refresh Tokens** | Por que access tokens curtos + refresh tokens longos são necessários | Implementar `POST /auth/refresh` | Refresh token gera novo access token |
| 2.3.6 | **Token Revocation** | Blacklist, Redis, ou curto expiry. Trade-offs | Decidir abordagem e implementar | Token invalidado funciona |
| 2.3.7 | **JWT com múltiplas academias** | Claims customizadas: `academiaIds: [...]` no payload do JWT | Incluir lista de academias no token | Token contém todas as academias do user |
| 2.3.8 | **Seleção de academia no login** | Client envia `academiaId` no login → server valida se pertence ao user → gera token com academia ativa | Criar `POST /auth/login` com `academiaId` opcional | Token reflete academia selecionada |

---

### Fase 2.4: Filtros de Segurança (Estudo + Implementação)

> Filtros são o coração da segurança. Cada filter tem um papel específico.

| # | Task | O que Estudar | Implementação | Critério de Aceite |
|---|------|---------------|---------------|-------------------|
| 2.4.1 | **O que é um Filter no Spring** | `jakarta.servlet.Filter` vs `OncePerRequestFilter`. Ordem. `@Order`. [Spring Security Filter Chain](https://docs.spring.io/spring-security/reference/7.0/servlet/architecture.html#servlet-securityarchitecture) | Criar filtro de logging de requests | Request logado antes de chegar ao controller |
| 2.4.2 | **CORS Filter** | Cross-Origin Resource Sharing. Por que browsers bloqueiam requests cross-origin. [MDN: CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) | Configurar CORS no `WebConfig` ou filter | Frontend consegue chamar API |
| 2.4.3 | **Rate Limiting Filter** | Throttling. Algoritmos: Token Bucket, Sliding Window. [Cloudflare: Rate Limiting](https://www.cloudflare.com/learning/bots/what-is-rate-limiting/) | Criar `RateLimitFilter` com bucket simples | Request excedente retorna 429 |
| 2.4.4 | **Security Headers Filter** | `X-Content-Type-Options`, `X-Frame-Options`, `Strict-Transport-Security`, `Content-Security-Policy`. [OWASP Headers](https://owasp.org/www-project-secure-headers/) | Criar `SecurityHeadersFilter` | Headers presentes em toda response |
| 2.4.5 | **Request Validation Filter** | Validar Content-Type, tamanho do body, encoding | Criar `RequestValidationFilter` | Request inválida rejeitada antes do controller |
| 2.4.6 | **Audit Log Filter** | Logar quem acessou o quê, quando. IP, user-agent | Criar `AuditLogFilter` | Logs de auditoria presentes |

---

### Fase 2.5: Testes de Segurança

> Segurança sem teste é achismo.

| # | Task | Descrição | Critério de Aceite |
|---|------|-----------|-------------------|
| 2.5.1 | **Teste de autenticação** | Request sem token → 401. Token inválido → 401. Token expirado → 401 | Todos os cenários cobertos |
| 2.5.2 | **Teste de autorização** | User comum não acessa admin. Avaliador não acessa dados de outro | Roles respeitadas |
| 2.5.3 | **Teste de JWT** | Token malformado → 401. Claims faltando → 401 | Edge cases cobertos |
| 2.5.4 | **Teste de rate limiting** | Muitas requests → 429 | Limitador funciona |
| 2.5.5 | **Teste de headers** | Respostas têm headers de segurança | Headers presentes |

---

### Checklist de Entrega — Sprint 2

- [ ] Login funcional (`POST /auth/login` retorna JWT)
- [ ] **JWT contém lista de academias do usuário**
- [ ] **Seleção de academia no login (`academiaId` no request)**
- [ ] `JwtAuthenticationFilter` validando tokens em endpoints protegidos
- [ ] `@PreAuthorize` funcionando em métodos sensíveis
- [ ] CORS configurado para frontend
- [ ] Rate limiting ativo
- [ ] Security headers em todas as responses
- [ ] Audit log funcionando
- [ ] Todos os testes de segurança passando
- [ ] Documentação de aprendizado (notas pessoais sobre o que foi estudado)

---

## SPRINT 3: Multi-Tenancy (Isolamento por Academia/Avaliador)

**Objetivo**: Implementar isolamento de dados por tenant. Cada academia vê apenas seus clientes e avaliações. Cada avaliador vê apenas seus dados.

**Duração estimada**: 3-4 semanas

**Filosofia**: Estudar padrões de multi-tenancy ANTES de implementar. Não inventar — usar padrões comprovados.

---

### Fase 3.1: Fundamentos de Multi-Tenancy (Estudo)

> Multi-tenancy é um problema de ARQUITETURA, não de código. Estudar antes de implementar.

| # | Task | O que Estudar | Por que Importa | Recursos Sugeridos |
|---|------|---------------|-----------------|-------------------|
| 3.1.1 | **O que é Multi-Tenancy** | Definição, tipos (SaaS, shared DB, isolated DB), trade-offs | Entender o problema antes da solução | Martin Fowler: "Multi-tenant Architecture" |
| 3.1.2 | **Padrões de Isolamento** | Shared Database/Shared Schema, Shared Database/Isolated Schema, Isolated Database | Cada padrão tem trade-offs diferentes | AWS Multi-Tenancy Whitepaper |
| 3.1.3 | **Row-Level Security** | Como filtrar linhas por tenant automaticamente. Database-level vs Application-level | É a abordagem mais comum e flexível | PostgreSQL RLS docs, MongoDB Authorization |
| 3.1.4 | **Tenant Context** | Como propagar "quem é o tenant" durante a request. ThreadLocal, SecurityContext | Base para todas as implementações | Spring Security Context docs |
| 3.1.5 | **MongoDB Authorization** | `db.grantRolesToUser()`, `readWrite` por database/collection. Limitações | MongoDB NÃO tem RLS nativo como PostgreSQL | MongoDB Security docs |

---

### Fase 3.2: Definir Modelo de Tenancy

> Decidir COMO o tenant será identificado e propagado.

| # | Task | Descrição | Entregável |
|---|------|-----------|------------|
| 3.2.1 | **Mapear entidades por tenant** | Quais entidades são por tenant? Academia? Avaliador? Cliente? Avaliação? | Diagrama de entidades com tenant_id |
| 3.2.2 | **Decidir estratégia de isolamento** | Shared Database + `tenantId` field em documentos | Documento de decisão com trade-offs |
| 3.2.3 | **Definir fonte do tenant** | JWT claim? Header customizado? URL path? | Decidir: JWT claim `tenantId` |
| 3.2.4 | **Adicionar `tenantId` aos Documents** | `ClienteDocument`, `AvaliacaoFisicaDocument`, etc. | Campo `tenantId` em todos os documentos relevantes |
| 3.2.5 | **Criar índice composto** | `{ tenantId: 1, ... }` para queries eficientes | Índices criados no MongoDB |
| 3.2.6 | **Definir "academia ativa"** | Usuário com múltiplas academias: qual é o "tenant" da request? Opção: header `X-Academia-Id` ou claim `activeAcademiaId` | Decisão documentada |
| 3.2.7 | **Endpoint de troca de academia** | `POST /auth/switch-academia` — gera novo token com `activeAcademiaId` atualizado | Usuário troca de academia |

---

### Fase 3.3: Implementar Tenant Context

> Criar o mecanismo que sabe "quem é o tenant" durante a request.

| # | Task | O que Estudar | Implementação | Critério de Aceite |
|---|------|---------------|---------------|-------------------|
| 3.3.1 | **`TenantContext` (ThreadLocal)** | ThreadLocal em Spring. Como funciona por request. Risks (thread pools) | Criar `TenantContext` com `ThreadLocal<String>` | Tenant disponível durante request |
| 3.3.2 | **`TenantFilter`** | `OncePerRequestFilter` que extrai tenant do JWT e coloca no `TenantContext` | Criar `TenantFilter` | Tenant extraído automaticamente |
| 3.3.3 | **`TenantCleanupFilter`** | Limpar `TenantContext` após a request (evitar memory leaks) | Criar `TenantCleanupFilter` | Sem leaks de ThreadLocal |
| 3.3.4 | **Testar propagação** | Unit test: TenantFilter → TenantContext → Service | Teste de integração | Tenant propagado corretamente |

---

### Fase 3.4: Isolar Queries (O Coro da Multi-Tenancy)

> Onde a mágica acontece — cada query filtra por tenant automaticamente.

| # | Task | O que Estudar | Implementação | Critério de Aceite |
|---|------|---------------|---------------|-------------------|
| 3.4.1 | **Spring Data MongoDB Query Derivation** | `findByTenantIdAnd...()`, `@Query` com `{ tenantId: ?0 }` | Adicionar `tenantId` a todos os repositories | Queries filtram por tenant |
| 3.4.2 | **`MongoTemplate` com Query dinâmica** | `Query.query(Criteria.where("tenantId").is(...))` | Criar `TenantAwareRepository` ou interceptor | Query automática por tenant |
| 3.4.3 | **`@DocumentListener` (beforeSave)** | Auto-preencher `tenantId` ao salvar | Criar listener que injeta tenantId | Todo documento salvo tem tenantId |
| 3.4.4 | **Teste de isolamento** | Criar 2 tenants, salvar dados, verificar que um não vê o outro | Teste de integração | Isolamento verificado |
| 3.4.5 | **Teste de bypass** | Tentar acessar dados de outro tenant via query manual | Teste negativo | Bypass impossível |

---

### Fase 3.5: Endpoints com Isolamento

> Adaptar controllers para respeitar o tenant.

| # | Task | Descrição | Critério de Aceite |
|---|------|-----------|-------------------|
| 3.5.1 | **`ClienteController` filtrado** | `GET /clientes` retorna apenas clientes da academia logada | Tenant filtering funcionando |
| 3.5.2 | **`AvaliacaoController` filtrado** | `GET /avaliacoes` retorna apenas avaliações do avaliador logado | Tenant filtering funcionando |
| 3.5.3 | **`AcademiaController` isolado** | Academia só vê seus próprios dados | Isolamento por academia |
| 3.5.4 | **`AvaliadorController` isolado** | Avaliador só vê seus próprios clientes/avaliações | Isolamento por avaliador |
| 3.5.5 | **Testes end-to-end** | Cenário completo: login → listar → verificar isolamento | Fluxo completo testado |

---

### Fase 3.6: Segurança Avançada de Tenants

> Proteger contra tentativas de bypass.

| # | Task | O que Estudar | Implementação | Critério de Aceite |
|---|------|---------------|---------------|-------------------|
| 3.6.1 | **`@PreAuthorize` com tenant** | SpEL: `#entity.tenantId == authentication.tenantId` | Method security com tenant check | Acesso negado para tenant errado |
| 3.6.2 | **Auditoria de tentativas de acesso** | Logar tentativas de acesso cross-tenant | Criar auditoria específica | Tentativas logadas |
| 3.6.3 | **Teste de adversário** | Simular atacante tentando acessar dados de outro tenant | Teste de segurança | Atacante bloqueado |
| 3.6.4 | **Documentar modelo de segurança** | Documentar como funciona, trade-offs, limitações | Documento em `docs/` | Documentação clara |

---

### Checklist de Entrega — Sprint 3

- [ ] `tenantId` em todos os documentos relevantes
- [ ] `TenantContext` propagado via JWT → Filter → ThreadLocal
- [ ] **Endpoint de troca de academia (`POST /auth/switch-academia`)**
- [ ] **Validação: usuário só pode selecionar academia que pertence a ele**
- [ ] Queries automaticamente filtradas por tenant (academia ativa)
- [ ] `@DocumentListener` auto-preenche `tenantId`
- [ ] Testes de isolamento passando (2 tenants, sem vazamento)
- [ ] Testes de adversário passando
- [ ] Endpoints de cadastro e avaliação isolados por tenant
- [ ] **Teste: usuário com 3 academias troca entre elas e vê apenas dados da selecionada**
- [ ] Documentação do modelo de segurança

---

## Resumo Visual do Roadmap

```
SPRINT 1                    SPRINT 2                    SPRINT 3
═════════                   ═════════                   ═════════
                            
Fase 1.1 ──► Bugs Críticos  
    │                        
Fase 1.2 ──► CRUDs         Fase 2.1 ──► Fundamentos    
    │                        │                           
Fase 1.3 ──► Avaliações    Fase 2.2 ──► Spring Security
    │                        │                           
Fase 1.4 ──► Limpeza       Fase 2.3 ──► JWT + Multi-Academia
    │                        │
Fase 1.5 ──► Multi-Academia Fase 2.4 ──► Filtros        Fase 3.1 ──► Fundamentos
  (modelo de dados)         │                           │
                            Fase 2.5 ──► Testes         Fase 3.2 ──► Modelo + Troca
                                            │           │
                                            │           Fase 3.3 ──► Tenant Context
                                            │           │
                                            │           Fase 3.4 ──► Queries Isoladas
                                            │           │
                                            │           Fase 3.5 ──► Endpoints
                                            │           │
                                            │           Fase 3.6 ──► Segurança
                                            │
                                         ✅ PRONTO     ✅ PRONTO
```

---

## Fluxo: Usuário com Múltiplas Academias

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    CENÁRIO: Usuário com 3 academias                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. LOGIN                                                               │
│     POST /auth/login                                                    │
│     { email: "joao@email.com", senha: "123", academiaId: "acad_1" }    │
│                                                                         │
│     Response: {                                                         │
│       "token": "eyJ...",                                                │
│       "academias": [                                                    │
│         { "id": "acad_1", "nome": "Academia Central" },                │
│         { "id": "acad_2", "nome": "Academia Norte" },                  │
│         { "id": "acad_3", "nome": "Academia Sul" }                     │
│       ],                                                                │
│       "academiaAtiva": "acad_1"                                        │
│     }                                                                   │
│                                                                         │
│  2. JWT CONTEM                                                           │
│     {                                                                   │
│       "sub": "user_123",                                                │
│       "academias": ["acad_1", "acad_2", "acad_3"],                     │
│       "activeAcademia": "acad_1",                                      │
│       "exp": 1720000000                                                 │
│     }                                                                   │
│                                                                         │
│  3. TROCA DE ACADEMIA                                                   │
│     POST /auth/switch-academia                                          │
│     Authorization: Bearer eyJ...                                        │
│     { "academiaId": "acad_2" }                                          │
│                                                                         │
│     Validação:                                                          │
│     ✓ academiaId está na lista do user                                  │
│     ✓ Gera novo token com activeAcademia: "acad_2"                      │
│                                                                         │
│     Response: { "token": "eyJ...(novo)...", "academiaAtiva": "acad_2" }│
│                                                                         │
│  4. QUERIES FILTRAM POR ACADEMIA ATIVA                                  │
│     GET /api/v1/clientes                                                │
│     → Retorna apenas clientes da academia "acad_2"                      │
│                                                                         │
│  5. ISOLAMENTO                                                          │
│     Usuário NÃO pode acessar dados da academia "acad_3"                │
│     sem trocar para ela antes                                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Detalhamento das Fases de Multi-Academia

### Decisões de Design

| Decisão | Opção A | Opção B | **Recomendada** | Motivo |
|---------|---------|---------|-----------------|--------|
| **Relação User↔Academia** | `User.academiaIds: List<String>` | Collection `user_academias` | **A (embed)** | Menos queries, academia é "propriedade" do user |
| **Academia ativa no JWT** | Claim `activeAcademiaId` | Header `X-Academia-Id` | **JWT claim** | Stateless, não precisa de estado no servidor |
| **Validação de acesso** | App-level (service) | @PreAuthorize | **Ambos** | Defense in depth |
| **Troca de academia** | Novo login | Endpoint dedicado | **Endpoint dedicado** | Melhor UX, não precisa re-autenticar |

### Entidades Afetadas

```
User
├── academiaIds: List<String>  ← NOVO CAMPO
│
Academia
├── (mantém userId para referência)
│
Cliente
├── responsavelId: String      ← JÁ EXISTE
├── responsavelType: ACADEMIA  ← JÁ EXISTE
├── tenantId: String           ← NOVO (Sprint 3)
│
AvaliacaoFisica
├── tenantId: String           ← NOVO (Sprint 3)
```

### Fluxo de Segurança

```
Request → JwtAuthenticationFilter
           │
           ├─ Decodifica JWT
           ├─ Extrai: sub, academias[], activeAcademia
           ├─ Valida: activeAcademia ∈ academias[]
           │
           └─ Coloca no SecurityContext:
              - userId
              - academiaIds
              - activeAcademia (tenant)
                    │
                    ▼
              Service/Repository
              │
              └─ Filtra por tenantId = activeAcademia
```

### Cenários de Teste

| # | Cenário | Esperado |
|---|---------|----------|
| 1 | User com 3 academias faz login com academia_1 | Token com activeAcademia=academia_1 |
| 2 | User troca para academia_2 | Novo token com activeAcademia=academia_2 |
| 3 | User tenta acessar dados da academia_3 sem trocar | **NEGADO** (403) |
| 4 | User tenta trocar para academia que NÃO é sua | **NEGADO** (403) |
| 5 | User com 1 academia tenta trocar | **ERRO** (precisa ter mais de 1) |
| 6 | Token expirado tenta trocar | **NEGADO** (401) |

---

## Notas Importantes

### Para o Sprint 1
- **Não adiar bugs críticos** — eles causam problemas cascata
- **Testes primeiro** — antes de fechar task, rodar `./mvnw clean verify`

### Para o Sprint 2
- **Estudar antes de implementar** — o objetivo é APRENDER, não só entregar
- **Documentar aprendizados** — criar notas pessoais sobre cada conceito
- **Não pular fundamentos** — entender HTTP, TLS, OWASP antes de JWT

### Para o Sprint 3
- **Não inventar** — usar padrões comprovados (Row-Level Security, Tenant Context)
- **MongoDB não tem RLS** — o isolamento é na aplicação, não no banco
- **Testar agressivamente** — multi-tenancy com bug é um vetor de ataque grave
- **Estudar PostgreSQL RLS** — mesmo usando MongoDB, entender como outros DBs resolvem isso ajuda

---

## Recomendações de Conteúdo para Segurança (Sprint 2)

Além dos links nas tasks acima, estes recursos são excelentes para aprendizado profundo:

### Cursos (vídeo)

| Recurso | Por que é bom | Link |
|---------|---------------|------|
| **Master Spring Security In One Shot 2026** (CodeSnippet) | Cobre arquitetura, Basic Auth, JWT, OAuth2, tudo em 4.5h com timestamps | [YouTube](https://www.youtube.com/watch?v=eYCOzPx3ht8) |
| **Spring Security 7 + OAuth2 + JWT** (Code Decode, Udemy) | Curso completo (13h) com Auth0, Keycloak, RBAC, microservices | [Udemy](https://www.udemy.com/course/code-decode-sprint-security-7/) |

### Tutoriais escritos

| Recurso | Por que é bom | Link |
|---------|---------------|------|
| **Spring Boot OAuth2 + JWT: End-to-End Zero-Trust API Security** | Guia prático para Spring Security 7 cobrindo JWT, RSA, roles, CSRF, CORS, method security | [Devops Monk Blog](https://blog.devops-monk.com/2026/05/spring-boot-oauth2-jwt-security/) |
| **Spring Boot 4 Authentication Tutorial** | Tutorial passo a passo com registro/login, BCrypt, JPA, Thymeleaf | [Qadr Labs](https://qadrlabs.com/post/spring-boot-4-authentication-tutorial-add-login-and-registration-with-spring-security-7-and-jpa) |
| **Spring Security Reference Docs (7.0)** | A fonte oficial — completa, precisa, sempre atualizada | [docs.spring.io](https://docs.spring.io/spring-security/reference/7.0/) |

### Ferramentas

| Ferramenta | Para que serve | Link |
|------------|----------------|------|
| **jwt.io** | Decodificar, verificar e depurar JWTs manualmente | [jwt.io](https://jwt.io) |
| **OWASP ZAP** | Scanner de vulnerabilidades para testar sua API | [ZAP](https://www.zaproxy.org/) |

---

*Planejamento criado: 2026-07-10*
*Versão: 1.1*