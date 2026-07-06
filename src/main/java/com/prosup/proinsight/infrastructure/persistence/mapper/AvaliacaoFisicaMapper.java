package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.Medicao;
import com.prosup.proinsight.domain.model.AvaliacaoFisica;
import com.prosup.proinsight.domain.model.MedicaoImc;
import com.prosup.proinsight.domain.model.MedicaoVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteImc;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxCooper;
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
        avaliacao.setStrategyKey(doc.getStrategyKey());

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
        doc.setStrategyKey(domain.getStrategyKey());

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
            i.getObservacoes(), i.getTabelaClassificacaoId(),
            List.of(teste)
        );

        medicao.setResultado(i.getImcCalculado());
        return medicao;
    }

    private Medicao vo2MaxToDomain(MedicaoDocument doc) {
        var v = (MedicaoVo2MaxDocument) doc;

        TesteVo2MaxCooper teste = new TesteVo2MaxCooper(v.getDistanciaMetros());

        MedicaoVo2Max medicao = new MedicaoVo2Max(
            MedicaoTipo.VO2_MAX,
            v.getMedidoEm(), v.getCreatedAt(), v.getUpdatedAt(),
            v.getObservacoes(), v.getTabelaClassificacaoId(),
            List.of(teste)
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
        doc.setTabelaClassificacaoId(m.getTabelaClassificacaoId());
        return doc;
    }

    private MedicaoDocument vo2MaxToDocument(Medicao domain) {
        var v = (MedicaoVo2Max) domain;
        var teste = (TesteVo2MaxCooper) v.getTestes().get(0);

        var doc = new MedicaoVo2MaxDocument();
        doc.setDistanciaMetros(teste.getDistanciaMetros());
        doc.setProtocolo(teste.getProtocolo());
        doc.setVo2MaxCalculado(v.getResultado());
        doc.setMedidoEm(v.getMedidoEm());
        doc.setCreatedAt(v.getCreatedAt());
        doc.setUpdatedAt(v.getUpdatedAt());
        doc.setObservacoes(v.getObservacoes());
        doc.setTabelaClassificacaoId(v.getTabelaClassificacaoId());
        return doc;
    }
}
