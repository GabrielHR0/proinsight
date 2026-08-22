package com.prosup.proinsight.domain.model.composite.tabelas;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.Composite;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.Teste;

public class TabelaSexo extends Composite {

    private Sexo sexo;

    public TabelaSexo() {}

    public TabelaSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    @Override
    public Leaf classificarComTeste(Teste teste) {
        return classificarComTeste(teste, null);
    }

    @Override
    public Leaf classificarComTeste(Teste teste, DadosAvaliacao dados) {
        if (dados == null || !dados.temSexo() || dados.getSexo() != this.sexo) {
            return null;
        }

        Leaf resultado = super.classificarComTeste(teste, dados);
        if (resultado != null) {
            return resultado;
        }

        // A idade não casou com nenhuma faixa (ou está ausente):
        // usa a faixa de idade mais próxima, em vez de retornar null.
        return classificarComFaixaDeIdadeMaisProxima(teste, dados);
    }

    private Leaf classificarComFaixaDeIdadeMaisProxima(Teste teste, DadosAvaliacao dados) {
        Integer idade = dados.getIdade();
        TabelaIdade maisProxima = null;
        double melhorDistancia = Double.MAX_VALUE;

        for (Component child : getChildren()) {
            if (!(child instanceof TabelaIdade faixa)) {
                continue;
            }
            double distancia = 0;
            if (idade != null) {
                if (idade < faixa.getIdadeMin()) {
                    distancia = faixa.getIdadeMin() - idade;
                } else if (idade > faixa.getIdadeMax()) {
                    distancia = idade - faixa.getIdadeMax();
                }
            }
            if (distancia < melhorDistancia) {
                melhorDistancia = distancia;
                maisProxima = faixa;
            }
        }

        if (maisProxima == null) {
            return null;
        }
        // A faixa escolhida aplica o clamp de valor internamente
        // (TabelaIdade.classificarComClampDeValor), se necessário.
        return maisProxima.classificarComClampDeValor(teste, dados);
    }
}
