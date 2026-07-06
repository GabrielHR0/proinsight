package com.prosup.proinsight.service;

import com.prosup.proinsight.domain.model.Avaliador;
import com.prosup.proinsight.api.dto.request.AvaliadorRequest;
import com.prosup.proinsight.api.dto.response.AvaliadorResponse;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliadorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class AvaliadorServiceTest {

    @Mock
    private AvaliadorRepository repository;

    @InjectMocks
    private AvaliadorService service;

    @Test
    void create_sholdConvertRequestRoDomainAndReturnDto() {
        var request = new AvaliadorRequest(
                "Joao",
                "Silva",
                "joao@example.com",
                "11999999999",
                "12706529407",
                "CREF-123"
        );

        var savedDocument = new com.prosup.proinsight.infrastructure.persistence.document.AvaliadorDocument(
                "generated-id-1",
                null,
                "CREF-123",
                "Joao",
                "Silva",
                "joao@example.com",
                "11999999999",
                "12706529407"
        );

        when(repository.save(any(com.prosup.proinsight.infrastructure.persistence.document.AvaliadorDocument.class))).thenReturn(savedDocument);

        AvaliadorResponse result = service.create(request);

        assertThat(result.id()).isNotNull();
        assertThat(result.cref()).isEqualTo(request.getCref());
        assertThat(result.email()).isEqualTo(request.getEmail());
        assertThat(result.telefone()).isEqualTo(request.getTelefone());
        assertThat(result.cpf()).isEqualTo(request.getCpf());
        assertThat(result.firstName()).isEqualTo(request.getFirstName());
        assertThat(result.lastName()).isEqualTo(request.getLastName());

    }
}
