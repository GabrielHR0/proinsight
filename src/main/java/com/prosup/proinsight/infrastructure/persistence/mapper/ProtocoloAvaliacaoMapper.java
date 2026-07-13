package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.api.dto.request.ProtocoloAvaliacaoRequest;
import com.prosup.proinsight.api.dto.response.ProtocoloAvaliacaoResponse;
import com.prosup.proinsight.domain.model.ProtocoloAvaliacao;
import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import org.springframework.stereotype.Component;

@Component
public class ProtocoloAvaliacaoMapper {

    public ProtocoloAvaliacaoDocument toDocument(ProtocoloAvaliacaoRequest req) {
        if (req == null) return null;
        var doc = new ProtocoloAvaliacaoDocument();
        doc.setNome(req.getNome());
        doc.setProtocoloVo2Max(req.getProtocoloVo2Max());
        doc.setStrategyKey(req.getStrategyKey());
        doc.setTabelaClassificacaoId(req.getTabelaClassificacaoId());
        doc.setDescricao(req.getDescricao());
        doc.setComoRealizar(req.getComoRealizar());
        doc.setCalculadora(req.getCalculadora());
        doc.setReferenciaBibliografica(req.getReferenciaBibliografica());
        doc.setUnidadeMedida(req.getUnidadeMedida());
        doc.setTempoMinimoSegundos(req.getTempoMinimoSegundos());
        doc.setTempoMaximoSegundos(req.getTempoMaximoSegundos());
        doc.setEquipamentoNecessario(req.getEquipamentoNecessario());
        doc.setCriteriosExclusao(req.getCriteriosExclusao());
        doc.setObservacoes(req.getObservacoes());
        return doc;
    }

    public ProtocoloAvaliacao toDomain(ProtocoloAvaliacaoDocument doc) {
        if (doc == null) return null;
        var domain = new ProtocoloAvaliacao(
            doc.getId(), doc.getNome(), doc.getProtocoloVo2Max(),
            doc.getStrategyKey(), doc.getTabelaClassificacaoId()
        );
        domain.setDescricao(doc.getDescricao());
        domain.setComoRealizar(doc.getComoRealizar());
        domain.setCalculadora(doc.getCalculadora());
        domain.setReferenciaBibliografica(doc.getReferenciaBibliografica());
        domain.setUnidadeMedida(doc.getUnidadeMedida());
        domain.setTempoMinimoSegundos(doc.getTempoMinimoSegundos());
        domain.setTempoMaximoSegundos(doc.getTempoMaximoSegundos());
        domain.setEquipamentoNecessario(doc.getEquipamentoNecessario());
        domain.setCriteriosExclusao(doc.getCriteriosExclusao());
        domain.setObservacoes(doc.getObservacoes());
        domain.setCreatedAt(doc.getCreatedAt());
        domain.setUpdatedAt(doc.getUpdatedAt());
        return domain;
    }

    public ProtocoloAvaliacaoResponse toResponse(ProtocoloAvaliacao domain) {
        if (domain == null) return null;
        return new ProtocoloAvaliacaoResponse(
            domain.getId(),
            domain.getNome(),
            domain.getProtocoloVo2Max(),
            domain.getStrategyKey(),
            domain.getTabelaClassificacaoId(),
            domain.getDescricao(),
            domain.getComoRealizar(),
            domain.getCalculadora(),
            domain.getReferenciaBibliografica(),
            domain.getUnidadeMedida(),
            domain.getTempoMinimoSegundos(),
            domain.getTempoMaximoSegundos(),
            domain.getEquipamentoNecessario(),
            domain.getCriteriosExclusao(),
            domain.getObservacoes()
        );
    }
}
