# Arquitetura Completa VO2Max - Guia Técnico

## 📋 Visão Geral

O sistema de avaliação VO2Max implementa uma arquitetura em **7 camadas** com separação clara de responsabilidades. Cada camada usa conceitos específicos do Spring e Java.

## 🏗️ As 7 Camadas

### 1. **HTTP Layer** - `AvaliacaoController`

**O que faz:** Recebe requisição HTTP e retorna response.

**Conceitos Spring:**
- `@RestController` - Controla endpoints HTTP
- `@PostMapping("/vo2max")` - Mapeia POST para /api/avaliacoes/vo2max
- `@RequestBody` - Jackson desserializa JSON automaticamente

**Fluxo:**
```java
@PostMapping("/vo2max")
public ResponseEntity<?> avaliarVo2Max(@RequestBody AvaliacaoVo2MaxRequest request) {
    // recebeu AvaliacaoVo2MaxRequest desserializada automaticamente
    // passa para o handler
    // retorna response
}
```

**Responsabilidade:** APENAS receber/retornar dados HTTP, nada mais.

---

### 2. **Validation Layer** - `RequestValidator` + `AvaliacaoVo2MaxValidator`

**O que faz:** Valida ESTRUTURA da requisição (tamanho, presença, formato).

**Conceitos Spring:**
- `@Component` - Spring gerencia bean (singleton, ciclo de vida)
- `@Autowired` - Constructor injection automática

**Exemplo:**
```java
@Component
public class AvaliacaoVo2MaxValidator {
    private final RequestValidator requestValidator;
    
    @Autowired
    public AvaliacaoVo2MaxValidator(RequestValidator validator) {
        this.requestValidator = validator;
    }
    
    public void validarRequisicao(AvaliacaoVo2MaxRequest request) {
        // Valida:
        // - IDs não nulos e tamanho <= 36 caracteres
        // - Medicao não nula
        // - Testes não vazio e quantidade <= 1000
        // NÃO valida valores (ex: frequência cardíaca válida)
    }
}
```

**O que NÃO faz:** Não entra em regras de negócio. Ex: não valida se frequência máxima > repouso.

**Lança:** `ValidacaoException` (400 Bad Request)

---

### 3. **Context Builder** - `AvaliacaoVo2MaxContextBuilder`

**O que faz:** Cria contexto tipado com validação DE NEGÓCIO.

**Conceitos Java/Design Pattern:**
- Builder Pattern - Construção fluente e validação
- Genéricos - `AvaliacaoVo2MaxContext implements AvaliacaoContext<MedicaoVo2, TesteVo2Max>`

**Exemplo:**
```java
var context = new AvaliacaoVo2MaxContextBuilder()
    .comCliente("client-123")           // fluent interface
    .comAvaliador("avaliador-456")
    .comMedicao(medicao)                // valida tipo TesteVo2Max
    .comTestes(testes)                  // valida que são instâncias corretas
    .build();                           // constrói se tudo estiver OK
```

**Validações:**
- Tipo correto (TesteVo2Max, não Teste genérico)
- Medicao tem tabelaClassificacaoId válido
- **Type-safety em compile-time** - impossível passar tipo errado

**Lança:** `IllegalArgumentException` (convertida em `RegraNeggocioException` pelo handler)

---

### 4. **Business Rules Layer** - `EstrategiaAvaliacaoVo2Max` (AvaliacaoVo2Max)

**O que faz:** Aplica lógica de domínio e retorna classificação.

**Conceitos:**
- Strategy Pattern - Algoritmo de avaliação específico
- Repository - Busca tabela de classificação

**Exemplo:**
```java
public class AvaliacaoVo2Max implements AvaliacaoStrategy<AvaliacaoVo2MaxContext> {
    
    @Override
    public Leaf avaliar(AvaliacaoVo2MaxContext contexto) {
        // 1. Busca tabela
        var tabelaDoc = tabelaRepository.findById(contexto.getTabelaClassificacaoId())
            .orElseThrow(() -> new RecursoNaoEncontradoException(...));
        
        // 2. Converte para domain
        var tabela = componentMapper.toDomain(tabelaDoc.getRaiz());
        
        // 3. Classifica
        for (TesteVo2Max teste : contexto.getTestes()) {
            var resultado = tabela.classificarComTeste(teste);
            if (resultado != null) {
                return resultado;  // Leaf (classificação)
            }
        }
        return null;
    }
}
```

**Responsabilidade:** Implementar algoritmo de VO2Max (pode ser diferente para IMC, Bioimpedância)

---

