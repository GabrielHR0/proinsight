package com.prosup.proinsight.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.prosup.proinsight.api.dto.response.AvaliacaoHistoricoResponse;
import com.prosup.proinsight.api.dto.response.NivelReferenciaResponse;
import com.prosup.proinsight.api.dto.response.ReferenciaClassificacaoResponse;
import com.prosup.proinsight.config.TenantContext;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.model.ClassificacaoLegivel;
import com.prosup.proinsight.domain.model.TabelaClassificacao;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.Composite;
import com.prosup.proinsight.domain.model.composite.classes.NivelImc;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaIdade;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaSexo;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.TabelaClassificacaoMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Consulta otimizada do histórico de avaliações de um cliente:
 * leitura crua (sem desserialização polimórfica) com projection,
 * ordenada por createdAt desc via índice composto, e cache curto
 * invalidado a cada nova avaliação salva.
 */
@Service
public class HistoricoAvaliacoesService {

    private static final String COLLECTION = "avaliacoesFisicas";

    private final MongoTemplate mongoTemplate;
    private final ProtocoloAvaliacaoRepository protocoloRepository;
    private final ClienteRepository clienteRepository;
    private final TabelaClassificacaoRepository tabelaClassificacaoRepository;
    private final TabelaClassificacaoMapper tabelaClassificacaoMapper;

