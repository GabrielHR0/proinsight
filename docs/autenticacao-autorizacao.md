# Autenticação e Autorização

## Índice

- [Fluxo de Login](#fluxo-de-login)
- [JWT — Estrutura do Token](#jwt--estrutura-do-token)
- [Escopo por Academia (X-Academia-Id)](#escopo-por-academia-x-academia-id)
- [Matriz de Permissões por Controller](#matriz-de-permissões-por-controller)
- [Exemplos com cURL](#exemplos-com-curl)

---

## Fluxo de Login

```
Cliente                          AuthController                  AuthenticationManager           UserDetailsService
   │                                   │                                   │                          │
   │  POST /api/auth/login             │                                   │                          │
   │  { "email", "password" }          │                                   │                          │
   │─────────────────────────────────>│                                   │                          │
   │                                   │  authenticate(                    │                          │
   │                                   │    UsernamePasswordAuthToken )    │                          │
   │                                   │──────────────────────────────────>│                          │
   │                                   │                                   │  loadUserByUsername(email) │
   │                                   │                                   │─────────────────────────>│
   │                                   │                                   │                          │
   │                                   │                                   │  <── User ──────────────│
   │                                   │                                   │                          │
   │                                   │  <─────── Authentication ────────│                          │
   │                                   │                                   │                          │
   │                                   │  generateToken(auth)              │                          │
   │                                   │──────────────────────┐            │                          │
   │                                   │                      │ JWT string │                          │
   │                                   │<─────────────────────┘            │                          │
   │                                   │                                   │                          │
   │  { "token", "userId",             │                                   │                          │
   │    "userName", "email",           │                                   │                          │
   │    "academiaPermissoes" }         │                                   │                          │
   │<─────────────────────────────────│                                   │                          │
```

### Request

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "dono@academia.com",
  "password": "senha123"
}
```

### Response (200)

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJkb25vQGFjYWRlbWlhLmNvbSIsInVzZXJJZCI6IjY0YWI...",
  "userId": "64ab...",
  "userName": "dono",
  "email": "dono@academia.com",
  "academiaPermissoes": {
    "academia_id_1": [
      "CLIENTES_CRIAR",
      "CLIENTES_LER",
      "CLIENTES_ATUALIZAR",
      "CLIENTES_EXCLUIR",
      "AVALIACOES_CRIAR",
      "AVALIACOES_LER",
      "AVALIACOES_ATUALIZAR",
      "AVALIACOES_EXCLUIR",
      "AVALIADORES_CRIAR",
      "AVALIADORES_LER",
      "AVALIADORES_ATUALIZAR",
      "PROTOCOLOS_LER",
      "USUARIOS_CRIAR",
      "USUARIOS_LER",
      "USUARIOS_ATUALIZAR",
      "USUARIOS_EXCLUIR",
      "ACADEMIAS_CRIAR",
      "ACADEMIAS_LER",
      "ACADEMIAS_ATUALIZAR",
      "RELATORIOS_LER",
      "RELATORIOS_EXPORTAR",
      "SUPER_ADMIN"
    ]
  }
}
```

### Response (401)

```json
{
  "message": "Bad credentials"
}
```

---

## JWT — Estrutura do Token

O token é **autocontido** (stateless): não há consulta ao banco para autorizar requisições. A validação é feita por assinatura HMAC-SHA384 com a chave configurada em `jwt.secret`.

### Claims

```json
{
  "sub": "email@dominio.com",           // subject = email
  "userId": "64ab...",                  // ID do usuário
  "userName": "dono",                   // nome de usuário
  "academiaIds": ["acad_id_1", ...],    // lista de IDs de academias
  "academiaPermissoes": {               // permissões agrupadas por academia
    "acad_id_1": ["CLIENTES_LER", "AVALIACOES_CRIAR", ...],
    "acad_id_2": ["PROTOCOLOS_LER"]
  },
  "iat": 1700000000,                    // emitido em
  "exp": 1700086400                     // expira em (config: jwt.expiration)
}
```

### Como usar

Enviar o token no header `Authorization`:

```
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

---

## Escopo por Academia (X-Academia-Id)

O usuário pode ter permissões diferentes em academias diferentes. O header opcional `X-Academia-Id` define qual conjunto de permissões será usado na requisição.

### Regras

| Cenário | Comportamento |
|---|---|
| Header `X-Academia-Id` presente e **válido** | Apenas as permissões **daquela academia** são aplicadas |
| Header `X-Academia-Id` **ausente** | **União** de todas as permissões do usuário |
| Header presente mas **inexistente** no mapa | **União** de todas (fallback seguro) |

### Exemplo

Usuário com `academiaRoles`:

```json
{
  "academia_a": ["role_admin"],
  "academia_b": ["role_readonly"]
}
```

| Requisição | Permissões efetivas |
|---|---|
| `GET /clientes` (sem header) | Admin + ReadOnly = todas |
| `GET /clientes` + `X-Academia-Id: academia_a` | Admin (acesso total) |
| `GET /clientes` + `X-Academia-Id: academia_b` | Apenas leitura |
| `GET /clientes` + `X-Academia-Id: invalida` | Todas (fallback) |

---

## Matriz de Permissões por Controller

### AcademiaController (`/academias`)

| Método | Rota | Permissão |
|---|---|---|
| POST | `/academias` | `ACADEMIAS_CRIAR` |
| GET | `/academias/{id}` | `ACADEMIAS_LER` |
| GET | `/academias/by-owner/{ownerId}` | `ACADEMIAS_LER` |
| PUT | `/academias/{id}` | `ACADEMIAS_ATUALIZAR` |
| DELETE | `/academias/{id}` | `SUPER_ADMIN` |

### AvaliadorController (`/avaliadores`)

| Método | Rota | Permissão |
|---|---|---|
| POST | `/avaliadores` | `AVALIADORES_CRIAR` |

### ClienteController (`/clientes`)

| Método | Rota | Permissão |
|---|---|---|
| POST | `/clientes` | `CLIENTES_CRIAR` |
| POST | `/clientes/com-imc` | `CLIENTES_CRIAR` |
| GET | `/clientes` | `CLIENTES_LER` |
| GET | `/clientes/{id}` | `CLIENTES_LER` |
| PUT | `/clientes/{id}` | `CLIENTES_ATUALIZAR` |
| GET | `/clientes/por-academia/{academiaId}` | `CLIENTES_LER` |
| GET | `/clientes/por-avaliador/{avaliadorId}` | `CLIENTES_LER` |
| GET | `/clientes/{clienteId}/avaliacoes` | `AVALIACOES_LER` |

### AvaliacaoController (`/avaliacoes`)

| Método | Rota | Permissão |
|---|---|---|
| POST | `/avaliacoes/vo2max` | `AVALIACOES_CRIAR` |
| GET | `/avaliacoes/{protocoloId}/dados-pre-avaliacao/{clienteId}` | `AVALIACOES_LER` |

### ProtocoloHubController (`/avaliacoes`)

| Método | Rota | Permissão |
|---|---|---|
| GET | `/avaliacoes/hub` | `PROTOCOLOS_LER` |
| GET | `/avaliacoes/protocolos` | `PROTOCOLOS_LER` |
| GET | `/avaliacoes/protocolos/{id}` | `PROTOCOLOS_LER` |
| POST | `/avaliacoes/favoritos` | `PROTOCOLOS_LER` |
| DELETE | `/avaliacoes/favoritos` | `PROTOCOLOS_LER` |
| GET | `/avaliacoes/favoritos` | `PROTOCOLOS_LER` |
| GET | `/avaliacoes/favoritos/verificar` | `PROTOCOLOS_LER` |

### TabelasClassificacaoController (`/tabelas_classificacao`)

| Método | Rota | Permissão |
|---|---|---|
| GET | `/tabelas_classificacao/{id}` | `PROTOCOLOS_LER` |
| GET | `/tabelas_classificacao` | `PROTOCOLOS_LER` |

### UserController (`/users`)

| Método | Rota | Permissão |
|---|---|---|
| POST | `/users` | `USUARIOS_CRIAR` |

---

## Exemplos com cURL

### Login

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@academia.com",
    "password": "senha123"
  }' | jq .
```

### Listar clientes (sem escopo — união de todas permissões)

```bash
TOKEN="eyJhbGciOiJIUzM4NCJ9..."

curl -s http://localhost:8080/clientes \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### Listar clientes (escopo academia A)

```bash
curl -s http://localhost:8080/clientes \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Academia-Id: academia_a_id" | jq .
```

### Criar cliente

```bash
curl -s -X POST http://localhost:8080/clientes \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Academia-Id: academia_a_id" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "João Silva",
    "email": "joao@email.com"
  }' | jq .
```

### Sem token (401)

```bash
curl -s -w "\n%{http_code}" http://localhost:8080/clientes
# Response: 401 Unauthorized
```

### Token sem permissão (403)

```bash
curl -s -w "\n%{http_code}" http://localhost:8080/academias \
  -H "Authorization: Bearer $TOKEN_AVALIADOR"
# Response: 403 Forbidden
```

---

## Enum de Permissões

```java
CLIENTES_CRIAR,
CLIENTES_LER,
CLIENTES_ATUALIZAR,
CLIENTES_EXCLUIR,

AVALIACOES_CRIAR,
AVALIACOES_LER,
AVALIACOES_ATUALIZAR,
AVALIACOES_EXCLUIR,

AVALIADORES_CRIAR,
AVALIADORES_LER,
AVALIADORES_ATUALIZAR,

PROTOCOLOS_LER,

USUARIOS_CRIAR,
USUARIOS_LER,
USUARIOS_ATUALIZAR,
USUARIOS_EXCLUIR,

ACADEMIAS_CRIAR,
ACADEMIAS_LER,
ACADEMIAS_ATUALIZAR,

RELATORIOS_LER,
RELATORIOS_EXPORTAR,

SUPER_ADMIN
```
