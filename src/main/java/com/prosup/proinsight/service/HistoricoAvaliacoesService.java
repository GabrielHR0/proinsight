package com.prosup.proinsight.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.prosup.proinsight.api.dto.response.AvaliacaoHistoricoResponse;
import com.prosup.proinsight.api.dto.response.ReferenciaClassificacaoResponse;
import com.prosup.proinsight.config.TenantContext;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.model.ClassificacaoLegivel;
import com.prosup.proinsight.domain.model.TabelaClassificacao;
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
    private final ReferenciaClassificacaoService referenciaService;

    private final Cache<String, List<AvaliacaoHistoricoResponse>> cache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(60))
        .maximumSize(1_000)
        .build();

    public HistoricoAvaliacoesService(MongoTemplate mongoTemplate,
                                      ProtocoloAvaliacaoRepository protocoloRepository,
                                      ClienteRepository clienteRepository,
                                      TabelaClassificacaoRepository tabelaClassificacaoRepository,
                                      TabelaClassificacaoMapper tabelaClassificacaoMapper,
                                      ReferenciaClassificacaoService referenciaService) {
        this.mongoTemplate = mongoTemplate;
        this.protocoloRepository = protocoloRepository;
        this.clienteRepository = clienteRepository;
        this.tabelaClassificacaoRepository = tabelaClassificacaoRepository;
        this.tabelaClassificacaoMapper = tabelaClassificacaoMapper;
        this.referenciaService = referenciaService;
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
            .include("medicoes.frequenciasCardiacas")
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
            "frequenciaCardiacaBpm", "frequenciasCardiacas", "pesoKg",
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
            ReferenciaClassificacaoResponse referencia = referenciaService.extrair(tabela.getRaiz(), sexo, idade);
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
}