# ProInsight — Anchored Summary

## Objective
Review the full codebase for security (OWASP Top 10), exception handling, and performance; ensure integration tests use no mocks and are fully realistic; fix flaky tests; create opencode agent with permanent project memory.

## Important Details
- Spring Boot 3.2.4, MongoDB, JWT with HMAC-SHA, BCrypt(12), stateless sessions
- All controllers under `/api/v1` prefix via `WebConfig`; `SecurityConfig` with `.permitAll()` for login/refresh and actuator/health+info
- Integration tests (`*IT.java`) use real HTTP (`TestRestTemplate`), real Mongo (`@SpringBootTest(webEnvironment=RANDOM_PORT)`), no `@Mock`/`@MockBean` — only `MockHttpServletRequest` as a utility POJO in `AutorizacaoFluxosIT`
- MongoDB standalone (no replica set) — `MongoTransactionManager` removed; `@Transactional` kept as documentation (no-op without replica set in dev)
- Caffeine cache added to pom.xml for rate limiter with automatic TTL
- `.opencode/agents/proinsight-mentor.md` created with full project memory; `opencode.json` references it as default agent
- **JDK 22 `HttpURLConnection` bug**: `getResponseCode()` on 401 tries to auth-retry; if body is "streaming" (non-repeatable), throws `HttpRetryException`. Fix: use `JdkClientHttpRequestFactory` (`java.net.http.HttpClient`) in tests instead of `SimpleClientHttpRequestFactory`.

## Work State

### Completed (this session)
- **Bug `academiaPermissoes` vazio no JWT**: `JwtTokenProvider.getAuthentication()` reconstrói o mapa de permissões por academia dos claims, em vez de `Map.of()` — `@PreAuthorize("@auth.hasAcademiaAccess(#id)")` volta a funcionar para JWT
- **`TenantContext`** (ThreadLocal) + `TenantContextFilter` — primeiro filtro do chain, com try/finally, expõe `X-Academia-Id` para a camada de serviço via `TenantContext.getAcademiaId()`
- **Auto-cadastro**: `POST /auth/register` com `RegistrationService` — cria role admin + usuário + academia + vínculo + auto-login (JWT + refresh)
- **Logout**: `POST /auth/logout` — revoga todos os refresh tokens do usuário logado
- **Documento de roadmap**: `docs/saad-roadmap.md` com MFA, convites, lockout, API keys, etc.

### Previous sessions (already completed)
- OWASP Top 10 review, exception handlers, sanitized DataIntegrityViolationException, security audit logging, actuator restricted, log verbosity reduced, MongoDB indexes verified, performance review no N+, LoginRateLimiterFilter memory leak fixed (Caffeine), ProtocoloHubService.getHub() optimized, @Transactional added, flaky tests fixed, Caffeine dependency, opencode agent created

### Active
- (none)

### Blocked
- (none)

## Relevant Files (new/changed this session)
- `src/main/java/com/prosup/proinsight/config/JwtTokenProvider.java` — fix `academiaPermissoes` reconstruction
- `src/main/java/com/prosup/proinsight/config/TenantContext.java` — ThreadLocal holder
- `src/main/java/com/prosup/proinsight/config/TenantContextFilter.java` — outermost filter, try/finally
- `src/main/java/com/prosup/proinsight/config/SecurityConfig.java` — register + TenantContextFilter
- `src/main/java/com/prosup/proinsight/config/JwtAuthenticationFilter.java` — reverted (TenantContext moved to dedicated filter)
- `src/main/java/com/prosup/proinsight/api/controller/api/v1/AuthController.java` — register + logout
- `src/main/java/com/prosup/proinsight/api/dto/request/RegisterRequest.java` — DTO
- `src/main/java/com/prosup/proinsight/service/RegistrationService.java` — signup logic
- `src/main/java/com/prosup/proinsight/service/AcademiaService.java` — (no change, but studied for register flow)
- `docs/saad-roadmap.md` — future features roadmap
