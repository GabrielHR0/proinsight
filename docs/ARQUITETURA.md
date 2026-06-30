# 🏗️ Arquitetura: Domain vs Persistence vs Converter

## 📋 Sumário Executivo

Esta arquitetura implementa **separação clara de responsabilidades** entre camadas de domínio (lógica de negócio) e persistência (dados), usando o padrão **Composite** com mapeamento automático bidirecional.

**Princípio**: Uma entidade existe em dois "mundos":
- **Domain**: Puro, com lógica de negócio, sem conhecimento de persistência
- **Persisted**: Estrutura apenas, serializável para MongoDB, sem lógica
- **Converter**: Mapeia entre os mundos automaticamente

---

## 🎯 Por que essa Arquitetura?

### Problema Original
```
❌ Hierarquia única (Domain herdava de Persisted)
   └─ Lógica misturada com anotações MongoDB
   └─ Difícil de testar Domain sem banco
   └─ Alta acoplamento

❌ Múltiplos Mappers
   └─ TabelaVo2MaxMapper (redundante)
   └─ AvaliacaoFisicaMapper (incompleto)
   └─ Não escalável
```

### Solução Implementada
```
✅ Dois mundos separados
   ├─ Domain: Lógica pura, testável
   ├─ Persisted: Estrutura serializável
   └─ Converter: Translador automático

✅ Um Converter bidirecional
   ├─ Persisted → Domain (carregar do banco)
   ├─ Domain → Persisted (salvar no banco)
   └─ Recursão automática em filhos

✅ Escalável
   ├─ Registry de tipos
   ├─ Novos tipos sem modificar Converter
   └─ Polimorfismo automático
```

---

## 🏛️ Arquitetura em Camadas

