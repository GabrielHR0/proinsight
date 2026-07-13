package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.MedicaoImc;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaClassificacaoGenerica;
import com.prosup.proinsight.domain.model.teste.TesteImc;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class AvaliacaoImcContextBuilderTest {

    private final TabelaClassificacaoGenerica tabela = new TabelaClassificacaoGenerica();
    private final MedicaoImc medicaoValida = new MedicaoImc(
        MedicaoTipo.IMC, Instant.now(), Instant.now(), Instant.now(), null,
        List.of(new TesteImc(70000, 170))
    );

    @Test
    void shouldBuildSuccessfully() {
        var context = new AvaliacaoImcContextBuilder()
            .comCliente("cliente-1")
            .comAvaliador("avaliador-1")
            .comTabelaClassificacaoId("classificacao_imc_test")
            .comMedicao(medicaoValida)
            .comDadosAvaliacao(new DadosAvaliacao().adicionar("peso", 70).adicionar("altura", 170))
            .comTabelaClassificacao(tabela)
            .build();

        assertThat(context.getClienteId()).isEqualTo("cliente-1");
        assertThat(context.getAvaliadorId()).isEqualTo("avaliador-1");
        assertThat(context.getTabelaClassificacaoId()).isEqualTo("classificacao_imc_test");
        assertThat(context.getMedicao()).isSameAs(medicaoValida);
        assertThat(context.getTestes()).hasSize(1);
        assertThat(context.getTabela()).isSameAs(tabela);
    }

    @Test
    void shouldRejectNullCliente() {
        assertThatThrownBy(() -> new AvaliacaoImcContextBuilder()
            .comAvaliador("avaliador-1")
            .comTabelaClassificacaoId("tabela-1")
            .comMedicao(medicaoValida)
            .comTabelaClassificacao(tabela)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ClienteId");
    }

    @Test
    void shouldRejectNullAvaliador() {
        assertThatThrownBy(() -> new AvaliacaoImcContextBuilder()
            .comCliente("cliente-1")
            .comTabelaClassificacaoId("tabela-1")
            .comMedicao(medicaoValida)
            .comTabelaClassificacao(tabela)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("AvaliadorId");
    }

    @Test
    void shouldRejectNullTabelaClassificacaoId() {
        assertThatThrownBy(() -> new AvaliacaoImcContextBuilder()
            .comCliente("cliente-1")
            .comAvaliador("avaliador-1")
            .comMedicao(medicaoValida)
            .comTabelaClassificacao(tabela)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("tabelaClassificacaoId");
    }

    @Test
    void shouldRejectNullMedicao() {
        assertThatThrownBy(() -> new AvaliacaoImcContextBuilder()
            .comCliente("cliente-1")
            .comAvaliador("avaliador-1")
            .comTabelaClassificacaoId("tabela-1")
            .comMedicao(null)
            .comTabelaClassificacao(tabela)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Medicao");
    }

    @Test
    void shouldRejectMedicaoSemTestes() {
        var medicaoSemTestes = new MedicaoImc(MedicaoTipo.IMC, Instant.now(), Instant.now(), Instant.now(), null, List.of());
        assertThatThrownBy(() -> new AvaliacaoImcContextBuilder()
            .comCliente("cliente-1")
            .comAvaliador("avaliador-1")
            .comTabelaClassificacaoId("tabela-1")
            .comMedicao(medicaoSemTestes)
            .comTabelaClassificacao(tabela)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("testes");
    }
}
