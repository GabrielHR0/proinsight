package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SentarLevantarTest {
    @Test
    void shouldGenerateCodeWithPrefix() {
        var teste = new SentarLevantar(TesteFuncionalTipo.SENTAR_LEVANTAR_30S, 10.0, 10);
        assertThat(teste.gerarCodigo()).startsWith("FUNCSL30S-");
    }

    @Test
    void shouldReturnValorAsStringInGetValorClassificacao() {
        var teste = new SentarLevantar(TesteFuncionalTipo.SENTAR_LEVANTAR_30S, 14.0);
        assertThat(teste.getValorClassificacao()).isEqualTo("14.0");
        assertThat(teste.repeticoes).isEqualTo(14);
    }
}