### 5. **Handler/Orchestrator** - `AvaliacaoVo2MaxHandler`

**O que faz:** Coordena todo fluxo (1→2→3→4→6→7).

**Conceitos Spring:**
- `@Service` - Especialização de `@Component` indicando camada de negócio
- `@Autowired` - Dependency injection de todas as dependências

**Exemplo:**
```java
@Service
public class AvaliacaoVo2MaxHandler {
    private final RequestValidator validator;
    private final AvaliacaoVo2MaxValidator vo2Validator;
    private final AvaliacaoVo2Max estrategia;
    
    @Autowired
    public AvaliacaoVo2MaxHandler(
        RequestValidator validator,
        AvaliacaoVo2MaxValidator vo2Validator,
        AvaliacaoVo2Max estrategia
    ) {
        // Spring injeta automaticamente as 3 dependências
        this.validator = validator;
        this.vo2Validator = vo2Validator;
        this.estrategia = estrategia;
    }
    
    public AvaliacaoVo2MaxResponse processar(AvaliacaoVo2MaxRequest request) {
        // Passo 1: Validar estrutura
        vo2Validator.validarRequisicao(request);  // → ValidacaoException se falhar
        
        // Passo 2: Criar contexto (valida negócio)
        var context = new AvaliacaoVo2MaxContextBuilder()...build();  // → RegraNeggocioException
        
        // Passo 3: Executar estratégia
        Leaf resultado = estrategia.avaliar(context);  // → RecursoNaoEncontradoException
        
        // Passo 4: Converter para response
        return converterParaResponse(resultado, context);
    }
}
```

**Tratamento de exceções:**
```java
try {
    // passo 1-4
} catch (ValidacaoException | RegraNeggocioException e) {
    throw e;  // relança, GlobalExceptionHandler pega
} catch (IllegalArgumentException e) {
    throw new RegraNeggocioException("Erro ao construir: " + e.getMessage());
} catch (Exception e) {
    throw new AvaliacaoException("Erro inesperado: " + e.getMessage(), e);
}
```

---

### 6. **Response Layer** - `AvaliacaoVo2MaxResponse`, `ClassificacaoVO2Max`

**O que faz:** DTOs que formatam dados para HTTP.

**Conceitos:**
- `@JsonProperty` - Jackson mapeia para JSON
- Imutável após construção

**Exemplo:**
```java
@JsonProperty("cliente_id")
private String clienteId;

@JsonProperty("data_avaliacao")
private LocalDateTime dataAvaliacao;

// JSON retornado ao cliente:
{
  "cliente_id": "client-123",
  "avaliador_id": "avaliador-456",
  "classificacao": {
    "nome": "CLASSIFICACAO_NivelForca",
    "descricao": "Resultado da avaliação VO2Max",
    "valor_vo2max": 0.0
  },
  "data_avaliacao": "2026-04-30T11:40:00"
}
```

---

### 7. **Exception Handling Layer** - `GlobalExceptionHandler`

**O que faz:** Converte exceções em respostas HTTP com status correto.

**Conceitos Spring:**
- `@RestControllerAdvice` - Intercepta todas as exceções de `@RestController`
- `@ExceptionHandler` - Mapeia exceção específica para method
- RFC7807 - Standard para error responses

**Mapeamento de Exceções:**

| Exceção | Status | Significa |
|---------|--------|-----------|
| `ValidacaoException` | 400 Bad Request | Entrada HTTP inválida |
| `RegraNeggocioException` | 422 Unprocessable Entity | Pré-requisito de negócio não atendido |
| `RecursoNaoEncontradoException` | 404 Not Found | Tabela/cliente não existe |
| `AvaliacaoException` | 500 Internal Server Error | Erro no processamento |
| `Exception` (genérica) | 500 Internal Server Error | Erro inesperado |

