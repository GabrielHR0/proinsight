# Persistência Polimórfica no MongoDB com Spring Data — Anatomia de um Bug

> **Problema:** Rota `GET /api/v1/tabelas_classificacao/{id}` retornava 500 com
> `MappingInstantiationException: Failed to instantiate PersistedComponent — Specified class is an interface`
>
> **Solução:** Conversores Jackson customizados que bypassam o `MappingMongoConverter` do Spring Data.

---

## 1. Contexto

O domínio de classificação física usa o padrão **Composite**:

```
Component (interface)
├── Leaf (abstract)
│   ├── NivelVo2Max
│   └── NivelForca
└── Composite (abstract)
    ├── TabelaVo2Max
    ├── TabelaSexo
    ├── TabelaIdade
    └── TabelaEquipamento
```

Cada nó da árvore é representado no MongoDB por uma classe `Persisted*` correspondente:

```
PersistedComponent (interface)
├── PersistedLeaf (abstract)
│   ├── PersistedNivelVo2Max    @TypeAlias("persistedNivelVo2Max")
│   └── PersistedNivelForca     @TypeAlias("persistedNivelForca")
└── PersistedComposite (abstract)
    ├── PersistedTabelaVo2Max   @TypeAlias("persistedTabelaVo2Max")
    ├── PersistedTabelaSexo     @TypeAlias("persistedTabelaSexo")
    ├── PersistedTabelaIdade    @TypeAlias("persistedTabelaIdade")
    └── PersistedTabelaEquipamento  @TypeAlias("persistedTabelaEquipamento")
```

O documento raiz no MongoDB:

```javascript
{
  _id: "classificacao_cooper_12min",
  _class: "tabelaClassificacao",           // @TypeAlias do documento
  nome: "Classificação Cooper 12 min",
  raiz: {
    _class: "persistedTabelaVo2Max",       // @TypeAlias da subclasse
    protocolo: "COOPER",
    componentes: [{
      _class: "persistedTabelaSexo",
      sexo: "MASCULINO",
      componentes: [ /* ... */ ]
    }]
  }
}
```

---

## 2. O Problema

### 2.1 Sintoma

```
org.springframework.data.mapping.model.MappingInstantiationException:
  Failed to instantiate PersistedComponent using constructor NO_CONSTRUCTOR
Caused by: org.springframework.beans.BeanInstantiationException:
  Failed to instantiate [PersistedComponent]: Specified class is an interface
```

### 2.2 Causa Raiz

O Spring Data MongoDB usa `MappingMongoConverter` para converter documentos BSON em objetos Java. Para tipos polimórficos, ele:

1. Lê o campo `_class` do subdocumento BSON
2. Consulta o `MappingContext` para resolver o alias na classe concreta
3. Instancia a classe resolvida

O fluxo de resolução no `DefaultMongoTypeMapper` é:

```java
TypeInformation<?> resolveType(String alias, TypeInformation<?> basicType) {
    // 1. Tenta o cache
    if (cache.containsKey(alias)) return cache.get(alias);

    // 2. Tenta o mapa de aliases (construído a partir das entidades registradas)
    TypeInformation<?> resolved = typeMapper.resolveType(alias, basicType);
    if (resolved != null) return resolved;

    // 3. Fallback: Class.forName(alias)
    try {
        return ClassTypeInformation.from(Class.forName(alias));
    } catch (ClassNotFoundException e) {
        return basicType;  // ← PersistedComponent (interface!)
    }
}
```

O problema: para que o passo 2 funcione, a classe concreta precisa estar registrada como **entidade persistente** no `MappingContext`. Sem `@Document` ou `@Persistent` na classe, o alias `"persistedTabelaVo2Max"` nunca é mapeado para `PersistedTabelaVo2Max.class`.

- Se a classe TEM `@TypeAlias("persistedTabelaVo2Max")`, o `_class` escrito no MongoDB é `"persistedTabelaVo2Max"`
- Mas se a classe NÃO é uma entidade persistente, o alias não pode ser resolvido na leitura
- `Class.forName("persistedTabelaVo2Max")` falha (não é FQN)
- O tipo cai para `PersistedComponent` (interface), que não pode ser instanciada

### 2.3 Por que `@Persistent` não resolveu

Adicionar `org.springframework.data.annotation.Persistent` às classes concretas AS VEZES funciona (depende do pacote base de escaneamento). Mas:

