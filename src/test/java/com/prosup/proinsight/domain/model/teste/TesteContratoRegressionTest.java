package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.SentarLevantar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TesteContratoRegressionTest {

    @Test
    @DisplayName("Testes funcionais devem expor o valor numerico usado pela classificacao")
    void funcionalDeveExporValorNumerico() {
        var teste = new SentarLevantar(TesteFuncionalTipo.SENTAR_LEVANTAR_30S, 14.0);

        assertThat(teste.getValorNumerico()).isEqualTo(14.0);
        assertThat(teste.getValorClassificacao()).isEqualTo("14.0");
        assertThat(teste.getValorNumerico(new DadosAvaliacao())).isEqualTo(14.0);
    }

    @Test
    @DisplayName("Cooper deve usar a distancia como metrica de tabela e manter o VO2max calculado")
    void cooperDeveUsarDistanciaComoMetricaDeTabela() {
        var teste = new TesteVo2MaxCooper(3000);

        assertThat(teste.getValorNumerico()).isEqualTo(3000.0);
        assertThat(teste.getValorNumerico(new DadosAvaliacao())).isEqualTo(3000.0);
        assertThat(teste.getValorClassificacao()).isEqualTo("3000.0");
        assertThat(teste.calcularVo2Max(new DadosAvaliacao())).isCloseTo(55.79, within(0.05));
    }

    @Test
    @DisplayName("Esteira deve calcular o VO2max sem alterar o estado do teste")
    void esteiraDeveCalcularVo2SemEfeitoColateral() {
        var teste = new TesteVo2MaxEsteiraIncremental(12.5, 5.0);
        var dados = new DadosAvaliacao();

        double esperado = teste.calcularVo2Max(dados);

        assertThat(teste.getValorNumerico(dados)).isCloseTo(esperado, within(0.0001));
        assertThat(teste.getValorNumerico()).isCloseTo(esperado, within(0.0001));
        assertThat(teste.getValorClassificacao(dados)).isEqualTo(String.valueOf(esperado));
    }

    @Test
    @DisplayName("NivelVo2Max deve classificar Cooper pela distancia")
    void nivelVo2MaxDeveClassificarCooperPelaDistancia() {
        var nivel = new NivelVo2Max("BOM", 2500.0, 3500.0);

        var resultado = nivel.classificarComTeste(
                new TesteVo2MaxCooper(3000), new DadosAvaliacao());

        assertThat(resultado).isSameAs(nivel);
    }

    @Test
    @DisplayName("NivelVo2Max deve classificar esteira pelo VO2max calculado")
    void nivelVo2MaxDeveClassificarEsteiraPeloVo2() {
        var nivel = new NivelVo2Max("BOM", 40.0, 50.0);
        var teste = new TesteVo2MaxEsteiraIncremental(12.5, 5.0);

        var resultado = nivel.classificarComTeste(teste, new DadosAvaliacao());

        assertThat(resultado).isSameAs(nivel);
    }
}
