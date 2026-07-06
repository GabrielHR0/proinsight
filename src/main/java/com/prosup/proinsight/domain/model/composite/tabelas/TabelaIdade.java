package com.prosup.proinsight.domain.model.composite.tabelas;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.model.composite.Composite;
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
        return super.classificarComTeste(teste, dados);
    }
}