- O escaneamento de entidades do `MongoMappingContext` só inclui `@Document` e `@Persistent` nos pacotes base configurados
- Se o pacote `composite` não está no escopo de escaneamento, as anotações são ignoradas
- O comportamento varia entre execução de testes (via `@SpringBootTest`) e execução real (via `spring-boot:run`)

---

## 3. Metodologia de Depuração

### 3.1 Hipótese Inicial

O `@Persistent` nas classes concretas deveria registrá-las no `MappingContext`, resolvendo os aliases.

**Teste:** Adicionar `@Persistent` + recompilar → **Erro persistiu.**

### 3.2 Segunda Hipótese

O escaneamento de entidades não encontra as classes em subpacotes.

**Teste:** Teste de integração que inspeciona `MappingContext.getPersistentEntities()` → mostrou que as classes `@Persistent` ERAM encontradas no teste, mas o erro ainda ocorria na aplicação real.

### 3.3 Terceira Hipótese

Documentos existentes no MongoDB foram salvos com `_class` pela versão antiga, e a resolução de alias difere entre escrita e leitura.

**Teste:** Deletar documentos e deixar o Initializer recriá-los → **Erro persistiu.**

### 3.4 Diagnóstico Final

O `MappingMongoConverter` tem **dois mecanismos de conversão** concorrentes:

1. **Conversão baseada em entidades** (`readDocument`): usa o `MappingContext` para resolver tipos — requer que a classe seja uma entidade persistente registrada.

2. **Conversores customizados** (`MongoCustomConversions`): registrados pelo usuário — têm **prioridade** sobre o mecanismo padrão (verificados primeiro em `DefaultConversionContext.convert()`).

A abordagem correta é **bypassar** o mecanismo de tipos do Spring Data usando conversores Jackson.

---

## 4. A Solução

### 4.1 Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                     MongoDB (BSON)                          │
├─────────────────────────────────────────────────────────────┤
│  TabelaClassificacaoDocument                                │
│  ├── _id: "classificacao_cooper_12min"                     │
│  ├── _class: "tabelaClassificacao"  ← MongoDB padrão       │
│  └── raiz: {                                                │
│        _class: "persistedTabelaVo2Max",   ← Jackson        │
│        protocolo: "COOPER",                                │
│        componentes: [...]                                   │
│      }                                                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
          ┌────────────┴────────────┐
          │  MongoCustomConversions │
          │                         │
          │  ReadingConverter       │
          │  Document → PersistedComponent  │
          │  (Jackson ObjectMapper) │
          │                         │
          │  WritingConverter       │
          │  PersistedComponent → Document  │
          │  (Jackson ObjectMapper) │
          └────────────┬────────────┘
                       │
          ┌────────────┴────────────┐
          │  PersistedComponentMixIn │
          │  @JsonTypeInfo(          │
          │    use=NAME,             │
          │    property="_class")    │
          │  @JsonSubTypes({         │
          │    "persistedTabelaVo2Max" → PersistedTabelaVo2Max,│
          │    ...                   │
          │  })                      │
          └─────────────────────────┘
```

### 4.2 Componentes

#### `PersistedComponentMixIn.java` (alterado)

```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_class")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PersistedTabelaEquipamento.class, name = "persistedTabelaEquipamento"),
    @JsonSubTypes.Type(value = PersistedTabelaIdade.class, name = "persistedTabelaIdade"),
    @JsonSubTypes.Type(value = PersistedTabelaSexo.class, name = "persistedTabelaSexo"),
    @JsonSubTypes.Type(value = PersistedTabelaVo2Max.class, name = "persistedTabelaVo2Max"),
    @JsonSubTypes.Type(value = PersistedNivelForca.class, name = "persistedNivelForca"),
    @JsonSubTypes.Type(value = PersistedNivelVo2Max.class, name = "persistedNivelVo2Max")
})
```

**Mudança crítica:** `property` mudou de `"tipo"` para `"_class"`, e os `name` agora correspondem exatamente aos `@TypeAlias` do MongoDB.

#### `PersistedComponentReadConverter.java`

```java
@ReadingConverter
public class PersistedComponentReadConverter implements Converter<Document, PersistedComponent> {
    private final ObjectMapper objectMapper;

    @Override
    public PersistedComponent convert(Document source) {
        return objectMapper.readValue(source.toJson(), PersistedComponent.class);
    }
}
```

#### `PersistedComponentWriteConverter.java`

```java
@WritingConverter
public class PersistedComponentWriteConverter implements Converter<PersistedComponent, Document> {
    private final ObjectMapper objectMapper;

