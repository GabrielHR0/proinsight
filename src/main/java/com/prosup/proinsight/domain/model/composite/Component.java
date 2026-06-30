package com.prosup.proinsight.domain.model.composite;

import com.prosup.proinsight.domain.model.teste.Teste;

public interface Component {
    Leaf classificar();
    
    Leaf classificarComTeste(Teste teste);
}
