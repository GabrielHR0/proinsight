package com.prosup.proinsight.domain.model.composite.classes;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.Teste;

/**
 * Representa um nível normativo de percentil para testes de aptidão física funcional (Fullerton).
 * Diferente de testes com categorias qualitativas ("ruim", "bom"), o resultado final entregue é o percentil.
 */
public class PercentilFuncional extends Leaf {

    private Integer percentil;
    private Double min;
    private Double max;
    private TipoLimite tipoMin = TipoLimite.INCLUSIVO;
    private TipoLimite tipoMax = TipoLimite.INCLUSIVO;

    public PercentilFuncional() {}

    public PercentilFuncional(Integer percentil, Double min, Double max) {
        this(percentil, min, max, TipoLimite.INCLUSIVO, TipoLimite.INCLUSIVO);
    }

    public PercentilFuncional(Integer percentil, Double min, Double max, TipoLimite tipoMin, TipoLimite tipoMax) {
        this.percentil = percentil;
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

        var idade = dados.getIdade();

        int idadeMin = dados.get("idadeMin");
        int idadeMax = dados.get("idadeMax");

        double normalizacao = (double) (idade - idadeMin) / ((idadeMax - idadeMin) + 1);
        if (normalizacao < 0.5) return null;

        String valorStr = teste.getValorClassificacao(dados);
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

    public Integer getPercentil() {
        return percentil;
    }

    public void setPercentil(Integer percentil) {
        this.percentil = percentil;
    }

    /**
     * Retorna a descrição do resultado de percentil (ex: "Percentil 75" ou "Abaixo do percentil 5").
     */
    public String getDescricaoPercentil() {
        if (percentil == null || percentil <= 0) {
            return "Abaixo do percentil 5";
        }
        return "Percentil " + percentil;
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
