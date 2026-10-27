package com.prosup.proinsight.domain.model.composite.tabelas;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.model.composite.classes.NivelImc;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.composite.classes.PercentilFuncional;
import com.prosup.proinsight.domain.model.teste.Teste;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ClassificacaoRobustezRegressionTest {

    @Test
    @DisplayName("TabelaSexo nao deve classificar quando a idade estiver ausente")
    void tabelaSexoSemIdadeNaoDeveClassificar() {
        var tabela = new TabelaSexo(Sexo.MASCULINO);
        var faixa = new TabelaIdade(20, 29);
        faixa.add(new NivelVo2Max("RUIM", 10.0, 20.0));
        tabela.add(faixa);

        var dados = new DadosAvaliacao().adicionar("sexo", Sexo.MASCULINO);

        assertThat(tabela.classificarComTeste(teste(15.0), dados)).isNull();
    }

    @Test
    @DisplayName("PercentilFuncional nao deve lancar NPE quando a idade estiver ausente")
    void percentilSemIdadeNaoDeveLancarExcecao() {
        var percentil = new PercentilFuncional(50, 10.0, 20.0);
        var dados = new DadosAvaliacao();

        assertThatCode(() -> percentil.classificarComTeste(teste(15.0), dados))
                .doesNotThrowAnyException();
        assertThat(percentil.classificarComTeste(teste(15.0), dados)).isNull();
    }

    @Test
    @DisplayName("PercentilFuncional nao deve lancar NPE quando a faixa etaria estiver ausente")
    void percentilSemFaixaEtariaNaoDeveLancarExcecao() {
        var percentil = new PercentilFuncional(50, 10.0, 20.0);
        var dados = new DadosAvaliacao().adicionar("idade", 62);

        assertThatCode(() -> percentil.classificarComTeste(teste(15.0), dados))
                .doesNotThrowAnyException();
        assertThat(percentil.classificarComTeste(teste(15.0), dados)).isNull();
    }

    @Test
    @DisplayName("PercentilFuncional deve aceitar limites etarios numericos de qualquer tipo")
    void percentilDeveAceitarLimitesNumericos() {
        var percentil = new PercentilFuncional(50, 10.0, 20.0);
        var dados = new DadosAvaliacao()
                .adicionar("idade", 64)
                .adicionar("idadeMin", 60L)
                .adicionar("idadeMax", 64L);

        assertThat(percentil.classificarComTeste(teste(15.0), dados)).isSameAs(percentil);
    }

    @Test
    @DisplayName("Clamp de valor nao deve mutar o contexto de dados da avaliacao")
    void clampNaoDeveMutarDados() {
        var faixa = new TabelaIdade(20, 29);
        faixa.add(new NivelVo2Max("RUIM", 10.0, 20.0));
        var dados = new DadosAvaliacao().adicionar("idade", 22);

        faixa.classificarComClampDeValor(teste(5.0), dados);

        assertThat(dados.tem("idadeMin")).isFalse();
        assertThat(dados.tem("idadeMax")).isFalse();
    }

    @Test
    @DisplayName("Clamp deve considerar NivelImc no fallback de nivel mais proximo")
    void clampDeveConsiderarNivelImc() {
        var faixa = new TabelaIdade(20, 29);
        faixa.add(new NivelImc("BAIXO", 10.0, 20.0));
        faixa.add(new NivelImc("ALTO", 20.0, 30.0));
        var dados = new DadosAvaliacao().adicionar("idade", 22);

        var resultado = faixa.classificarComClampDeValor(teste(5.0), dados);

        assertThat(resultado).isInstanceOf(NivelImc.class);
        assertThat(((NivelImc) resultado).getClassificacao()).isEqualTo("BAIXO");
    }

    private Teste teste(double valor) {
        return new Teste() {
            @Override
            public String gerarCodigo() {
                return "teste-regressao";
            }

            @Override
            public Double getValorNumerico() {
                return valor;
            }
        };
    }
}
