# 🏗️ Arquitetura Completa: Avaliação VO2Max com Separação de Responsabilidades

## 📊 Visão Geral das Camadas

```
┌─────────────────────────────────────────────────────────────────┐
│ HTTP Request                                                    │
│ POST /api/avaliacoes/vo2max                                     │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                    ┌───────▼────────┐
                    │   CONTROLLER   │
                    │ (Roteamento)   │
                    └───────┬────────┘
                            │ (dto request)
           ┌────────────────▼─────────────────┐
           │  1. VALIDATION LAYER             │
           │  (Validar Entrada)               │
           │  ├─ AvaliacaoVo2MaxValidator     │
           │  │  └─ validarRequest()          │
           │  └─ RequestValidator             │
           │     └─ validarCampos()           │
           └───────┬────────────────────────┘
                   │ (se válido → contexto)
           ┌───────▼────────────────────────┐
           │  2. CONTEXT BUILDER             │
           │  (Criar Contexto Tipado)        │
           │  └─ AvaliacaoVo2MaxContextBuilder
           │     └─ build() ← Valida tipos   │
           └───────┬────────────────────────┘
                   │ (contexto tipado)
           ┌───────▼────────────────────────────┐
           │  3. BUSINESS RULES LAYER           │
           │  (Regras de Negócio)               │
           │  ├─ RegrasNegocioAvaliacaoVo2Max   │
           │  │  ├─ validarPré-requisitos()     │
           │  │  ├─ calcularClassificacao()     │
           │  │  └─ aplicarRegras()             │
           │  └─ EstrategiaAvaliacaoVo2Max      │
           │     └─ avaliar(testes, tabela)     │
           └───────┬────────────────────────┘
                   │ (resultado)
           ┌───────▼────────────────────────┐
           │  4. HANDLER LAYER              │
           │  (Orquestração)                │
           │  └─ AvaliacaoVo2MaxHandler    │
           │     ├─ validar()              │
           │     ├─ avaliar()              │
           │     └─ persistir()            │
           └───────┬────────────────────┘
                   │ (response)
           ┌───────▼────────────────────┐
           │  5. RESPONSE LAYER         │
           │  (Formatação Saída)        │
           │  ├─ AvaliacaoVo2MaxResponse│
           │  └─ ClassificacaoResponse  │
           └───────┬────────────────────┘
                   │ (JSON)
                   ▼
         ResponseEntity<AvaliacaoVo2MaxResponse>
                   │
        ┌──────────▼────────────┐
        │ 200 OK + Response      │
        │ ou                     │
        │ 400 Bad Request        │
        │ ou                     │
        │ 500 Internal Error     │
        └────────────────────────┘
```

---

## 🔍 Camada 1: VALIDATION (Validação de Entrada)

### Responsabilidade
- Validar dados de entrada do usuário
- Garantir que todos os campos obrigatórios estão presentes
- Validar formatos (email, telefone, etc)
- Lançar exceções específicas se dados inválidos

### Classes

#### `RequestValidator.java` (Genérico)
```java
@Component
public class RequestValidator {
    
    // Validações genéricas reutilizáveis
    public void validarStringNaoVazia(String valor, String nomeCampo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("Campo obrigatório: " + nomeCampo);
        }
    }
    
    public void validarObjetoNaoNulo(Object objeto, String nomeCampo) {
        if (objeto == null) {
            throw new ValidacaoException("Campo obrigatório: " + nomeCampo);
        }
    }
    
    public void validarListaNaoVazia(List<?> lista, String nomeCampo) {
        if (lista == null || lista.isEmpty()) {
            throw new ValidacaoException("Lista não pode estar vazia: " + nomeCampo);
        }
    }
}
```

#### `AvaliacaoVo2MaxValidator.java` (Específico VO2Max)
```java
@Component
public class AvaliacaoVo2MaxValidator {
    
    private final RequestValidator requestValidator;
    
    public void validarRequest(AvaliacaoVo2MaxRequest request) {
        // Validar campos obrigatórios
        requestValidator.validarStringNaoVazia(request.getClienteId(), "clienteId");
        requestValidator.validarStringNaoVazia(request.getAvaliadorId(), "avaliadorId");
        requestValidator.validarObjetoNaoNulo(request.getMedicaoVo2Max(), "medicaoVo2Max");
        requestValidator.validarListaNaoVazia(request.getTestes(), "testes");
        
        // Validar dados da medicao
        validarMedicao(request.getMedicaoVo2Max());
        
        // Validar testes
        validarTestes(request.getTestes());
    }
    
    private void validarMedicao(MedicaoVo2 medicao) {
        requestValidator.validarStringNaoVazia(
            medicao.getTabelaClassificacaoId(),
            "medicao.tabelaClassificacaoId"
        );
    }
    
    private void validarTestes(List<?> testes) {
        for (int i = 0; i < testes.size(); i++) {
            Object teste = testes.get(i);
            if (!(teste instanceof TesteVo2Max)) {
                throw new ValidacaoException(
                    "Teste na posição " + i + " não é TesteVo2Max"
                );
            }
        }
    }
}
```

