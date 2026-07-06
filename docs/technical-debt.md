# Technical Debt — Proinsight

> Status: ❌ Pendente &nbsp;|&nbsp; 🔄 Em andamento &nbsp;|&nbsp; ✅ Concluído

---

## 🔴 Críticos (impedem o funcionamento)

### 1. `AvaliacaoFisicaDocument.strategy` — tipo não persistível
- **Arquivo:** `src/main/java/.../infrastructure/persistence/document/AvaliacaoFisicaDocument.java:24`
- **Problema:** O campo `AvaliacaoStrategy<?> strategy` era uma interface Spring, impossível de serializar no MongoDB.
- **O que foi feito:** Substituído por `String strategyKey`. Criado `@StrategyFor`, `StrategyRegistry` com injeção automática via `List<AvaliacaoStrategy<?>>`. Handler usa `registry.resolve(strategyKey)`.
- **Status:** ✅ Concluído

### 2. `AvaliacaoFisicaMapper` — TODOs retornam `null`
- **Arquivo:** `src/main/java/.../infrastructure/persistence/mapper/AvaliacaoFisicaMapper.java`
- **Problema:** `convertMedicaoDocumentToDomain()` e `convertMedicaoDomainToDocument()` retornavam `null`.
- **O que foi feito:** Implementado `Map<MedicaoTipo, Function>` para conversão polimórfica. Criados métodos `vo2MaxToDomain`, `imcToDomain`, `vo2MaxToDocument`, `imcToDocument`. Criados domains `MedicaoImc` e `TesteImc`. Refatorados Document e Domain para usar `Integer` como tipo interno com métodos de conversão para apresentação (`getPesoKg()`, `getAlturaMetros()`, `getDistanciaKm()`).
- **Status:** ✅ Concluído

### 3. `TabelaVo2Max.classificarComTeste()` — método inexistente
- **Arquivo:** `src/main/java/.../domain/model/composite/tabelas/TabelaVo2Max.java:23`
- **Problema:** `teste.getCriterio()` não existia na interface `Teste`. Erro de compilação.
- **O que foi feito:** Adicionado `String getCriterio()` à interface `Teste`. Implementado em `TesteVo2Max` (retorna `protocolo.name()`) e `TesteImc` (retorna `"IMC"`).
- **Status:** ✅ Concluído

### 4. `Role.setId()` é no-op
- **Arquivo:** `src/main/java/.../domain/model/Role.java:42-43`
- **Problema:** `setId(String id) {}` não atribui o valor. Ao carregar uma Role do banco, o ID fica `null`.
- **O que fazer:** Corrigir o setter para `this.id = id;`.
- **Status:** ❌ Pendente

### 5. `UserController.register()` expõe senha
- **Arquivo:** `src/main/java/.../api/controller/UserController.java:36-38`
- **Problema:** Retorna `User` (entidade de domínio) diretamente, incluindo o campo `password` (hash).
- **O que fazer:** Criar um `UserResponse` DTO (já existe em `api/dto/response/`) sem o campo password e usá-lo no retorno.
- **Status:** ❌ Pendente

---

## 🟠 Altos

### 6. `AcademiaService.findByUserId()` — scan completo
- **Arquivo:** `src/main/java/.../service/AcademiaService.java:31`
- **Problema:** `repo.findAll().stream().filter(...)` carrega toda a coleção em memória.
- **O que fazer:** Adicionar `findByUserId(String userId)` ao `AcademiaRepository`.
- **Status:** ❌ Pendente

### 7. `UserService.findByEmail()` — scan completo
- **Arquivo:** `src/main/java/.../service/UserService.java:100-104`
- **Problema:** Mesmo problema: `findAll().stream().filter(...)` em vez de consulta indexada.
- **O que fazer:** Adicionar `findByEmail(String email)` ao `UserRepository`.
- **Status:** ❌ Pendente

### 8. `ClienteDocument` sem `responsavelId`/`responsavelType`
- **Arquivo:** `src/main/java/.../infrastructure/persistence/document/ClienteDocument.java`
- **Problema:** O domain `Cliente` tem esses campos, mas o Document não. Dados são perdidos na persistência.
- **O que fazer:** Adicionar campos e ajustar mapeamento.
- **Status:** ❌ Pendente

