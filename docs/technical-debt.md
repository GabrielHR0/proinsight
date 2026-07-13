# Technical Debt — Proinsight

> Status: ❌ Pendente | ✅ Concluído

Atualizado: 2026-07-12

---

## 🔴 Críticos (impedem o funcionamento)

### 1. `AvaliacaoFisicaDocument.strategy` — tipo não persistível
- **Status:** ✅ Concluído
- **Solução:** `StrategyRegistry` + `@StrategyFor` com resolução por key string.

### 2. `AvaliacaoFisicaMapper` — TODOs retornam `null`
- **Status:** ✅ Concluído
- **Solução:** `Map<MedicaoTipo, Function>` com conversores polimórficos.

### 3. `TabelaVo2Max.classificarComTeste()` — método inexistente
- **Status:** ✅ Concluído
- **Solução:** `getCriterio()` adicionado à interface `Teste`.

### 4. `Role.setId()` é no-op
- **Status:** ✅ Concluído
- **Solução:** Setter corrigido para `this.id = id;`.

### 5. `UserController.register()` expõe senha
- **Status:** ✅ Concluído
- **Solução:** Retorna `UserResponse` DTO (sem campo password).

---

## 🟠 Altos

### 6. `AcademiaService.findByUserId()` — scan completo
- **Status:** ✅ Concluído
- **Solução:** `AcademiaRepository.findByUserId(String)` — query derivada Spring Data.

### 7. `UserService.findByEmail()` — scan completo
- **Status:** ✅ Concluído
- **Solução:** `UserRepository.findByEmail(String)` — query derivada Spring Data.

### 8. `ClienteDocument` sem `responsavelId`/`responsavelType`
- **Status:** ✅ Concluído
- **Solução:** Campos adicionados com `@Indexed` em `responsavelId`.

### 9. `TesteVo2MaxMapperRegistry` — só registra COOPER
- **Status:** ✅ Concluído
- **Solução:** Registrados COOPER, ROCKPORT e ESTEIRA_INCREMENTAL.

### 10. `AvaliacaoService.salvarAvaliacao()` não persiste
- **Status:** ✅ Concluído
- **Solução:** Service refatorado com `save()` que valida cliente/avaliador/protocolo e persiste via `AvaliacaoFisicaRepository`.

---

## 🟡 Médios

### 11. `AvaliadorService.toDocument()` ignora `cpf`
- **Status:** ✅ Concluído
- **Solução:** `AvaliadorMapper.toDocument()` copia `req.getCpf()` para o documento.

### 12. `AcademiaController` usa domínio no lugar de DTOs
- **Status:** ✅ Concluído
- **Solução:** Controller já usa `AcademiaRequest`/`AcademiaResponse`.

### 13. `UserController` usa inner class com campos públicos
- **Status:** ✅ Concluído
- **Solução:** Agora usa `UserRequest` do pacote `api/dto/request/`.

### 14. `AvaliacaoVo2MaxHandler.avaliar()` sem null-check
- **Status:** ✅ Concluído
- **Solução:** `IllegalStateException` lançado quando `strategy.avaliar()` retorna null.

### 15. `AvaliacaoVo2MaxHandler.converterParaResponse()` valores fixos
- **Status:** ✅ Concluído
- **Solução:** Extrai dados reais do `NivelVo2Max` (classificação, resultado).

---

## 🟢 Baixos

### 16. `AvaliadorControllerValidationTest` vazio
- **Status:** ❌ Pendente
- **Problema:** Classe sem métodos de teste.

### 17. `ExampleRepositoryIT` comentado
- **Status:** ❌ Pendente
- **Problema:** Teste de integração desativado (código todo comentado).

### 18. `AvaliacaoController` usa `@Autowired`
- **Status:** ✅ Concluído
- **Solução:** Agora usa construtor explícito.

### 19. Plugins duplicados no `pom.xml`
- **Status:** ✅ Concluído
- **Solução:** POM limpo, cada plugin declarado uma vez.

### 20. URLs placeholder no `GlobalExceptionHandler`
- **Status:** ✅ Concluído
- **Solução:** Substituído por `proinsight://problems/*` (URI customizado válido).

---

## Resumo

| Prioridade | Total | Concluído | Pendente |
|------------|-------|-----------|----------|
| 🔴 Críticos | 5 | 5 | 0 |
| 🟠 Altos | 5 | 5 | 0 |
| 🟡 Médios | 5 | 5 | 0 |
| 🟢 Baixos | 5 | 3 | 2 |
| **Total** | **20** | **18** | **2** |

**Progresso: 90% concluído**

### Pendências restantes (Sprint 2)
- #16 `AvaliadorControllerValidationTest` vazio — será implementado com autenticação
- #17 `ExampleRepositoryIT` comentado — será removido ou implementado
