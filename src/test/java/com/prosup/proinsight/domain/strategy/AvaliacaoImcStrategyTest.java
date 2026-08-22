package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.domain.model.MedicaoImc;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.NivelImc;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaClassificacaoGenerica;
import com.prosup.proinsight.domain.model.teste.TesteImc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvaliacaoImcStrategyTest {

    private TabelaClassificacaoGenerica tabela;
    private AvaliacaoImc strategy;

    @BeforeEach
    void setUp() {
        strategy = new AvaliacaoImc();

        tabela = new TabelaClassificacaoGenerica();
        tabela.add(new NivelImc("ABAIXO_DO_PESO", null, 18.5, null, TipoLimite.EXCLUSIVO));
        tabela.add(new NivelImc("NORMAL", 18.5, 25.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        tabela.add(new NivelImc("SOBREPESO", 25.0, 30.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        tabela.add(new NivelImc("OBESIDADE_I", 30.0, 35.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        tabela.add(new NivelImc("OBESIDADE_II", 35.0, 40.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        tabela.add(new NivelImc("OBESIDADE_III", 40.0, null, TipoLimite.INCLUSIVO, null));
    }

    private AvaliacaoImcContext criarContexto(int pesoGramas, int alturaCm) {
        var teste = new TesteImc(pesoGramas, alturaCm);
        var medicao = new MedicaoImc(MedicaoTipo.IMC, Instant.now(), Instant.now(), Instant.now(), null, List.of(teste));
        var dados = new DadosAvaliacao()
            .adicionar("peso_gramas", pesoGramas)
            .adicionar("altura_cm", alturaCm);

        return new AvaliacaoImcContextBuilder()
            .comCliente("cliente-1")
            .comAvaliador("avaliador-1")
            .comTabelaClassificacaoId("classificacao_imc_test")
            .comMedicao(medicao)
            .comDadosAvaliacao(dados)
            .comTabelaClassificacao(tabela)
            .build();
    }

    @Test
    void shouldClassifyAbaixoDoPeso() {
        Leaf resultado = strategy.avaliar(criarContexto(50000, 170));
        assertThat(resultado).isNotNull();
        assertThat(((NivelImc) resultado).getClassificacao()).isEqualTo("ABAIXO_DO_PESO");
    }

    @Test
    void shouldClassifyNormal() {
        Leaf resultado = strategy.avaliar(criarContexto(70000, 175));
        assertThat(resultado).isNotNull();
        assertThat(((NivelImc) resultado).getClassificacao()).isEqualTo("NORMAL");
    }

    @Test
    void shouldClassifySobrepeso() {
        Leaf resultado = strategy.avaliar(criarContexto(85000, 175));
        assertThat(resultado).isNotNull();
        assertThat(((NivelImc) resultado).getClassificacao()).isEqualTo("SOBREPESO");
    }

    @Test
    void shouldClassifyObesidadeI() {
        Leaf resultado = strategy.avaliar(criarContexto(95000, 175));
        assertThat(resultado).isNotNull();
        assertThat(((NivelImc) resultado).getClassificacao()).isEqualTo("OBESIDADE_I");
    }

    @Test
    void shouldClassifyObesidadeII() {
        Leaf resultado = strategy.avaliar(criarContexto(110000, 175));
        assertThat(resultado).isNotNull();
        assertThat(((NivelImc) resultado).getClassificacao()).isEqualTo("OBESIDADE_II");
    }

    @Test
    void shouldClassifyObesidadeIII() {
        Leaf resultado = strategy.avaliar(criarContexto(130000, 170));
        assertThat(resultado).isNotNull();
        assertThat(((NivelImc) resultado).getClassificacao()).isEqualTo("OBESIDADE_III");
    }

    @Test
    void shouldReturnNullWhenNoTestData() {
        var teste = new TesteImc();
        var medicao = new MedicaoImc(MedicaoTipo.IMC, Instant.now(), Instant.now(), Instant.now(), null, List.of(teste));
        var dados = new DadosAvaliacao();

        var context = new AvaliacaoImcContextBuilder()
            .comCliente("cliente-1")
            .comAvaliador("avaliador-1")
            .comTabelaClassificacaoId("classificacao_imc_test")
            .comMedicao(medicao)
            .comDadosAvaliacao(dados)
            .comTabelaClassificacao(tabela)
            .build();

        Leaf resultado = strategy.avaliar(context);
        assertThat(resultado).isNull();
    }
}
