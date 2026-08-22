# Auditoria de Segurança — Proinsight Backend

> Documento vivo: cada problema abaixo deve ser marcado como `[RESOLVIDO]` após a correção
> ser aplicada e os testes do plano forem executados com sucesso.
>
> Última atualização: 2026-08-01

---

## 1. Visão geral

A auditoria cobriu a stack de autenticação, autorização, isolamento multi-tenant e
exposição da API. Foram encontrados **20 problemas** (4 críticos, 9 altos, 7 médios).
Este documento descreve cada um com:

1. **Problema** — o que acontece hoje e por que é um risco.
2. **Severidade** — CRÍTICO / ALTO / MÉDIO.
3. **Localização** — arquivo e linha (a linha pode ter mudado após correções anteriores).
4. **Solução** — a mudança de código recomendada.
5. **Plano de testes** — teste manual + teste unitário + teste de integração.

### Convenções do plano de testes

- **Manual**: curl ou browser. Sempre com 2 academias (`A` e `B`) e 2 usuários (`uA` com
  acesso só a A, `uB` com acesso só a B).
- **Unit**: JUnit 5 + Mockito, sufixo `*Test.java`, sem Spring context.
- **IT**: JUnit 5 + Spring Boot Test + MongoDB real, sufixo `*IT.java` estendendo
  `AbstractIntegrationTest`. MongoDB precisa estar rodando (`docker compose ps`).
- Comando de verificação final: `./mvnw clean verify`.

### Status da auditoria (2026-08-01)

| Item | Status |
|---|---|
| C1, C2, C3, C4 | [RESOLVIDO] — fail-closed completo; verificado por `TenantIsolationIT`, `TenantContextFilterTest`, `AutorizacaoFluxosIT` |
| A1, A3, A4, A5, A6, A8, A9 | [RESOLVIDO] — verificado por `AuthIntegrationIT`, `AuthorizationServiceTest`, `AcademiaControllerTest`, `ApiKeyAuthenticationFilterTest`/`ApiKeyIntegrationIT`, `JwtTokenProviderTest` |
| A2, A7 | [RESOLVIDO] — rate limit em /login+/register+/refresh/forgot-password+/reset-password; two-bucket por identidade (login: e-mail; refresh: hash do token); decremento em 2xx; XFF só com flag `security.trust-proxy-headers` |
| M1, M2, M3, M6 | [RESOLVIDO] — verificado por `SecurityHeadersIT` |
| M4 | [RESOLVIDO] — permissões dinâmicas com cache de 60s via `UserPermissionService`; troca de senha (`PUT /me/password` com senha atual + invalidação de refresh/denylist); reset via `POST /forgot-password` (token SHA-256 single-use TTL 15min) + `POST /reset-password` |
| M5 | [RESOLVIDO] — `AuditLogDocument` + `AuditLogService` + `@Audited` + `AuditAspect`; aplicado a endpoints mutáveis (clientes, avaliadores, users, academias, auth password/reset) |
| M7 | [RESOLVIDO] — `GlobalExceptionHandler` retorna mensagens genéricas para `IllegalArgumentException`, `NoSuchElementException`, `IllegalStateException`, `MethodArgumentTypeMismatch` e `RateLimitExceededException`; detalhes apenas no log com traceId |

Execução de verificação: `./mvnw clean verify` → **BUILD SUCCESS** (154 unitários + 58 ITs).
Nota: o failsafe passou a usar `forkCount=1` (era `1C`), porque os ITs compartilham o
mesmo MongoDB e a execução em forks paralelos causava corridas de dados entre classes
(`deleteAll` de uma classe corrompia o seed de outra).

---

## 2. Problemas CRÍTICOS

### [RESOLVIDO] C1. Tenant header sem validação de pertencimento — `TenantContextFilter`

- **Severidade**: CRÍTICO
- **Localização**: `config/TenantContextFilter.java:19-21`

