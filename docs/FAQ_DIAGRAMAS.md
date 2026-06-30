# ❓ FAQ & Diagramas - Domain/Persistence/Converter

## 🔷 Diagramas Visuais

### Diagrama 1: Fluxo Completo

```
┌────────────────────────────────────────────────────────────────┐
│ USUÁRIO/API                                                    │
│ POST /avaliacoes                                               │
└──────────────────────────┬─────────────────────────────────────┘
                           │ JSON
                           ▼
┌────────────────────────────────────────────────────────────────┐
│ CONTROLLER                                                     │
│ POST /avaliacoes(request)                                      │
└──────────────────────────┬─────────────────────────────────────┘
                           │ DTO → Domain (manual)
                           ▼
┌────────────────────────────────────────────────────────────────┐
│ SERVICE (Lógica de Negócio)                                   │
│ • avaliar(avaliacaoFisica)                                    │
│ • Carrega TabelaClassificacao do banco                        │
│ • Executa classificação                                       │
└──────────────────────────┬─────────────────────────────────────┘
                           │
                    ┌──────┴──────┐
                    │             │
         ┌──────────▼──┐  ┌───────▼────────┐
         │ Carregar DB │  │ Usar Lógica    │
         └──────────┬──┘  └────────┬───────┘
                    │             │
         ┌──────────▼──────────────▼────────────┐
         │ MongoRepository.findById()            │
         └──────────┬─────────────────────────┘
                    │
         ┌──────────▼──────────────────────────┐
         │ Document (TabelaClassificacaoDocument)
         │ {                                    │
         │   _id: "123",                       │
         │   nome: "VO2Max",                  │
         │   raiz: {                          │
         │     _class: "persistedTabelaVo2Max"│
         │     protocolo: "COOPER",            │
         │     componentes: [                 │
         │       {_class: "persistedNivel..."}│
         │     ]                              │
         │   }                                │
         │ }                                  │
         └──────────┬──────────────────────────┘
                    │
         ┌──────────▼──────────────────────────┐
         │ PersistedComponentMapper.toDomain() │
         │ - Identifica tipo via instanceof    │
         │ - Cria Domain correspondente        │
         │ - Recursa filhos                   │
         └──────────┬──────────────────────────┘
                    │
         ┌──────────▼──────────────────────────┐
         │ Component (Domain)                  │
         │ TabelaVo2Max {                      │
         │   protocolo: "COOPER",              │
         │   children: [                      │
         │     NivelForca("Bom", 40, 50)      │
         │   ]                                │
         │ }                                  │
         └──────────┬──────────────────────────┘
                    │ (usa lógica)
         ┌──────────▼──────────────────────────┐
         │ component.classificarComTeste(teste)│
         │ - Valida protocolo (lógica!)       │
         │ - Traversa filhos                  │
         │ - Retorna Leaf                     │
         └──────────┬──────────────────────────┘
                    │
         ┌──────────▼──────────────────────────┐
         │ Leaf (NivelForca)                   │
         │ - classificacao: "Bom"              │
         │ - min: 40                           │
         │ - max: 50                           │
         └──────────┬──────────────────────────┘
                    │ (mapeando de volta)
         ┌──────────▼──────────────────────────┐
         │ Retorna ao usuário                 │
         │ 200 OK {                            │
         │   classificacao: "Bom",             │
         │   intervalo: [40, 50]               │
         │ }                                  │
         └──────────────────────────────────────┘
```

---

### Diagrama 2: Hierarquias Paralelas

```
╔════════════════════════╗              ╔════════════════════════╗
║ DOMAIN (Lógica)        ║              ║ PERSISTED (Estrutura)  ║
╚════════════════════════╝              ╚════════════════════════╝

  Component (interface)                   PersistedComponent (marker)
         ▲                                        ▲
         │                                        │
    ┌────┴────┐                               ┌───┴────┐
    │          │                               │         │
Composite   Leaf                          Composite   Leaf
    ▲         ▲                               ▲         ▲
    │         │                               │         │
    │    NivelForca                          │   PersistedNivel
    │                                        │        Forca
    │                                        │
    ├─ TabelaVo2Max                         ├─ PersistedTabela
    │   (valida protocolo)                  │    Vo2Max
    │                                        │   (getters/setters)
    ├─ TabelaSexo                           ├─ PersistedTabela
    │   (genérica)                          │    Sexo
    │                                        │
    └─ TabelaEquipamento                    └─ PersistedTabela
       (genérica)                              Equipamento


         ↕️
    CONVERTER mapeia

  TabelaVo2Max          ←→    PersistedTabelaVo2Max
    protocolo ─────────────────→ protocolo
    children ─────────────────→ componentes
```

---

### Diagrama 3: Estrutura de Árvore

