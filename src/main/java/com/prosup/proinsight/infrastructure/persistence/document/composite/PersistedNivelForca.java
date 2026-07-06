package com.prosup.proinsight.infrastructure.persistence.document.composite;

import com.prosup.proinsight.domain.enums.TipoLimite;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("persistedNivelForca")
public class PersistedNivelForca extends PersistedLeaf {

    private String classificacao;
    private Double min;
    private Double max;
    private TipoLimite tipoMin;
    private TipoLimite tipoMax;

    public PersistedNivelForca() {}

    public PersistedNivelForca(String classificacao, Double min, Double max) {
        this(classificacao, min, max, null, null);
    }

    public PersistedNivelForca(String classificacao, Double min, Double max, TipoLimite tipoMin, TipoLimite tipoMax) {
        this.classificacao = classificacao;
        this.min = min;
        this.max = max;
        this.tipoMin = tipoMin;
        this.tipoMax = tipoMax;
    }

    public String getClassificacao() {
        return classificacao;
    }

    public void setClassificacao(String classificacao) {
        this.classificacao = classificacao;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public TipoLimite getTipoMin() {
        return tipoMin;
    }

    public void setTipoMin(TipoLimite tipoMin) {
        this.tipoMin = tipoMin;
    }

    public TipoLimite getTipoMax() {
        return tipoMax;
    }

    public void setTipoMax(TipoLimite tipoMax) {
        this.tipoMax = tipoMax;
    }
}