```
┌─────────────────────────────────────────────────────────┐
│ CONTROLLER / SERVICE (Entrada da Aplicação)             │
│ • Recebe requests                                        │
│ • Mapeia para DTOs                                       │
└────────────────┬────────────────────────────────────────┘
                 │ (trabalha com Domain)
                 ↓
┌─────────────────────────────────────────────────────────┐
│ DOMAIN - Lógica de Negócio Pura                          │
├─────────────────────────────────────────────────────────┤
│ Responsabilidades:                                       │
│ ✅ Implementar regras de negócio                         │
│ ✅ Validar dados                                         │
│ ✅ Executar algoritmos (ex: classificação)              │
│ ✅ Definir invariantes da entidade                      │
│                                                          │
│ Estrutura:                                               │
│ • Component (interface): define contrato                │
│ • Composite (abstrato): navegação genérica              │
│ • Leaf (abstrato): terminador da árvore                 │
│ • TabelaVo2Max: validação de protocolo                 │
│ • TabelaSexo, TabelaEquipamento: compostos genéricos    │
│ • NivelForca: folha com dados                           │
│                                                          │
│ Características:                                         │
│ • ❌ Zero anotações MongoDB                             │
│ • ❌ Zero anotações Spring                              │
│ • ✅ Métodos: classificarComTeste(Teste)               │
│ • ✅ Polimorfismo: Component.classificarComTeste()      │
│ • ✅ Totalmente testável (sem dependências externas)   │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ (PersistedComponentMapper)
                 ↓
┌─────────────────────────────────────────────────────────┐
│ CONVERTER - Mapeador Bidirecional                        │
├─────────────────────────────────────────────────────────┤
│ Componentes:                                             │
│ • PersistedComponentMapper: conversão automática        │
│ • PersistedComponentRegistry: registro de tipos         │
│                                                          │
│ Responsabilidades:                                       │
│ ✅ Converter Persisted → Domain (toDomain)             │
│ ✅ Converter Domain → Persisted (toPersisted)          │
│ ✅ Recursar automaticamente em filhos                   │
│ ✅ Identificar tipos via instanceof                     │
│ ✅ Copiar campos campo-a-campo                          │
│                                                          │
│ Características:                                         │
│ • ❌ Sem lógica de negócio                              │
│ • ❌ Sem lógica de persistência                         │
│ • ✅ Polimórfico: toDomain(PersistedComponent) works    │
│ • ✅ Genérico: funciona com qualquer tipo               │
│ • ✅ Sem manutenção: novos tipos auto-funcionam        │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ (armazena/recupera)
                 ↓
┌─────────────────────────────────────────────────────────┐
│ PERSISTED - Estrutura de Persistência                    │
├─────────────────────────────────────────────────────────┤
│ Responsabilidades:                                       │
│ ✅ Armazenar dados em memória (getters/setters)         │
│ ✅ Ser serializável para MongoDB                        │
│ ✅ Marcar tipo via @TypeAlias (polimorfismo DB)        │
│                                                          │
│ Estrutura:                                               │
│ • PersistedComponent (interface vazia): marker          │
│ • PersistedComposite: container genérico               │
│ • PersistedLeaf: vazio                                  │
│ • PersistedTabelaVo2Max: só getters/setters            │
│ • PersistedNivelForca: só getters/setters              │
│                                                          │
│ Características:                                         │
│ • ❌ Sem @Document (isso é do Document!)               │
│ • ❌ Sem lógica                                         │
│ • ❌ Sem validação                                      │
│ • ✅ @TypeAlias para MongoDB saber tipo                │
│ • ✅ Serialização automática                            │
│ • ✅ Estrutura genérica (reutilizável)                 │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ (persiste)
                 ↓
┌─────────────────────────────────────────────────────────┐
│ DOCUMENT - Container MongoDB                             │
├─────────────────────────────────────────────────────────┤
│ Exemplo: TabelaClassificacaoDocument                    │
│                                                          │
│ Responsabilidades:                                       │
│ ✅ @Document(collection = "...") - mapping db           │
│ ✅ @Id - identificação única                            │
│ ✅ Metadata (nome, timestamps) - auditoria              │
│ ✅ Conter raiz da árvore Persisted                      │
│                                                          │
│ Estrutura:                                               │
│ {                                                        │
│   @Id: "abc123",                                        │
│   nome: "Tabela VO2Max",                               │
│   raiz: PersistedComponent {                            │
│     _class: "persistedTabelaVo2Max",                   │
│     protocolo: "COOPER",                                │
│     componentes: [...]                                  │
│   }                                                      │
│ }                                                        │
│                                                          │
│ Características:                                         │
│ • ✅ Anotações MongoDB centralizadas                    │
│ • ✅ Separado de Persisted (composition)               │
│ • ❌ Sem lógica                                         │
│ • ❌ Sem validação                                      │
└────────────────┬────────────────────────────────────────┘
                 │
                 ↓
┌─────────────────────────────────────────────────────────┐
│ MongoDB (Base de Dados)                                  │
│ Colecções, indexação, replicação...                     │
└─────────────────────────────────────────────────────────┘
```

---

## 🔄 Fluxo de Dados

### 1️⃣ Carregar do Banco (Read)

```
MongoDB
  ↓
TabelaClassificacaoDocument
  └─ @Document
  └─ raiz: PersistedComponent
       └─ PersistedTabelaVo2Max
            └─ protocolo: "COOPER"
            └─ componentes: [
                 PersistedTabelaSexo,
                 PersistedNivelForca,
                 ...
               ]

  ↓ PersistedComponentMapper.toDomain()

Component (Domain)
  └─ TabelaVo2Max
       └─ protocolo: "COOPER"
       └─ children: [
            TabelaSexo,
            NivelForca,
            ...
          ]

  ↓ (usa lógica)

Service.avaliar(teste)
  └─ component.classificarComTeste(teste)
       └─ TabelaVo2Max valida protocolo
       └─ Traversa filhos
       └─ Retorna Leaf (classificação)
```

### 2️⃣ Salvar no Banco (Write)

