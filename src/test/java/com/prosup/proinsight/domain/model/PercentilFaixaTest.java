package com.prosup.proinsight.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PercentilFaixaTest {

    @Test
    void deveClassificarAbaixoDe25ComoBaixo() {
        assertThat(PercentilFaixa.classificar(0)).isEqualTo(PercentilFaixa.BAIXO);
        assertThat(PercentilFaixa.classificar(5)).isEqualTo(PercentilFaixa.BAIXO);
        assertThat(PercentilFaixa.classificar(24)).isEqualTo(PercentilFaixa.BAIXO);
    }

    @Test
    void deveClassificarEntre25E75ComoNormal() {
        assertThat(PercentilFaixa.classificar(25)).isEqualTo(PercentilFaixa.NORMAL);
        assertThat(PercentilFaixa.classificar(45.5)).isEqualTo(PercentilFaixa.NORMAL);
        assertThat(PercentilFaixa.classificar(50)).isEqualTo(PercentilFaixa.NORMAL);
        assertThat(PercentilFaixa.classificar(75)).isEqualTo(PercentilFaixa.NORMAL);
    }

    @Test
    void deveClassificarAcimaDe75ComoElevado() {
        assertThat(PercentilFaixa.classificar(76)).isEqualTo(PercentilFaixa.ELEVADO);
        assertThat(PercentilFaixa.classificar(95)).isEqualTo(PercentilFaixa.ELEVADO);
    }

    @Test
    void deveRetornarNuloQuandoPercentilAusente() {
        assertThat(PercentilFaixa.classificar(null)).isNull();
    }
}