**Problema**: o filtro aceita qualquer valor de `X-Academia-Id` e o coloca no
`TenantContext` sem verificar se o usuário autenticado tem acesso àquela academia. Um
usuário da academia A pode enviar `X-Academia-Id: <id-da-academia-B>` e, combinado com
C2/C3 (permissões unificadas + AOP fail-open), ler e alterar dados da academia B.

**Solução**:

```java
String academiaId = request.getHeader("X-Academia-Id");
if (academiaId != null && !academiaId.isBlank()) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()
            && auth.getPrincipal() instanceof CustomUserDetails userDetails
            && userDetails.getAcademiaPermissoes() != null
            && userDetails.getAcademiaPermissoes().containsKey(academiaId)) {
        TenantContext.setAcademiaId(academiaId);
    } else {
        // fail-closed: rejeita em vez de prosseguir com o header ignorado
        throw new AccessDeniedException("Acesso à academia " + academiaId + " não permitido");
    }
}
```

Detalhes de implementação:

- Se o header estiver **ausente**, prossegue sem tenant (comportamento atual).
- Se o header estiver **presente mas o usuário não tiver acesso**, rejeita com 403 —
  **fail-closed**. Retornar 403 em vez de silenciosamente ignorar o header evita que o
  cliente pense que o escopo foi aplicado.
- Para requisições autenticadas por **API key**, o `TenantContextFilter` roda antes do
  `ApiKeyAuthenticationFilter`, então a validação de pertencimento não se aplica; o
  `ApiKeyAuthenticationFilter` continua sendo a fonte autoritativa do tenant nesse fluxo
  (ele já seta o tenant a partir da API key persistida). Para evitar dupla configuração,
  o `TenantContextFilter` pode sobrescrever o tenant apenas se `X-Academia-Id` estiver
  presente **e** o usuário autenticado (JWT) tiver acesso; caso a autenticação ainda não
  tenha ocorrido (filtros anteriores não rodaram), não define tenant.

**Plano de testes**:

- Manual:
  - Login como uA → `GET /api/v1/academias/{idB}` com `X-Academia-Id: idB` → **403**.
  - Login como uA → `GET /api/v1/academias/{idA}` com `X-Academia-Id: idA` → **200**.
  - Login como uA → `GET /api/v1/clientes` **sem** `X-Academia-Id` → **200** (vazio ou
    lista, depende da política pós-C4).
- Unit (`TenantContextFilterTest`): mockar request/response/filterChain e
  `SecurityContextHolder`; 3 casos — header válido seta TenantContext; header inválido
  lança `AccessDeniedException` e não chama o chain; header ausente não seta tenant.
- IT (`TenantIsolationIT`): fluxo completo — usuário A acessa dados de B via header →
  espera 403.

---

### [RESOLVIDO] C2. Isolamento multi-tenant fail-open — `AcademiaScopingAspect`

- **Severidade**: CRÍTICO
- **Localização**: `config/AcademiaScopingAspect.java:62-63`

**Problema**: quando `TenantContext.getAcademiaId()` é `null` ou vazio, o aspect **retorna
sem adicionar o filtro** — ou seja, a consulta roda sem escopo. Qualquer endpoint que não
passe o header (ou que use `findAll()`/`findById()` não coberto pelo pointcut — ver C3)
retorna dados de **todas** as academias.

**Solução**:

1. Tornar o aspect **fail-closed** para classes `@ScopedByAcademia`: se o tenant estiver
   ausente, lançar exceção em vez de prosseguir sem filtro. Mas atenção: chamadas
   legítimas fora de contexto HTTP (initializers, jobs, testes) precisam de um
   mecanismo explícito — um método `TenantContext.bypassScope()` (flag ThreadLocal)
   usado pelos initializers e jobs, ou exigir que todo acesso fora de HTTP passe por
   serviço com tenant explícito.
