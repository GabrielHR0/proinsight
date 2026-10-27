package com.prosup.proinsight.service;

import com.prosup.proinsight.AbstractIntegrationTest;
import com.prosup.proinsight.api.dto.response.ProtocoloResumoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProtocoloHubServiceIT extends AbstractIntegrationTest {

    @Autowired
    private ProtocoloHubService service;

    @Test
    void projectionsDeResumoFuncionamNoMongo() {
        var resumos = service.listarTodos();

        assertThat(resumos).isNotEmpty();
        assertThat(resumos.get(0).id()).isNotBlank();
        assertThat(resumos.get(0).nome()).isNotBlank();

        var protocoloId = resumos.get(0).id();
        var userId = "it-hub-" + UUID.randomUUID();

        try {
            service.favoritar(userId, protocoloId);

            var hub = service.getHub(userId);
            assertThat(hub.favoritos())
                    .extracting(ProtocoloResumoResponse::id)
                    .containsExactly(protocoloId);
            assertThat(hub.porCategoria()).isNotEmpty();

            assertThat(service.listarFavoritos(userId))
                    .extracting(ProtocoloResumoResponse::id)
                    .containsExactly(protocoloId);
        } finally {
            service.desfavoritar(userId, protocoloId);
        }
    }
}