---

## 💼 Camada 2: BUSINESS RULES (Regras de Negócio)

### Responsabilidade
- Implementar lógica de classificação
- Validar regras de negócio
- Calcular resultados
- NÃO deve conhecer HTTP, Controllers, etc

### Classes

#### `RegrasNegocioAvaliacaoVo2Max.java`
```java
@Service
public class RegrasNegocioAvaliacaoVo2Max {
    
    /**
     * Valida pré-requisitos da avaliação
     */
    public void validarPreRequisitos(AvaliacaoVo2MaxContext contexto) {
        // Ex: idade mínima, sexo válido, etc
        if (contexto.getMedicao().getIdade() < 18) {
            throw new RegraNeggocioException("Cliente deve ter pelo menos 18 anos");
        }
    }
    
    /**
     * Aplica regras de negócio específicas
     */
    public boolean aplicarRegras(Leaf resultado, AvaliacaoVo2MaxContext contexto) {
        // Ex: se resultado é "Fraco" mas cliente é atleta, reclassificar?
        
        return true; // Regra aplicada com sucesso
    }
    
    /**
     * Calcula classificação final
     */
    public ClassificacaoVO2Max calcularClassificacao(
        Leaf resultado,
        AvaliacaoVo2MaxContext contexto
    ) {
        return ClassificacaoVO2Max.builder()
            .classificacao(resultado.getClassificacao())
            .minimo(resultado.getMin())
            .maximo(resultado.getMax())
            .descricao(gerarDescricao(resultado))
            .recomendacoes(gerarRecomendacoes(resultado))
            .build();
    }
    
    private String gerarDescricao(Leaf resultado) {
        // Descrever o resultado em linguagem natural
        switch (resultado.getClassificacao()) {
            case "Excelente":
                return "Capacidade cardiorrespiratória excelente";
            case "Bom":
                return "Capacidade cardiorrespiratória boa";
            // ...
            default:
                return "";
        }
    }
}
```

---

## 🎯 Camada 3: STRATEGY (Estratégia de Avaliação)

### Responsabilidade
- Implementar lógica específica de classificação
- Usar tabelas e testes já carregados
- Retornar resultado (Leaf)

### Classe

#### `EstrategiaAvaliacaoVo2Max.java`
```java
@Service
public class EstrategiaAvaliacaoVo2Max {
    
    private final MongoTabelaClassificacaoDataRepository tabelaRepository;
    private final PersistedComponentMapper componentMapper;
    
    public Leaf avaliar(List<TesteVo2Max> testes, String tabelaId) {
        // Buscar tabela
        var tabelaDoc = tabelaRepository.findById(tabelaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Tabela não encontrada"));
        
        // Converter para domain
        var tabela = componentMapper.toDomain(tabelaDoc.getRaiz());
        
        // Classificar com cada teste
        for (TesteVo2Max teste : testes) {
            var resultado = tabela.classificarComTeste(teste);
            if (resultado != null) {
                return resultado;
            }
        }
        
        return null;
    }
}
```

---

## 🔗 Camada 4: HANDLER (Orquestração)

### Responsabilidade
- Orquestrar fluxo completo
- Coordenar validação → negócio → persistência
- Tratar exceções
- Retornar resultado

### Classe

