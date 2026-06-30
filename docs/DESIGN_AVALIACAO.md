# 📊 Design: Fluxo de Dados na Avaliação

## 🔴 Problema Atual

```
AvaliacaoFisica {
  medicoes: [VO2Max, IMC, Bioimpedancia]  ← Tudo junto!
  estrategyKey: "VO2_MAX"
}
        ↓
AvaliacaoVo2Max.avaliar() {
  // Precisa filtrar:
  var medicao = medicoes.filter(tipo == VO2_MAX)
  // Precisa confiar que os testes estão certos
  // Precisa confiar que tabelaId é do VO2Max
}
```

**Problemas**:
- ❌ Dados misturados (VO2Max com IMC?)
- ❌ Sem type-safety (compilador não valida)
- ❌ Runtime errors (medicação VO2Max não encontrada)
- ❌ Difícil testar (precisa montar toda estrutura)
- ❌ Acoplamento alto (Strategy precisa conhecer estrutura completa)

---

## ✅ Solução: Contexto Tipado por Avaliação

A ideia: **Criar um objeto específico para cada tipo de avaliação** que encapsule:
1. ✅ Apenas os dados relevantes
2. ✅ Type-safe (compilador valida)
3. ✅ Testável (simples de montar)
4. ✅ Sem acoplamento (Strategy não precisa filtrar)

### Arquitetura

```
┌─────────────────────────────────────────────────────┐
│ HTTP Request (Controller)                           │
│ POST /avaliacoes/vo2max                             │
│ {                                                   │
│   clienteId: "123",                                │
│   avaliadorId: "456",                              │
│   vo2maxData: {                                     │
│     tabelaClassificacaoId: "xyz",                  │
│     testes: [TesteCooper(50)]                      │
│   }                                                │
│ }                                                  │
└────────────────┬────────────────────────────────────┘
                 │ (DTO)
                 ▼
┌─────────────────────────────────────────────────────┐
│ Factory / Builder                                   │
│ AvaliacaoVo2MaxContextBuilder                      │
└────────────────┬────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────┐
│ NOVO: Contexto Tipado                               │
│ AvaliacaoVo2MaxContext {                            │
│   - clienteId: String                              │
│   - avaliadorId: String                            │
│   - medicao: MedicaoVo2Max (específica!)          │
│   - testes: List<TesteVo2Max> (específico!)       │
│   - tabelaId: String (validado)                   │
│ }                                                  │
└────────────────┬────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────┐
│ AvaliacaoStrategy<AvaliacaoVo2MaxContext>          │
│ AvaliacaoVo2Max.avaliar(context) {                 │
│   // Sem filtros! Já tem exatamente o que precisa │
│   return tabelaRepository.findById(context...)     │
│     .classificarComTeste(context.getTestes())      │
│ }                                                  │
└────────────────┬────────────────────────────────────┘
                 │
                 ▼
          Resultado: Leaf
```

---

## 🏗️ Implementação

### Passo 1: Criar Context Genérico

```java
public interface AvaliacaoContext<M extends Medicao, T extends Teste> {
    String getClienteId();
    String getAvaliadorId();
    M getMedicao();
    List<T> getTestes();
    String getTabelaClassificacaoId();
}
```

### Passo 2: Implementação VO2Max

```java
public class AvaliacaoVo2MaxContext implements AvaliacaoContext<MedicaoVo2Max, TesteVo2Max> {
    
    private final String clienteId;
    private final String avaliadorId;
    private final MedicaoVo2Max medicao;      // Já filtrada!
    private final List<TesteVo2Max> testes;   // Já tipada!
    
    // Construtor privado (usar builder)
    private AvaliacaoVo2MaxContext(
        String clienteId,
        String avaliadorId,
        MedicaoVo2Max medicao,
        List<TesteVo2Max> testes
    ) {
        this.clienteId = clienteId;
        this.avaliadorId = avaliadorId;
        this.medicao = medicao;
        this.testes = testes;
    }
    
    @Override
    public String getClienteId() { return clienteId; }
    
    @Override
    public String getAvaliadorId() { return avaliadorId; }
    
    @Override
    public MedicaoVo2Max getMedicao() { return medicao; }
    
    @Override
    public List<TesteVo2Max> getTestes() { return testes; }
    
    @Override
    public String getTabelaClassificacaoId() {
        return medicao.getTabelaClassificacaoId();
    }
}
```

