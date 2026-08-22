package com.prosup.proinsight.infrastructure.persistence.document.composite;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("persistedTabelaIdade")
public class PersistedTabelaIdade extends PersistedComposite {

    private Integer idadeMin;
    private Integer idadeMax;

    public PersistedTabelaIdade() {}

    public PersistedTabelaIdade(Integer idadeMin, Integer idadeMax) {
        this.idadeMin = idadeMin;
        this.idadeMax = idadeMax;
    }

    public Integer getIdadeMin() {
        return idadeMin;
    }

    public void setIdadeMin(Integer idadeMin) {
        this.idadeMin = idadeMin;
    }

    public Integer getIdadeMax() {
        return idadeMax;
    }

    public void setIdadeMax(Integer idadeMax) {
        this.idadeMax = idadeMax;
    }
}
