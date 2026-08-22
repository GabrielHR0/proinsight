# Proinsight — Documentação do Sistema

> Versão: 1.0.0 | Atualizado: 2026-07-30

---

## Sumário

1. [Visão Geral](#1-visão-geral)
2. [Arquitetura em Camadas](#2-arquitetura-em-camadas)
3. [Módulo 1 — Segurança e Autenticação](#3-módulo-1--segurança-e-autenticação)
4. [Módulo 2 — Academias](#4-módulo-2--academias)
5. [Módulo 3 — Clientes](#5-módulo-3--clientes)
6. [Módulo 4 — Avaliadores](#6-módulo-4--avaliadores)
7. [Módulo 5 — Avaliações Físicas](#7-módulo-5--avaliações-físicas)
8. [Módulo 6 — Tabelas de Classificação](#8-módulo-6--tabelas-de-classificação)
9. [Módulo 7 — Protocolos e Hub](#9-módulo-7--protocolos-e-hub)
10. [Módulo 8 — Infraestrutura](#10-módulo-8--infraestrutura)
11. [Referência de API](#11-referência-de-api)

---

## 1. Visão Geral

Proinsight é um sistema SaaS para gestão de avaliações físicas em academias. Ele permite que profissionais de educação física (avaliadores) cadastrem clientes, realizem avaliações (VO₂ máx, IMC), classifiquem resultados por protocolos científicos (Cooper, Rockport, AHA/FRIEND, Esteira Incremental) e gerenciem múltiplas academias.

**Stack:** Java 21 + Spring Boot 3.2.4 + MongoDB 6.0  
**Autenticação:** JWT HMAC-SHA (jjwt 0.12.5) + Refresh Tokens + Chaves de API (M2M)  
**Segurança:** BCrypt(12), sessões stateless, HSTS, CSP, CORS configurável  
**Testes:** JUnit 5 + Mockito + Spring Boot Test (166 testes: 125 unit + 41 IT)

### 1.1 Entidades Core

```
Academia ──┐
            ├── Avaliador (funcionário ou autônomo)
            │
User ───────┤
            └── Cliente (avaliado)
                  │
                  └── AvaliacaoFisica
                        ├── MedicaoVo2Max
                        │     └── TesteVo2Max (Cooper | Rockport | EsteiraIncremental)
                        └── MedicaoImc
                              └── TesteImc
```

### 1.2 Modelo de Permissões

```
Role (ex: "admin")
  └── Set<Permissao>

User
  └── Map<AcademiaId, Set<RoleId>>  →  permissões específicas por academia
```

O usuário tem um mapa `academiaRoles` onde a chave é o ID da academia e o valor é um conjunto de IDs de roles. Cada role contém um conjunto de permissões (`Permissao`). O `CustomUserDetailsService` resolve esse mapa em tempo de login, carregando as roles do MongoDB e montando um `Map<AcademiaId, Set<Permissao>>`.

---

## 2. Arquitetura em Camadas

O projeto segue DDD simplificado com 5 camadas:

```
┌─────────────────────────────────────────────┐
│  api/ (Adaptadores de Entrada)              │
│  ├── controller/api/v1/*Controller.java     │
│  ├── dto/request/*Request.java              │
│  ├── dto/response/*Response.java            │
│  └── handler/GlobalExceptionHandler.java    │
├─────────────────────────────────────────────┤
│  service/ (Casos de Uso / Aplicação)        │
│  ├── *Service.java                          │
│  ├── handler/*Handler.java                  │
│  └── AuthorizationService.java              │
├─────────────────────────────────────────────┤
│  domain/ (Regras de Negócio / Modelo)       │
│  ├── model/ (POJOs, entidades)              │
│  ├── enums/                                 │
│  ├── strategy/ (Strategy Pattern)           │
│  └── DadosAvaliacao.java                    │
├─────────────────────────────────────────────┤
│  infrastructure/persistence/ (Persistência) │
│  ├── document/ (MongoDB @Document)          │
│  ├── repository/ (Spring Data)              │
│  ├── mapper/ (Document ↔ Domain)            │
│  └── adapter/                               │
├─────────────────────────────────────────────┤
│  config/ (Configuração Global)              │
│  │   SecurityConfig, JwtTokenProvider, etc. │
│  └── bootstrap/ (Initializers)              │
└─────────────────────────────────────────────┘
```

### Fluxo de dados típico

```
HTTP Request
  → ApiKeyAuthenticationFilter (api key, se presente)
  → LoginRateLimiterFilter (rate limit por IP — PROTECTED_PATHS: login/register/refresh/forgot-password/reset-password)
  → JwtAuthenticationFilter (Bearer JWT + re-resolução de permissões do banco com cache 60s)
  → TenantContextFilter (X-Academia-Id, fail-closed, roda DEPOIS do JWT)
  → AuthorizationFilter (@PreAuthorize)
  → Controller (@Valid @RequestBody)
  → AuditAspect (@Audited → AuditLogService)
  → Service (domínio / regras)
  → Repository (MongoDB)
```

---

## 3. Módulo 1 — Segurança e Autenticação

### 3.1 Visão Geral

O módulo de segurança implementa:

- **Autenticação JWT stateless** com suporte a refresh tokens
- **Auto-registro** de academias com criação automática de admin
- **Lockout** de conta após 5 tentativas inválidas (15 min)
- **Rate limiting** two-bucket (OWASP): por IP + por identidade (login: e-mail, refresh: hash do token)
- **Permissões dinâmicas** re-resolvidas do banco com cache de 60s (não mais congeladas no JWT)
- **Troca de senha** autenticada (`PUT /me/password`) com invalidação de refresh/denylist
- **Reset de senha** via token SHA-256 single-use com TTL 15min
- **Trilha de auditoria** via `@Audited` + `AuditAspect` (quem, quando, qual recurso)
- **Chaves de API** (M2M) para integrações máquina-a-máquina
- **RBAC** multi-tenant com permissões granulares por academia
- **Isolamento por tenant** via `X-Academia-Id` + `@PreAuthorize` + fail-closed
- **Security headers** (HSTS, CSP, X-Content-Type-Options, X-Frame-Options)
- **CORS** dinâmico via properties
- **Mensagens de erro genéricas** no GlobalExceptionHandler (detalhes só no log)

### 3.2 Arquivos do Módulo

```
config/
├── SecurityConfig.java           → Cadeia de filtros, policies
├── JwtTokenProvider.java         → Geração/validação JWT (HMAC-SHA)
├── JwtAuthenticationFilter.java  → Filtro JWT + re-resolução de permissões do banco
├── ApiKeyAuthenticationFilter.java → Filtro de chave de API (prefixo pk_)
├── LoginRateLimiterFilter.java   → Rate limiter por IP (Caffeine)
├── TenantContext.java            → ThreadLocal do tenant atual
├── TenantContextFilter.java      → Filtro que lê X-Academia-Id (fail-closed)
├── TenantContextTaskDecorator.java → Propagação para @Async
├── AuditAspect.java              → Aspecto AOP de auditoria (@Audited)
├── CorsProperties.java           → Record de CORS configurável
└── WebConfig.java                → CORS registry + path prefix

service/
├── AuthorizationService.java     → Bean "auth" para @PreAuthorize
├── CustomUserDetailsService.java → UserDetailsService com resolução de roles
├── LoginLockoutService.java      → Lockout 5 tentativas / 15 min
├── RateLimitService.java         → Rate limit two-bucket por IP e identidade (Caffeine)
├── UserPermissionService.java    → Permissões dinâmicas do banco com cache 60s
├── PasswordResetService.java     → Token SHA-256 single-use + reset de senha
├── AuditLogService.java          → Trilha de auditoria (best-effort)
├── RefreshTokenService.java      → Rotação e revogação de refresh tokens
└── RegistrationService.java      → Auto-cadastro de academia + admin

api/annotation/
└── Audited.java                  → Anotação de auditoria para endpoints

api/handler/
├── GlobalExceptionHandler.java   → Handler RFC 7807 com mensagens genéricas
└── RateLimitExceededException.java → Exceção de rate limit

infrastructure/persistence/document/
├── PasswordResetTokenDocument.java → Token de reset com TTL MongoDB
└── AuditLogDocument.java           → Log de auditoria

infrastructure/persistence/repository/
├── PasswordResetTokenRepository.java → Repositório de tokens de reset
└── AuditLogRepository.java           → Repositório de logs de auditoria

api/controller/api/v1/
├── AuthController.java           → /login, /register, /refresh, /logout, /me, /me/password, /forgot-password, /reset-password
└── UserController.java           → CRUD de usuários (admin)
```

### 3.3 Fluxo de Login

```
POST /api/v1/auth/login
  Content-Type: application/json
  { "login": "user@email.com", "password": "..." }

  1. LoginRateLimiterFilter  → verifica se IP não excedeu 5 req/min
  2. AuthController.login()  → verifica rate limit por identidade (e-mail, Caffeine 5/min)
  3. LoginLockoutService.checkLockout() → verifica se conta não está lockada
  4. authenticationManager.authenticate()
     → CustomUserDetailsService.loadUserByUsername()
       → busca por email ou userName no MongoDB
       → resolve todas as roles do usuário
       → monta Map<AcademiaId, Set<Permissao>>
       → retorna CustomUserDetails
  5. Se sucesso:
     - LoginLockoutService.resetAttempts()
     - RateLimitService.recordSuccess(identityKey)  → decrementa contador
     - Gera JWT com claims: jti, iss, aud, sub, userId, userName,
       academiaIds, academiaPermissoes
     - Gera RefreshToken (UUID, 7 dias)
     - Retorna LoginResponse { token, refreshToken, tokenType, expiresIn,
       userId, userName, email, academiaPermissoes }
  6. Se falha:
     - LoginLockoutService.recordFailedAttempt()
     - RateLimitService.recordFailure(identityKey)
     - Se >= 5 falhas: lockedUntil = now + 15 min
     - Retorna 401
```

#### Estrutura do JWT

```json
{
  "jti": "uuid-v4",
  "iss": "proinsight-api",
  "aud": "proinsight-app",
  "sub": "user@email.com",
  "userId": "mongo-id",
  "userName": "joao",
  "academiaIds": ["academia-id-1"],
  "academiaPermissoes": {
    "academia-id-1": ["CLIENTES_CRIAR", "AVALIACOES_LER", "..."]
  },
  "iat": 1700000000,
  "exp": 1700086400
}
```

### 3.4 Fluxo de Auto-registro

```
POST /api/v1/auth/register
  { "email": "...", "password": "...", "userName": "...",
    "academiaNome": "...", "cnpj": "..." }

  1. Valida duplicidade de email e userName
  2. Cria Role "admin" com TODAS as Permissao.values()
  3. Hash da senha (BCrypt 12)
  4. Cria UserDocument com academiaRoles = { "pending": [roleId] }
  5. Cria AcademiaDocument vinculada ao ownerId
  6. Atualiza UserDocument:
     - academiaRoles["academia-id"] = [roleId]
     - addAcademiaId(academia-id)
     - remove "pending"
  7. Gera JWT + Refresh Token
  8. Retorna LoginResponse (auto-login)
```

### 3.5 Fluxo de Refresh Token

```
POST /api/v1/auth/refresh
  { "refreshToken": "uuid" }

  1. RefreshTokenService.validateAndRevoke():
     - Busca por id + revoked=false
     - Se expirado: marca como revoked=true, lança erro
     - Se válido: marca como revoked=true (rotação)
  2. Carrega UserDocument pelo userId
  3. Gera NOVO JWT + NOVO Refresh Token
  4. Retorna LoginResponse
```

### 3.6 Cadeia de Filtros (SecurityConfig)

A ordem dos filtros é crítica:

```
1. TenantContextFilter            → Lê X-Academia-Id, seta TenantContext
   (antes do DisableEncodeUrlFilter — o primeiro da cadeia)
2. DisableEncodeUrlFilter         → Desabilita encoding de URL da sessão
3. WebAsyncManagerIntegrationFilter
4. SecurityContextHolderFilter     → Mantém o SecurityContext
5. HeaderWriterFilter             → Escreve headers de segurança
6. CorsFilter                     → CORS (lê origens de app.cors.allowed-origins)
7. LogoutFilter                   → Logout padrão (não usado — temos /auth/logout)
8. ApiKeyAuthenticationFilter     → Chaves de API (prefixo "Bearer pk_")
9. LoginRateLimiterFilter         → Rate limit: 5 req/min/IP no /login
10. JwtAuthenticationFilter       → Bearer JWT → parse + validação
11. RequestCacheAwareFilter
12. SecurityContextHolderAwareRequestFilter
13. AnonymousAuthenticationFilter → Anônimo se nenhum dos acima autenticou
14. SessionManagementFilter       → Stateless (não cria sessão)
15. ExceptionTranslationFilter    → Trata AccessDeniedException
16. AuthorizationFilter           → @PreAuthorize, matchers
```

### 3.7 Matchers de Autorização (HTTP)

`SecurityConfig.securityFilterChain()`:

```java
.requestMatchers("/api/v1/auth/login").permitAll()
.requestMatchers("/api/v1/auth/register").permitAll()
.requestMatchers("/api/v1/auth/refresh").permitAll()
.requestMatchers("/actuator/health").permitAll()
.requestMatchers("/actuator/info").permitAll()
.requestMatchers("/actuator/**").authenticated()
.anyRequest().authenticated()
```

### 3.8 Autorização Fina com @PreAuthorize

O `AuthorizationService` é registrado como bean `"auth"` e usado em expressões SpEL:

```java
@Component("auth")
public class AuthorizationService {

    // Verifica se o usuário tem permissão na academia específica
    hasAcademiaAccess(String academiaId)

    // Verifica se o usuário tem acesso a TODAS as academias da lista
    hasAnyAcademiaAccess(Collection<String> academiaIds)

    // Verifica se o userId do token corresponde ao userId do parâmetro
    isCurrentUser(String userId)

    // Verifica se é SUPER_ADMIN
    isSuperAdmin()

    // Valida que o academiaId do corpo corresponde ao X-Academia-Id do header
    validateCurrentTenant(String academiaId)
}
```

**Exemplos de uso nos controllers:**

```java
// ClienteController
@PreAuthorize("hasAuthority('CLIENTES_CRIAR') and @auth.hasAcademiaAccess(#request.academiaId)")

// AvaliacaoController
@PreAuthorize("hasAuthority('AVALIACOES_LER') and @auth.isCurrentUser(#userId)")

// ProtocoloHubController
@PreAuthorize("hasAuthority('PROTOCOLOS_LER') and @auth.isCurrentUser(#userId)")

// UserController
@PreAuthorize("@auth.hasAnyAcademiaAccess(#request.getAcademiaRoles().keySet())")
```

### 3.9 Isolamento Multi-Tenant

O tenant (academia) é propagado por toda a requisição via `TenantContext`:

```
Request com header "X-Academia-Id: abc123"
  → TenantContextFilter: setAcademiaId("abc123")
    → ApiKeyAuthenticationFilter: seta TenantContext da chave
    → JwtAuthenticationFilter: filtra permissões pela academia
      → Controller/Service: lê TenantContext.getAcademiaId()
        → Repository: usa academiaId para filtrar dados
  → finally: TenantContext.clear()
```

O `TenantContext` usa `InheritableThreadLocal` para propagar para threads filhas (ex: `@Async`).

### 3.10 Chaves de API (M2M)

Para integrações máquina-a-máquina sem usuário:

```
Request: "Authorization: Bearer pk_<raw-key>"

1. ApiKeyAuthenticationFilter detecta prefixo "Bearer pk_"
2. Faz SHA-256 da chave crua
3. Busca no MongoDB: ApiKeyRepository.findByKeyHashAndActiveTrue(hash)
4. Verifica se active=true e expiresAt > now
5. Seta TenantContext com academiaId da chave
6. Cria UsernamePasswordAuthenticationToken com as permissões da chave
7. Após chain: TenantContext.clear() + SecurityContextHolder.clearContext()
```

### 3.11 Lockout de Conta

```
UserDocument {
  failedLoginAttempts: 0..5,
  lockedUntil: Instant | null
}

LoginLockoutService:
  checkLockout(email)
    → se lockedUntil > now → throw LockedException (429)
  recordFailedAttempt(email)
    → incrementa failedLoginAttempts
    → se >= 5 → lockedUntil = now + 15 min
  resetAttempts(email)
    → failedLoginAttempts = 0, lockedUntil = null
```

### 3.12 Rate Limiting (Login)

```
LoginRateLimiterFilter (Caffeine cache, 1 min window):

  Por IP (X-Forwarded-For ou RemoteAddr):
    ≤ 5 tentativas/min → passa
    > 5 tentativas/min → 429 Too Many Requests

  Só se aplica à rota: /api/v1/auth/login
```

### 3.13 Security Headers

| Header | Valor | Origem |
|---|---|---|
| Strict-Transport-Security | `max-age=31536000; includeSubDomains` | SecurityConfig |
| X-Content-Type-Options | `nosniff` | SecurityConfig |
| X-Frame-Options | `DENY` | SecurityConfig |
| Content-Security-Policy | `default-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'` | SecurityConfig |

### 3.14 CORS

Configurado via properties, lido em runtime por `WebConfig`:

```properties
app.cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

### 3.15 Propriedades JWT

```properties
jwt.secret=Base64 com 256+ bits (HMAC-SHA)
jwt.expiration=86400000       (24 horas)
jwt.refresh-expiration=604800000  (7 dias)
jwt.issuer=proinsight-api
jwt.audience=proinsight-app
```

### 3.16 Validação de JWT

`JwtTokenProvider.validateToken()` verifica:

1. Assinatura HMAC-SHA (via `Jwts.parser().verifyWith(secretKey)`)
2. `iss` igual a `jwt.issuer` (via `requireIssuer`)
3. `aud` igual a `jwt.audience` (via `requireAudience`)
4. `jti` presente e não-branco (via `claims.getId()`)
5. Expiração (automático pelo parser)

### 3.17 Endpoints de Autenticação

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Público | Login com email/senha |
| POST | `/api/v1/auth/register` | Público | Auto-cadastro de academia |
| POST | `/api/v1/auth/refresh` | Público | Renovar token expirado |
| POST | `/api/v1/auth/logout` | `isAuthenticated()` | Revoga todos refresh tokens |
| GET | `/api/v1/auth/me` | `isAuthenticated()` | Dados do usuário logado |

---

## 4. Módulo 2 — Academias

### 4.1 Responsabilidade

Gerencia as academias (organizações) do sistema. Cada academia é o tenant no modelo multi-tenant.

### 4.2 Estrutura

```
domain/model/Academia.java
  - id, userId (owner), nomeFantasia, razaoSocial, cnpj, endereco, telefone
  - Guard clauses: nomeFantasia e cnpj não podem ser null/blank

infrastructure/persistence/document/AcademiaDocument.java
  - @Document(collection = "academias")
  - Inner class Endereco (rua, numero, cidade, estado, cep)
  - @Indexed: ownerId, cnpj (unique)

infrastructure/persistence/repository/AcademiaRepository.java
  - findByOwnerId(String)
  - findByOwnerIdIn(List<String>)

api/controller/api/v1/AcademiaController.java
  - POST /api/v1/academias → @PreAuthorize("hasAuthority('ACADEMIAS_CRIAR')")
  - GET /api/v1/academias → @PreAuthorize("hasAuthority('ACADEMIAS_LER')")
  - GET /api/v1/academias/{id} → @PreAuthorize("@auth.hasAcademiaAccess(#id)")
  - PUT /api/v1/academias/{id} → @PreAuthorize("@auth.hasAcademiaAccess(#id)")

service/AcademiaService.java
```

---

## 5. Módulo 3 — Clientes

### 5.1 Responsabilidade

Gerencia os clientes (avaliados) de uma academia ou avaliador autônomo.

### 5.2 Estrutura

```
domain/model/Cliente.java
  - fullName, email, phone, cpf, dataNascimento, sexo (MASCULINO/FEMININO)
  - endereco (Endereco), academiaId, avaliadorId, active
  - Guard clauses: fullName e email não podem ser null/blank

infrastructure/persistence/document/ClienteDocument.java
  - @Document(collection = "clientes")
  - @CompoundIndex(name = "academia_cliente_idx", def = "{'academiaId': 1, 'fullName': 1}")
  - @Indexed: academiaId, avaliadorId

api/controller/api/v1/ClienteController.java
  - CRUD completo + criarComImc (cria cliente + avaliação IMC em 1 request)
  - @PreAuthorize com hasAcademiaAccess nas rotas que recebem academiaId
```

### 5.3 Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/clientes` | Criar cliente |
| GET | `/api/v1/clientes` | Listar clientes (filtro por academia ou avaliador) |
| GET | `/api/v1/clientes/{id}` | Buscar cliente |
| PUT | `/api/v1/clientes/{id}` | Atualizar cliente |
| DELETE | `/api/v1/clientes/{id}` | Excluir cliente |
| POST | `/api/v1/clientes/imc` | Criar cliente + avaliação IMC |

---

## 6. Módulo 4 — Avaliadores

### 6.1 Responsabilidade

Gerencia os profissionais de educação física. Podem ser autônomos (`academiaId == null`) ou vinculados a uma academia.

### 6.2 Estrutura

```
domain/model/Avaliador.java
  - cref, firstName, lastName, email, telefone, cpf, userId, academiaId
  - isAutonomo(): academiaId == null

infrastructure/persistence/document/AvaliadorDocument.java
  - @Document(collection = "avaliadores")
  - @Indexed: userId, cpf (unique)

api/controller/api/v1/AvaliadorController.java
  - CRUD completo
  - @PreAuthorize nos métodos sensíveis
```

---

## 7. Módulo 5 — Avaliações Físicas

### 7.1 Responsabilidade

- Core do sistema. Processa avaliações físicas usando **Strategy Pattern**.
- Suporta múltiplos protocolos (Cooper, Rockport, AHA/FRIEND, Esteira Incremental, IMC).
- Cada avaliação pode conter múltiplas medições (VO₂ máx e/ou IMC).

### 7.2 Estrutura

```
domain/
├── model/
│   ├── AvaliacaoFisica.java         → clienteId, avaliadorId, protocoloId, List<Medicao>
│   ├── Medicao.java (abstract)      → tipo, medidoEm, List<Teste>
│   ├── MedicaoVo2Max.java           → extends Medicao<TesteVo2Max>
│   ├── MedicaoImc.java              → extends Medicao<TesteImc>
│   ├── teste/Teste.java (interface) → getResultado(), getCriterio()
│   ├── teste/TesteVo2Max.java (abstract)
│   │   ├── TesteVo2MaxCooper.java
│   │   ├── TesteVo2MaxRockport.java
│   │   └── TesteVo2MaxEsteiraIncremental.java
│   └── teste/TesteImc.java
├── strategy/
│   ├── AvaliacaoStrategy.java (interface) → avaliar(Contexto) → Leaf
│   ├── StrategyRegistry.java              → resolve por @StrategyFor("protocolo")
│   ├── AvaliacaoVo2MaxContext.java        → contexto com protocolo, testes, idade, sexo
│   ├── AvaliacaoImcContext.java           → contexto com peso, altura
│   ├── AvaliacaoVo2Max.java              → strategy VO₂ máx
│   ├── AvaliacaoVo2MaxAdaptado.java      → strategy VO₂ máx adaptado
│   ├── AvaliacaoVo2MaxEsteiraIncremental.java → strategy esteira
│   └── AvaliacaoImc.java                  → strategy IMC
└── DadosAvaliacao.java                    → Map<String, Object> permissivo
```

### 7.3 Fluxo de Avaliação

```
1. POST /avaliacoes/vo2max
   Request: { clienteId, avaliadorId, protocoloId, testes: [...] }

2. AvaliacaoService.salvarAvaliacao()
   → valida cliente, avaliador, protocolo
   → cria AvaliacaoFisicaDocument
   → salva no MongoDB

3. AvaliacaoVo2MaxHandler.avaliar()
   → StrategyRegistry.resolve(protocoloId)
   → monta AvaliacaoVo2MaxContext (idade, sexo, resultados)
   → strategy.avaliar(contexto)
   → retorna NivelVo2Max (Leaf do Composite)

4. Converte para AvaliacaoVo2MaxResponse
   → classificação, resultado, detalhes do teste
```

### 7.4 Strategy Pattern

```java
@StrategyFor("protocolo_vo2max_cooper")
public class AvaliacaoVo2Max implements AvaliacaoStrategy<AvaliacaoVo2MaxContext> {
    Leaf avaliar(AvaliacaoVo2MaxContext contexto);
}

// Registry resolve por chave string (protocoloId)
StrategyRegistry.resolve("protocolo_vo2max_cooper") → AvaliacaoVo2Max
```

---

## 8. Módulo 6 — Tabelas de Classificação

### 8.1 Responsabilidade

Armazena as tabelas de referência para classificar resultados de avaliações. Usa **Composite Pattern** para representar hierarquias:

```
Tabela (Composite)
├── TabelaSexo (MASCULINO | FEMININO)
│   ├── TabelaIdade (18-25, 26-30, ...)
│   │   ├── TabelaEquipamento (esteira, bicicleta)
│   │   │   └── NivelVo2Max (EXCELENTE, BOM, REGULAR, FRACO)
│   │   └── TabelaClassificacaoGenerica
│   │       └── NivelImc (NORMAL, SOBREPESO, OBESIDADE)
```

### 8.2 Estrutura

```
domain/model/composite/
├── Component.java (interface)        → getNome(), getTipo()
├── Composite.java (abstract)         → filhos, adicionar(), remover()
├── Leaf.java (abstract)              → valor, tipoLimite, comparator
├── classes/
│   ├── NivelVo2Max.java              → classificação, vo2max, pontos
│   ├── NivelImc.java                 → classificação, imc
│   └── NivelForca.java               → classificação, forca
└── tabelas/
    ├── TabelaVo2Max.java             → raiz VO₂ máx
    ├── TabelaSexo.java               → filtra por sexo
    ├── TabelaIdade.java              → filtra por faixa etária
    ├── TabelaEquipamento.java        → filtra por equipamento
    └── TabelaClassificacaoGenerica.java → leaf genérica

bootstrap/TabelaClassificacaoInitializer.java
  → popula 5 tabelas no startup:
    1. Cooper 12min
    2. Rockport 1 mile
    3. AHA/FRIEND
    4. Esteira Incremental
    5. IMC - OMS
```

### 8.3 Persistência Polimórfica

O pacote `infrastructure/persistence/document/composite/` contém os conversores customizados (`PersistedComponentReadConverter`, `PersistedComponentWriteConverter`) que serializam/deserializam a hierarquia `Component` para MongoDB usando um campo `type`.

---

## 9. Módulo 7 — Protocolos e Hub

### 9.1 Responsabilidade

Gerencia os protocolos de avaliação disponíveis e oferece um hub de consulta rápida por usuário.

### 9.2 Estrutura

```
domain/model/ProtocoloAvaliacao.java   → id, nome, versao, tipo, descricao
domain/enums/Protocolo.java            → COOPER, ROCKPORT, ESTEIRA, ESTEIRA_INCREMENTAL

service/ProtocoloHubService.java       → hub, favoritos, listagem

api/dto/response/
├── ProtocoloResumoResponse.java       → id, nome
├── ProtocoloDetalheResponse.java      → id, nome, versao, descricao, requisitos

api/controller/api/v1/ProtocoloHubController.java
  → @Validated na classe
  → GET /avaliacoes/hub          → hub completo do usuário
  → GET /avaliacoes/protocolos   → listar todos protocolos
  → GET /avaliacoes/protocolos/{id} → detalhe do protocolo
  → POST /avaliacoes/favoritos   → favoritar protocolo
  → DELETE /avaliacoes/favoritos → desfavoritar
  → GET /avaliacoes/favoritos    → listar favoritos
  → GET /avaliacoes/favoritos/verificar → verificar se é favorito

bootstrap/ProtocoloAvaliacaoInitializer.java
  → popula 5 protocolos no startup
```

---

## 10. Módulo 8 — Infraestrutura

### 10.1 MongoDB

- **Banco local:** `proinsight_dev` (via Docker Compose)
- **Banco de teste:** `proinsight_test` (via Docker Compose)
- **MongoDB URI local:** `mongodb://root:example@localhost:27017/proinsight_dev?authSource=admin`

### 10.2 Coleções

| Coleção | Documento Principal |
|---|---|
| `users` | UserDocument |
| `academias` | AcademiaDocument |
| `clientes` | ClienteDocument |
| `avaliadores` | AvaliadorDocument |
| `avaliacoes_fisicas` | AvaliacaoFisicaDocument |
| `tabelas_classificacao` | TabelaClassificacaoDocument |
| `protocolos_avaliacao` | ProtocoloAvaliacaoDocument |
| `roles` | RoleDocument |
| `refresh_tokens` | RefreshTokenDocument |
| `api_keys` | ApiKeyDocument |
| `favoritos_protocolos` | UsuarioProtocoloFavoritoDocument |

### 10.3 Mappers

Os mappers (`infrastructure/persistence/mapper/`) convertem entre documentos MongoDB e modelos de domínio:

| Mapper | Origem → Destino |
|---|---|
| UserMapper | UserDocument ↔ User |
| ClienteMapper | ClienteDocument ↔ Cliente |
| AcademiaMapper | AcademiaDocument ↔ Academia |
| AvaliadorMapper | AvaliadorDocument ↔ Avaliador |
| AvaliacaoFisicaMapper | AvaliacaoFisicaDocument ↔ AvaliacaoFisica |
| TabelaClassificacaoMapper | TabelaClassificacaoDocument ↔ TabelaClassificacao |
| ProtocoloAvaliacaoMapper | ProtocoloAvaliacaoDocument ↔ ProtocoloAvaliacao |

### 10.4 Configuração por Ambiente

| Arquivo | Uso |
|---|---|
| `application.properties` | Padrão: define `jwt.issuer`, `jwt.audience`, CORS |
| `application-local.properties` | Local dev: MongoDB, JWT secret, default-user |
| `application-test.properties` | Testes: MongoDB test, JWT secret fixo |

### 10.5 Profiles

| Profile | Ativação | Uso |
|---|---|---|
| `local` | Padrão (`spring.profiles.default=local`) | Desenvolvimento local |
| `test` | Spring test | Testes de integração |

### 10.6 Actuator

Endpoints expostos: `health`, `info`, `metrics`, `prometheus`

- `/actuator/health` e `/actuator/info` → público
- `/actuator/**` → autenticado

### 10.7 Testes

| Tipo | Sufixo | Framework | Quantidade |
|---|---|---|---|
| Unitário | `*Test.java` | JUnit 5 + Mockito | 125 |
| Integração | `*IT.java` | Spring Boot Test (RANDOM_PORT) | 41 |
| **Total** | | | **166** |

Os testes de integração estendem `AbstractIntegrationTest` e usam MongoDB real via Docker.

---

## 11. Referência de API

### 11.1 Prefixo

Todas as rotas são prefixadas com `/api/v1` automaticamente via `WebConfig.configurePathMatch()`.

### 11.2 Headers Comuns

| Header | Obrigatório | Descrição |
|---|---|---|
| `Authorization: Bearer <jwt>` | Sim (rotas protegidas) | Token JWT |
| `Authorization: Bearer pk_<key>` | Alternativo | Chave de API |
| `X-Academia-Id` | Sim (rotas multi-tenant) | ID da academia |
| `Content-Type: application/json` | Sim (POST/PUT) | Tipo do corpo |

### 11.3 Respostas de Erro (RFC 7807)

```json
{
  "type": "proinsight://problems/validation-error",
  "status": 400,
  "detail": "Campo obrigatório: email"
}
```

### 11.4 Mapa de Endpoints

```
POST   /auth/login                    → LoginResponse
POST   /auth/register                 → LoginResponse
POST   /auth/refresh                  → LoginResponse
POST   /auth/logout                   → 204
GET    /auth/me                       → MeResponse

GET    /users                         → List<UserResponse>
POST   /users                         → UserResponse
GET    /users/{id}                    → UserResponse

GET    /academias                     → List<AcademiaResponse>
POST   /academias                     → AcademiaResponse
GET    /academias/{id}                → AcademiaResponse
PUT    /academias/{id}                → AcademiaResponse

GET    /clientes                      → List<ClienteResponse>
POST   /clientes                      → ClienteResponse
GET    /clientes/{id}                 → ClienteResponse
PUT    /clientes/{id}                 → ClienteResponse
DELETE /clientes/{id}                 → 204
POST   /clientes/imc                  → ClienteComImcResponse

GET    /avaliadores                   → List<AvaliadorResponse>
POST   /avaliadores                   → AvaliadorResponse
GET    /avaliadores/{id}              → AvaliadorResponse
PUT    /avaliadores/{id}              → AvaliadorResponse
DELETE /avaliadores/{id}              → 204

GET    /avaliacoes                    → List<AvaliacaoListaResponse>
POST   /avaliacoes/vo2max             → AvaliacaoVo2MaxResponse
POST   /avaliacoes/imc                → AvaliacaoImcResponse
GET    /avaliacoes/{id}               → AvaliacaoVo2MaxResponse
GET    /avaliacoes/dados-pre          → DadosPreAvaliacaoResponse

GET    /avaliacoes/hub                → Map<String, Object>
GET    /avaliacoes/protocolos         → List<ProtocoloResumoResponse>
GET    /avaliacoes/protocolos/{id}    → ProtocoloDetalheResponse
POST   /avaliacoes/favoritos          → 200
DELETE /avaliacoes/favoritos          → 204
GET    /avaliacoes/favoritos          → List<ProtocoloResumoResponse>
GET    /avaliacoes/favoritos/verificar→ Map<String, Boolean>

GET    /tabelas-classificacao         → List<TabelaClassificacaoResponse>
```
