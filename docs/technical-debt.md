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

## 🔴 Críticos (impedem validação de entrada)

### 21. `UserController`, `AcademiaController`, `AvaliacaoController` sem `@Valid`
- **Arquivos:** `UserController.java:25`, `AcademiaController.java:23/42`, `AvaliacaoController.java:23`
- **Problema:** `@RequestBody` sem `@Valid` — anotações de validação nos DTOs jamais disparam.
- **Status:** ✅ Concluído
- **Solução:** `@Valid @RequestBody` adicionado nos métodos POST/PUT.

### 22. `AvaliacaoVo2MaxRequest` sem anotações de validação
- **Arquivo:** `api/dto/request/AvaliacaoVo2MaxRequest.java`
- **Problema:** Nenhum campo tem `@NotBlank`, `@NotNull`, `@Positive`. Dados nulos/vazios são aceitos.
- **Status:** ✅ Concluído
- **Solução:** `@NotBlank` nos IDs, `@Positive`/`@NotNull` nos numéricos.

---

## 🟠 Altos

### 23. `MedicaoVo2MaxDto` e `TesteVo2MaxDto` sem validação cascata
- **Arquivos:** `MedicaoVo2MaxDto.java`, `TesteVo2MaxDto.java`
- **Problema:** `medidoEm` sem `@NotNull`; lista `testes` sem `@Valid`; `protocolo` sem `@NotNull`.
- **Status:** ✅ Concluído
- **Solução:** `@NotNull/@Valid` nos campos, cascata ativada.

### 24. `AcademiaRequest.EnderecoRequest` sem validação
- **Arquivo:** `api/dto/request/AcademiaRequest.java` (inner class)
- **Problema:** rua, cidade, estado, cep sem `@NotBlank`.
- **Status:** ✅ Concluído
- **Solução:** Anotações de validação no EnderecoRequest.

### 25. Controllers com `@RequestParam` sem `@Validated`
- **Arquivo:** `ProtocoloHubController.java`
- **Problema:** `@RequestParam` não dispara validação sem `@Validated` na classe.
- **Status:** ✅ Concluído
- **Solução:** `@Validated` + `@NotBlank` nos parâmetros.

---

## 🟡 Médios

### 26. Domínios core sem validação em construtores/setters
- **Arquivos:** `User.java`, `Cliente.java`, `Academia.java`
- **Problema:** Construtores aceitam null/blank sem guard clauses; setters permitem sobrescrever com inválidos.
- **Status:** ✅ Concluído
- **Solução:** Guard clauses com `IllegalArgumentException` em construtores e setters.

### 27. `AvaliacaoVo2MaxHandler` lança `RuntimeException` genérico
- **Arquivo:** `service/handler/AvaliacaoVo2MaxHandler.java:60-61`
- **Problema:** Inconsistente — resto do código usa `NoSuchElementException`.
- **Status:** ❌ Pendente
- **Solução:** Trocar por `NoSuchElementException`.

### 28. Validação redundante handlers vs services
- **Arquivos:** `AvaliacaoVo2MaxHandler`, `AvaliacaoImcHandler`, `AvaliacaoService`
- **Problema:** Handlers e services validam existência dos mesmos registros.
- **Status:** ❌ Pendente
- **Solução:** Manter validação apenas no service; handler confia no service.

---

## 🟢 Baixos

### 29. Mensagens de erro sem padronização
- **Arquivos:** Todos os DTOs Request
- **Problema:** Alguns DTOs têm `message` em português, outros não. Faltam mensagens em vários.
- **Status:** ❌ Pendente
- **Solução:** `@NotBlank(message = "Campo obrigatório")` em todos os campos.

### 30. Faltam testes de validação
- **Arquivo:** N/A (novos testes)
- **Problema:** Nenhum teste envia payload inválido e verifica 400 + RFC 7807.
- **Status:** ❌ Pendente
- **Solução:** Testes parametrizados com payloads inválidos.

---

## Resumo

| Prioridade | Total | Concluído | Pendente |
|------------|-------|-----------|----------|
| 🔴 Críticos | 7 | 7 | 0 |
| 🟠 Altos | 8 | 8 | 0 |
| 🟡 Médios | 8 | 6 | 2 |
| 🟢 Baixos | 7 | 3 | 4 |
| **Total** | **30** | **24** | **6** |

**Progresso: 80% concluído**

### Pendências restantes
- #16 `AvaliadorControllerValidationTest` vazio
- #17 `ExampleRepositoryIT` comentado
- #27 `AvaliacaoVo2MaxHandler` lança `RuntimeException` genérico
- #28 Validação redundante handlers vs services
- #29 Mensagens de erro sem padronização
- #30 Faltam testes de validação
