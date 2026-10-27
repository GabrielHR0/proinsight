package com.prosup.proinsight.domain.model.composite.tabelas;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.Composite;
import com.prosup.proinsight.domain.model.composite.FaixaNumerica;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.Teste;

public class TabelaIdade extends Composite {

    private Integer idadeMin;
    private Integer idadeMax;

    public TabelaIdade() {}

    public TabelaIdade(Integer idadeMin, Integer idadeMax) {
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

    @Override
    public Leaf classificarComTeste(Teste teste) {
        return classificarComTeste(teste, null);
    }

    @Override
    public Leaf classificarComTeste(Teste teste, DadosAvaliacao dados) {
        if (dados == null || !dados.temIdade()) {
            return null;
        }
        Integer idade = dados.getIdade();
        if (idade < idadeMin || idade > idadeMax) {
            return null;
        }

        return classificarComClampDeValor(teste, dados);
    }

    /**
     * Classifica ignorando a checagem de faixa etária: tenta o match exato nos
     * níveis e, se nenhum casar, aplica o clamp de valor (nível mais próximo).
     * Usado pelo fallback de faixa de idade mais próxima em {@link TabelaSexo},
     * quando a idade do avaliado não pertence a nenhuma faixa.
     */
    public Leaf classificarComClampDeValor(Teste teste, DadosAvaliacao dados) {
        DadosAvaliacao contexto = dados != null ? dados.copia() : new DadosAvaliacao();
        contexto.adicionar("idadeMin", idadeMin);
        contexto.adicionar("idadeMax", idadeMax);
        for (Component child : getChildren()) {
            Leaf result = child.classificarComTeste(teste, contexto);
            if (result != null) {
                return result;
            }
        }

        // Nenhum nível casou com o valor do teste: classifica no nível mais próximo
        // (clamp), em vez de retornar null e quebrar a avaliação.
        return classificarNivelMaisProximo(teste, contexto);
    }

    private Leaf classificarNivelMaisProximo(Teste teste, DadosAvaliacao dados) {
        Double valor = teste.getValorNumerico(dados);
        if (valor == null) {
            return null;
        }

        Leaf melhor = null;
        double melhorDistancia = Double.MAX_VALUE;

        for (Component child : getChildren()) {
            if (!(child instanceof FaixaNumerica faixa)) {
                continue;
            }
            Double min = faixa.getMin();
            Double max = faixa.getMax();
            double distancia = 0;
            if (min != null && valor < min) {
                distancia = min - valor;
            } else if (max != null && valor > max) {
                distancia = valor - max;
            }
            if (distancia < melhorDistancia) {
                melhorDistancia = distancia;
                melhor = (Leaf) child;
            }
        }
        return melhor;
    }
}
