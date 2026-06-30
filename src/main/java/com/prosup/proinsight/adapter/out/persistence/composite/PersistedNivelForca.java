package com.prosup.proinsight.adapter.out.persistence.composite;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("persistedNivelForca")
public class PersistedNivelForca extends PersistedLeaf {

    private String classificacao;
    private Double min;
    private Double max;

    public PersistedNivelForca() {
    }

    public PersistedNivelForca(String classificacao, Double min, Double max) {
        this.classificacao = classificacao;
        this.min = min;
        this.max = max;
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
}
