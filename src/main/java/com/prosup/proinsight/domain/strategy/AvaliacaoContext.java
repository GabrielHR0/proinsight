package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.model.Medicao;
import com.prosup.proinsight.domain.model.teste.Teste;


public interface AvaliacaoContext<M extends Medicao, T extends Teste> {

    String getClienteId();

    String getAvaliadorId();

    M getMedicao();

    java.util.List<T> getTestes();

    String getTabelaClassificacaoId();

    DadosAvaliacao getDadosAvaliacao();
}