```
                    ┌─────────────────┐
                    │  TabelaVo2Max   │
                    │ protocolo:"COOP"│
                    │ (Composite)     │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
         ┌────▼────┐    ┌────▼────┐   ┌───▼────┐
         │TabelaSex│    │NivelForça│   │TabelaEq│
         │(Compos) │    │  (Leaf)  │   │(Compos)│
         └────┬────┘    └──────────┘   └────┬───┘
              │                              │
         ┌────┴────────────────┐             │
         │                     │             │
    ┌────▼────┐          ┌─────▼────┐  ┌───▼────┐
    │NivelForça│          │NivelForça│  │NivelFor│
    │  (Leaf)  │          │  (Leaf)  │  │ ça(L)  │
    └──────────┘          └──────────┘  └────────┘
    
    Classificação:
    teste.protocolo = "COOPER" → TabelaVo2Max.classificarComTeste()
      ↓ valida protocolo
    ✅ protocolo OK → traversa filhos
      ↓
    TabelaSexo.classificarComTeste() → seu próprio teste
      ↓
    NivelForca.classificarComTeste() → retorna this (é Leaf!)
    ✅ Resultado: NivelForca("Excelente", 40, 50)
```

---

### Diagrama 4: MongoDB Storage

```
┌──────────────────────────────────────────────────────┐
│ MongoDB: db.tabelasClassificacao                     │
├──────────────────────────────────────────────────────┤
│ {                                                    │
│   _id: ObjectId("507f1f77bcf86cd799439011"),       │
│   nome: "Tabela VO2Max COOPER",                    │
│   raiz: {                                          │
│     _class: "persistedTabelaVo2Max",              │
│     protocolo: "COOPER",                          │
│     componentes: [                                │
│       {                                           │
│         _class: "persistedTabelaSexo",           │
│         sexo: "MASCULINO",                       │
│         componentes: [                           │
│           {                                     │
│             _class: "persistedNivelForca",     │
│             classificacao: "Excelente",        │
│             min: 40,                           │
│             max: 50                            │
│           },                                   │
│           {                                     │
│             _class: "persistedNivelForca",     │
│             classificacao: "Bom",              │
│             min: 30,                           │
│             max: 39                            │
│           }                                     │
│         ]                                      │
│       },                                       │
│       {                                        │
│         _class: "persistedTabelaSexo",        │
│         sexo: "FEMININO",                     │
│         componentes: [...]                    │
│       }                                        │
│     ]                                         │
│   }                                           │
│ }                                            │
└──────────────────────────────────────────────────────┘

_class: MongoDB sabe qual classe desserializar
         (usando @TypeAlias)
```

---

## ❓ Perguntas Frequentes

### P1: Por que não usar apenas Domain?

**R**: Porque:
- Domain não sabe serializar para JSON/BSON
- Não pode ter anotações MongoDB (aumenta acoplamento)
- Testes de Domain ficariam lentos (precisariam do banco)
- Mudar MongoDB para Redis quebraria Domain

**Resultado**: Domain fica testável e independente!

---

### P2: Por que não usar apenas Persisted?

**R**: Porque:
- Persisted não pode ter lógica (é estrutura apenas)
- MongoDB é detalhe de implementação
- Mudar banco quebraria lógica
- Difícil testar regras de negócio

**Resultado**: Lógica fica próximo do conceito de negócio!

---

### P3: Por que dois métodos (classificar e classificarComTeste)?

**R**: 
- `classificar()`: Sem parâmetro, genérico (padrão Composite)
- `classificarComTeste()`: Com Teste, específico (valida protocolo)

Ter ambos permite:
```java
// Uso genérico
Leaf resultado = component.classificar();

// Uso específico (com validação)
Leaf resultado = component.classificarComTeste(teste);
```

---

### P4: Como adicionar validação em Domain?

**R**: Adicione método/validação em Domain:

```java
public class TabelaVo2Max extends Composite {
    
    public void setProtocolo(ProtocoloVo2Max protocolo) {
        if (protocolo == null) {
            throw new IllegalArgumentException("Protocolo não pode ser nulo");
        }
        this.protocolo = protocolo;
    }
}
```

Domain valida, Persisted não precisa (confia em Domain).

---

### P5: E se eu tiver campos em Persisted que não existem em Domain?

**R**: Isso é OK! Exemplo:

```java
// Domain
public class TabelaVo2Max extends Composite {
    private ProtocoloVo2Max protocolo;  // Lógica
}

// Persisted
public class PersistedTabelaVo2Max extends PersistedComposite {
    private ProtocoloVo2Max protocolo;   // Dados
    private Instant createdAt;           // Metadata (não tem em Domain)
    private String createdBy;            // Metadata (não tem em Domain)
}

// Converter copia o que existe
PersistedComponent p = mapper.toPersisted(domain);
// protocolo é copiado, createdAt/createdBy ficam null/default
```

---

### P6: Como fazer cache do Converter?

**R**: Converter já é stateless e rápido (sem reflexão). Se precisar cache:

```java
@Service
public class PersistedComponentMapper {
    private final Map<Class<?>, Supplier<?>> cache = new ConcurrentHashMap<>();
    
    public Component toDomain(PersistedComponent persisted) {
        // ... logica
    }
}
```

