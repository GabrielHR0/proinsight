# 🏗️ Diagrama de Implementação: Contextos Tipados

## 📊 Antes vs Depois (Visual)

### ❌ ANTES: Dados Misturados

```
HTTP Request (VO2Max)
         ↓
    AvaliacaoFisicaController
         ↓
    AvaliacaoFisica {
        clienteId: "123",
        avaliadorId: "456",
        medicoes: [VO2Max, IMC, Bioimpedancia]  ← Tudo junto!
        strategyKey: "VO2_MAX"
    }
         ↓
    AvaliacaoService.avaliar()
         ↓
    AvaliacaoVo2Max.avaliar(AvaliacaoFisica) {
        // Precisa filtrar:
        medicao = medicoes.filter(tipo == VO2_MAX)  ← Runtime!
        testes = medicao.getTestes()
    }
         ↓
    ❌ PROBLEMAS:
    - Sem type-safety
    - Filtro em runtime
    - Pode passar dados errados
    - Difícil testar
```

---

### ✅ DEPOIS: Contextos Tipados

```
HTTP Request (VO2Max)
         ↓ (JSON)
    AvaliacaoVo2MaxRequest {
        clienteId: "123"
        avaliadorId: "456"
        medicaoVo2Max: MedicaoVo2 { ... }
        testes: List<TesteVo2Max> { ... }
    }
         ↓ (Builder com validação)
    AvaliacaoVo2MaxContextBuilder.build()
    ├─ Valida clienteId? ✅
    ├─ Valida avaliadorId? ✅
    ├─ Medicao é MedicaoVo2? ✅
    ├─ TabelaId existe? ✅
    ├─ Testes são TesteVo2Max? ✅
    └─ Lança IllegalArgumentException se falhar
         ↓ (Se passou validação)
    AvaliacaoVo2MaxContext {
        clienteId: "123"
        avaliadorId: "456"
        medicao: MedicaoVo2 { ... }     ← Específico!
        testes: List<TesteVo2Max> { ... }  ← Específico!
    }
         ↓ (Type-safe)
    AvaliacaoVo2Max.avaliar(AvaliacaoVo2MaxContext) {
        // Sem filtros, sem casts!
        tabela = repo.findById(contexto.getTabelaClassificacaoId());
        for (TesteVo2Max teste : contexto.getTestes()) {
            resultado = tabela.classificarComTeste(teste);
        }
    }
         ↓
    Leaf resultado
         ↓
    AvaliacaoService.salvarAvaliacao(contexto, resultado)
         ↓
    ✅ RESULTADO OK + 200
```

---

## 🔗 Mapa de Arquivos

```
src/main/java/com/prosup/proinsight/

domain/
└── avalicao_strategy/
    ├── AvaliacaoContext.java              ← Interface genérica
    │   public interface AvaliacaoContext<M, T>
    │   - getClienteId()
    │   - getAvaliadorId()
    │   - getMedicao()
    │   - getTestes()
    │   - getTabelaClassificacaoId()
    │
    ├── AvaliacaoVo2MaxContext.java        ← Implementação VO2Max
    │   public class AvaliacaoVo2MaxContext
    │       implements AvaliacaoContext<MedicaoVo2, TesteVo2Max>
    │   - clienteId
    │   - avaliadorId
    │   - medicao (MedicaoVo2)
    │   - testes (List<TesteVo2Max>)
    │   - equals(), hashCode(), toString()
    │
    ├── AvaliacaoVo2MaxContextBuilder.java ← Builder com validação
    │   public class AvaliacaoVo2MaxContextBuilder
    │   - comCliente(String) → this
    │   - comAvaliador(String) → this
    │   - comMedicao(MedicaoVo2) → this (valida não nulo)
    │   - comTestes(List<TesteVo2Max>) → this (valida não vazio)
    │   - build() → AvaliacaoVo2MaxContext (valida tudo)
    │
    ├── AvaliacaoStrategy.java             ← Strategy genérica
    │   public interface AvaliacaoStrategy<C extends AvaliacaoContext>
    │   - Leaf avaliar(C contexto)
    │
    └── AvaliacaoVo2Max.java               ← Implementação VO2Max
        public class AvaliacaoVo2Max
            implements AvaliacaoStrategy<AvaliacaoVo2MaxContext>
        - avaliar(AvaliacaoVo2MaxContext contexto)
          ├─ Busca tabela pelo ID
          ├─ Classifica com cada teste
          └─ Retorna Leaf ou null

controller/
└── AvaliacaoController.java              ← HTTP Endpoint
    public class AvaliacaoController
    @PostMapping("/api/avaliacoes/vo2max")
    └─ avaliarVo2Max(AvaliacaoVo2MaxRequest request)
       ├─ Build contexto (validação)
       ├─ Avaliar
       ├─ Salvar
       └─ Retorna 200 ou 400

service/
└── AvaliacaoService.java                 ← Serviço genérico
    public class AvaliacaoService
    - salvarAvaliacao(AvaliacaoContext<M,T>, Leaf)

dto/request/
└── AvaliacaoVo2MaxRequest.java           ← DTO
    public class AvaliacaoVo2MaxRequest
    - clienteId
    - avaliadorId
    - medicaoVo2Max (MedicaoVo2)
    - testes (List<TesteVo2Max>)
```

