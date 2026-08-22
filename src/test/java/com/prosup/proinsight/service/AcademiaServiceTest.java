package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.request.AcademiaRequest;
import com.prosup.proinsight.api.dto.response.AcademiaResponse;
import com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.AcademiaMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AcademiaServiceTest {

    @Mock
    private AcademiaRepository repo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private AcademiaMapper mapper;

    @InjectMocks
    private AcademiaService service;

    // ── CREATE ──────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveAndReturnResponse() {
        // arrange
        var request = buildRequest();
        var doc = buildDocument("new-id");
        var response = buildResponse("new-id");

        when(userRepo.existsById("user-1")).thenReturn(true);
        when(mapper.toDocument(request)).thenReturn(doc);
        when(repo.save(doc)).thenReturn(doc);
        when(mapper.toResponse(doc)).thenReturn(response);

        // act
        var result = service.create(request);

        // assert
        assertThat(result).isSameAs(response);
        verify(repo).save(doc);
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        var request = buildRequest();
        when(userRepo.existsById("user-1")).thenReturn(false);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("User not found");

        verify(repo, never()).save(any());
    }

    // ── FIND BY ID ──────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnResponse() {
        var doc = buildDocument("id-1");
        var response = buildResponse("id-1");

        when(repo.findById("id-1")).thenReturn(Optional.of(doc));
        when(mapper.toResponse(doc)).thenReturn(response);

        var result = service.findById("id-1");

        assertThat(result).isSameAs(response);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(repo.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("missing"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("não encontrada");
    }

    // ── FIND BY OWNER ────────────────────────────────────────────────────

    @Test
    void findByOwnerId_shouldReturnResponse() {
        var doc = buildDocument("id-1");
        var response = buildResponse("id-1");

        when(repo.findByOwnerId("user-1")).thenReturn(List.of(doc));
        when(mapper.toResponse(doc)).thenReturn(response);

        var result = service.findByOwnerId("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(response);
    }

    @Test
    void findByOwnerId_shouldReturnEmptyListWhenNotFound() {
        when(repo.findByOwnerId("user-unknown")).thenReturn(List.of());

        var result = service.findByOwnerId("user-unknown");

        assertThat(result).isEmpty();
    }

    // ── UPDATE ──────────────────────────────────────────────────────────

    @Test
    void update_shouldSaveAndReturnResponse() {
        var request = buildRequest();
        request.setNomeFantasia("Academia Atualizada");
        var existingDoc = buildDocument("id-1");
        var newDoc = buildDocument("id-1");
        var response = buildResponse("id-1");

        when(repo.findById("id-1")).thenReturn(Optional.of(existingDoc));
        when(mapper.toDocument(request)).thenReturn(newDoc);
        when(repo.save(newDoc)).thenReturn(newDoc);
        when(mapper.toResponse(newDoc)).thenReturn(response);

        var result = service.update("id-1", request);

        assertThat(result).isSameAs(response);
        verify(repo).save(newDoc);
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var request = buildRequest();
        when(repo.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("missing", request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("não encontrada");
    }

    // ── DELETE ──────────────────────────────────────────────────────────

    @Test
    void delete_shouldCallRepository() {
        service.delete("id-1");

        verify(repo).deleteById("id-1");
    }

    // ── HELPERS ─────────────────────────────────────────────────────────

    private AcademiaRequest buildRequest() {
        var request = new AcademiaRequest();
        request.setOwnerId("user-1");
        request.setNomeFantasia("Academia Teste");
        request.setCnpj("12.345.678/0001-90");
        request.setTelefone("11999999999");
        return request;
    }

    private AcademiaDocument buildDocument(String id) {
        return new AcademiaDocument(id, "user-1", "12.345.678/0001-90",
                "Academia Teste", null, null, "11999999999");
    }

    private AcademiaResponse buildResponse(String id) {
        return new AcademiaResponse(id, "user-1", "Academia Teste",
                null, "12.345.678/0001-90", null, "11999999999", null, null);
    }
}
