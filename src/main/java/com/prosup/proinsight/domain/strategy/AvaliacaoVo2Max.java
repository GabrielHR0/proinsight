package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import org.springframework.stereotype.Component;

@Component
@StrategyFor("VO2_MAX")
public class AvaliacaoVo2Max implements AvaliacaoStrategy<AvaliacaoVo2MaxContext> {

    public AvaliacaoVo2Max() {}

    @Override
    public Leaf avaliar(AvaliacaoVo2MaxContext contexto) {
        var tabelaClassificacao = contexto.getTabela();
        var dados = contexto.getDadosAvaliacao();

        for (TesteVo2Max teste : contexto.getTestes()) {
            var resultado = tabelaClassificacao.classificarComTeste(teste, dados);

            if (resultado instanceof NivelVo2Max n) {
                Double vo2 = teste.calcularVo2Max(dados);
                n.setResultadoVo2Max(vo2 != null ? (int) Math.round(vo2) : null);
                return n;
            }

            if (resultado != null) {
                return resultado;
            }
        }

        return null;
    }
}