    private final Cache<String, List<AvaliacaoHistoricoResponse>> cache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(60))
        .maximumSize(1_000)
        .build();

    public HistoricoAvaliacoesService(MongoTemplate mongoTemplate,
                                      ProtocoloAvaliacaoRepository protocoloRepository,
                                      ClienteRepository clienteRepository,
                                      TabelaClassificacaoRepository tabelaClassificacaoRepository,
                                      TabelaClassificacaoMapper tabelaClassificacaoMapper) {
        this.mongoTemplate = mongoTemplate;
        this.protocoloRepository = protocoloRepository;
        this.clienteRepository = clienteRepository;
        this.tabelaClassificacaoRepository = tabelaClassificacaoRepository;
        this.tabelaClassificacaoMapper = tabelaClassificacaoMapper;
    }

    public List<AvaliacaoHistoricoResponse> listarPorCliente(String clienteId) {
        String academiaId = TenantContext.getAcademiaId();
        String key = (academiaId == null ? "" : academiaId) + ":" + clienteId;
        return cache.get(key, k -> buscarNoMongo(clienteId));
    }

    public void invalidar(String clienteId) {
        cache.asMap().keySet().removeIf(k -> k.endsWith(":" + clienteId));
    }

    private List<AvaliacaoHistoricoResponse> buscarNoMongo(String clienteId) {
        var query = new Query(Criteria.where("clienteId").is(clienteId))
            .with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.fields()
            .include("clienteId")
            .include("protocoloId")
            .include("createdAt")
            .include("medicoes.tipo")
            .include("medicoes.protocolo")
            .include("medicoes.medidoEm")
            .include("medicoes.observacoes")
            .include("medicoes.vo2MaxCalculado")
            .include("medicoes.classificacaoVo2")
            .include("medicoes.velocidadeKmh")
            .include("medicoes.inclinacaoPercent")
            .include("medicoes.distanciaMetros")
            .include("medicoes.tempoSegundos")
            .include("medicoes.frequenciaCardiacaBpm")
            .include("medicoes.pesoKg")
            .include("medicoes.imcCalculado")
            .include("medicoes.classificacaoImc")
            .include("medicoes.massaCorporalGramas")
            .include("medicoes.alturaCm")
            .include("medicoes.percentualGordura")
            .include("medicoes.massaMagraKg")
            .include("medicoes.massaGordaKg")
            .include("medicoes.aguaCorporalPercentual")
            .include("medicoes.gorduraVisceral")
            .include("medicoes.tmbKcal")
            .include("medicoes.idadeMetabolica");

        List<Document> rawDocs = mongoTemplate.find(query, Document.class, COLLECTION);
        if (rawDocs.isEmpty()) {
            return List.of();
        }

        Map<String, String> protocoloNomes = new LinkedHashMap<>();
        protocoloRepository.findAll().forEach(p ->
            protocoloNomes.put(p.getId(), p.getNome())
        );

        Map<String, ReferenciaClassificacaoResponse> referenciasPorProtocolo =
            montarReferenciasPorProtocolo(protocoloNomes.keySet(), clienteId);

        List<AvaliacaoHistoricoResponse> responses = new ArrayList<>(rawDocs.size());
        for (Document doc : rawDocs) {
            responses.add(toResponse(doc, protocoloNomes, referenciasPorProtocolo));
        }
        return List.copyOf(responses);
    }

    private AvaliacaoHistoricoResponse toResponse(Document doc,
                                                  Map<String, String> protocoloNomes,
                                                  Map<String, ReferenciaClassificacaoResponse> referenciasPorProtocolo) {
        String id = doc.getObjectId("_id") != null
            ? doc.getObjectId("_id").toHexString()
            : doc.getString("_id");
        String clienteId = doc.getString("clienteId");
        String protocoloId = doc.getString("protocoloId");
        Instant createdAt = doc.getDate("createdAt") != null
            ? doc.getDate("createdAt").toInstant()
            : null;

        List<Document> medicoes = castMedicoes(doc.get("medicoes"));
        Document medicao = medicoes.isEmpty() ? null : medicoes.get(0);

        String tipo = medicao != null ? medicao.getString("tipo") : null;
        Double valor = extrairValor(medicao, tipo);
        String classificacao = extrairClassificacao(medicao, tipo);
        Map<String, Object> detalhes = montarDetalhes(medicao);
        ReferenciaClassificacaoResponse referencias =
            protocoloId != null ? referenciasPorProtocolo.get(protocoloId) : null;

        return new AvaliacaoHistoricoResponse(
            id,
            clienteId,
            protocoloId,
            protocoloId != null ? protocoloNomes.get(protocoloId) : null,
            tipo,
            createdAt != null ? createdAt.toString() : null,
            valor,
            classificacao,
            ClassificacaoLegivel.humanizar(classificacao),
            detalhes,
            referencias
        );
    }

    @SuppressWarnings("unchecked")
    private static List<Document> castMedicoes(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> lista) {
            List<Document> result = new ArrayList<>(lista.size());
            for (Object item : lista) {
                if (item instanceof Document d) {
                    result.add(d);
                }
            }
            return result;
        }
        return List.of();
    }

    private static Double extrairValor(Document medicao, String tipo) {
        if (medicao == null || tipo == null) {
            return null;
        }
        return switch (tipo) {
            case "VO2_MAX" -> {
                Integer v = medicao.getInteger("vo2MaxCalculado");
                yield v != null ? v.doubleValue() : null;
            }
            case "IMC" -> medicao.getDouble("imcCalculado");
            case "BIOIMPEDANCIA" -> medicao.getDouble("pesoKg");
            default -> null;
        };
    }

    private static String extrairClassificacao(Document medicao, String tipo) {
        if (medicao == null || tipo == null) {
            return null;
        }
        return switch (tipo) {
            case "VO2_MAX" -> medicao.getString("classificacaoVo2");
            case "IMC" -> medicao.getString("classificacaoImc");
            default -> null;
        };
    }

    private static Map<String, Object> montarDetalhes(Document medicao) {
        if (medicao == null) {
            return Map.of();
        }
        Map<String, Object> detalhes = new LinkedHashMap<>();
        for (String campo : List.of(
            "protocolo", "medidoEm", "observacoes",
            "velocidadeKmh", "inclinacaoPercent", "distanciaMetros", "tempoSegundos",
            "frequenciaCardiacaBpm", "pesoKg",
            "massaCorporalGramas", "alturaCm",
            "percentualGordura", "massaMagraKg", "massaGordaKg",
            "aguaCorporalPercentual", "gorduraVisceral", "tmbKcal", "idadeMetabolica"
        )) {
            Object valor = medicao.get(campo);
            if (valor != null) {
                detalhes.put(campo, valor);
            }
        }
        return detalhes;
    }

    private Map<String, ReferenciaClassificacaoResponse> montarReferenciasPorProtocolo(Set<String> protocoloIds,
                                                                                       String clienteId) {
        if (protocoloIds.isEmpty()) {
            return Map.of();
        }
        ClienteDocument cliente = clienteRepository.findById(clienteId).orElse(null);
        if (cliente == null) {
            return Map.of();
        }
        Sexo sexo = cliente.getSexo();
        Integer idade = calcularIdade(cliente.getDataNascimento());

        Map<String, ReferenciaClassificacaoResponse> resultado = new HashMap<>();
        for (String protocoloId : protocoloIds) {
            ProtocoloAvaliacaoDocument protocolo = protocoloRepository.findById(protocoloId).orElse(null);
            if (protocolo == null || protocolo.getTabelaClassificacaoId() == null) {
                continue;
            }
            TabelaClassificacaoDocument doc = tabelaClassificacaoRepository
                .findById(protocolo.getTabelaClassificacaoId())
                .orElse(null);
            if (doc == null) {
                continue;
            }
            TabelaClassificacao tabela = tabelaClassificacaoMapper.toDomain(doc);
            ReferenciaClassificacaoResponse referencia = extrairReferencia(tabela.getRaiz(), sexo, idade);
            if (referencia != null) {
                resultado.put(protocoloId, referencia);
            }
        }
        return resultado;
    }

    private static Integer calcularIdade(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            return null;
        }
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    private static ReferenciaClassificacaoResponse extrairReferencia(Component raiz, Sexo sexo, Integer idade) {
        if (!(raiz instanceof Composite composite)) {
            return null;
        }

        List<NivelImc> niveisImc = new ArrayList<>();
        List<TabelaSexo> tabelasSexo = new ArrayList<>();
        for (Component child : composite.getChildren()) {
            if (child instanceof NivelImc nivel) {
                niveisImc.add(nivel);
            } else if (child instanceof TabelaSexo tabela) {
                tabelasSexo.add(tabela);
            }
        }

        if (!niveisImc.isEmpty()) {
            List<NivelReferenciaResponse> niveis = ordenarPorMinimo(niveisImc.stream()
                .map(nivel -> toReferencia(nivel.getClassificacao(), nivel.getMin(), nivel.getMax(),
                    nivel.getTipoMin() != null ? nivel.getTipoMin().name() : null,
                    nivel.getTipoMax() != null ? nivel.getTipoMax().name() : null))
                .toList());
            return niveis.isEmpty() ? null
                : new ReferenciaClassificacaoResponse(null, null, null, niveis);
        }

        if (sexo == null) {
            return null;
        }
        for (TabelaSexo tabela : tabelasSexo) {
            if (tabela.getSexo() != sexo) {
                continue;
            }
            TabelaIdade faixa = selecionarFaixaEtaria(tabela, idade);
            if (faixa == null) {
                return null;
            }
            List<NivelReferenciaResponse> niveis = ordenarPorMinimo(faixa.getChildren().stream()
                .filter(NivelVo2Max.class::isInstance)
                .map(nivel -> {
                    NivelVo2Max n = (NivelVo2Max) nivel;
                    return toReferencia(n.getClassificacao(), n.getMin(), n.getMax(),
                        n.getTipoMin() != null ? n.getTipoMin().name() : null,
                        n.getTipoMax() != null ? n.getTipoMax().name() : null);
                })
                .toList());
            if (niveis.isEmpty()) {
                return null;
            }
            return new ReferenciaClassificacaoResponse(
                sexo.name(), faixa.getIdadeMin(), faixa.getIdadeMax(), niveis);
        }
        return null;
    }

    private static TabelaIdade selecionarFaixaEtaria(TabelaSexo tabela, Integer idade) {
        List<TabelaIdade> faixas = new ArrayList<>();
        for (Component child : tabela.getChildren()) {
            if (child instanceof TabelaIdade faixa) {
                faixas.add(faixa);
            }
        }
        if (faixas.isEmpty()) {
            return null;
        }
        if (idade != null) {
            for (TabelaIdade faixa : faixas) {
                if (idade >= faixa.getIdadeMin() && idade <= faixa.getIdadeMax()) {
                    return faixa;
                }
            }
        }
        // Sem idade ou sem match exato: usa a faixa etária mais próxima,
        // replicando o fallback do classificador (TabelaSexo).
        TabelaIdade maisProxima = null;
        long melhorDistancia = Long.MAX_VALUE;
        for (TabelaIdade faixa : faixas) {
            long distancia;
            if (idade == null) {
                distancia = 0;
            } else if (idade < faixa.getIdadeMin()) {
                distancia = faixa.getIdadeMin() - idade;
            } else if (idade > faixa.getIdadeMax()) {
                distancia = idade - faixa.getIdadeMax();
            } else {
                distancia = 0;
            }
            if (distancia < melhorDistancia) {
                melhorDistancia = distancia;
                maisProxima = faixa;
            }
        }
        return maisProxima;
    }

    private static List<NivelReferenciaResponse> ordenarPorMinimo(List<NivelReferenciaResponse> niveis) {
        if (niveis.size() < 2) {
            return niveis;
        }
        List<NivelReferenciaResponse> ordenados = new ArrayList<>(niveis);
        ordenados.sort(Comparator.comparing(NivelReferenciaResponse::min,
            Comparator.nullsFirst(Comparator.naturalOrder())));
        return List.copyOf(ordenados);
    }

    private static NivelReferenciaResponse toReferencia(String classificacao, Double min, Double max,
                                                        String tipoMin, String tipoMax) {
        return new NivelReferenciaResponse(
            classificacao,
            ClassificacaoLegivel.humanizar(classificacao),
            min,
            max,
            tipoMin,
            tipoMax
        );
    }
}