#### `AvaliacaoVo2MaxHandler.java`
```java
@Service
public class AvaliacaoVo2MaxHandler {
    
    private final AvaliacaoVo2MaxValidator validator;
    private final AvaliacaoVo2MaxContextBuilder contextBuilder;
    private final RegrasNegocioAvaliacaoVo2Max regrasNegocio;
    private final EstrategiaAvaliacaoVo2Max estrategia;
    private final AvaliacaoService avaliacaoService;
    
    public AvaliacaoVo2MaxResponse processar(AvaliacaoVo2MaxRequest request) {
        try {
            // 1. Validar entrada
            validator.validarRequest(request);
            
            // 2. Criar contexto tipado
            var contexto = contextBuilder
                .comCliente(request.getClienteId())
                .comAvaliador(request.getAvaliadorId())
                .comMedicao(request.getMedicaoVo2Max())
                .comTestes(request.getTestes())
                .build();
            
            // 3. Validar regras de negócio
            regrasNegocio.validarPreRequisitos(contexto);
            
            // 4. Avaliar
            Leaf resultado = estrategia.avaliar(
                contexto.getTestes(),
                contexto.getTabelaClassificacaoId()
            );
            
            if (resultado == null) {
                throw new AvaliacaoException("Nenhuma classificação encontrada");
            }
            
            // 5. Aplicar regras
            regrasNegocio.aplicarRegras(resultado, contexto);
            
            // 6. Calcular classificação final
            var classificacao = regrasNegocio.calcularClassificacao(resultado, contexto);
            
            // 7. Persistir
            avaliacaoService.salvarAvaliacao(contexto, resultado);
            
            // 8. Retornar resposta
            return AvaliacaoVo2MaxResponse.builder()
                .sucesso(true)
                .classificacao(classificacao)
                .timestamp(Instant.now())
                .build();
                
        } catch (ValidacaoException e) {
            throw e; // Controller converte em 400
        } catch (RegraNeggocioException e) {
            throw e; // Controller converte em 422
        } catch (Exception e) {
            throw new AvaliacaoException("Erro ao processar avaliação", e);
        }
    }
}
```

---

## 📤 Camada 5: RESPONSE (Resposta)

### Responsabilidade
- Formatar resposta para o cliente
- Incluir metadados (timestamp, sucesso, etc)

### Classes

#### `AvaliacaoVo2MaxResponse.java`
```java
@Data
@Builder
public class AvaliacaoVo2MaxResponse {
    private boolean sucesso;
    private ClassificacaoVO2Max classificacao;
    private Instant timestamp;
    private String mensagem;
}
```

#### `ClassificacaoVO2Max.java`
```java
@Data
@Builder
public class ClassificacaoVO2Max {
    private String classificacao;  // Ex: "Bom"
    private double minimo;         // Ex: 40.0
    private double maximo;         // Ex: 50.0
    private String descricao;      // Ex: "Capacidade cardiorrespiratória boa"
    private List<String> recomendacoes;
}
```

---

## 🎮 Camada 6: CONTROLLER (HTTP)

### Responsabilidade
- Receber requisições HTTP
- Delegar para Handler
- Retornar ResponseEntity com status correto

### Classe

#### `AvaliacaoVo2MaxController.java`
```java
@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoVo2MaxController {
    
    private final AvaliacaoVo2MaxHandler handler;
    
    @PostMapping("/vo2max")
    public ResponseEntity<AvaliacaoVo2MaxResponse> avaliarVo2Max(
        @RequestBody AvaliacaoVo2MaxRequest request
    ) {
        var response = handler.processar(request);
        return ResponseEntity.ok(response);
    }
}
```

---

## 🚨 Camada 7: EXCEPTION HANDLING (Tratamento de Erros)

### Exceções Customizadas

```java
// Validação
public class ValidacaoException extends RuntimeException { }

// Negócio
public class RegraNeggocioException extends RuntimeException { }

// Recurso não encontrado
public class RecursoNaoEncontradoException extends RuntimeException { }

// Avaliação
public class AvaliacaoException extends RuntimeException { }
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<?> handleValidacao(ValidacaoException e) {
        return ResponseEntity.badRequest().body(
            ErrorResponse.builder()
                .erro("Validação falhou")
                .mensagem(e.getMessage())
                .status(400)
                .build()
        );
    }
    
    @ExceptionHandler(RegraNeggocioException.class)
    public ResponseEntity<?> handleRegraNegocio(RegraNeggocioException e) {
        return ResponseEntity.status(422).body(
            ErrorResponse.builder()
                .erro("Regra de negócio violada")
                .mensagem(e.getMessage())
                .status(422)
                .build()
        );
    }
}
```

---

## 📊 Fluxo Completo com Exemplos

### Request
```json
POST /api/avaliacoes/vo2max
{
  "clienteId": "cliente-123",
  "avaliadorId": "avaliador-456",
  "medicaoVo2Max": {
    "tabelaClassificacaoId": "tabela-vo2max-1",
    "testes": []
  },
  "testes": [
    {
      "protocolo": "COOPER",
      "resultado": 50.0
    }
  ]
}
```

### Fluxo Interno

