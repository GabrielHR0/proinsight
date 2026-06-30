package com.prosup.proinsight.domain.model.composite.tabelas;

import com.prosup.proinsight.domain.enums.Sexo;
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
    public Leaf classificar() {
        return super.classificar();
    }
    
    @Override
    public Leaf classificarComTeste(Teste teste) {
        return super.classificarComTeste(teste);
    }
}
