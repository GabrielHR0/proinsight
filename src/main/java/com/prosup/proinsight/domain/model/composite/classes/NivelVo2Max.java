package com.prosup.proinsight.domain.model.composite.classes;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.Teste;

public class NivelVo2Max extends Leaf {

    private String classificacao;
    private Double min;
    private Double max;
    private TipoLimite tipoMin = TipoLimite.INCLUSIVO;
    private TipoLimite tipoMax = TipoLimite.INCLUSIVO;
    private Integer resultadoVo2Max;

    public NivelVo2Max() {}

    public NivelVo2Max(String classificacao, Double min, Double max) {
        this(classificacao, min, max, TipoLimite.INCLUSIVO, TipoLimite.INCLUSIVO);
    }

    public NivelVo2Max(String classificacao, Double min, Double max, TipoLimite tipoMin, TipoLimite tipoMax) {
        this.classificacao = classificacao;
        this.min = min;
        this.max = max;
        this.tipoMin = tipoMin;
        this.tipoMax = tipoMax;
    }

    @Override
    public Leaf classificarComTeste(Teste teste) {
        return classificarComTeste(teste, null);
    }

    @Override
    public Leaf classificarComTeste(Teste teste, DadosAvaliacao dados) {
        teste.getValorClassificacao(dados);
        String valorStr = teste.getValorClassificacao();
        if (valorStr == null) return null;
        try {
            double valor = Double.parseDouble(valorStr);
            if (min != null) {
                if (tipoMin == TipoLimite.EXCLUSIVO && valor <= min) return null;
                if (tipoMin == TipoLimite.INCLUSIVO && valor < min) return null;
            }
            if (max != null) {
                if (tipoMax == TipoLimite.EXCLUSIVO && valor >= max) return null;
                if (tipoMax == TipoLimite.INCLUSIVO && valor > max) return null;
            }
            return this;
        } catch (NumberFormatException e) {
            return null;
        }
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

    public Integer getResultadoVo2Max() {
        return resultadoVo2Max;
    }

    public void setResultadoVo2Max(Integer resultadoVo2Max) {
        this.resultadoVo2Max = resultadoVo2Max;
    }
}
