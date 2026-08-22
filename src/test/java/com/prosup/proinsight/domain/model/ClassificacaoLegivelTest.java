package com.prosup.proinsight.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificacaoLegivelTest {

    @Test
    void deveHumanizarNiveisVo2Max() {
        assertThat(ClassificacaoLegivel.humanizar("MUITO_RUIM")).isEqualTo("Muito ruim");
        assertThat(ClassificacaoLegivel.humanizar("RUIM")).isEqualTo("Ruim");
        assertThat(ClassificacaoLegivel.humanizar("MEDIO")).isEqualTo("Médio");
        assertThat(ClassificacaoLegivel.humanizar("MÉDIO")).isEqualTo("Médio");
        assertThat(ClassificacaoLegivel.humanizar("BOM")).isEqualTo("Bom");
        assertThat(ClassificacaoLegivel.humanizar("EXCELENTE")).isEqualTo("Excelente");
    }

    @Test
    void deveHumanizarNiveisImc() {
        assertThat(ClassificacaoLegivel.humanizar("ABAIXO_DO_PESO")).isEqualTo("Abaixo do peso");
        assertThat(ClassificacaoLegivel.humanizar("NORMAL")).isEqualTo("Normal");
        assertThat(ClassificacaoLegivel.humanizar("SOBREPESO")).isEqualTo("Sobrepeso");
        assertThat(ClassificacaoLegivel.humanizar("OBESIDADE_I")).isEqualTo("Obesidade I");
        assertThat(ClassificacaoLegivel.humanizar("OBESIDADE_II")).isEqualTo("Obesidade II");
        assertThat(ClassificacaoLegivel.humanizar("OBESIDADE_III")).isEqualTo("Obesidade III");
    }

    @Test
    void deveRetornarCodigoQuandoDesconhecido() {
        assertThat(ClassificacaoLegivel.humanizar("SEM_CLASSIFICACAO")).isEqualTo("SEM_CLASSIFICACAO");
    }

    @Test
    void deveTratarNull() {
        assertThat(ClassificacaoLegivel.humanizar(null)).isNull();
    }
}