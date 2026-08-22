package com.prosup.proinsight.infrastructure.persistence.document.composite;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "_class"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PersistedTabelaEquipamento.class, name = "persistedTabelaEquipamento"),
        @JsonSubTypes.Type(value = PersistedTabelaIdade.class, name = "persistedTabelaIdade"),
        @JsonSubTypes.Type(value = PersistedTabelaSexo.class, name = "persistedTabelaSexo"),
        @JsonSubTypes.Type(value = PersistedTabelaVo2Max.class, name = "persistedTabelaVo2Max"),
        @JsonSubTypes.Type(value = PersistedNivelForca.class, name = "persistedNivelForca"),
        @JsonSubTypes.Type(value = PersistedNivelVo2Max.class, name = "persistedNivelVo2Max"),
        @JsonSubTypes.Type(value = PersistedNivelImc.class, name = "persistedNivelImc"),
        @JsonSubTypes.Type(value = PersistedTabelaClassificacaoGenerica.class, name = "persistedTabelaClassificacaoGenerica")
})
public interface PersistedComponentMixIn {
}
