package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.domain.model.aquecimento.*;
import com.prosup.proinsight.infrastructure.persistence.document.AquecimentoDocument;
import org.springframework.stereotype.Component;

@Component
public class AquecimentoMapper {

    public AquecimentoDocument toDocument(AquecimentoVo2Max domain) {
        if (domain == null) return null;

        var doc = new AquecimentoDocument();
        doc.setProtocolo(domain.getProtocolo());
        doc.setDescricao(domain.getDescricao());
        doc.setObservacoes(domain.getObservacoes());
        doc.setTempoMinutos(domain.getTempoMinutos());

        if (domain instanceof AquecimentoVo2MaxCooper cooper) {
            doc.setDistanciaMetros(cooper.getDistanciaMetros());
        } else if (domain instanceof AquecimentoVo2MaxRockport rockport) {
            doc.setDistanciaMetros(rockport.getDistanciaMetros());
        } else if (domain instanceof AquecimentoVo2MaxEsteiraIncremental esteira) {
            doc.setVelocidadeKmh(esteira.getVelocidadeKmh());
            doc.setInclinacaoPercent(esteira.getInclinacaoPercent());
        }

        return doc;
    }

    public AquecimentoVo2Max toDomain(AquecimentoDocument doc) {
        if (doc == null) return null;

        if (doc.getProtocolo() == null) return null;

        return switch (doc.getProtocolo()) {
            case COOPER -> {
                var a = new AquecimentoVo2MaxCooper(doc.getDistanciaMetros(), doc.getTempoMinutos());
                a.setObservacoes(doc.getObservacoes());
                yield a;
            }
            case ROCKPORT -> {
                var a = new AquecimentoVo2MaxRockport(doc.getTempoMinutos(), doc.getDistanciaMetros());
                a.setObservacoes(doc.getObservacoes());
                yield a;
            }
            case ESTEIRA, ESTEIRA_INCREMENTAL -> {
                var a = new AquecimentoVo2MaxEsteiraIncremental(
                        doc.getVelocidadeKmh(), doc.getInclinacaoPercent(), doc.getTempoMinutos());
                a.setObservacoes(doc.getObservacoes());
                yield a;
            }
        };
    }
}