2. Definir explicitamente a política: em requisição HTTP autenticada, **todo acesso a
   documento `@ScopedByAcademia` exige `X-Academia-Id` válido** (senão 403). Isso
   elimina consultas globais acidentais.
3. Recomenda-se também mudar a estratégia de ponto de corte: em vez de interceptar
   `MongoOperations`, escopar nos **services** (camada de aplicação), onde a política de
   negócio é clara e testável, e onde o ponto de corte não depende de nomes de métodos
   internos do Spring Data (ver C3).

**Plano de testes**:

- Unit (`AcademiaScopingAspectTest`):
  - tenant presente → query recebe `Criteria academiaId`.
  - tenant ausente em classe `@ScopedByAcademia` → exceção lançada (fail-closed).
  - tenant ausente em classe **sem** `@ScopedByAcademia` → passa direto.
  - query já contém `academiaId` → não duplica.
  - bypass explícito → passa sem filtro.
- IT (`TenantIsolationIT`): uA sem header acessando `GET /clientes` → 403 (ou vazio, de
  acordo com a política escolhida — registrar a decisão aqui).

---

### [RESOLVIDO] C3. Escopo não cobre `findAll/findById/save/delete` — pointcut incompleto

- **Severidade**: CRÍTICO
- **Localização**: `config/AcademiaScopingAspect.java:20-42`; `service/ClienteService.java:91-95`

**Problema**: o pointcut só intercepta `find`, `findOne`, `count` e `exists` de
`MongoOperations`. Os repositórios Spring Data derivam `findAll()`, `findById()`,
`save()` e `delete()` por outros caminhos de execução (`MongoTemplate` internamente
chama `find`, mas os derived query methods do Spring Data **não passam** por
`MongoOperations.find(Query, Class)` — passam por `MongoTemplate.find(Query, Class)` de
forma direta ou via execução de repositório), então o aspecto **não** filtra essas
operações. Resultado confirmado: `ClienteService.listAll()` (`clienteRepository.findAll()`)
faz **dump cross-tenant de PII** (nome, CPF, e-mail, telefone) para qualquer usuário
autenticado.

**Solução** (estratégia combinada):

1. **Escopo na camada de serviço** (recomendado como principal): cada serviço que acessa
   documento `@ScopedByAcademia` deve:
   - receber o tenant do `TenantContext` e usá-lo em **todas** as consultas
     (`findByAcademiaId` explícito, ou `findById` + verificação de `academiaId` no
     documento retornado);
   - lançar `AccessDeniedException` se o tenant estiver ausente para operações de lista;
   - verificar `academiaId` do documento **antes de retornar/alterar** em operações por
     id (`findById`, `update`, `delete`).
2. **Aspect de defesa em profundidade**: ampliar os pointcuts para
   `MongoTemplate.find(Query, Class)` e `MongoTemplate.findOne/findAll(Query, Class)`
   (o `MongoTemplate` também oferece `find(Query, Class)`), mantendo o fail-closed de C2.
   Atenção: `save` não recebe `Query`, então o escopo de save só pode ser validado
   comparando `academiaId` do documento com o tenant.
3. **Remover `listAll()`** ou convertê-lo em `listByTenant()` exigindo tenant. Atualizar
   o controller `ClienteController` (e os demais controllers v1/v2 que usam
   `findAll`) para exigir `X-Academia-Id`.

**Plano de testes**:

- Unit (`ClienteServiceTest`):
  - `listAll()` sem tenant → lança `AccessDeniedException`.
  - `listAll()` com tenant → chama `findByAcademiaId(tenant)` (nunca `findAll()`).
  - `findById(id)` de documento de outra academia → lança `AccessDeniedException`.
  - `update(id)` de documento de outra academia → lança antes de qualquer `save`.
  - `save` de documento com `academiaId` diferente do tenant → lança.
- IT (`TenantIsolationIT`): uA com tenant A; inserir cliente na academia B via
  `ClienteRepository`; `GET /clientes/{idB}` → 404/403 (nunca 200); `GET /clientes` →
  só clientes da academia A.

