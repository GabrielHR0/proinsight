package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class FlexaoCotoveloTest {
    @Test
    void shouldGenerateCodeWithPrefix() {
        var teste = new FlexaoCotovelo(TesteFuncionalTipo.FLEXAO_COTOVELO_30S, 10.0, 10);
        assertThat(teste.gerarCodigo()).startsWith("FUNFC30S-");
    }
}
