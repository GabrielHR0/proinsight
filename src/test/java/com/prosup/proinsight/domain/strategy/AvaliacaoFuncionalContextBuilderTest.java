package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import com.prosup.proinsight.domain.model.MedicaoFuncional;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaClassificacaoGenerica;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.SentarLevantar;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvaliacaoFuncionalContextBuilderTest {

    private MedicaoFuncional medicaoValida() {
        return new MedicaoFuncional(
            MedicaoTipo.FUNCIONAL, Instant.now(), Instant.now(), Instant.now(),
            null, List.of(new SentarLevantar(TesteFuncionalTipo.SENTAR_LEVANTAR_30S, 14.0))
        );
    }

    private AvaliacaoFuncionalContextBuilder builderValido() {
        return new AvaliacaoFuncionalContextBuilder()
            .comCliente("cliente-1")
            .comAvaliador("avaliador-1")
            .comTabelaClassificacaoId("classificacao_fullerton_sentar_levantar")
            .comMedicao(medicaoValida())
            .comDadosAvaliacao(new DadosAvaliacao()
                .adicionar("idade", 70)
                .adicionar("sexo", Sexo.MASCULINO))
            .comTabelaClassificacao(new TabelaClassificacaoGenerica());
    }

    @Test
    void shouldBuildSuccessfully() {
        var context = builderValido().build();

        assertThat(context.getClienteId()).isEqualTo("cliente-1");
        assertThat(context.getAvaliadorId()).isEqualTo("avaliador-1");
        assertThat(context.getTabelaClassificacaoId()).isEqualTo("classificacao_fullerton_sentar_levantar");
        assertThat(context.getTestes()).hasSize(1);
        assertThat(context.getDadosAvaliacao()).isNotNull();
        assertThat(context.getTabela()).isNotNull();
    }

    @Test
    void shouldRejectNullCliente() {
        assertThatThrownBy(() -> builderValido().comCliente(null).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ClienteId é obrigatório");
    }

    @Test
    void shouldRejectNullAvaliador() {
        assertThatThrownBy(() -> builderValido().comAvaliador(null).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("AvaliadorId é obrigatório");
    }

    @Test
    void shouldRejectNullTabelaClassificacaoId() {
        assertThatThrownBy(() -> builderValido().comTabelaClassificacaoId(null).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("tabelaClassificacaoId é obrigatório");
    }

    @Test
    void shouldRejectNullMedicao() {
        assertThatThrownBy(() -> builderValido().comMedicao(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Medicao funcional não pode ser nula");
    }

    @Test
    void shouldRejectMedicaoSemTestes() {
        var medicaoVazia = new MedicaoFuncional(
            MedicaoTipo.FUNCIONAL, Instant.now(), Instant.now(), Instant.now(),
            null, List.of()
        );
        assertThatThrownBy(() -> builderValido().comMedicao(medicaoVazia).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Medicao funcional deve ter testes associados");
    }

    @Test
    void shouldRejectNullTabela() {
        assertThatThrownBy(() -> builderValido().comTabelaClassificacao(null).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("tabelaClassificacao é obrigatória");
    }
}
