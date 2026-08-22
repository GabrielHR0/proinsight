package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.TesteImc;
import org.springframework.stereotype.Component;

@Component
@StrategyFor("IMC")
public class AvaliacaoImc implements AvaliacaoStrategy<AvaliacaoImcContext> {

    public AvaliacaoImc() {}

    @Override
    public Leaf avaliar(AvaliacaoImcContext contexto) {
        var tabelaClassificacao = contexto.getTabela();
        var dados = contexto.getDadosAvaliacao();

        for (TesteImc teste : contexto.getTestes()) {
            var resultado = tabelaClassificacao.classificarComTeste(teste, dados);

            if (resultado != null) {
                return resultado;
            }
        }

        return null;
    }
}