---

## 🔄 Fluxo de Dados Passo-a-Passo

### Passo 1: Receber Request

```json
POST /api/avaliacoes/vo2max
{
  "clienteId": "cliente-123",
  "avaliadorId": "avaliador-456",
  "medicaoVo2Max": {
    "tabelaClassificacaoId": "tabela-xyz",
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

### Passo 2: Desserializar em AvaliacaoVo2MaxRequest

```java
AvaliacaoVo2MaxRequest request = objectMapper.readValue(json, AvaliacaoVo2MaxRequest.class);
// request.clienteId = "cliente-123"
// request.avaliadorId = "avaliador-456"
// request.medicaoVo2Max = MedicaoVo2(...)
// request.testes = List<TesteVo2Max>(...)
```

### Passo 3: Build Contexto com Validação

```java
AvaliacaoVo2MaxContext contexto = new AvaliacaoVo2MaxContextBuilder()
    .comCliente(request.getClienteId())           // ✅ "cliente-123"
    .comAvaliador(request.getAvaliadorId())       // ✅ "avaliador-456"
    .comMedicao(request.getMedicaoVo2Max())       // ✅ MedicaoVo2 (valida não nulo)
    .comTestes(request.getTestes())               // ✅ List<TesteVo2Max> (valida não vazio)
    .build();                                      // ✅ Tudo validado!

// Se algo falhar:
// IllegalArgumentException: "ClienteId é obrigatório e não pode ser vazio"
// → ResponseEntity.badRequest().body(mensagem)
// → 400 Bad Request
```

### Passo 4: Executar Avaliação

```java
Leaf resultado = avaliacaoVo2Max.avaliar(contexto);
// Dentro de AvaliacaoVo2Max.avaliar():
// - Busca tabela: repo.findById(contexto.getTabelaClassificacaoId())
// - Para cada teste: tabela.classificarComTeste(teste)
// - Retorna primeiro resultado não nulo
```

### Passo 5: Salvar Resultado

```java
avaliacaoService.salvarAvaliacao(contexto, resultado);
// Salva em banco:
// - clienteId
// - avaliadorId
// - resultado (Leaf)
// - timestamp
```

### Passo 6: Retornar Resposta

```json
200 OK
{
  "classificacao": "Excelente",
  "min": 40,
  "max": 50
}
```

---

## 🎯 Type-Safety em Ação

### ❌ Impossível (tipo errado)

```java
// Código que NÃO COMPILA:
AvaliacaoVo2MaxContext contexto = new AvaliacaoVo2MaxContextBuilder()
    .comMedicao(medicaoIMC)  // ❌ ERRO: MedicaoIMC não é MedicaoVo2
    .build();
```

**Erro do compilador**:
```
error: incompatible types: MedicaoIMC cannot be converted to MedicaoVo2
```

### ❌ Impossível (tipo de teste errado)

```java
List<TesteIMC> testes = List.of(new TesteIMC(...));

AvaliacaoVo2MaxContext contexto = new AvaliacaoVo2MaxContextBuilder()
    .comTestes(testes)  // ❌ ERRO: TesteIMC não é TesteVo2Max
    .build();
```

**Erro do compilador**:
```
error: incompatible types: List<TesteIMC> cannot be converted to List<TesteVo2Max>
```

### ✅ Possível (tipo correto)

```java
MedicaoVo2 medicao = new MedicaoVo2(...);
List<TesteVo2Max> testes = List.of(new TesteVo2MaxCooper(...));

AvaliacaoVo2MaxContext contexto = new AvaliacaoVo2MaxContextBuilder()
    .comMedicao(medicao)    // ✅ OK: MedicaoVo2
    .comTestes(testes)      // ✅ OK: List<TesteVo2Max>
    .build();               // ✅ Compila!