---

### [RESOLVIDO] C4. Permissões unificadas quando header ausente — `JwtTokenProvider`

- **Severidade**: CRÍTICO
- **Localização**: `config/JwtTokenProvider.java:103-116`

**Problema**: quando `X-Academia-Id` está ausente (ou o mapa não contém a academia),
o provider faz **união de todas as permissões de todas as academias** do token. Um
usuário com acesso a múltiplas academias recebe, sem header, a soma de todos os poderes
— e se o token contém `SUPER_ADMIN` em alguma academia, ele é efetivamente
super-admin global sem header.

**Solução**: remover o fallback de união. Se o header estiver ausente ou a academia não
estiver no mapa de permissões, `effectivePermissions = List.of()` — o usuário fica
autenticado porém **sem autoridades**; os `@PreAuthorize` por permissão falharão com
403. (A combinação com C1 garante que todo acesso efetivo exige header válido.)

```java
String academiaId = request.getHeader("X-Academia-Id");
List<String> effectivePermissions;
if (academiaId != null && academiaPermissoes != null
        && academiaPermissoes.containsKey(academiaId)) {
    effectivePermissions = academiaPermissoes.get(academiaId);
} else {
    effectivePermissions = List.of();   // fail-closed: sem união
}
```

**Plano de testes**:

- Unit (`JwtTokenProviderTest`): adicionar caso — token com permissões de 2 academias,
  sem header → authorities vazia; com header de academia X → só permissões de X; com
  header de academia inexistente → authorities vazia.
- IT (`AutorizacaoFluxosIT`): usuário com acesso a A e B; `GET` recurso protegido por
  permissão existente apenas na academia B, sem header → 403; com header B → 200.

---

## 3. Problemas ALTOS

### [RESOLVIDO] A1. Enumeração de usuários via `/register`

- **Severidade**: ALTO
- **Localização**: `service/RegistrationService.java:54-60`

**Problema**: `POST /register` responde com mensagens distintas para "e-mail já
cadastrado" e "nome de usuário já cadastrado" (e o 200 em sucesso inclui token). Isso
permite enumerar e-mails/usuários existentes.

**Solução**: devolver a **mesma resposta genérica** para e-mail/nome duplicado
("dados inválidos") com status 400 ou 409 indistinguível, e manter o mesmo texto para
sucesso e falha onde fizer sentido (por exemplo, em `login`). Considerar também
cronometragem constante para reduzir timing oracle.

**Plano de testes**:

- Unit (`RegistrationServiceTest`): registrar com e-mail existente e com nome existente
  → exceções/erros com mensagem idêntica.
- IT (`AuthIntegrationIT`): dois POSTs de registro duplicado → mesmo status e mesma
  mensagem de resposta.

---

### [RESOLVIDO] A2. Sem rate limit em `/register` e `/refresh`

- **Severidade**: ALTO
- **Localização**: `config/LoginRateLimiterFilter.java:46-49` (só cobre `/login`)

**Problema**: `LoginRateLimiterFilter.shouldNotFilter` só protege `/api/v1/auth/login`.
`/register` pode ser usado para inundação de criação de contas (spam/abuso de
recursos) e `/refresh` para força bruta de refresh tokens.

**Solução**:

1. Aplicar o rate limiter também a `/register` e `/refresh` (ajustar `shouldNotFilter`).
2. **Two-bucket** (OWASP): limitar por IP **e** por identidade (e-mail/username) no
   login; no refresh, por hash do token.
3. Em `/register`, aplicar **velocity limit** por IP (ex.: 5 contas/hora) e considerar
   verificação de e-mail/telefone (OWASP Bot Management) antes de emitir tokens.
4. Priorizar `X-Forwarded-For` apenas se houver confiança no proxy (ver A7).

**Plano de testes**:

