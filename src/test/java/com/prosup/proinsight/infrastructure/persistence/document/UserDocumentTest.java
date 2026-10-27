package com.prosup.proinsight.infrastructure.persistence.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDocumentTest {

    @Test
    @DisplayName("deve normalizar cref e cpf em branco para null")
    void deveNormalizarCrefECpfEmBrancoParaNull() {
        var doc = new UserDocument();

        doc.setCref("   ");
        doc.setCpf("\t\n");

        assertThat(doc.getCref()).isNull();
        assertThat(doc.getCpf()).isNull();
    }

    @Test
    @DisplayName("deve remover espacos de cref e cpf validos")
    void deveRemoverEspacosDeCrefECpfValidos() {
        var doc = new UserDocument();

        doc.setCref("  CREF-12345  ");
        doc.setCpf("  12345678901  ");

        assertThat(doc.getCref()).isEqualTo("CREF-12345");
        assertThat(doc.getCpf()).isEqualTo("12345678901");
    }
}