```

---

## 🚀 Adicionando IMC em 5 Minutos

### 1. Criar Context

```java
public class AvaliacaoIMCContext implements AvaliacaoContext<MedicaoIMC, TesteIMC> {
    private final String clienteId;
    private final String avaliadorId;
    private final MedicaoIMC medicao;
    private final List<TesteIMC> testes;
    // ... (mesmo que VO2Max)
}
```

### 2. Criar Builder

```java
public class AvaliacaoIMCContextBuilder {
    // ... (mesma estrutura que VO2Max)
    public AvaliacaoIMCContext build() { ... }
}
```

### 3. Criar Strategy

```java
public class AvaliacaoIMC implements AvaliacaoStrategy<AvaliacaoIMCContext> {
    @Override
    public Leaf avaliar(AvaliacaoIMCContext contexto) {
        // Lógica específica de IMC
    }
}
```

### 4. Criar DTO

```java
public class AvaliacaoIMCRequest {
    String clienteId;
    String avaliadorId;
    MedicaoIMC medicaoIMC;
    List<TesteIMC> testes;
}
```

### 5. Adicionar Endpoint

```java
@PostMapping("/imc")
public ResponseEntity<?> avaliarIMC(@RequestBody AvaliacaoIMCRequest request) {
    var contexto = new AvaliacaoIMCContextBuilder()
        .comCliente(request.getClienteId())
        .comAvaliador(request.getAvaliadorId())
        .comMedicao(request.getMedicaoIMC())
        .comTestes(request.getTestes())
        .build();
    
    Leaf resultado = avaliacaoIMC.avaliar(contexto);
    avaliacaoService.salvarAvaliacao(contexto, resultado);
    return ResponseEntity.ok(resultado);
}
```

**Pronto! Sem dependências, sem quebra do código existente.**

---

## 📈 Escalabilidade

| Tipo | Context | Builder | Strategy | DTO | Endpoint | Total |
|------|---------|---------|----------|-----|----------|-------|
| VO2Max | 100 linhas | 120 linhas | 50 linhas | 50 linhas | 40 linhas | 360 linhas |
| IMC | 100 linhas | 120 linhas | 50 linhas | 50 linhas | 40 linhas | 360 linhas |
| Bioimpedancia | 100 linhas | 120 linhas | 50 linhas | 50 linhas | 40 linhas | 360 linhas |

**Padrão**: Novo tipo = ~360 linhas de código boilerplate (copiável)

---

## ✅ Validações Garantidas

1. ✅ ClienteId não vazio
2. ✅ AvaliadorId não vazio
3. ✅ Medicao não nula
4. ✅ TabelaId não vazio
5. ✅ Testes não vazio
6. ✅ Tipo correto de medicao (MedicaoVo2)
7. ✅ Tipo correto de testes (TesteVo2Max)

**Resultado**: Se `build()` sucede, 100% dos dados estão válidos!

---

## 💾 Imutabilidade

```java
AvaliacaoVo2MaxContext contexto = builder.build();

// Tentativa de modificar:
contexto.getTestes().clear();  // ❌ Falha!
// UnsupportedOperationException: 
// because List.copyOf() retorna lista imutável

// Thread-safe garantido
// Posso passar para múltiplas threads sem worry
```

---

## 🎓 Padrões Utilizados

1. **Builder Pattern**: Validação fluente e legível
2. **Generic Types**: Type-safety em compile-time
3. **Immutability**: Dados seguros após construção
4. **Strategy Pattern**: Interfaces genéricas
5. **Composition**: Context encapsula dados
6. **Factory Method**: Builder é factory

---

## 📞 Como Usar na Prática

### Cenário 1: Avaliar VO2Max

```java
// Request chega do cliente
AvaliacaoVo2MaxRequest request = ...;

// Build contexto
AvaliacaoVo2MaxContext contexto = new AvaliacaoVo2MaxContextBuilder()
    .comCliente(request.getClienteId())
    .comAvaliador(request.getAvaliadorId())
    .comMedicao(request.getMedicaoVo2Max())
    .comTestes(request.getTestes())
    .build();

// Avaliar
Leaf resultado = avaliacaoVo2Max.avaliar(contexto);

// Salvar
avaliacaoService.salvarAvaliacao(contexto, resultado);
```

### Cenário 2: Teste Unitário

```java
@Test
public void testAvaliacaoVo2Max() {
    var medicao = new MedicaoVo2("obs", Collections.emptyList());
    medicao.setTabelaClassificacaoId("tabela-123");
    
    var testes = List.of(new TesteVo2MaxCooper(ProtocoloVo2Max.COOPER, 50.0));
    
    var contexto = new AvaliacaoVo2MaxContextBuilder()
        .comCliente("c1")
        .comAvaliador("a1")
        .comMedicao(medicao)
        .comTestes(testes)
        .build();
    
    Leaf resultado = avaliacaoVo2Max.avaliar(contexto);
    assertNotNull(resultado);
}
```

---

## ✨ Status Final

✅ **Implementado**: Context, Builder, Strategy, Controller  
✅ **Compilado**: Sem erros  
✅ **Type-safe**: Garantido pelo compilador  
✅ **Testável**: Fácil de mockar/testar  
✅ **Escalável**: Novo tipo em 20 minutos  
✅ **Produção**: Pronto!  

Próximo passo: Implementar IMC e Bioimpedancia seguindo o mesmo padrão! 🚀
