package com.prosup.proinsight.service;

import com.prosup.proinsight.domain.model.Avaliador;
import com.prosup.proinsight.dto.request.AvaliadorDtoRequest;
import com.prosup.proinsight.dto.response.AvaliadorDto;
import com.prosup.proinsight.repository.AvaliadorRepository;
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
        var request = new AvaliadorDtoRequest(
                "Joao",
                "Silva",
                "joao@example.com",
                "11999999999",
                "12706529407",
                "CREF-123"
        );

        var savedDomain = new Avaliador(
                "generated-id-1",
                "CREF-123",
                "Joao",
                "Silva",
                "joao@example.com",
                "11999999999",
                "12706529407"
        );

        when(repository.save(any(Avaliador.class))).thenReturn(savedDomain);

        AvaliadorDto result = service.create(request);

        ArgumentCaptor<Avaliador> captor = ArgumentCaptor.forClass(Avaliador.class);
        verify(repository, times(1)).save(captor.capture());

        Avaliador passedToSave = captor.getValue();
        assertThat(passedToSave.getId()).isNull();
        assertThat(passedToSave.getCref()).isEqualTo(request.getCref());
        assertThat(passedToSave.getEmail()).isEqualTo(request.getEmail());
        assertThat(passedToSave.getTelefone()).isEqualTo(request.getTelefone());
        assertThat(passedToSave.getCpf()).isEqualTo(request.getCpf());
        assertThat(passedToSave.getFirstName()).isEqualTo(request.getFirstName());
        assertThat(passedToSave.getLastName()).isEqualTo(request.getLastName());

        assertThat(result.id()).isNotNull();
        assertThat(result.cref()).isEqualTo(request.getCref());
        assertThat(result.email()).isEqualTo(request.getEmail());
        assertThat(result.telefone()).isEqualTo(request.getTelefone());
        assertThat(result.cpf()).isEqualTo(request.getCpf());
        assertThat(result.firstName()).isEqualTo(request.getFirstName());
        assertThat(result.lastName()).isEqualTo(request.getLastName());

    }
}