### Passo 3: Builder com Validação

```java
public class AvaliacaoVo2MaxContextBuilder {
    
    private String clienteId;
    private String avaliadorId;
    private MedicaoVo2Max medicao;
    private List<TesteVo2Max> testes;
    
    public AvaliacaoVo2MaxContextBuilder comCliente(String clienteId) {
        this.clienteId = clienteId;
        return this;
    }
    
    public AvaliacaoVo2MaxContextBuilder comAvaliador(String avaliadorId) {
        this.avaliadorId = avaliadorId;
        return this;
    }
    
    public AvaliacaoVo2MaxContextBuilder comMedicao(MedicaoVo2Max medicao) {
        if (medicao == null) {
            throw new IllegalArgumentException("Medicao não pode ser nula");
        }
        this.medicao = medicao;
        return this;
    }
    
    public AvaliacaoVo2MaxContextBuilder comTestes(List<TesteVo2Max> testes) {
        if (testes == null || testes.isEmpty()) {
            throw new IllegalArgumentException("Testes não podem ser vazios");
        }
        // Validar que todos os testes são TesteVo2Max
        boolean todosSaoVo2Max = testes.stream()
            .allMatch(t -> t instanceof TesteVo2Max);
        
        if (!todosSaoVo2Max) {
            throw new IllegalArgumentException("Todos os testes devem ser TesteVo2Max");
        }
        
        this.testes = testes;
        return this;
    }
    
    public AvaliacaoVo2MaxContext build() {
        // Validações finais
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("ClienteId é obrigatório");
        }
        if (avaliadorId == null || avaliadorId.isBlank()) {
            throw new IllegalArgumentException("AvaliadorId é obrigatório");
        }
        if (medicao == null) {
            throw new IllegalArgumentException("Medicao é obrigatória");
        }
        if (testes == null || testes.isEmpty()) {
            throw new IllegalArgumentException("Testes são obrigatórios");
        }
        if (medicao.getTabelaClassificacaoId() == null) {
            throw new IllegalArgumentException("Medicao deve ter tabelaId");
        }
        
        return new AvaliacaoVo2MaxContext(clienteId, avaliadorId, medicao, testes);
    }
}
```

### Passo 4: Strategy Atualizada

```java
// Opção A: Genérica (mais flexível)
public interface AvaliacaoStrategy<C extends AvaliacaoContext<?, ?>> {
    Leaf avaliar(C contexto);
}

// Opção B: Específica (mais simples para VO2Max)
public class AvaliacaoVo2Max implements AvaliacaoStrategy<AvaliacaoVo2MaxContext> {
    
    private final MongoTabelaClassificacaoDataRepository tabelaRepository;
    private final PersistedComponentMapper componentMapper;
    
    public AvaliacaoVo2Max(
        MongoTabelaClassificacaoDataRepository tabelaRepository,
        PersistedComponentMapper componentMapper
    ) {
        this.tabelaRepository = tabelaRepository;
        this.componentMapper = componentMapper;
    }
    
    @Override
    public Leaf avaliar(AvaliacaoVo2MaxContext context) {
        // SEM FILTROS! Já tem tudo que precisa
        var tabelaDoc = tabelaRepository.findById(context.getTabelaClassificacaoId())
            .orElseThrow(() -> new RuntimeException("Tabela não encontrada"));
        
        var tabela = componentMapper.toDomain(tabelaDoc.getRaiz());
        
        // Testes já estão tipados e validados
        for (TesteVo2Max teste : context.getTestes()) {
            var resultado = tabela.classificarComTeste(teste);
            if (resultado != null) {
                return resultado;
            }
        }
        
        return null;
    }
}
```

### Passo 5: Controller

```java
@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {
    
    private final AvaliacaoVo2Max avaliacaoVo2Max;
    private final AvaliacaoService avaliacaoService;
    
    @PostMapping("/vo2max")
    public ResponseEntity<?> avaliarVo2Max(
        @RequestBody AvaliacaoVo2MaxRequest request
    ) {
        try {
            // Builder com validação
            AvaliacaoVo2MaxContext context = new AvaliacaoVo2MaxContextBuilder()
                .comCliente(request.getClienteId())
                .comAvaliador(request.getAvaliadorId())
                .comMedicao(request.getMedicaoVo2Max())
                .comTestes(request.getTestes())
                .build();  // ← Valida tudo aqui!
            
            // Se chegou aqui, dados estão corretos
            Leaf resultado = avaliacaoVo2Max.avaliar(context);
            
            // Salvar avaliação
            avaliacaoService.salvarAvaliacao(context, resultado);
            
            return ResponseEntity.ok(resultado);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
```

