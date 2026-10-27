package com.prosup.proinsight.infrastructure.persistence.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.index.Indexed;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenDocumentTest {

    @Test
    @DisplayName("expiryDate deve ter indice TTL para limpeza automatica")
    void expiryDateDeveTerIndiceTtl() throws NoSuchFieldException {
        Field field = RefreshTokenDocument.class.getDeclaredField("expiryDate");
        Indexed indexed = field.getAnnotation(Indexed.class);

        assertThat(indexed).isNotNull();
        assertThat(indexed.expireAfterSeconds()).isZero();
    }
}