Mas geralmente não é necessário.

---

### P7: E se a árvore for muito profunda?

**R**: StackOverflow é risco com recursão profunda. Solução:

```java
// Iterativa ao invés de recursiva
private void converterFilhosIterativo(PersistedComposite persisted, Composite domain) {
    Queue<Pair<PersistedComponent, Composite>> queue = new LinkedList<>();
    queue.add(new Pair<>(persisted, domain));
    
    while (!queue.isEmpty()) {
        // Processa nó atual
        // Adiciona filhos à fila
    }
}
```

Mas para casos normais, recursão é mais limpa.

---

### P8: Como testar sem MongoDB?

**R**: Teste apenas Domain, sem banco:

```java
@Test
public void testDomain() {
    // Sem @SpringBootTest, sem MongoDB
    Composite tabela = new TabelaVo2Max();
    tabela.setProtocolo(ProtocoloVo2Max.COOPER);
    tabela.add(new NivelForca("Bom", 40, 50));
    
    Leaf resultado = tabela.classificarComTeste(teste);
    assertNotNull(resultado);
}
```

Teste Converter separado com mocks:

```java
@Test
public void testConversor() {
    // Sem banco, mas testa conversão
    Composite domain = new TabelaVo2Max();
    PersistedComponent persisted = mapper.toPersisted(domain);
    Component loaded = mapper.toDomain(persisted);
    
    assertEquals(domain.getClass(), loaded.getClass());
}
```

---

### P9: Como versionar estrutura no MongoDB?

**R**: Adicione campo de versão:

```java
@Document
public class TabelaClassificacaoDocument {
    @Id private String id;
    private Integer version = 1;  // Novo campo
    private PersistedComponent raiz;
}

// No Converter, checke versão:
public Component toDomain(PersistedComponent persisted, Integer version) {
    switch(version) {
        case 1: return toDomainV1(persisted);
        case 2: return toDomainV2(persisted);
        default: throw new UnsupportedVersionException();
    }
}
```

---

### P10: Posso usar GenericType com reflection?

**R**: Não é recomendado porque:
- ❌ Lento (reflexão é custosa)
- ❌ Frágil (nomes precisam bater)
- ❌ Boilerplate (precisa anotar tudo)

**Melhor**:
- ✅ instanceof (explícito, rápido)
- ✅ Registry (centralizador, escalável)

---

## 📚 Comparação: Antes vs Depois

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Mappers** | 2+ (redundantes) | 1 (centralizado) |
| **Escalabilidade** | O(n) por tipo | O(1) por tipo |
| **Testabilidade** | Difícil (acoplado) | Fácil (isolado) |
| **Linhas para novo tipo** | 50+ | 5+ |
| **Separação SoC** | 5/10 | 10/10 |
| **Documentação** | Implícita | Explícita |
| **Compilação** | ❌ Erros | ✅ OK |

---

## 🎓 Referências de Padrões

### Composite Pattern
```
Fonte: Gang of Four (Design Patterns)
- Estruturar objetos em árvore
- Trabalhar com eles uniformemente
- Exemplo: DOM do browser
```

### Factory Pattern
```
Fonte: Gang of Four (Design Patterns)
- Criar objetos sem especificar classes
- Encapsular lógica de criação
- Exemplo: Document.createElement()
```

### Adapter Pattern
```
Fonte: Gang of Four (Design Patterns)
- Converter interface de um objeto
- Fazer objetos incompatíveis trabalhar juntos
- Exemplo: Conversor de voltagem
```

---

## 🚀 Próximas Evoluções

**Phase 1** (Atual):
- ✅ Domain vs Persisted separados
- ✅ Converter bidirecional
- ✅ Polimorfismo automático

**Phase 2** (Proposto):
- [ ] Repository pattern para não expor Persisted
- [ ] Event sourcing (histórico de mudanças)
- [ ] Snapshot pattern (cache de estados)

**Phase 3** (Futuro):
- [ ] GraphQL para explorar árvore
- [ ] Audit trail automático
- [ ] Versionamento de estrutura

---

## ✅ Checklist de Compreensão

- [ ] Entendo por que Domain e Persisted são separados
- [ ] Sei onde colocar lógica (Domain)
- [ ] Sei onde colocar getters/setters (Persisted)
- [ ] Entendo como Converter mapeia entre eles
- [ ] Consigo adicionar novo tipo em 5 minutos
- [ ] Sei testar Domain isolado
- [ ] Entendo os padrões utilizados
- [ ] Li `ARQUITETURA.md` e `GUIA_RAPIDO.md`

Se todos marcados ✅, você está pronto!

---

## 📞 Suporte

1. **Dúvida conceitual**: Leia ARQUITETURA.md
2. **Como fazer algo**: Leia GUIA_RAPIDO.md
3. **Erro específico**: Procure em "Erros Comuns"
4. **Nova funcionalidade**: Siga checklist "Adicionar Novo Tipo"

**Lembre-se**: Quando em dúvida, "Lógica = Domain, Estrutura = Persisted"