### 9. `TesteVo2MaxMapperRegistry` — só registra COOPER
- **Arquivo:** `src/main/java/.../api/mapper/TesteVo2MaxMapperRegistry.java:24-27`
- **Problema:** Apenas `ProtocoloVo2Max.COOPER` está mapeado. ROCKPORT causa `IllegalArgumentException`.
- **O que fazer:** Registrar `ProtocoloVo2Max.ROCKPORT` com `TesteVo2MaxRockport`.
- **Status:** ❌ Pendente

### 10. `AvaliacaoService.salvarAvaliacao()` não persiste
- **Arquivo:** `src/main/java/.../service/AvaliacaoService.java:26-37`
- **Problema:** Valida cliente/avaliador mas não injeta `AvaliacaoFisicaRepository` e não salva a avaliação.
- **O que fazer:** Adicionar dependência e implementar a persistência.
- **Status:** ❌ Pendente

---

## 🟡 Médios

### 11. `AvaliadorService.toDocument()` ignora `cpf`
- **Arquivo:** `src/main/java/.../service/AvaliadorService.java:42-52`
- **Problema:** O `cpf` do `AvaliadorRequest` nunca é copiado para o documento.
- **Status:** ❌ Pendente

### 12. `AcademiaController` usa domínio no lugar de DTOs
- **Arquivo:** `src/main/java/.../api/controller/AcademiaController.java:21,22,28`
- **Problema:** Recebe `Academia` como `@RequestBody` e retorna `Academia` diretamente. DTOs `AcademiaRequest`/`AcademiaResponse` existem mas não são usados.
- **Status:** ❌ Pendente

### 13. `UserController` usa inner class com campos públicos
- **Arquivo:** `src/main/java/.../api/controller/UserController.java:27-33`
- **Problema:** Inconsistente com o padrão de DTOs do projeto. `RegisterRequest` com fields públicos em vez de `UserRequest` que já existe no pacote `api/dto/request/`.
- **Status:** ❌ Pendente

### 14. `AvaliacaoVo2MaxHandler.avaliar()` sem null-check
- **Arquivo:** `src/main/java/.../service/handler/AvaliacaoVo2MaxHandler.java:52`
- **Problema:** `avaliacao.getStrategy()` pode retornar `null`, causando NPE.
- **Status:** ❌ Pendente

### 15. `AvaliacaoVo2MaxHandler.converterParaResponse()` valores fixos
- **Arquivo:** `src/main/java/.../service/handler/AvaliacaoVo2MaxHandler.java:67-80`
- **Problema:** Nome genérico `"CLASSIFICACAO_..."` e valor `0.0`. Não extrai dados reais do `NivelForca` (classificação, min, max).
- **Status:** ❌ Pendente

---

## 🟢 Baixos

### 16. `AvaliadorControllerValidationTest` vazio
- **Arquivo:** `src/test/java/.../controller/AvaliadorControllerValidationTest.java`
- **Problema:** Classe sem métodos de teste.
- **Status:** ❌ Pendente

### 17. `ExampleRepositoryIT` comentado
- **Arquivo:** `src/test/java/.../ExampleRepositoryIT.java`
- **Problema:** Teste de integração desativado.
- **Status:** ❌ Pendente

### 18. `AvaliacaoController` usa `@Autowired`
- **Arquivo:** `src/main/java/.../api/controller/AvaliacaoController.java:24-25`
- **Problema:** Inconsistente com os demais controllers que usam construtor explícito.
- **Status:** ❌ Pendente

### 19. Plugins duplicados no `pom.xml`
- **Arquivo:** `pom.xml:93-159`
- **Problema:** `maven-compiler-plugin` e `spring-boot-maven-plugin` aparecem declarados duas vezes cada.
- **Status:** ❌ Pendente

### 20. URLs placeholder no `GlobalExceptionHandler`
- **Arquivo:** `src/main/java/.../api/handler/GlobalExceptionHandler.java`
- **Problema:** URLs como `https://example.com/problems/*` são placeholders para documentação.
- **Status:** ❌ Pendente
