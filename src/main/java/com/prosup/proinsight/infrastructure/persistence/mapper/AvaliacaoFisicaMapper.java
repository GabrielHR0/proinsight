package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.model.Medicao;
import com.prosup.proinsight.domain.model.AvaliacaoFisica;
import com.prosup.proinsight.domain.model.MedicaoImc;
import com.prosup.proinsight.domain.model.MedicaoVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteImc;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxCooper;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxEsteiraIncremental;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxRockport;
import com.prosup.proinsight.infrastructure.persistence.document.AvaliacaoFisicaDocument;
import com.prosup.proinsight.infrastructure.persistence.document.MedicaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.MedicaoImcDocument;
import com.prosup.proinsight.infrastructure.persistence.document.MedicaoVo2MaxDocument;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AvaliacaoFisicaMapper {

    private final Map<MedicaoTipo, Function<MedicaoDocument, Medicao>> documentToDomain;
    private final Map<MedicaoTipo, Function<Medicao, MedicaoDocument>> domainToDocument;

    public AvaliacaoFisicaMapper() {
        documentToDomain = new HashMap<>();
        documentToDomain.put(MedicaoTipo.VO2_MAX, this::vo2MaxToDomain);
        documentToDomain.put(MedicaoTipo.IMC, this::imcToDomain);

        domainToDocument = new HashMap<>();
        domainToDocument.put(MedicaoTipo.VO2_MAX, this::vo2MaxToDocument);
        domainToDocument.put(MedicaoTipo.IMC, this::imcToDocument);
    }

    public AvaliacaoFisica toDomain(AvaliacaoFisicaDocument doc) {
        if (doc == null) {
            return null;
        }

        var avaliacao = new AvaliacaoFisica();
        avaliacao.setId(doc.getId());
        avaliacao.setClienteId(doc.getClienteId());
        avaliacao.setAvaliadorId(doc.getAvaliadorId());
        avaliacao.setProtocoloId(doc.getProtocoloId());

        if (doc.getMedicoes() != null && !doc.getMedicoes().isEmpty()) {
            List<Medicao> medicoesDomain = doc.getMedicoes().stream()
                .map(this::convertMedicaoDocumentToDomain)
                .collect(Collectors.toList());
            avaliacao.setMedicoes(medicoesDomain);
        } else {
            avaliacao.setMedicoes(new ArrayList<>());
        }

        return avaliacao;
    }

    public AvaliacaoFisicaDocument toDocument(AvaliacaoFisica domain) {
        if (domain == null) {
            return null;
        }

        var doc = new AvaliacaoFisicaDocument();
        doc.setId(domain.getId());
        doc.setClienteId(domain.getClienteId());
        doc.setAvaliadorId(domain.getAvaliadorId());
        doc.setProtocoloId(domain.getProtocoloId());

        if (domain.getMedicoes() != null && !domain.getMedicoes().isEmpty()) {
            List<MedicaoDocument> medicoesDoc = domain.getMedicoes().stream()
                .map(this::convertMedicaoDomainToDocument)
                .collect(Collectors.toList());
            doc.setMedicoes(medicoesDoc);
        } else {
            doc.setMedicoes(new ArrayList<>());
        }

        return doc;
    }

    private Medicao convertMedicaoDocumentToDomain(MedicaoDocument doc) {
        if (doc == null) return null;
        var fn = documentToDomain.get(doc.getTipo());
        if (fn == null) {
            throw new IllegalArgumentException("Tipo de medição não mapeado: " + doc.getTipo());
        }
        return fn.apply(doc);
    }

    private MedicaoDocument convertMedicaoDomainToDocument(Medicao domain) {
        if (domain == null) return null;
        var fn = domainToDocument.get(domain.getTipo());
        if (fn == null) {
            throw new IllegalArgumentException("Tipo de medição não mapeado: " + domain.getTipo());
        }
        return fn.apply(domain);
    }

    private Medicao imcToDomain(MedicaoDocument doc) {
        var i = (MedicaoImcDocument) doc;

        TesteImc teste = new TesteImc(
            i.getMassaCorporalGramas(),
            i.getAlturaCm()
        );

        MedicaoImc medicao = new MedicaoImc(
            MedicaoTipo.IMC,
            i.getMedidoEm(), i.getCreatedAt(), i.getUpdatedAt(),
            i.getObservacoes(),
            List.of(teste)
        );

        medicao.setResultado(i.getImcCalculado());
        return medicao;
    }

    private Medicao vo2MaxToDomain(MedicaoDocument doc) {
        var v = (MedicaoVo2MaxDocument) doc;

        var testes = new ArrayList<TesteVo2Max>();

        if (v.getProtocolo() == Protocolo.ROCKPORT) {
            double tempoMinutos = v.getTempoSegundos() != null
                ? v.getTempoSegundos() / 60.0
                : 0;
            testes.add(new TesteVo2MaxRockport(tempoMinutos, v.getFrequenciaCardiacaBpm(), v.getPesoKg()));
        } else if (v.getProtocolo() == Protocolo.ESTEIRA_INCREMENTAL) {
            testes.add(new TesteVo2MaxEsteiraIncremental(v.getVelocidadeKmh(), v.getInclinacaoPercent()));
        } else {
            testes.add(new TesteVo2MaxCooper(v.getDistanciaMetros()));
        }

        if (v.getTestesAdicionais() != null) {
            for (var item : v.getTestesAdicionais()) {
                if (item instanceof java.util.Map<?, ?> map) {
                    String protocolo = (String) map.get("protocolo");
                    if (Protocolo.ROCKPORT.name().equals(protocolo)) {
                        Double t = (Double) map.get("tempoMinutos");
                        Integer fc = (Integer) map.get("frequenciaCardiaca");
                        Double peso = (Double) map.get("pesoKg");
                        testes.add(new TesteVo2MaxRockport(t != null ? t : 0, fc, peso));
                    } else if (Protocolo.ESTEIRA_INCREMENTAL.name().equals(protocolo)) {
                        Double vel = (Double) map.get("velocidadeKmh");
                        Double inc = (Double) map.get("inclinacaoPercent");
                        testes.add(new TesteVo2MaxEsteiraIncremental(vel, inc));
                    } else {
                        Integer dist = (Integer) map.get("distanciaMetros");
                        testes.add(new TesteVo2MaxCooper(dist != null ? dist : 0));
                    }
                }
            }
        }

        MedicaoVo2Max medicao = new MedicaoVo2Max(
            MedicaoTipo.VO2_MAX,
            v.getMedidoEm(), v.getCreatedAt(), v.getUpdatedAt(),
            v.getObservacoes(),
            testes
        );

        medicao.setResultado(v.getVo2MaxCalculado());
        return medicao;
    }

    private MedicaoDocument imcToDocument(Medicao domain) {
        var m = (MedicaoImc) domain;
        var teste = m.getTeste();

        var doc = new MedicaoImcDocument();
        doc.setMassaCorporalGramas(teste.getMassaCorporalGramas());
        doc.setAlturaCm(teste.getAlturaCentimetros());
        doc.setImcCalculado(m.getResultado());
        doc.setMedidoEm(m.getMedidoEm());
        doc.setCreatedAt(m.getCreatedAt());
        doc.setUpdatedAt(m.getUpdatedAt());
        doc.setObservacoes(m.getObservacoes());
        return doc;
    }

    private MedicaoDocument vo2MaxToDocument(Medicao domain) {
        var v = (MedicaoVo2Max) domain;
        var testes = v.getTestes();

        var doc = new MedicaoVo2MaxDocument();
        doc.setVo2MaxCalculado(v.getResultado());
        doc.setMedidoEm(v.getMedidoEm());
        doc.setCreatedAt(v.getCreatedAt());
        doc.setUpdatedAt(v.getUpdatedAt());
        doc.setObservacoes(v.getObservacoes());

        if (testes == null || testes.isEmpty()) return doc;

        var primeiro = testes.get(0);
        doc.setProtocolo(primeiro.getProtocolo());

        if (primeiro instanceof TesteVo2MaxCooper cooper) {
            doc.setDistanciaMetros(cooper.getDistanciaMetros());
        } else if (primeiro instanceof TesteVo2MaxEsteiraIncremental esteira) {
            doc.setVelocidadeKmh(esteira.getVelocidadeKmh());
            doc.setInclinacaoPercent(esteira.getInclinacaoPercent());
        } else if (primeiro instanceof TesteVo2MaxRockport rockport) {
            doc.setTempoSegundos(rockport.getTempoMinutos() != null
                ? (int) Math.round(rockport.getTempoMinutos() * 60)
                : null);
            doc.setFrequenciaCardiacaBpm(rockport.getFrequenciaCardiaca());
            doc.setPesoKg(rockport.getPesoKg());
        }

        if (testes.size() > 1) {
            doc.setTestesAdicionais(new ArrayList<>());
            for (int i = 1; i < testes.size(); i++) {
                var t = testes.get(i);
                var map = new java.util.HashMap<String, Object>();
                map.put("protocolo", t.getProtocolo().name());
                if (t instanceof TesteVo2MaxCooper c) {
                    map.put("distanciaMetros", c.getDistanciaMetros());
                } else if (t instanceof TesteVo2MaxEsteiraIncremental e) {
                    map.put("velocidadeKmh", e.getVelocidadeKmh());
                    map.put("inclinacaoPercent", e.getInclinacaoPercent());
                } else if (t instanceof TesteVo2MaxRockport r) {
                    map.put("tempoMinutos", r.getTempoMinutos());
                    map.put("frequenciaCardiaca", r.getFrequenciaCardiaca());
                    map.put("pesoKg", r.getPesoKg());
                }
                doc.getTestesAdicionais().add(map);
            }
        }

        return doc;
    }

    public AvaliacaoFisicaDocument toImcDocument(String clienteId, String avaliadorId, String protocoloId, MedicaoImc medicao, double imcCalculado, String classificacao) {
        var medicaoDoc = new MedicaoImcDocument();
        medicaoDoc.setMedidoEm(medicao.getMedidoEm());
        medicaoDoc.setImcCalculado(imcCalculado);
        medicaoDoc.setClassificacaoImc(classificacao);
        var teste = medicao.getTestes().get(0);
        medicaoDoc.setMassaCorporalGramas(teste.getMassaCorporalGramas());
        medicaoDoc.setAlturaCm(teste.getAlturaCentimetros());

        var avaliacaoDoc = new AvaliacaoFisicaDocument();
        avaliacaoDoc.setClienteId(clienteId);
        avaliacaoDoc.setAvaliadorId(avaliadorId);
        avaliacaoDoc.setProtocoloId(protocoloId);
        avaliacaoDoc.setMedicoes(List.of(medicaoDoc));

        return avaliacaoDoc;
    }

    public AvaliacaoFisicaDocument toVo2MaxDocument(String clienteId, String avaliadorId, String protocoloId, MedicaoVo2Max medicao, String classificacao) {
        var medicaoDoc = new MedicaoVo2MaxDocument();
        medicaoDoc.setMedidoEm(medicao.getMedidoEm());
        medicaoDoc.setObservacoes(medicao.getObservacoes());
        medicaoDoc.setVo2MaxCalculado(medicao.getResultado());

        var primeiroTeste = medicao.getTestes().get(0);
        medicaoDoc.setProtocolo(primeiroTeste.getProtocolo());
        if (primeiroTeste instanceof TesteVo2MaxCooper cooper) {
            medicaoDoc.setDistanciaMetros(cooper.getDistanciaMetros());
        } else if (primeiroTeste instanceof TesteVo2MaxEsteiraIncremental esteira) {
            medicaoDoc.setVelocidadeKmh(esteira.getVelocidadeKmh());
            medicaoDoc.setInclinacaoPercent(esteira.getInclinacaoPercent());
        } else if (primeiroTeste instanceof TesteVo2MaxRockport rockport) {
            medicaoDoc.setTempoSegundos(rockport.getTempoMinutos() != null
                    ? (int) Math.round(rockport.getTempoMinutos() * 60)
                    : null);
            medicaoDoc.setFrequenciaCardiacaBpm(rockport.getFrequenciaCardiaca());
            medicaoDoc.setPesoKg(rockport.getPesoKg());
        }

        if (classificacao != null) {
            medicaoDoc.setClassificacaoVo2(classificacao);
        }

        if (medicao.getTestes().size() > 1) {
            medicaoDoc.setTestesAdicionais(new ArrayList<>());
            for (int i = 1; i < medicao.getTestes().size(); i++) {
                var t = medicao.getTestes().get(i);
                var map = new HashMap<String, Object>();
                map.put("protocolo", t.getProtocolo().name());
                if (t instanceof TesteVo2MaxCooper c) {
                    map.put("distanciaMetros", c.getDistanciaMetros());
                } else if (t instanceof TesteVo2MaxEsteiraIncremental e) {
                    map.put("velocidadeKmh", e.getVelocidadeKmh());
                    map.put("inclinacaoPercent", e.getInclinacaoPercent());
                } else if (t instanceof TesteVo2MaxRockport r) {
                    map.put("tempoMinutos", r.getTempoMinutos());
                    map.put("frequenciaCardiaca", r.getFrequenciaCardiaca());
                    map.put("pesoKg", r.getPesoKg());
                }
                medicaoDoc.getTestesAdicionais().add(map);
            }
        }

        var avaliacaoDoc = new AvaliacaoFisicaDocument();
        avaliacaoDoc.setClienteId(clienteId);
        avaliacaoDoc.setAvaliadorId(avaliadorId);
        avaliacaoDoc.setProtocoloId(protocoloId);
        avaliacaoDoc.setMedicoes(List.of(medicaoDoc));

        return avaliacaoDoc;
    }
}
