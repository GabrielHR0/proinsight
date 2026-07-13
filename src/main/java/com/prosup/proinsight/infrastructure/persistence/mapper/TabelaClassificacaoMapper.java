package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.api.dto.response.TabelaClassificacaoResponse;
import com.prosup.proinsight.domain.model.TabelaClassificacao;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import org.springframework.stereotype.Component;

@Component
public class TabelaClassificacaoMapper {

    private final PersistedComponentMapper componentMapper;

    public TabelaClassificacaoMapper(PersistedComponentMapper componentMapper) {
        this.componentMapper = componentMapper;
    }

    public TabelaClassificacao toDomain(TabelaClassificacaoDocument doc) {
        if (doc == null) return null;
        var raiz = componentMapper.toDomain(doc.getRaiz());
        return new TabelaClassificacao(doc.getId(), doc.getNome(), raiz);
    }

    public TabelaClassificacaoResponse toResponse(TabelaClassificacaoDocument doc) {
        if (doc == null) return null;
        return new TabelaClassificacaoResponse(
                doc.getId(),
                doc.getNome(),
                doc.getRaiz()
        );
    }
}
