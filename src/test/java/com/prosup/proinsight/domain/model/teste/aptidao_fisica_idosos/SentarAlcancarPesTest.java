package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SentarAlcancarPesTest {
    @Test
    void shouldGenerateCodeWithPrefix() {
        var teste = new SentarAlcancarPes(TesteFuncionalTipo.SENTAR_ALCANCAR_PES, 10.0, 5.0);
        assertThat(teste.gerarCodigo()).startsWith("FUNSAP-");
    }
}