```
Service cria Domain
  └─ new TabelaVo2Max()
       └─ setProtocolo(COOPER)
       └─ add(new TabelaSexo())
       └─ add(new NivelForca())

  ↓ PersistedComponentMapper.toPersisted()

Persisted (estrutura)
  └─ new PersistedTabelaVo2Max()
       └─ protocolo: "COOPER"
       └─ addComponente(new PersistedTabelaSexo())
       └─ addComponente(new PersistedNivelForca())

  ↓ (em Document)

TabelaClassificacaoDocument
  └─ setRaiz(persisted)

  ↓ MongoDB serializa

MongoDB
  └─ salva JSON com @TypeAlias
```

---

## 📦 Estrutura de Pacotes

```
com.prosup.proinsight
│
├─ domain/
│  ├─ model/
│  │  ├─ composite/
│  │  │  ├─ Component.java (interface)
│  │  │  ├─ Composite.java (abstrato)
│  │  │  ├─ Leaf.java (abstrato)
│  │  │  ├─ classes/
│  │  │  │  └─ NivelForca.java
│  │  │  └─ tabelas/
│  │  │     ├─ TabelaVo2Max.java ← LÓGICA
│  │  │     ├─ TabelaSexo.java
│  │  │     └─ TabelaEquipamento.java
│  │  ├─ AvaliacaoFisica.java
│  │  └─ Medicao.java
│  │
│  └─ avalicao_strategy/
│     ├─ AvaliacaoStrategy.java (retorna Domain.Leaf)
│     └─ AvaliacaoVo2Max.java
│
└─ adapter/
   └─ out/
      └─ persistence/
         │
         ├─ converter/
         │  ├─ PersistedComponentMapper.java (@Service)
         │  └─ PersistedComponentRegistry.java (helper)
         │
         ├─ composite/
         │  ├─ PersistedComponent.java (interface)
         │  ├─ PersistedComposite.java (abstrato)
         │  ├─ PersistedLeaf.java (abstrato)
         │  ├─ PersistedNivelForca.java
         │  ├─ PersistedTabelaVo2Max.java
         │  ├─ PersistedTabelaSexo.java
         │  └─ PersistedTabelaEquipamento.java
         │
         ├─ TabelaClassificacaoDocument.java ← DOCUMENT
         ├─ AvaliacaoFisicaDocument.java
         ├─ MedicaoDocument.java (abstrato)
         ├─ MedicaoVo2MaxDocument.java
         │
         ├─ MongoTabelaClassificacaoDataRepository.java
         └─ MongoAvaliacaoFisicaDataRepository.java
```

---

## 🔑 Conceitos-Chave

### 1. Padrão Composite

**O que é**: Um padrão que permite construir objetos em estruturas de árvore e trabalhar com eles como se fossem objetos individuais.

**Como usamos**:
```
Component (interface)
├── Composite
│   ├── TabelaVo2Max (pode ter filhos)
│   ├── TabelaSexo (pode ter filhos)
│   └── TabelaEquipamento (pode ter filhos)
│
└── Leaf
    └── NivelForca (sem filhos)
```

**Exemplos de uso**:
```java
// Construir árvore
Composite tabela = new TabelaVo2Max();
tabela.add(new NivelForca("Baixo", 0, 20));
tabela.add(new NivelForca("Médio", 21, 40));

// Navegar polimorficamente
Leaf resultado = tabela.classificarComTeste(teste);
```

### 2. Mapeamento Bidirecional Automático

**Ideia**: Converter automaticamente entre Domain e Persisted sem código repetitivo.

**Como funciona**:
```java
// Carregar
PersistedComponent persisted = repository.findById(...);
Component domain = mapper.toDomain(persisted);  // Automático!

// Salvar
Component domain = new TabelaVo2Max();
PersistedComponent persisted = mapper.toPersisted(domain);  // Automático!
repository.save(new Document().setRaiz(persisted));
```

