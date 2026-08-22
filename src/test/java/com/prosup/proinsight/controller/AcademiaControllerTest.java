package com.prosup.proinsight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosup.proinsight.api.controller.api.v1.AcademiaController;
import com.prosup.proinsight.api.dto.request.AcademiaRequest;
import com.prosup.proinsight.api.dto.response.AcademiaResponse;
import com.prosup.proinsight.api.handler.GlobalExceptionHandler;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.service.AcademiaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AcademiaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AcademiaService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AcademiaController controller = new AcademiaController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        var user = new User();
        user.setId("user-1");
        user.setEmail("owner@test.com");
        user.setUserName("owner");
        user.setAcademiaIds(Set.of("academia-1"));
        var principal = new CustomUserDetails(user, List.of(), Map.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "", List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── POST /academias ────────────────────────────────────────────────

    @Test
    void create_shouldReturn201WithBody() throws Exception {
        var request = buildRequest();
        var response = buildResponse("new-id");

        when(service.create(any(AcademiaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/academias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value("new-id"))
                .andExpect(jsonPath("$.nomeFantasia").value("Academia Teste"))
                .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-90"));
    }

    @Test
    void create_shouldReturn400WhenUserNotFound() throws Exception {
        var request = buildRequest();

        when(service.create(any(AcademiaRequest.class)))
                .thenThrow(new NoSuchElementException("User not found: user-1"));

        mockMvc.perform(post("/academias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found: user-1"));
    }

    @Test
    void create_shouldUseAuthenticatedUserAsOwnerIgnoringBodyOwnerId() throws Exception {
        var request = buildRequest();
        request.setOwnerId("usuario-malicioso");
        var response = buildResponse("new-id");

        when(service.create(any(AcademiaRequest.class))).thenAnswer(inv -> {
            AcademiaRequest captured = inv.getArgument(0);
            assertThat(captured.getOwnerId()).isEqualTo("user-1");
            return response;
        });

        mockMvc.perform(post("/academias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // ── GET /academias/{id} ────────────────────────────────────────────

    @Test
    void getById_shouldReturn200() throws Exception {
        var response = buildResponse("id-1");
        when(service.findById("id-1")).thenReturn(response);

        mockMvc.perform(get("/academias/id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-1"))
                .andExpect(jsonPath("$.nomeFantasia").value("Academia Teste"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(service.findById("missing"))
                .thenThrow(new NoSuchElementException("Academia não encontrada"));

        mockMvc.perform(get("/academias/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    // ── GET /academias/by-owner/{ownerId} ───────────────────────────────

    @Test
    void getByOwner_shouldReturn200() throws Exception {
        var response = buildResponse("id-1");
        when(service.findByOwnerId("user-1")).thenReturn(List.of(response));

        mockMvc.perform(get("/academias/by-owner/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerId").value("user-1"));
    }

    @Test
    void getByOwner_shouldReturnEmptyListWhenNotFound() throws Exception {
        when(service.findByOwnerId("user-unknown")).thenReturn(List.of());

        mockMvc.perform(get("/academias/by-owner/user-unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── PUT /academias/{id} ────────────────────────────────────────────

    @Test
    void update_shouldReturn200() throws Exception {
        var request = buildRequest();
        request.setNomeFantasia("Academia Atualizada");
        var response = buildResponse("id-1");

        when(service.update(eq("id-1"), any(AcademiaRequest.class))).thenReturn(response);

        mockMvc.perform(put("/academias/id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-1"))
                .andExpect(jsonPath("$.nomeFantasia").value("Academia Teste"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = buildRequest();
        when(service.update(eq("missing"), any(AcademiaRequest.class)))
                .thenThrow(new NoSuchElementException("Academia não encontrada"));

        mockMvc.perform(put("/academias/missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /academias/{id} ─────────────────────────────────────────

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/academias/id-1"))
                .andExpect(status().isNoContent());

        verify(service).delete("id-1");
    }

    @Test
    void delete_shouldCallServiceOnlyOnce() throws Exception {
        mockMvc.perform(delete("/academias/id-1"));

        verify(service, times(1)).delete("id-1");
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

    private AcademiaResponse buildResponse(String id) {
        return new AcademiaResponse(id, "user-1", "Academia Teste",
                null, "12.345.678/0001-90", null, "11999999999", null, null);
    }
}