- Unit (`LoginRateLimiterFilterTest`): estender para `/register` e `/refresh`.
- IT: N requisições a `/register` do mesmo IP → a N+1 retorna 429; idem `/refresh`.

---

### [RESOLVIDO] A3. `hasAnyAcademiaAccess` retorna `true` para lista vazia

- **Severidade**: ALTO
- **Localização**: `service/AuthorizationService.java:26`

**Problema**: `if (academiaIds == null || academiaIds.isEmpty()) return true;` — uma
requisição com lista vazia de academias passa a autorização sem nenhuma checagem real.
É um fail-open clássico.

**Solução**: retornar `false` para `null`/vazia. Chamadores que dependiam do
comportamento anterior devem validar antes (ex.: controller que lista academias de um
owner com 0 academias deve retornar lista vazia sem chamar esse método).

**Plano de testes**:

- Unit (`AuthorizationServiceTest`): `null` → false; vazia → false; lista com academia
  não acessível → false; lista acessível → true.
- IT (`AutorizacaoFluxosIT`): endpoint que usa `hasAnyAcademiaAccess` com lista vazia →
  403.

---

### [RESOLVIDO] A4. `POST /academias` aceita `ownerId` arbitrário

- **Severidade**: ALTO
- **Localização**: `api/controller/api/v1/AcademiaController.java:24-25`

**Problema**: a criação de academia exige apenas `hasAuthority('ACADEMIAS_CRIAR')`;
se o body aceitar `ownerId`, qualquer usuário com a permissão pode criar academia
vinculada a outro usuário (apropriação de contas).

**Solução**: o `ownerId` da academia criada deve ser sempre o usuário autenticado
(ou o `RegistrationService` no fluxo de auto-cadastro). Remover `ownerId` do DTO de
criação, ou ignorar/sobrescrever com o `userId` do `Authentication`.

**Plano de testes**:

- Unit (`AcademiaServiceTest`): create com ownerId do body ≠ autenticado → ignorado ou
  rejeitado.
- IT (`AutorizacaoFluxosIT`): uA cria academia com ownerId do uB → academia criada fica
  com ownerId = uA; `GET /academias/by-owner/{uB}` não retorna essa academia.

---

### [RESOLVIDO] A5. API keys sem brute-force protection / lockout

- **Severidade**: ALTO
- **Localização**: `config/ApiKeyAuthenticationFilter.java:51-55`

**Problema**: a validação de API key é pura consulta ao hash; um atacante com acesso à
lista de chaves (ou brute-force de chaves curtas) pode testar indefinidamente, sem
throttling. Uma chave `pk_...` sequencial/curta é adivinhavel em alto volume.

**Solução**:

1. Rate limit por IP para requisições autenticadas por API key (reutilizar o padrão do
   `LoginRateLimiterFilter`, com janela curta — ex.: 20 req/min/IP com prefixo `pk_`).
2. Após N falhas consecutivas com a mesma chave (ex.: 5), **desativar a chave** e
   registrar evento de auditoria.
3. Garantir entropia mínima de chaves na geração (>= 32 bytes aleatórios).

**Plano de testes**:

- Unit (`ApiKeyAuthenticationFilterTest`): N+1 tentativas inválidas → 429; chave
  desativada após lockout → 401 mesmo com chave correta.
- IT (`ApiKeyIntegrationIT`): fluxo com chave válida → 200; chave bloqueada → 401/403.

---

### [RESOLVIDO] A6. `jti` não persistido — sem revogação server-side

- **Severidade**: ALTO
- **Localização**: `config/JwtTokenProvider.java:66-67` (gera `jti` mas não persiste);
  `service/RefreshTokenService.java`

**Problema**: o token tem `jti` (já validado em `getAuthentication`), mas não existe
lista de revogação. Logout não invalida o access token (válido por 24h). OWASP
recomenda `jti` + denylist para revogação antecipada (e `iat`/`exp` para replay).

**Solução**:

