package com.prosup.proinsight.domain.model.composite.classes;

import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.Teste;

public class NivelForca extends Leaf {

    private String classificacao;
    private Double min ;
    private Double max;

    public NivelForca() {}

    public NivelForca(String classificacao, Double min, Double max) {
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

