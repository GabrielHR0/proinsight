# 🚀 Guia Rápido: Domain/Persistence/Converter

## ⚡ TL;DR (Muito Longo; Não Leu)

**Dois mundos**, **um mapeador**:

```
Domain (Lógica) ←→ Converter ←→ Persisted (Estrutura) ←→ Document (MongoDB)
```

---

## 📍 Onde fazer o quê?

### 🟦 Domain (Lógica de Negócio)

**Arquivo**: `domain/model/composite/`

**O que colocar**:
- ✅ Regras de negócio
- ✅ Validações
- ✅ Algoritmos
- ✅ Métodos que resolvem problemas

**Exemplo**:
```java
public class TabelaVo2Max extends Composite {
    private ProtocoloVo2Max protocolo;
    
    @Override
    public Leaf classificarComTeste(Teste teste) {
        // LÓGICA: Valida protocolo
        if (protocolo == null || !teste.getCriterio().equals(protocolo.name())) {
            return null;  // Rejeita
        }
        
        // Traversa filhos
        for (Component child : getChildren()) {
            Leaf result = child.classificarComTeste(teste);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }
}
```

---

### 🟪 Persisted (Estrutura Apenas)

**Arquivo**: `adapter/out/persistence/composite/`

**O que colocar**:
- ✅ Getters/Setters
- ✅ @TypeAlias para MongoDB
- ✅ Campos de dados

**O que NÃO colocar**:
- ❌ Lógica
- ❌ Validação
- ❌ @Document

**Exemplo**:
```java
@TypeAlias("persistedTabelaVo2Max")
public class PersistedTabelaVo2Max extends PersistedComposite {
    
    private ProtocoloVo2Max protocolo;
    
    public PersistedTabelaVo2Max() {}
    
    public ProtocoloVo2Max getProtocolo() {
        return protocolo;
    }
    
    public void setProtocolo(ProtocoloVo2Max protocolo) {
        this.protocolo = protocolo;
    }
}
```

---

### 🟩 Converter (Tradutor)

**Arquivo**: `adapter/out/persistence/converter/`

**O que fazer**:
- ✅ Converter Domain → Persisted
- ✅ Converter Persisted → Domain
- ✅ Recursar em filhos

**Você NÃO precisa fazer nada** se usar o Converter fornecido!

**Como usar**:
```java
@Service
public class MyService {
    
    @Autowired
    private PersistedComponentMapper mapper;
    
    public void exemplo() {
        // Carregar do banco
        TabelaClassificacaoDocument doc = repository.findById("123").orElse(null);
        Component domain = mapper.toDomain(doc.getRaiz());  // ✅ Automático!
        
        // Usar lógica
        Leaf resultado = domain.classificarComTeste(teste);
        
        // Salvar no banco
        PersistedComponent persisted = mapper.toPersisted(domain);  // ✅ Automático!
        doc.setRaiz(persisted);
        repository.save(doc);
    }
}
```

---

### 🟫 Document (MongoDB)

**Arquivo**: `adapter/out/persistence/`

**O que colocar**:
- ✅ @Document(collection = "...")
- ✅ @Id
- ✅ Metadata (nome, timestamps)
- ✅ PersistedComponent raiz

**Exemplo**:
```java
@Document(collection = "tabelasClassificacao")
public class TabelaClassificacaoDocument {
    
    @Id
    private String id;
    
    private String nome;
    
    private PersistedComponent raiz;  // A árvore aqui
    
    // Getters/Setters
}
```

---

## 🔧 Adicionar Novo Tipo

**Checklist**:

- [ ] 1. Criar `domain/model/composite/tabelas/TabelaNova.java`
- [ ] 2. Criar `adapter/out/persistence/composite/PersistedTabelaNova.java`
- [ ] 3. Registrar em `PersistedComponentRegistry`
- [ ] 4. Adicionar em `PersistedComponentMapper`
- [ ] 5. ✅ Pronto!

**Código template**:

```java
// 1. DOMAIN
public class TabelaNova extends Composite {
    private String campo1;
    
    @Override
    public Leaf classificarComTeste(Teste teste) {
        // Sua lógica aqui
        return super.classificarComTeste(teste);
    }
}

// 2. PERSISTED
@TypeAlias("persistedTabelaNova")
public class PersistedTabelaNova extends PersistedComposite {
    private String campo1;
    // Getters/Setters
}

// 3. REGISTRY (PersistedComponentRegistry.java)
public PersistedComponentRegistry() {
    // Adicionar:
    registrarDomain("persistedTabelaNova", TabelaNova::new);
    registrarPersisted("tabelaNova", PersistedTabelaNova::new);
}

// 4. MAPPER (PersistedComponentMapper.java)
private Composite toCompositeDomain(PersistedComposite persisted) {
    // Adicionar antes do throw:
    if (persisted instanceof PersistedTabelaNova) {
        PersistedTabelaNova p = (PersistedTabelaNova) persisted;
        TabelaNova result = new TabelaNova();
        result.setCampo1(p.getCampo1());
        converterFilhos(p, result);
        domain = result;
    }
}

private PersistedComposite toPersistedComposite(Composite domain) {
    // Adicionar antes do throw (simétrico):
    if (domain instanceof TabelaNova) {
        TabelaNova t = (TabelaNova) domain;
        PersistedTabelaNova result = new PersistedTabelaNova();
        result.setCampo1(t.getCampo1());
        converterFilhosPersisted(t, result);
        persisted = result;
    }
}
```