**Escalável**:
```java
// Adicionar novo tipo:
// 1. Criar domain/model/composite/tabelas/TabelaNova.java
// 2. Criar adapter/out/persistence/composite/PersistedTabelaNova.java
// 3. Adicionar em PersistedComponentRegistry (2 linhas)
// 4. Adicionar em PersistedComponentMapper.toCompositeDomain() (3 linhas)
// 5. Pronto! Converter funciona automaticamente
```

### 3. Polimorfismo Recursivo

**Problema**: Como converter uma árvore com múltiplos tipos sem saber os tipos de antemão?

**Solução**: Recursão com `instanceof`
```java
public Component toDomain(PersistedComponent persisted) {
    if (persisted instanceof PersistedLeaf) {
        return toLeafDomain((PersistedLeaf) persisted);
    }
    
    if (persisted instanceof PersistedComposite) {
        return toCompositeDomain((PersistedComposite) persisted);
    }
    
    throw new IllegalArgumentException("Tipo desconhecido");
}

private Composite toCompositeDomain(PersistedComposite persisted) {
    Composite domain;
    
    if (persisted instanceof PersistedTabelaVo2Max) {
        // ... cria domain correspondente
    } else if (persisted instanceof PersistedTabelaSexo) {
        // ... cria domain correspondente
    }
    
    // Recursa filhos
    for (PersistedComponent child : persisted.getComponentes()) {
        domain.add(toDomain(child));  // Recursão!
    }
    
    return domain;
}
```

---

## 💡 Padrões de Design Utilizados

### 1. **Composite Pattern** ✅
- **Propósito**: Criar estrutura de árvore de objetos
- **Implementação**: Component/Composite/Leaf
- **Benefício**: Navegar árvore polimorficamente

### 2. **Factory Pattern** ✅
- **Propósito**: Criar objetos de tipos desconhecidos
- **Implementação**: `toDomain()` com instanceof
- **Benefício**: Extensível sem modificar factory

### 3. **Adapter Pattern** ✅
- **Propósito**: Adaptar interface de um objeto
- **Implementação**: Domain ↔ Persisted via Converter
- **Benefício**: Cada mundo tem sua interface

### 4. **Strategy Pattern** (bônus)
- **Propósito**: Encapsular algoritmos intercambiáveis
- **Implementação**: AvaliacaoStrategy (classificação)
- **Benefício**: Fácil adicionar novas estratégias

### 5. **Repository Pattern** (bônus)
- **Propósito**: Abstrair acesso a dados
- **Implementação**: MongoTabelaClassificacaoDataRepository
- **Benefício**: Trocar DB sem afetar Domain

---

## 📝 Exemplo Prático: Adicionar Novo Tipo

### Cenário
Você quer adicionar uma nova tabela de classificação: **TabelaIMC**

### Passo 1: Criar Domain

**Arquivo**: `domain/model/composite/tabelas/TabelaIMC.java`

```java
package com.prosup.proinsight.domain.model.composite.tabelas;

import com.prosup.proinsight.domain.model.composite.Composite;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.Teste;

public class TabelaIMC extends Composite {

    private String categoria;  // ex: "Adulto", "Criança"

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public Leaf classificarComTeste(Teste teste) {
        // Lógica específica do IMC
        if (categoria == null || !teste.getCriterio().equals("IMC")) {
            return null;
        }
        
        // Traversa filhos normalmente
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

### Passo 2: Criar Persisted

**Arquivo**: `adapter/out/persistence/composite/PersistedTabelaIMC.java`

```java
package com.prosup.proinsight.adapter.out.persistence.composite;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("persistedTabelaIMC")
public class PersistedTabelaIMC extends PersistedComposite {

    private String categoria;

    public PersistedTabelaIMC() {}

