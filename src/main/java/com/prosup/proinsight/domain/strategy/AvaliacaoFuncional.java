package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.TesteAptdaoFisicaIdosos;
import org.springframework.stereotype.Component;

@Component
@StrategyFor("AVALIACAO_FUNCIONAL_IDOSO")
public class AvaliacaoFuncional implements AvaliacaoStrategy<AvaliacaoFuncionalContext> {

    public AvaliacaoFuncional() {}

    @Override
    public Leaf avaliar(AvaliacaoFuncionalContext contexto) {
        var tabelaClassificacao = contexto.getTabela();
        var dados = contexto.getDadosAvaliacao();

        for (TesteAptdaoFisicaIdosos teste : contexto.getTestes()) {
            var resultado = tabelaClassificacao.classificarComTeste(teste, dados);

            if (resultado != null) {
                return resultado;
            }
        }

        return null;
    }
}
