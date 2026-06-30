package com.prosup.proinsight.mapper;

import com.prosup.proinsight.adapter.out.persistence.AvaliacaoFisicaDocument;
import com.prosup.proinsight.adapter.out.persistence.MedicaoDocument;
import com.prosup.proinsight.domain.model.AvaliacaoFisica;
import com.prosup.proinsight.domain.model.Medicao;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para AvaliacaoFisica (entidade simples, não Composite).
 * 
 * Este mapper é diferente de ComponentConverter porque:
 * - AvaliacaoFisica é um agregado simples, não um Composite
 * - Medicoes precisam ser convertidas recursivamente
 * - Metadata e campos simples são copiados
 */
@Component
public class AvaliacaoFisicaMapper {

    public static AvaliacaoFisica toDomain(AvaliacaoFisicaDocument doc) {
        if (doc == null) {
            return null;
        }
        
        var avaliacao = new AvaliacaoFisica();
        avaliacao.setId(doc.getId());
        avaliacao.setClienteId(doc.getClienteId());
        avaliacao.setAvaliadorId(doc.getAvaliadorId());
        avaliacao.setStrategyKey(doc.getStrategyKey());
        avaliacao.setVersaoProtocolo(doc.getVersaoProtocolo());
        avaliacao.setMetadados(doc.getMetadados());
        
        // Converter medições: Document → Domain
        if (doc.getMedicoes() != null && !doc.getMedicoes().isEmpty()) {
            List<Medicao> medicoesDomain = doc.getMedicoes().stream()
                .map(AvaliacaoFisicaMapper::convertMedicaoDocumentToDomain)
                .collect(Collectors.toList());
            avaliacao.setMedicoes(medicoesDomain);
        } else {
            avaliacao.setMedicoes(new ArrayList<>());
        }
        
        return avaliacao;
    }

    public static AvaliacaoFisicaDocument toDocument(AvaliacaoFisica domain) {
        if (domain == null) {
            return null;
        }
        
        var doc = new AvaliacaoFisicaDocument();
        doc.setId(domain.getId());
        doc.setClienteId(domain.getClienteId());
        doc.setAvaliadorId(domain.getAvaliadorId());
        doc.setStrategyKey(domain.getStrategyKey());
        doc.setVersaoProtocolo(domain.getVersaoProtocolo());
        doc.setMetadados(domain.getMetadados());
        
        // Converter medições: Domain → Document
        if (domain.getMedicoes() != null && !domain.getMedicoes().isEmpty()) {
            List<MedicaoDocument> medicoesDoc = domain.getMedicoes().stream()
                .map(AvaliacaoFisicaMapper::convertMedicaoDomainToDocument)
                .collect(Collectors.toList());
            doc.setMedicoes(medicoesDoc);
        } else {
            doc.setMedicoes(new ArrayList<>());
        }
        
        return doc;
    }

    /**
     * Converte MedicaoDocument para Medicao (Domain).
     * Nota: Medicao é abstrata, então retorna a classe específica baseada no tipo.
     * Por enquanto, é placeholder - implementar de acordo com subclasses reais.
     */
    private static Medicao convertMedicaoDocumentToDomain(MedicaoDocument doc) {
        if (doc == null) {
            return null;
        }
        // TODO: Implementar conversão específica baseada em subtipo (IMC, VO2Max, etc)
        // Retornar instância correta do subtipo de Medicao
        return null;
    }

    /**
     * Converte Medicao (Domain) para MedicaoDocument.
     * Nota: Medicao é abstrata, então recebe a classe específica.
     * Por enquanto, é placeholder - implementar de acordo com subclasses reais.
     */
    private static MedicaoDocument convertMedicaoDomainToDocument(Medicao domain) {
        if (domain == null) {
            return null;
        }
        // TODO: Implementar conversão específica baseada em subtipo (IMC, VO2Max, etc)
        // Retornar instância correta do subtipo de MedicaoDocument
        return null;
    }
}