package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.domain.model.MedicaoFuncional;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaClassificacaoGenerica;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.SentarLevantar;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.TesteAptdaoFisicaIdosos;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvaliacaoFuncionalStrategyTest {

    private final AvaliacaoFuncional strategy = new AvaliacaoFuncional();

    private AvaliacaoFuncionalContext context(Component raiz, TesteAptdaoFisicaIdosos teste) {
        var medicao = new MedicaoFuncional(
            MedicaoTipo.FUNCIONAL, Instant.now(), Instant.now(), Instant.now(),
            null, List.of(teste)
        );
        return new AvaliacaoFuncionalContextBuilder()
            .comCliente("cliente-1")
            .comAvaliador("avaliador-1")
            .comTabelaClassificacaoId("classificacao_fullerton_sentar_levantar")
            .comMedicao(medicao)
            .comDadosAvaliacao(new DadosAvaliacao()
                .adicionar("idade", 70)
                .adicionar("sexo", Sexo.MASCULINO))
            .comTabelaClassificacao(raiz)
            .build();
    }

    @Test
    void shouldClassifyWhenValueMatchesTable() {
        var raiz = new TabelaClassificacaoGenerica();
        raiz.add(new NivelVo2Max("RUIM", null, 12.0, null, TipoLimite.EXCLUSIVO));
        raiz.add(new NivelVo2Max("BOM", 12.0, 18.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        raiz.add(new NivelVo2Max("EXCELENTE", 18.0, null, TipoLimite.INCLUSIVO, null));

        Leaf resultado = strategy.avaliar(
            context(raiz, new SentarLevantar(TesteFuncionalTipo.SENTAR_LEVANTAR_30S, 15.0)));

        assertThat(resultado).isNotNull();
        assertThat(((NivelVo2Max) resultado).getClassificacao()).isEqualTo("BOM");
    }

    @Test
    void shouldClassifyOutlierValueUsingClampWhenInsideAgeGroup() {
        var raiz = new TabelaClassificacaoGenerica();
        raiz.add(new com.prosup.proinsight.domain.model.composite.tabelas.TabelaSexo(Sexo.MASCULINO));
        var faixa = new com.prosup.proinsight.domain.model.composite.tabelas.TabelaIdade(60, 69);
        faixa.add(new NivelVo2Max("RUIM", null, 12.0, null, TipoLimite.EXCLUSIVO));
        faixa.add(new NivelVo2Max("BOM", 12.0, 18.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        ((com.prosup.proinsight.domain.model.composite.tabelas.TabelaSexo) raiz.getChildren().get(0)).add(faixa);

        Leaf resultado = strategy.avaliar(
            context(raiz, new SentarLevantar(TesteFuncionalTipo.SENTAR_LEVANTAR_30S, 50.0)));

        assertThat(resultado).isNotNull();
        assertThat(((NivelVo2Max) resultado).getClassificacao()).isEqualTo("BOM");
    }

    @Test
    void shouldReturnNullWhenTableHasNoMatchingChildren() {
        var raiz = new TabelaClassificacaoGenerica();

        Leaf resultado = strategy.avaliar(
            context(raiz, new SentarLevantar(TesteFuncionalTipo.SENTAR_LEVANTAR_30S, 14.0)));

        assertThat(resultado).isNull();
    }
}
