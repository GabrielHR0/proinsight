package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.DadosAvaliacao;

public interface Teste {

    String gerarCodigo();
    String getCriterio();
    String getValorClassificacao();

    default String getValorClassificacao(DadosAvaliacao dados) {
        return getValorClassificacao();
    }
}