1. Persistir `jti`, `exp`, `userId` em coleção `RevokedTokenDocument` (ou usar
   `expireAfterSeconds` do MongoDB TTL index para limpeza automática).
2. No `logout`, adicionar o `jti` do token atual na denylist (além de revogar o refresh
   token, que já é single-use).
3. Em `getAuthentication`, consultar a denylist antes de aceitar o token.

**Plano de testes**:

- Unit (`JwtTokenProviderTest`): token na denylist → `getAuthentication` lança.
- IT (`AuthIntegrationIT`): login → logout → reuso do access token no endpoint
  protegido → 401.

---

### [RESOLVIDO] A7. Rate limiter conta logins bem-sucedidos e confia em `X-Forwarded-For`

- **Severidade**: ALTO
- **Localização**: `config/LoginRateLimiterFilter.java:30-34,51-57`

**Problema**: (1) a contagem incrementa **antes** da autenticação e nunca decrementa em
sucesso, então um usuário legítimo que faz login várias vezes (ou falha e depois
sucede) é bloqueado; (2) `X-Forwarded-For` é lido sem validação — um cliente direto
pode spoofar o header e resetar o próprio contador.

**Solução**:

1. Incrementar apenas em **falha** (ex.: registrar no `AuthenticationFailureHandler`
   ou checar o status da resposta). Manter janela deslizante.
2. Confiar em `X-Forwarded-For` apenas quando a aplicação está atrás de proxy
   conhecido e confiável (propriedade `security.trusted-proxies` ou ler `request.getRemoteAddr()`).
3. Aplicar o **two-bucket**: por IP **e** por username/e-mail (OWASP).

**Plano de testes**:

- Unit (`LoginRateLimiterFilterTest`): 5 falhas → 6ª bloqueada; 5 falhas + 1 sucesso →
  contador resetado (ou não bloqueado o próximo login); spoof de XFF → não reseta
  contador.
- IT: fluxo de login com XFF forjado → contador mantém.

---

### [RESOLVIDO] A8. `DefaultUserInitializer` com credenciais hardcoded fora do profile `test`

- **Severidade**: ALTO
- **Localização**: `bootstrap/DefaultUserInitializer.java:20,52-76`

**Problema**: `@Profile("!test")` significa que **qualquer** ambiente (dev, staging,
prod) cria o super admin com senha vinda de properties versionadas. Se um ambiente
esquecer de configurar `default-user.*`, não cria (bom), mas se a property existir no
repo, há super admin com senha conhecida em produção.

**Solução**:

1. Restringir ao profile `local`: `@Profile("local")`.
2. Exigir que `default-user.*` venham de variáveis de ambiente (nunca de
   `application.properties` versionado); falhar (fail-fast) se ausentes no profile
   `local`? Não — apenas logar warning (manter o comportamento atual de skip).
3. Forçar troca de senha no primeiro login (requer feature de troca — ver M4).

**Plano de testes**:

- Manual: `SPRING_PROFILES_ACTIVE=local` com properties → usuário criado;
  `SPRING_PROFILES_ACTIVE=prod` → **não** criado.
- IT: subir contexto com profile ≠ local → `userRepository.findByEmail` vazio.

---

### [RESOLVIDO] A9. Segredos versionados no git (`.env` e `application-local.properties`)

- **Severidade**: ALTO
- **Localização**: `.env`, `src/main/resources/application-local.properties` (confirmado
  via `git ls-files`)

**Problema**: `jwt.secret`, senha do admin padrão e URI do MongoDB (com credenciais)
estão no histórico do repositório. Qualquer pessoa com acesso ao repo pode forjar JWTs
e acessar o banco.

**Solução**:

1. `git rm --cached .env src/main/resources/application-local.properties` (manter no
   disco para o dev local).
2. Adicionar `.gitignore` com `.env`, `*.properties.local`, `application-local.properties`.
3. **Rotacionar** o `jwt.secret` e a senha do admin e a senha do MongoDB (segredos
   comprometidos não podem ser "desversionados" — o histórico permanece).