    public PersistedTabelaIMC(String categoria) {
        this.categoria = categoria;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
```

### Passo 3: Registrar no Converter

**Arquivo**: `adapter/out/persistence/converter/PersistedComponentRegistry.java`

```java
public PersistedComponentRegistry() {
    // Adicionar 2 linhas:
    registrarDomain("persistedTabelaIMC", TabelaIMC::new);
    registrarPersisted("tabelaIMC", PersistedTabelaIMC::new);
    
    // ... resto
}
```

### Passo 4: Adicionar no Mapper

**Arquivo**: `adapter/out/persistence/converter/PersistedComponentMapper.java`

```java
private Composite toCompositeDomain(PersistedComposite persisted) {
    Composite domain;
    
    // Adicionar 3 linhas:
    if (persisted instanceof PersistedTabelaIMC) {
        PersistedTabelaIMC p = (PersistedTabelaIMC) persisted;
        TabelaIMC result = new TabelaIMC();
        result.setCategoria(p.getCategoria());
        converterFilhos(p, result);
        domain = result;
    }
    
    // ... resto
}

// Mesmo para toPersisted (simétrico)
private PersistedComposite toPersistedComposite(Composite domain) {
    PersistedComposite persisted;
    
    // Adicionar 3 linhas:
    if (domain instanceof TabelaIMC) {
        TabelaIMC t = (TabelaIMC) domain;
        PersistedTabelaIMC result = new PersistedTabelaIMC();
        result.setCategoria(t.getCategoria());
        converterFilhosPersisted(t, result);
        persisted = result;
    }
    
    // ... resto
}
```

### ✅ Pronto!

Agora você pode usar:
```java
// Criar
Composite tabela = new TabelaIMC("Adulto");
tabela.add(new NivelForca("Baixo peso", 0, 18));
tabela.add(new NivelForca("Peso normal", 18, 25));

// Converter automaticamente
PersistedComponent persisted = mapper.toPersisted(tabela);
Component loaded = mapper.toDomain(persisted);

// Usar
Leaf resultado = tabela.classificarComTeste(teste);
```

---

## 🧪 Como Testar

### Teste de Domain (sem dependências)

```java
@Test
public void testTabelaVo2MaxValidaProtocolo() {
    // Arrange
    TabelaVo2Max tabela = new TabelaVo2Max();
    tabela.setProtocolo(ProtocoloVo2Max.COOPER);
    tabela.add(new NivelForca("Bom", 40, 50));
    
    // Act
    Teste teste = new TesteVo2MaxCooper(ProtocoloVo2Max.COOPER, 45.0);
    Leaf resultado = tabela.classificarComTeste(teste);
    
    // Assert
    assertNotNull(resultado);
    assertTrue(resultado instanceof NivelForca);
    assertEquals("Bom", ((NivelForca) resultado).getClassificacao());
}

@Test
public void testTabelaVo2MaxRejeita ProtocoloErrado() {
    // Arrange
    TabelaVo2Max tabela = new TabelaVo2Max();
    tabela.setProtocolo(ProtocoloVo2Max.COOPER);
    tabela.add(new NivelForca("Bom", 40, 50));
    
    // Act
    Teste teste = new TesteVo2MaxCooper(ProtocoloVo2Max.LEGER, 45.0);
    Leaf resultado = tabela.classificarComTeste(teste);
    
    // Assert
    assertNull(resultado);  // Protocolo não bate
}
```

### Teste do Converter (integração)

```java
@Test
public void testConversorBidirecionalPreservaEstrutura() {
    // Arrange
    TabelaVo2Max domain = new TabelaVo2Max();
    domain.setProtocolo(ProtocoloVo2Max.COOPER);
    domain.add(new NivelForca("Baixo", 0, 20));
    domain.add(new NivelForca("Alto", 40, 50));
    
    // Act
    PersistedComponent persisted = mapper.toPersisted(domain);
    Component loaded = mapper.toDomain(persisted);
    
    // Assert
    assertTrue(loaded instanceof TabelaVo2Max);
    TabelaVo2Max loadedTabela = (TabelaVo2Max) loaded;
    assertEquals(ProtocoloVo2Max.COOPER, loadedTabela.getProtocolo());
    assertEquals(2, loadedTabela.getChildren().size());
}
```

---

## ⚠️ Decisões de Design

### 1. Por que duas hierarquias (Domain + Persisted)?

**Alternativas consideradas**:
- ❌ Uma hierarquia única: Mistura lógica com persistência
- ❌ DTOs: Código repetitivo, não escalável
- ✅ Duas hierarquias: Clean, testável, escalável

**Benefício**: Domain fica puro, testável, independente de frameworks.

### 2. Por que Converter ao invés de Mapper individual?

**Alternativas consideradas**:
- ❌ Um mapper por tipo (TabelaVo2MaxMapper): Não escalável
- ❌ Reflection automática: Lento, frágil
- ✅ Um Converter bidirecional: Rápido, explícito, escalável

**Benefício**: Adicionar novo tipo é trivial (5 linhas).

### 3. Por que Document separado de Persisted?

**Alternativas consideradas**:
- ❌ TabelaClassificacaoDocument extends PersistedComposite: Confuso
- ✅ TabelaClassificacaoDocument tem PersistedComponent: Limpo

**Benefício**: Responsabilidades claras (Document = metadata, Persisted = estrutura).

### 4. Por que classificarComTeste ao invés de classificar?

**Razão**: Necessidade de contexto (Teste) para validar protocolo.

```java
// classificar() - sem contexto
public Leaf classificar() { return super.classificar(); }  // Genérico

// classificarComTeste(Teste) - com contexto
public Leaf classificarComTeste(Teste teste) {
    if (!teste.getCriterio().equals(protocolo.name())) {
        return null;  // Valida protocolo
    }
    // ... traversa filhos
}
```

---

## 📚 Glossário

| Termo | Significado |
|-------|------------|
| **Component** | Interface que define contrato (classificar, classificarComTeste) |
| **Composite** | Nó interno que pode ter filhos |
| **Leaf** | Nó terminal sem filhos |
| **Domain** | Mundo da lógica de negócio |
| **Persisted** | Mundo da estrutura de dados |
| **Converter** | Tradutor Domain ↔ Persisted |
| **Document** | Container MongoDB com metadata |
| **TypeAlias** | Marcador MongoDB para saber tipo de objeto |
| **Polimorfismo** | Trabalhar com tipos abstratos (Component, PersistedComponent) |
| **Recursão** | Aplicar operação em toda a árvore |

---

## 🎯 Benefícios Alcançados

✅ **Testabilidade**: Domain não depende de frameworks
✅ **Escalabilidade**: Adicionar tipo = 5 linhas de código
✅ **Maintabilidade**: Responsabilidades claras
✅ **Separação de Conceitos**: Lógica ≠ Persistência
✅ **Reutilização**: Estrutura genérica (Composite/Leaf)
✅ **Flexibilidade**: Trocar persistência sem afetar Domain
✅ **Documentação**: Código autoexplicativo
✅ **Performance**: Sem reflexão, sem serialização extra

---

## 🚀 Próximas Melhorias

- [ ] Adicionar validação em Domain (ex: setProtocolo)
- [ ] Implementar Medicação.toDomain/toPersisted
- [ ] Cache de Converter para tipos
- [ ] Evento de mudança em entidades
- [ ] Audit trail (quem modificou, quando)
- [ ] Versioning de estrutura
- [ ] GraphQL para explorar árvore

---

## 📞 Suporte

Para dúvidas sobre essa arquitetura:

1. Consulte exemplos em `/src/main/java/com/prosup/proinsight/domain/model/composite/`
2. Veja testes em `/src/test` (quando criados)
3. Leia documentação de padrões (Composite, Factory, Adapter)
4. Compare Domain vs Persisted em arquivos lado-a-lado

**Regra de Ouro**: Se sua lógica está em Persisted → mova para Domain!
