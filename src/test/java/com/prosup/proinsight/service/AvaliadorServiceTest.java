package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.request.AvaliadorRequest;
import com.prosup.proinsight.api.dto.response.AvaliadorResponse;
import com.prosup.proinsight.domain.model.Avaliador;
import com.prosup.proinsight.infrastructure.persistence.document.AvaliadorDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliadorMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliadorRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock
    private UserRepository userRepo;

    @Mock
    private AcademiaRepository academiaRepo;

    @Mock
    private AvaliadorMapper mapper;

    @InjectMocks
    private AvaliadorService service;

    @Test
    void create_sholdConvertRequestRoDomainAndReturnDto() {
        var request = new AvaliadorRequest();
        request.setFirstName("Joao");
        request.setLastName("Silva");
        request.setEmail("joao@example.com");
        request.setTelefone("11999999999");
        request.setCpf("12706529407");
        request.setCref("CREF-123");
        request.setUserId("user-1");

        var document = new AvaliadorDocument("generated-id-1", "user-1", null, "CREF-123", "Joao", "Silva", "joao@example.com", "11999999999", "12706529407");
        var domain = new Avaliador("generated-id-1", "user-1", null, "CREF-123", "Joao", "Silva", "joao@example.com", "11999999999", "12706529407");
        var response = new AvaliadorResponse("generated-id-1", "Joao", "Silva", "joao@example.com", "11999999999", "12706529407", "CREF-123", null);

        when(userRepo.existsById("user-1")).thenReturn(true);
        when(mapper.toDocument(request)).thenReturn(document);
        when(repository.save(document)).thenReturn(document);
        when(mapper.toDomain(document)).thenReturn(domain);
        when(mapper.toResponse(domain)).thenReturn(response);

        AvaliadorResponse result = service.create(request);

        assertThat(result).isSameAs(response);
    }
}
