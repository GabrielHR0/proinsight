package com.prosup.proinsight.domain.model.composite.tabelas;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.Composite;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
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
        dados.adicionar("idadeMin", idadeMin);
        dados.adicionar("idadeMax", idadeMax);
        for (Component child : getChildren()) {
            Leaf result = child.classificarComTeste(teste, dados);
            if (result != null) {
                return result;
            }
        }

        // Nenhum nível casou com o valor do teste: classifica no nível mais próximo
        // (clamp), em vez de retornar null e quebrar a avaliação.
        return classificarNivelMaisProximo(teste, dados);
    }

    private Leaf classificarNivelMaisProximo(Teste teste, DadosAvaliacao dados) {
        String valorStr = teste.getValorClassificacao(dados);
        if (valorStr == null) {
            return null;
        }
        double valor;
        try {
            valor = Double.parseDouble(valorStr);
        } catch (NumberFormatException e) {
            return null;
        }

        Leaf melhor = null;
        double melhorDistancia = Double.MAX_VALUE;

        for (Component child : getChildren()) {
            Double min = null;
            Double max = null;
            if (child instanceof NivelVo2Max nivel) {
                min = nivel.getMin();
                max = nivel.getMax();
            } else if (child instanceof com.prosup.proinsight.domain.model.composite.classes.PercentilFuncional p) {
                min = p.getMin();
                max = p.getMax();
            } else {
                continue;
            }
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