4. Adicionar `application-local.properties.example` (sem segredos) para documentar as
   variáveis necessárias.
5. Futuramente: consumir segredos de variáveis de ambiente / secret manager.

**Plano de testes**:

- Manual: `git ls-files | grep -E "\.env|application-local"` → vazio.
- Manual: subir a app com `SPRING_PROFILES_ACTIVE=local` e as variáveis vindas do `.env`
  → login do admin funciona.

---

## 4. Problemas MÉDIOS

### [RESOLVIDO] M1. `/actuator/prometheus` e `/metrics` acessíveis a qualquer autenticado

- **Severidade**: MÉDIO
- **Localização**: `config/SecurityConfig.java:75`

**Problema**: qualquer usuário (inclusive auto-cadastrado) lê métricas internas da JVM
e do Mongo.

**Solução**: restringir a `hasAuthority('SUPER_ADMIN')` (ou a role dedicada
`MONITORING`). Ex.: `.requestMatchers("/actuator/prometheus").hasAuthority("SUPER_ADMIN")`.

**Plano de testes**:

- IT (`SecurityHeadersIT` ou novo): usuário comum → 403 em `/actuator/prometheus`;
  super admin → 200.

---

### [RESOLVIDO] M2. `server.error.include-stacktrace=always`

- **Severidade**: MÉDIO
- **Localização**: `src/main/resources/application.properties`

**Problema**: stacktraces em respostas de erro podem vazar nomes de classes internas,
caminhos de arquivo e detalhes de infraestrutura.

**Solução**: `server.error.include-stacktrace=never` (ou `on_param`) e
`server.error.include-message=never` em produção (manter detalhes só no log JSON).

**Plano de testes**:

- Manual: disparar erro → resposta sem stacktrace.
- IT: chamada 500 → corpo sem "at com.prosup".

---

### [RESOLVIDO] M3. Headers de segurança faltantes

- **Severidade**: MÉDIO
- **Localização**: `config/SecurityConfig.java:62-68`

**Problema**: não há `Referrer-Policy`, `Permissions-Policy`, `Cross-Origin-Opener-Policy`
nem `Cache-Control: no-store` em respostas autenticadas (token no navegador pode ser
cacheado).

**Solução**:

```java
.headers(headers -> headers
        .httpStrictTransportSecurity(...)   // mantém
        .contentTypeOptions(Customizer.withDefaults())
        .frameOptions(frame -> frame.deny())
        .contentSecurityPolicy(...)         // mantém
        .referrerPolicy(policy -> policy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
        .permissionsPolicy(policy -> policy.policy("camera=(), microphone=(), geolocation=()"))
        .crossOriginOpenerPolicy(policy -> policy.policy(CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN))
        .cacheControl(cache -> cache.disable())   // adicionar 'Cache-Control: no-store' via filter se necessário
)
```

**Plano de testes**:

- IT (`SecurityHeadersIT`): verificar presença dos novos headers e ausência de
  `Cache-Control: no-store` ausente (ou valor esperado).
- Manual: `curl -i` em endpoint autenticado.

---

### [RESOLVIDO] M4. Permissões congeladas no JWT por 24h + sem troca/reset de senha

- **Severidade**: MÉDIO
- **Localização**: `config/JwtTokenProvider.java` (claims de permissões); ausência de
  endpoint de troca/reset de senha

**Problema**: alterações de permissão/role do usuário só valem após novo login (24h
máx.). Além disso, não há fluxo de troca de senha (usuário não consegue rotacionar
senha comprometida) nem reset por e-mail.

**Solução**:

1. (Fase 2) Carregar permissões do banco no `JwtAuthenticationFilter` a cada request
   (cache curto, ex.: 60s), em vez de confiar nas claims — remove o problema de
   revogação de permissão.