---

## ❌ Erros Comuns

### ❌ Erro 1: Colocar lógica em Persisted

```java
// ❌ ERRADO
public class PersistedTabelaVo2Max extends PersistedComposite {
    @Override
    public PersistedLeaf classificar(Teste teste) {
        // Lógica aqui!  ← NÃO FAÇA ISSO
        if (!teste.getCriterio().equals(protocolo.name())) {
            return null;
        }
        // ...
    }
}

// ✅ CORRETO
public class TabelaVo2Max extends Composite {
    @Override
    public Leaf classificarComTeste(Teste teste) {
        // Lógica aqui é OK
        if (!teste.getCriterio().equals(protocolo.name())) {
            return null;
        }
        // ...
    }
}
```

### ❌ Erro 2: Anotação @Document em Persisted

```java
// ❌ ERRADO
@Document
public class PersistedTabelaVo2Max extends PersistedComposite {
    // MongoDB annotations em Persisted
}

// ✅ CORRETO
@Document
public class TabelaClassificacaoDocument {
    private PersistedComponent raiz;  // Persisted aqui
}
```

### ❌ Erro 3: Chamar Persisted direto do Domain

```java
// ❌ ERRADO (Domain conhece Persisted)
public class AvaliacaoVo2Max implements AvaliacaoStrategy {
    @Override
    public PersistedLeaf avaliar(AvaliacaoFisica avaliacaoFisica) {
        // Return Persisted!  ← NÃO FAÇA ISSO
    }
}

// ✅ CORRETO (Domain retorna Domain)
public class AvaliacaoVo2Max implements AvaliacaoStrategy {
    @Override
    public Leaf avaliar(AvaliacaoFisica avaliacaoFisica) {
        // Return Domain  ← OK
    }
}
```

### ❌ Erro 4: Esquecer de registrar novo tipo

```java
// ❌ Criou novo tipo mas não registrou
public class TabelaNova extends Composite { }
public class PersistedTabelaNova extends PersistedComposite { }

// ✅ Registrou
public PersistedComponentRegistry() {
    registrarDomain("persistedTabelaNova", TabelaNova::new);
    registrarPersisted("tabelaNova", PersistedTabelaNova::new);
}
```

---

## 🧪 Como Testar

### Teste Domain Isolado

```java
@Test
public void testClassificacao() {
    // Sem banco, sem converter, sem framework
    Composite tabela = new TabelaVo2Max();
    tabela.setProtocolo(ProtocoloVo2Max.COOPER);
    tabela.add(new NivelForca("Bom", 40, 50));
    
    Teste teste = new TesteVo2MaxCooper(ProtocoloVo2Max.COOPER, 45.0);
    Leaf resultado = tabela.classificarComTeste(teste);
    
    assertNotNull(resultado);
}
```

### Teste Converter

```java
@Test
public void testConverter() {
    // Domain → Persisted → Domain
    Composite domain = new TabelaVo2Max();
    domain.setProtocolo(ProtocoloVo2Max.COOPER);
    domain.add(new NivelForca("Bom", 40, 50));
    
    PersistedComponent persisted = mapper.toPersisted(domain);
    Component loaded = mapper.toDomain(persisted);
    
    assertEquals(domain, loaded);  // Preservou estrutura
}
```

---

## 📊 Decisão Rápida: Onde colocar X?

| Situação | Domain | Converter | Persisted | Document |
|----------|--------|-----------|-----------|----------|
| Validar protocolo | ✅ | ❌ | ❌ | ❌ |
| Converter JSON | ❌ | ✅ | ❌ | ❌ |
| Armazenar campo | ❌ | ❌ | ✅ | ❌ |
| @Document | ❌ | ❌ | ❌ | ✅ |
| Classificar | ✅ | ❌ | ❌ | ❌ |
| Recursive filhos | ❌ | ✅ | ❌ | ❌ |
| @TypeAlias | ❌ | ❌ | ✅ | ❌ |
| @Id | ❌ | ❌ | ❌ | ✅ |

---

## 🔗 Referências

- **Padrão Composite**: Permite estrutura de árvore
- **Padrão Factory**: Criar objetos de tipos desconhecidos
- **Padrão Adapter**: Adaptar interface entre mundos

**Documentação completa**: Veja `ARQUITETURA.md`

---

## 💡 Dica de Ouro

```
Se você está em Domain e precisa fazer X:
├─ Se X for lógica de negócio → Faça em Domain
├─ Se X for converter dados → Use o Converter
└─ Se X for armazenar → Vá para Persisted

Se você está em Persisted e quer fazer Y:
└─ PARE! Isso provavelmente é lógica
   └─ Mova para Domain em vez disso
```

---

## 📞 Checklist Final

- [ ] Domain não tem anotações Spring/MongoDB
- [ ] Persisted não tem lógica
- [ ] Document tem @Document
- [ ] Novo tipo está registrado no Registry
- [ ] Novo tipo está no Mapper (toDomain + toPersisted)
- [ ] Testes passam
- [ ] Compilação OK

✅ **Pronto para deploy!**
