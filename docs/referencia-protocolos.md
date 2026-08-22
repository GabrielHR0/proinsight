# Referência dos Protocolos de Avaliação

## Protocolo Cooper 12 min

### Referência

**Cooper, K.H. (1968).** A means of assessing maximal oxygen intake. *Journal of the American Medical Association*, 203(3), 201–204.

### Descrição

Teste de campo máximo em que o avaliado corre a maior distância possível em 12 minutos. A classificação é determinada pela **distância percorrida em metros**, e o VO₂max é estimado a partir dessa distância.

### Cálculo do VO₂max

```
VO₂max (mL/kg/min) = (distanciaMetros - 504.9) / 44.73
```

### Tabela de Classificação — Distância (metros)

#### Homens

| Idade | MUITO_RUIM | RUIM | MÉDIO | BOM | EXCELENTE |
|-------|-----------|------|-------|-----|-----------|
| 20–29 | < 1600 | 1600–2199 | 2200–2399 | 2400–2799 | ≥ 2800 |
| 30–39 | < 1500 | 1500–1899 | 1900–2299 | 2300–2699 | ≥ 2700 |
| 40–49 | < 1400 | 1400–1699 | 1700–2099 | 2100–2499 | ≥ 2500 |
| 50–59 | < 1300 | 1300–1599 | 1600–1999 | 2000–2399 | ≥ 2400 |
| 60–99 | < 1200 | 1200–1499 | 1500–1899 | 1900–2299 | ≥ 2300 |

#### Mulheres

| Idade | MUITO_RUIM | RUIM | MÉDIO | BOM | EXCELENTE |
|-------|-----------|------|-------|-----|-----------|
| 20–29 | < 1500 | 1500–1799 | 1800–2099 | 2100–2299 | ≥ 2300 |
| 30–39 | < 1400 | 1400–1699 | 1700–1999 | 2000–2199 | ≥ 2200 |
| 40–49 | < 1200 | 1200–1499 | 1500–1799 | 1800–2099 | ≥ 2100 |
| 50–59 | < 1100 | 1100–1399 | 1400–1699 | 1700–1999 | ≥ 2000 |
| 60–99 | < 1000 | 1000–1299 | 1300–1599 | 1600–1899 | ≥ 1900 |

---

## Protocolo Rockport 1 mile

### Referência

**Kline, G.M., Porcari, J.P., Hintermeister, R., et al. (1987).** Estimation of VO2max from a one-mile track walk, gender, age, and body weight. *Medicine & Science in Sports & Exercise*, 19(3), 253–259.

**Classificação VO₂max:** *ACSM's Guidelines for Exercise Testing and Prescription* (11ª ed., 2022). Wolters Kluwer. Dados normativos do Cooper Institute e ACSM.

### Descrição

Teste de campo submáximo em que o avaliado caminha 1 milha (1.609 km) o mais rápido possível. O VO₂max é estimado diretamente pela fórmula de Rockport, que leva em conta gênero, idade, peso, tempo e frequência cardíaca.

### Cálculo do VO₂max

```
VO₂max = 132.853
       − (0.0769 × pesoLbs)
       − (0.3877 × idade)
       + (6.315 × sexo)
       − (3.2649 × tempoMinutos)
       − (0.1565 × frequenciaCardiaca)

Onde:
  pesoLbs = pesoKg × 2.20462
  sexo    = 1 para MASCULINO, 0 para FEMININO
```

### Tabela de Classificação — VO₂max (mL/kg/min)

Fonte: *ACSM Guidelines for Exercise Testing and Prescription* (11ª ed., 2022), via Continue (2026). Disponível em: https://www.continue.com.br/blog/vo2-max-o-que-e/

#### Homens

| Idade | MUITO_RUIM | RUIM | MÉDIO | BOM | EXCELENTE |
|-------|-----------|------|-------|-----|-----------|
| 20–29 | < 33 | 33–36 | 37–41 | 42–45 | ≥ 46 |
| 30–39 | < 31 | 31–34 | 35–39 | 40–43 | ≥ 44 |
| 40–49 | < 28 | 28–32 | 33–36 | 37–41 | ≥ 42 |
| 50–59 | < 25 | 25–28 | 29–33 | 34–37 | ≥ 38 |
| 60–99 | < 22 | 22–25 | 26–30 | 31–34 | ≥ 35 |

#### Mulheres

| Idade | MUITO_RUIM | RUIM | MÉDIO | BOM | EXCELENTE |
|-------|-----------|------|-------|-----|-----------|
| 20–29 | < 24 | 24–28 | 29–32 | 33–36 | ≥ 37 |
| 30–39 | < 22 | 22–26 | 27–30 | 31–34 | ≥ 35 |
| 40–49 | < 20 | 20–24 | 25–28 | 29–32 | ≥ 33 |
| 50–59 | < 18 | 18–21 | 22–25 | 26–29 | ≥ 30 |
| 60–99 | < 16 | 16–19 | 20–23 | 24–27 | ≥ 28 |

---

## Mapeamento para o Banco

As classificações acima são semeadas no MongoDB pelo `TabelaClassificacaoInitializer` na inicialização da aplicação. Cada protocolo gera dois documentos:

| Collection | ID | Protocolo |
|------------|-----|-----------|
| `tabelasClassificacao` | `classificacao_cooper_12min` | Cooper 12 min |
| `tabelasClassificacao` | `classificacao_rockport_1mile` | Rockport 1 mile |

A estrutura segue o padrão **Composite**:

```
TabelaVo2Max (protocolo: COOPER | ROCKPORT)
  └── TabelaSexo (MASCULINO)
  │     └── TabelaIdade (20–29, 30–39, ...)
  │           └── NivelVo2Max (MUITO_RUIM, RUIM, MÉDIO, BOM, EXCELENTE)
  └── TabelaSexo (FEMININO)
        └── TabelaIdade (20–29, 30–39, ...)
              └── NivelVo2Max (MUITO_RUIM, RUIM, MÉDIO, BOM, EXCELENTE)
```

## Fluxo de Classificação

```
Teste (Cooper/Rockport)
  → Calcula VO₂max via fórmula específica do protocolo
  → Percorre a árvore de classificação:
       Sexo → Idade → Nível
  → Retorna NivelVo2Max com:
       - classificação (ex: "EXCELENTE")
       - resultadoVo2Max (ex: 56 mL/kg/min)
```
