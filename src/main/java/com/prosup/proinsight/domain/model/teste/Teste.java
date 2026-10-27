package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.DadosAvaliacao;

public interface Teste {

    String gerarCodigo();

    Double getValorNumerico();

    default Double getValorNumerico(DadosAvaliacao dados) {
        return getValorNumerico();
    }

    default String getValorClassificacao() {
        Double valor = getValorNumerico();
        return valor != null ? String.valueOf(valor) : null;
    }

    default String getValorClassificacao(DadosAvaliacao dados) {
        Double valor = getValorNumerico(dados);
        return valor != null ? String.valueOf(valor) : null;
    }
}