    @Override
    public Document convert(PersistedComponent source) {
        String json = objectMapper.writeValueAsString(source);
        return Document.parse(json);
    }
}
```

#### `MongoConfig.java`

```java
@Configuration
public class MongoConfig {
    @Bean
    public MongoCustomConversions mongoCustomConversions(ObjectMapper objectMapper) {
        return MongoCustomConversions.create(adapter -> {
            adapter.registerConverter(new PersistedComponentReadConverter(objectMapper));
            adapter.registerConverter(new PersistedComponentWriteConverter(objectMapper));
        });
    }
}
```

### 4.3 Fluxo de Leitura

```
MongoTemplate.findById(id, TabelaClassificacaoDocument.class)
  → MappingMongoConverter.read(TabelaClassificacaoDocument.class, reader)
    → Para propriedade "raiz":
      → DefaultConversionContext.convert(Document, PersistedComponent)
        → MongoCustomConversions tem Converter<Document, PersistedComponent>? SIM
        → PersistedComponentReadConverter.convert(document)
          → document.toJson() → string JSON com _class: "persistedTabelaVo2Max"
          → objectMapper.readValue(json, PersistedComponent.class)
            → MixIn detecta _class: "persistedTabelaVo2Max"
            → Instancia PersistedTabelaVo2Max via @JsonSubTypes
            → Popula recursivamente toda a árvore
```

### 4.4 Fluxo de Escrita

```
repository.save(documento)
  → MappingMongoConverter.write(documento, writer)
    → Para propriedade "raiz":
      → Converter<PersistedComponent, Document> customizado
      → PersistedComponentWriteConverter.convert(componente)
        → objectMapper.writeValueAsString(componente)
          → MixIn adiciona _class: "persistedTabelaVo2Max"
          → Jackson serializa recursivamente toda a árvore
        → Document.parse(json) → Document BSON
```

---

## 5. Teste de Integração

### `PolymorphicRaizPersistenceIT.java`

```java
@BeforeEach
void setUp() {
    mongoTemplate.dropCollection(TabelaClassificacaoDocument.class);

    PersistedTabelaVo2Max raiz = new PersistedTabelaVo2Max(ProtocoloVo2Max.COOPER);
    PersistedTabelaSexo filho = new PersistedTabelaSexo(Sexo.MASCULINO);
    raiz.addComponente(filho);

    TabelaClassificacaoDocument doc = new TabelaClassificacaoDocument();
    doc.setNome("test-polymorphic");
    doc.setRaiz(raiz);

    TabelaClassificacaoDocument saved = mongoTemplate.save(doc);
    savedId = saved.getId();
}

