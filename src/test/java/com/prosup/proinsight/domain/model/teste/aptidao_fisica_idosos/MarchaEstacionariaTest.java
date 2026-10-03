package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MarchaEstacionariaTest {
    @Test
    void shouldGenerateCodeWithPrefix() {
        var teste = new MarchaEstacionaria(TesteFuncionalTipo.MARCHA_ESTACIONARIA_2MIN, 10.0, 100);
        assertThat(teste.gerarCodigo()).startsWith("FUNME2M-");
    }
}
