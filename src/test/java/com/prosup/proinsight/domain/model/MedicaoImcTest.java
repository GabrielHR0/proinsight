package com.prosup.proinsight.domain.model;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.teste.TesteImc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MedicaoImcTest {

    @Test
    @DisplayName("IMC sem teste deve falhar com erro de dominio claro")
    void imcSemTesteDeveFalharComErroClaro() {
        var medicao = new MedicaoImc(MedicaoTipo.IMC);

        assertThatThrownBy(medicao::getTeste)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("IMC");
    }

    @Test
    @DisplayName("IMC com teste deve retornar o primeiro teste")
    void imcComTesteDeveRetornarTeste() {
        var teste = new TesteImc(70000, 175);
        var medicao = new MedicaoImc(MedicaoTipo.IMC);
        medicao.addTestes(teste);

        assertThat(medicao.getTeste()).isSameAs(teste);
    }
}