**Exemplo:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RegraNeggocioException.class)
    public ResponseEntity<Object> handleRegraNeggocio(RegraNeggocioException ex, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "https://example.com/problems/business-rule-violation");
        body.put("title", "Business Rule Violation");
        body.put("status", 422);
        body.put("detail", ex.getMessage());
        body.put("instance", request.getRequestURI());
        body.put("timestamp", Instant.now().toString());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/problem+json"));
        return new ResponseEntity<>(body, headers, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
```

---

## 🔄 Fluxo Completo

```
Cliente HTTP
    ↓
[1. HTTP Layer] AvaliacaoController recebe POST /api/avaliacoes/vo2max
    ↓ Jackson desserializa JSON → AvaliacaoVo2MaxRequest
    ↓
[2. Validation Layer] AvaliacaoVo2MaxValidator valida estrutura
    ↓ Se falhar: ValidacaoException (400)
    ↓
[3. Context Builder] AvaliacaoVo2MaxContextBuilder cria contexto
    ↓ Se falhar: IllegalArgumentException → RegraNeggocioException (422)
    ↓
[4. Business Rules] EstrategiaAvaliacaoVo2Max.avaliar() executa lógica
    ↓ Se não encontrar tabela: RecursoNaoEncontradoException (404)
    ↓
[5. Handler] AvaliacaoVo2MaxHandler orquestra 2→3→4 e tratamento
    ↓
[6. Response Layer] Converte Leaf em AvaliacaoVo2MaxResponse
    ↓ Jackson serializa para JSON
    ↓
[7. Exception Handler] GlobalExceptionHandler pega exceções e mapeia status
    ↓
Cliente HTTP recebe response com status correto
```

---

## 🔑 Conceitos Spring Explicados

### Dependency Injection (Injeção de Dependência)

**O que é:** Spring cria e gerencia instâncias de beans automaticamente.

**Exemplo:**
```java
@Service
public class AvaliacaoVo2MaxHandler {
    private final RequestValidator validator;  // dependência
    
    @Autowired  // Spring injeta aqui
    public AvaliacaoVo2MaxHandler(RequestValidator validator) {
        this.validator = validator;
    }
}
```

**Por que:** Desacoplamento - Handler não precisa criar RequestValidator.

### @Component vs @Service vs @RestController

```
┌─ @Component (base)
│  │
│  ├─ @Service (lógica de negócio)
│  │
│  ├─ @Repository (acesso a dados)
│  │
│  └─ @RestController (HTTP endpoints)
```

Todas têm mesmo efeito (bean gerenciado por Spring), mas @Service e @RestController indicam propósito.

### @RequestBody (Desserialização)

**O que faz:**
```java
@PostMapping("/vo2max")
public ResponseEntity<?> avaliarVo2Max(@RequestBody AvaliacaoVo2MaxRequest request) {
    // Spring via Jackson:
    // JSON {"clienteId":"123",...} → AvaliacaoVo2MaxRequest object
}
```

### ResponseEntity (Controle de Response)

**O que faz:**
```java
ResponseEntity<AvaliacaoVo2MaxResponse> response =
    ResponseEntity.ok(resultado);  // 200 OK

ResponseEntity<Map> error =
    ResponseEntity.status(422).body(erro);  // 422 Unprocessable Entity
```

### @RestControllerAdvice (Tratamento Global)

**O que faz:** Intercepta exceções de QUALQUER @RestController:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<?> handle(ValidacaoException ex) {
        // Qualquer ValidacaoException lançada em qualquer controller
        // vem aqui
    }
}
```

---

## ✅ Checklist de Responsabilidades

| Camada | Responsável por | NÃO responsável por |
|--------|-----------------|-------------------|
| HTTP | Mapear request/response HTTP | Lógica de validação/negócio |
| Validator | Estrutura (tamanho, presença, tipo) | Regras de negócio |
| Builder | Criar contexto, validar negócio | Lógica de avaliação |
| Strategy | Implementar algoritmo | Persister resultado |
| Handler | Orquestrar fluxo 1-6 | Nenhuma validação direta |
| Response | Formatar para HTTP | Transformar dado bruto |
| Exception Handler | Mapear exceção para HTTP | Criar exceção |

---

## 📊 Validação vs Negócio

**Validação (Camada 2):**
```
- ID não é nulo?
- ID tem <= 36 caracteres?
- Lista de testes não é vazia?
- Quantidade de testes <= 1000?
```

**Negócio (Camada 3):**
```
- Frequência máxima > frequência repouso?
- Medicao tem tabelaClassificacaoId?
- Testes são do tipo correto (TesteVo2Max)?
- Tabela existe no banco?
```

---

## 🚀 Próximos Passos

1. **Atualizar Controller** para usar o novo handler
2. **Criar testes unitários** para cada camada (mockable)
3. **Implementar IMC** seguindo mesmo padrão
4. **Implementar Bioimpedância** seguindo mesmo padrão
5. **Adicionar logs** com SLF4J/Logback em cada camada

---

## 📝 Notas

- **Type-safety:** Impossible passar `TesteImc` para VO2Max (compile error)
- **Testable:** Cada camada pode ser testada independently
- **Extensível:** Novo tipo de avaliação? Copy pattern (Validator, Builder, Strategy)
- **Maintainable:** Responsabilidade clara, fácil encontrar onde bug está

