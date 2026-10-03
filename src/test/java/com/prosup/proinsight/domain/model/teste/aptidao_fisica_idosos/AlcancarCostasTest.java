package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AlcancarCostasTest {
    @Test
    void shouldGenerateCodeWithPrefix() {
        var teste = new AlcancarCostas(TesteFuncionalTipo.ALCANCAR_COSTAS, 10.0, 5.0);
        assertThat(teste.gerarCodigo()).startsWith("FUNAC-");
    }
}