```
1. Controller recebe request
   ↓
2. AvaliacaoVo2MaxValidator.validarRequest()
   ├─ Valida clienteId não vazio? ✅
   ├─ Valida avaliadorId não vazio? ✅
   ├─ Valida medicaoVo2Max não nula? ✅
   ├─ Valida testes não vazio? ✅
   └─ Se falhar → ValidacaoException (400)
   ↓
3. AvaliacaoVo2MaxContextBuilder.build()
   ├─ Cria contexto tipado? ✅
   ├─ Testes são TesteVo2Max? ✅
   └─ Se falhar → IllegalArgumentException (400)
   ↓
4. RegrasNegocioAvaliacaoVo2Max.validarPreRequisitos()
   ├─ Cliente >= 18 anos? ✅
   └─ Se falhar → RegraNeggocioException (422)
   ↓
5. EstrategiaAvaliacaoVo2Max.avaliar()
   ├─ Busca tabela? ✅
   ├─ Classifica com testes? ✅
   └─ Se falhar → RecursoNaoEncontradoException (404)
   ↓
6. RegrasNegocioAvaliacaoVo2Max.aplicarRegras()
   ├─ Aplica lógica adicional? ✅
   ↓
7. RegrasNegocioAvaliacaoVo2Max.calcularClassificacao()
   ├─ Gera descrição? ✅
   ├─ Gera recomendações? ✅
   ↓
8. AvaliacaoService.salvarAvaliacao()
   ├─ Persiste em banco? ✅
   ↓
9. Retorna AvaliacaoVo2MaxResponse
   ↓
200 OK + JSON
```

### Response
```json
200 OK
{
  "sucesso": true,
  "classificacao": {
    "classificacao": "Bom",
    "minimo": 40,
    "maximo": 50,
    "descricao": "Capacidade cardiorrespiratória boa",
    "recomendacoes": [
      "Manter programa de atividade física",
      "Realizar testes periódicos"
    ]
  },
  "timestamp": "2024-04-30T10:30:00Z"
}
```

---

## 🌱 Spring Concepts Explicados

### 1. @Component
```java
@Component
public class RequestValidator { }
```
- Marca classe para ser gerenciada pelo Spring
- Spring cria instância única (singleton)
- Injeta em outras classes via @Autowired

### 2. @Service
```java
@Service
public class RegrasNegocioAvaliacaoVo2Max { }
```
- Especialização de @Component
- Indica que é um serviço de negócio
- Spring gerencia instância

### 3. @Autowired (Injeção de Dependência)
```java
@Service
public class AvaliacaoVo2MaxHandler {
    
    @Autowired
    private AvaliacaoVo2MaxValidator validator;
    
    // Spring injecta automaticamente
}
```
- Spring procura bean do tipo `AvaliacaoVo2MaxValidator`
- Injeta quando AvaliacaoVo2MaxHandler é criado
- Permite teste fácil (mockar validator)

### 4. @RestController
```java
@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoVo2MaxController { }
```
- Marca como controller HTTP
- @RequestMapping define rota base
- Métodos com @PostMapping, @GetMapping, etc

### 5. @RequestBody
```java
@PostMapping("/vo2max")
public ResponseEntity<?> avaliarVo2Max(
    @RequestBody AvaliacaoVo2MaxRequest request
) { }
```
- Spring desserializa JSON em objeto Java
- Usa Jackson library
- Falha automaticamente se JSON inválido (400)

### 6. ResponseEntity
```java
return ResponseEntity.ok(response);
return ResponseEntity.badRequest().body(error);
return ResponseEntity.status(422).body(error);
```
- Controla HTTP status e body
- ok() = 200
- badRequest() = 400
- status(N) = código customizado

### 7. @RestControllerAdvice
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<?> handle(ValidacaoException e) {
        return ResponseEntity.badRequest().body(...);
    }
}
```
- Intercepta exceções globalmente
- Trata diferentes tipos diferentemente
- Retorna resposta formatada

---

## 📝 Checklist de Implementação

- [ ] 1. Criar exceções customizadas
- [ ] 2. Criar RequestValidator
- [ ] 3. Criar AvaliacaoVo2MaxValidator
- [ ] 4. Criar RegrasNegocioAvaliacaoVo2Max
- [ ] 5. Criar EstrategiaAvaliacaoVo2Max
- [ ] 6. Criar AvaliacaoVo2MaxHandler
- [ ] 7. Criar DTOs de Response
- [ ] 8. Atualizar AvaliacaoVo2MaxController
- [ ] 9. Criar GlobalExceptionHandler
- [ ] 10. Compilar e validar
- [ ] 11. Documentar cada camada

---

## 🎯 Próximas Etapas

1. Implementar todas as camadas (este documento)
2. Validar compilação
3. Criar testes unitários para cada camada
4. Repetir padrão para IMC e Bioimpedancia
5. Integração com banco de dados