@Test
void shouldPersistAndRetrievePolymorphicRaiz() {
    TabelaClassificacaoDocument loaded = mongoTemplate.findById(
            savedId, TabelaClassificacaoDocument.class);

    assertThat(loaded).isNotNull();
    assertThat(loaded.getRaiz()).isInstanceOfSatisfying(
        PersistedTabelaVo2Max.class, r -> {
            assertThat(r.getComponentes()).hasSize(1);
            assertThat(r.getComponentes().get(0))
                .isInstanceOf(PersistedTabelaSexo.class);
        });
}
```

O teste cobre o ciclo completo: **CREATE → SAVE → READ → ASSERT**. A saída esperada:

```
SUCCESS: raiz=PersistedTabelaVo2Max, filho=PersistedTabelaSexo
```

### 5.1 Saída do Endpoint (validação final)

```json
{
  "id": "classificacao_cooper_12min",
  "nome": "Classificação Cooper 12 min",
  "raiz": {
    "_class": "persistedTabelaVo2Max",
    "protocolo": "COOPER",
    "componentes": [
      {
        "_class": "persistedTabelaSexo",
        "sexo": "MASCULINO",
        "componentes": [
          {
            "_class": "persistedTabelaIdade",
            "idadeMin": 20,
            "idadeMax": 29,
            "componentes": [
              { "_class": "persistedNivelVo2Max", "classificacao": "RUIM", ... },
              { "_class": "persistedNivelVo2Max", "classificacao": "ABAIXO", ... }
            ]
          }
        ]
      }
    ]
  }
}
```

Status: **200 OK**

---

## 6. Referências

### Spring Data MongoDB — Type Mapping

- **`MappingMongoConverter`** (`org.springframework.data.mongodb.core.convert`)
  - `read(Class<S> type, BsonReader reader)` — método principal de leitura
  - `readDocument()` — lê documentos BSON, resolve `_class`
  - A implementação varia entre versões; no 4.x, o fluxo passa por `MongoConversionContext`

- **`DefaultMongoTypeMapper`** (`org.springframework.data.mongodb.core.convert`)
  - `readType(BsonReader, TypeInformation)` — lê `_class` e tenta resolver
  - `resolveType(String, TypeInformation)` — cache → alias map → Class.forName → fallback
  - Fonte: [Spring Data MongoDB — Type Mapper](https://docs.spring.io/spring-data/mongodb/reference/mongodb/mapping/type-mapping.html)

- **`AliasAwareTypeInformationMapper`** (`o.s.d.m.core.convert`)
  - Mantém o mapa `alias → Class<?>` construído a partir das entidades registradas
  - Só contém aliases de classes que são `PersistentEntity` no `MappingContext`

### Spring Data Commons — Entity Scanning

- **`MongoMappingContext`** (`o.s.d.m.core.mapping`)
  - `isCandidate(Class<?>)` — verifica `@Document` ou `@Persistent`
  - `getInitialEntitySet()` — escaneia pacotes base com `ClassPathScanningCandidateComponentProvider`
  - Fonte: [Spring Data MongoDB — Mapping](https://docs.spring.io/spring-data/mongodb/reference/mongodb/mapping.html)

### Jackson — Type Info

- **`@JsonTypeInfo`** — anotação para incluir informação de tipo na serialização
  - `use = Id.NAME` — usa nomes lógicos em vez de nomes de classe
  - `property = "_class"` — campo que carrega a informação de tipo
- **`@JsonSubTypes`** — mapeia nomes para classes concretas
- Documentação: [Jackson Polymorphic Type Handling](https://github.com/FasterXML/jackson-docs/wiki/JacksonPolymorphicDeserialization)

### MongoCustomConversions

- `MongoCustomConversions.create(Consumer<MongoConverterConfigurationAdapter>)` — registra conversores customizados
- Conversores com `@ReadingConverter` / `@WritingConverter` são categorizados automaticamente
- Custom converters têm prioridade sobre o mecanismo padrão
- Fonte: [Spring Data MongoDB — Custom Conversions](https://docs.spring.io/spring-data/mongodb/reference/mongodb/mapping-custom-conversions.html)

---

## 7. Decisões Técnicas

| Decisão | Alternativa Rejeitada | Motivo |
|---|---|---|
| `Converter<Document, PersistedComponent>` | `Converter<String, PersistedComponent>` | `Document` é a representação nativa do BSON, evita conversão extra |
| Jackson ObjectMapper | Gson / manual | Já está no classpath (Spring Boot Starter Web) e tem MixIn suportado |
| `@ReadingConverter` + `@WritingConverter` | `GenericConverter` | Mais simples, a categorização automática já atende |
| `property = "_class"` | `property = "tipo"` (original) | Alinha com o campo `_class` que o MongoDB já usa, evita duplicidade |
| Reutilizar `WebConfig` MixIn | MixIn separado para MongoDB vs HTTP | Apenas `PersistedComponent` usa o MixIn, e ela nunca é exposta diretamente em HTTP |

---

## 8. Lições Aprendidas

1. **O Spring Data MongoDB tem dois mecanismos de conversão concorrentes.** O mecanismo de entidades (`MappingMongoConverter`) e o de conversores customizados (`MongoCustomConversions`). Custom converters SEMPRE têm prioridade.

2. **`@Persistent` não garante que a classe será encontrada.** O escaneamento depende da configuração de pacotes base, que pode diferir entre execução de testes e execução via Maven.

3. **O campo `_class` no MongoDB é escrito pelo `DefaultMongoTypeMapper`**, que usa `@TypeAlias` da classe (mesmo sem `@Persistent`). Mas a LEITURA requer que o alias esteja mapeado — e sem a classe como entidade persistente, o mapa de aliases fica vazio.

4. **Jackson já gerencia polimorfismo corretamente.** `@JsonTypeInfo` + `@JsonSubTypes` fazem a desserialização polimórfica de forma confiável. A chave é usar `_class` como property name para alinhar com o que já está no MongoDB.
