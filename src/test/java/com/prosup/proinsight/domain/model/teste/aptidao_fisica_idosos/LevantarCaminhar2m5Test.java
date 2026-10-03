package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LevantarCaminhar2m5Test {
    @Test
    void shouldGenerateCodeWithPrefix() {
        var teste = new LevantarCaminhar2m5(TesteFuncionalTipo.LEVANTAR_CAMINHAR_2M5, 10.0, 5.0);
        assertThat(teste.gerarCodigo()).startsWith("FUNLC2M5-");
    }
}