2. Endpoints `PUT /me/password` (autenticado, exige senha atual) e, posteriormente,
   `POST /auth/forgot-password` + `POST /auth/reset-password` com token de redefinição
   single-use expirável.

**Plano de testes**:

- Unit: troca de senha com senha atual errada → 400; correta → senha alterada no
  repositório.
- IT: login → alterar permissão do usuário → request imediato reflete nova permissão
  (com cache, aguardar TTL ou usar o fluxo direto).

---

### [RESOLVIDO] M5. Sem trilha de auditoria

- **Severidade**: MÉDIO
- **Localização**: global

**Problema**: não há registro de eventos sensíveis (login, logout, criação de conta,
alteração de permissão, exclusão de dados, falha de API key). Difícil investigar
incidentes.

**Solução**: `AuditLogService` + filtro/aspecto anotado `@Audited` para eventos-chave;
persistir em coleção `audit_log` com `userId`, ação, detalhe, IP, timestamp; log JSON
também via Logstash.

**Plano de testes**:

- Unit: serviço de auditoria registra entrada corretamente.
- IT: fluxo de login → documento `audit_log` criado.

---

### [RESOLVIDO] M6. Código morto: `validateCurrentTenant` e `TenantContextTaskDecorator`

- **Severidade**: MÉDIO
- **Localização**: `service/AuthorizationService.java:53-59`; `config/TenantContextTaskDecorator.java`

**Problema**: `validateCurrentTenant` não é chamado em lugar nenhum (validação de
academia do corpo deve acontecer nos services — ver C3); `TenantContextTaskDecorator`
não é registrado no `TaskExecutor`, então o tenant **não propaga** para threads de
tarefas assíncronas (se houver, risco de cross-tenant em jobs).

**Solução**:

1. Remover `validateCurrentTenant` (a validação real fica nos services) **ou** usá-lo
   sistematicamente nos controllers que recebem `academiaId` no body.
2. Registrar o decorator no `ThreadPoolTaskExecutor` dos serviços assíncronos existentes
   (e futuros) para propagar o tenant, ou documentar que jobs usam tenant explícito.

**Plano de testes**:

- Unit: se mantido, teste do decorator propagando tenant para nova thread.
- Compilação: sem referências mortas (remover ou usar).

---

### [RESOLVIDO] M7. `show-details=always` / mensagens de erro verbosas

- **Severidade**: MÉDIO
- **Localização**: `src/main/resources/application.properties`; `api/handler/GlobalExceptionHandler.java`

**Problema**: mensagens de exceção de negócio (ex.: "Cliente não encontrado: {id}")
são retornadas ao cliente com detalhes internos (IDs). Em ambiente público, usar
mensagens genéricas e logar o detalhe.

**Solução**:

1. `server.error.include-message=never` (ou apenas para os handlers de negócio).
2. No `GlobalExceptionHandler`, manter mensagens amigáveis para validação (`@Valid`)
   e mensagens genéricas + `log.error` para exceções internas.

**Plano de testes**:

- IT: erro 500 → corpo sem mensagem interna; `@Valid` → mensagens de campo normais.

---

## 5. Ordem de execução recomendada

| Ordem | Item | Esforço |
|---|---|---|
| 1 | C1 + C2 + C3 (fail-closed + escopo real) | Grande |
| 2 | C4 (fim da união de permissões) | Pequeno |
| 3 | A3 + A4 (autorização fail-closed) | Pequeno |
| 4 | A9 (segredos fora do git + rotação) | Pequeno |
| 5 | A1 + A2 + A7 (abuso de auth) | Médio |
| 6 | A6 (denylist de jti) | Médio |
| 7 | A5 (proteção de API keys) | Médio |
| 8 | A8 (initializer restrito a local) | Pequeno |
| 9 | M1, M2, M3, M7 (hardening HTTP) | Pequeno |
| 10 | M4, M5, M6 (features/limpeza) | Grande |

Critério de "concluído": todos os testes do plano do item passando + `./mvnw clean
verify` verde.

