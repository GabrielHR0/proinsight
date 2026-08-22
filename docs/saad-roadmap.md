# Roadmap — Funcionalidades Futuras de Autenticação SaaS

## Já implementado
- [x] **Bug `academiaPermissoes` vazio no JWT** — `JwtTokenProvider.getAuthentication()` agora reconstrói o mapa de permissões por academia corretamente
- [x] **`TenantContext`** — ThreadLocal com `InheritableThreadLocal` + `X-Academia-Id` disponível para a camada de serviço; limpo automaticamente via `TenantContextFilter` (try/finally)
- [x] **Auto-cadastro** — `POST /auth/register` com criação de usuário + academia + role admin + auto-login
- [x] **Logout** — `POST /auth/logout` revoga todos os refresh tokens do usuário
- [x] **Bloqueio de conta (lockout)** — `LoginLockoutService` com 5 tentativas / 15 min de bloqueio
- [x] **Headers de segurança** — HSTS (includeSubDomains, 1 ano), `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY` configurados via `SecurityConfig.headers()`
- [x] **CORS para produção** — Origens movidas para `app.cors.allowed-origins` no `application.properties`, lidas via `CorsProperties` (`@ConfigurationProperties`)
- [x] **Chaves de API (M2M)** — `ApiKeyDocument` + `ApiKeyRepository` + `ApiKeyAuthenticationFilter` com prefixo `pk_`, hash SHA-256, validação de expiry
- [x] **Propagação de tenant em tarefas assíncronas** — `TenantContext` usa `InheritableThreadLocal`; `TenantContextTaskDecorator` criado para `@Async`
- [x] **Testes de integração isolados** — `cleanAndSetUp()` em `AutorizacaoFluxosIT` agora limpa academias + refresh tokens; `AuthIntegrationIT.cleanUp()` limpa refresh tokens

---

## Prioridade média (próximas sprints)

### Verificação de e-mail
- Adicionar campo `emailVerified` no `UserDocument` (booleano, padrão `false`)
- Criar entidade `EmailVerificationToken` com expiry
- Endpoint `POST /auth/verify-email?token=...` para confirmar
- Endpoint `POST /auth/resend-verification` para reenviar
- **Dependência externa:** serviço de e-mail (`spring-boot-starter-mail` + provedor SMTP)

### Lockout avançado
- Rate limit **por e-mail** (além do atual por IP)
- Endpoint `POST /auth/unlock` para desbloquear via e-mail
- Requer serviço de e-mail

### Recuperação de senha
- `POST /auth/forgot-password` — gera token com expiry, envia e-mail
- `POST /auth/reset-password` — valida token + nova senha
- Requer serviço de e-mail

---

## Prioridade baixa (visão de produto)

### MFA / 2FA
- Campo `mfaSecret` no `UserDocument` (para TOTP)
- Endpoints:
  - `POST /auth/mfa/setup` — gera secret + QR code
  - `POST /auth/mfa/verify` — valida código TOTP, ativa MFA
  - `POST /auth/mfa/disable` — desativa MFA
- Fluxo de login modificado: após senha válida, retorna `mfaRequired: true`, cliente envia código
- Alternativa: MFA via e-mail (código de 6 dígitos)

### Convite de usuários
- Entidade `Invitation` com token, academiaId, roleIds, expiry
- Endpoint `POST /academias/{id}/invite` — envia e-mail de convite
- Endpoint `POST /auth/accept-invite?token=...` — cria usuário + vincula à academia
- Requer serviço de e-mail

### Gerenciamento de membros da academia
- `GET /academias/{id}/members` — lista usuários com papéis
- `PUT /academias/{id}/members/{userId}/roles` — altera papéis
- `DELETE /academias/{id}/members/{userId}` — remove usuário da academia

---

## Infraestrutura / Arquitetura

### Chaves de API (M2M)
- [x] Entidade `ApiKeyDocument` com hash da chave (SHA-256), academiaId, permissões, expiry
- [x] `ApiKeyRepository` — consulta por `keyHashAndActiveTrue`
- [x] Filtro `ApiKeyAuthenticationFilter` para `Authorization: Bearer pk_...`
- [x] Rate limiting separado para API keys (mesmo `LoginRateLimiterFilter`)
- [x] Registrado no `SecurityConfig` antes do `JwtAuthenticationFilter`

### Isolamento de dados por tenant (camada de dados)
- [x] `TenantContext` + `TenantContextFilter` propagam `X-Academia-Id`
- [x] `@PreAuthorize("@auth.hasAcademiaAccess(#request.academiaId)")` nos controllers
- [x] `AuthorizationService.validateCurrentTenant()` compara academiaId do request com `TenantContext`
- [ ] Futuro: repositórios com escopo automático de tenant (Spring Data MongoDB `@Query` ou `AbstractMongoEventListener`)

### Headers de segurança
- [x] `Strict-Transport-Security` (HSTS, includeSubDomains, maxAge 1 ano)
- [x] `X-Content-Type-Options: nosniff`
- [x] `X-Frame-Options: DENY`
- [ ] `Content-Security-Policy` (pendente — definir política por perfil)

### CORS para produção
- [x] Origens movidas para `application.properties`: `app.cors.allowed-origins`
- [x] `CorsProperties` record com `@ConfigurationProperties(prefix = "app.cors")`
- [x] `WebConfig` lê origens dinamicamente via `CorsProperties`
- [ ] Em produção: sobrescrever `app.cors.allowed-origins` via variável de ambiente ou `application-prod.properties`

### Propagação de tenant em tarefas assíncronas
- [x] `TenantContext` usa `InheritableThreadLocal`
- [x] `TenantContextTaskDecorator` criado para uso futuro com `@Async`

### Subscription / Planos
- Entidade `Subscription` vinculada à academia (trial, mensal, anual)
- Feature flags baseadas no plano (ex: número máximo de alunos, relatórios avançados)
- Webhook de pagamento (Stripe, Mercado Pago, etc.)

---

## Dívida técnica conhecida

- [x] ~~`UserDocument.academiaIds` é redundante com `academiaRoles.keySet()` — risco de inconsistência~~ (aceito como design)
- [x] ~~`JwtTokenProvider` não valida `jti`, `iss`, `aud` — sem proteção contra replay de token entre ambientes~~ (corrigido: `issuer` + `audience` injetados via properties, validados no parser via `requireIssuer`/`requireAudience`; `jti` verificado no `getAuthentication`)
- [x] ~~Testes de integração compartilham MongoDB — `AuthIntegrationIT.cleanUp()` pode poluir dados de outras classes de teste~~ (corrigido parcialmente — `cleanUp`/`cleanAndSetUp` agora limpam academias + refresh tokens; falha intermitente residual persiste em cenários de concorrência no suite completo)
