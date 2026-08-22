package com.prosup.proinsight.domain.model.composite;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.model.teste.Teste;

public interface Component {
    Leaf classificar();

    Leaf classificarComTeste(Teste teste);

    default Leaf classificarComTeste(Teste teste, DadosAvaliacao dados) {
        return classificarComTeste(teste);
    }
}