---

## 🧪 Teste Unitário (Bem mais simples!)

```java
@Test
public void testAvaliacaoVo2Max() {
    // Montar apenas o contexto específico
    var medicao = new MedicaoVo2Max();
    medicao.setTabelaClassificacaoId("tabela-123");
    
    var testes = List.of(
        new TesteVo2MaxCooper(ProtocoloVo2Max.COOPER, 50.0)
    );
    
    var context = new AvaliacaoVo2MaxContextBuilder()
        .comCliente("cliente-123")
        .comAvaliador("avaliador-456")
        .comMedicao(medicao)
        .comTestes(testes)
        .build();
    
    // Teste
    Leaf resultado = avaliacaoVo2Max.avaliar(context);
    
    assertNotNull(resultado);
}
```

---

## 📊 Comparação: Antes vs Depois

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Validação** | Runtime (erro durante execução) | Build-time (Builder valida) |
| **Type-safety** | ❌ Sem tipos específicos | ✅ Tipos específicos |
| **Filtros** | ✅ Precisa filtrar | ❌ Já vem filtrado |
| **Testabilidade** | ❌ Complexo | ✅ Simples |
| **Erro possível** | Passar medicação IMC para VO2Max | ❌ Impossível (tipo errado) |
| **Acoplamento** | Alto (Strategy conhece AvaliacaoFisica) | Baixo (Strategy conhece Context) |

---

## 🔧 Para Adicionar Novo Tipo (IMC)

### 1️⃣ Context

```java
public class AvaliacaoIMCContext implements AvaliacaoContext<MedicaoIMC, TesteIMC> {
    // Mesma estrutura que VO2Max
}
```

### 2️⃣ Builder

```java
public class AvaliacaoIMCContextBuilder {
    // Mesma estrutura que VO2Max
}
```

### 3️⃣ Strategy

```java
public class AvaliacaoIMC implements AvaliacaoStrategy<AvaliacaoIMCContext> {
    @Override
    public Leaf avaliar(AvaliacaoIMCContext context) {
        // Lógica específica de IMC
    }
}
```

### 4️⃣ Controller

```java
@PostMapping("/imc")
public ResponseEntity<?> avaliarIMC(@RequestBody AvaliacaoIMCRequest request) {
    AvaliacaoIMCContext context = new AvaliacaoIMCContextBuilder()
        .comCliente(request.getClienteId())
        .comAvaliador(request.getAvaliadorId())
        .comMedicao(request.getMedicaoIMC())
        .comTestes(request.getTestes())
        .build();
    
    return ResponseEntity.ok(avaliacaoIMC.avaliar(context));
}
```

---

## 🎯 Benefícios

1. **Type-safe**: Compilador valida tipos
2. **Imutável**: Context criado e imutável (thread-safe)
3. **Testável**: Fácil montar contexto no teste
4. **Escalável**: Novo tipo = 4 classes simples
5. **Claro**: Fluxo de dados explícito
6. **Sem redundância**: Não precisa filtrar em Strategy
7. **Validação cedo**: Builder valida antes de Strategy

---

## 📝 Checklist de Implementação

- [ ] 1. Criar `AvaliacaoContext<M, T>` interface genérica
- [ ] 2. Criar `AvaliacaoVo2MaxContext` implementando interface
- [ ] 3. Criar `AvaliacaoVo2MaxContextBuilder` com validações
- [ ] 4. Atualizar `AvaliacaoVo2Max` para usar Context
- [ ] 5. Atualizar Controller para usar Builder
- [ ] 6. Adicionar testes unitários
- [ ] 7. Repeater para IMC e Bioimpedancia
- [ ] 8. Remover `AvaliacaoFisica.getMedicoes()` ou deixar como fallback

---

## 🚀 Próximos Passos

**Opção 1** (Recomendada): Implementar isso agora
- Mais type-safe
- Melhor testabilidade
- Design futuro-proof

**Opção 2**: Manter estrutura atual mas com Factory
- Menos mudanças
- Menos refactoring
- Menos performance (filtra em runtime)

Qual você quer fazer?
