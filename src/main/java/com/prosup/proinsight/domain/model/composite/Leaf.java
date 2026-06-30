package com.prosup.proinsight.domain.model.composite;

import com.prosup.proinsight.domain.model.teste.Teste;

public abstract class Leaf implements Component {
    @Override
    public Leaf classificar() {
        return this;
    }
    
    @Override
    public Leaf classificarComTeste(Teste teste) {
        return this;
    }
}
