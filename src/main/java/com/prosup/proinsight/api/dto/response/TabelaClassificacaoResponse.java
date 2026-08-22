package com.prosup.proinsight.api.dto.response;

import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComponent;

public record TabelaClassificacaoResponse(
        String id,
        String nome,
        PersistedComponent raiz
) {}
