package com.prosup.proinsight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosup.proinsight.api.controller.api.v1.AvaliacaoController;
import com.prosup.proinsight.api.dto.request.AvaliacaoVo2MaxRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoVo2MaxResponse;
import com.prosup.proinsight.api.dto.response.ClassificacaoVo2Max;
import com.prosup.proinsight.api.handler.GlobalExceptionHandler;
import com.prosup.proinsight.service.PreAvaliacaoService;
import com.prosup.proinsight.service.handler.AvaliacaoImcHandler;
import com.prosup.proinsight.service.handler.AvaliacaoVo2MaxHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoControllerValidationTest {

    private MockMvc mockMvc;

    @Mock
    private AvaliacaoVo2MaxHandler handler;

    @Mock
    private AvaliacaoImcHandler imcHandler;

    @Mock
    private PreAvaliacaoService preAvaliacaoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        var controller = new AvaliacaoController(handler, imcHandler, preAvaliacaoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn200WhenPayloadIsValid() throws Exception {
        var request = buildValidRequest();
        var response = new AvaliacaoVo2MaxResponse();
        response.setAvaliacaoId("avaliacao-1");
        response.setClienteId("cliente-1");
        response.setAvaliadorId("avaliador-1");
        var classificacao = new ClassificacaoVo2Max();
        classificacao.setNome("EXCELENTE");
        response.setClassificacao(classificacao);

        when(handler.processar(any())).thenReturn(response);

        mockMvc.perform(post("/avaliacoes/vo2max")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.classificacao.nome").value("EXCELENTE"))
                .andExpect(jsonPath("$.avaliacao_id").value("avaliacao-1"));
    }

    @Test
    void shouldReturn400WhenClienteIdIsBlank() throws Exception {
        var request = buildValidRequest();
        request.setClienteId("");

        mockMvc.perform(post("/avaliacoes/vo2max")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("proinsight://problems/validation-error"))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.violations[0].field").value("clienteId"))
                .andExpect(jsonPath("$.violations[0].message").value("cliente_id é obrigatório"));
    }

    @Test
    void shouldReturn400WhenProtocoloIdIsBlank() throws Exception {
        var request = buildValidRequest();
        request.setProtocoloId("");

        mockMvc.perform(post("/avaliacoes/vo2max")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("proinsight://problems/validation-error"))
                .andExpect(jsonPath("$.violations[0].field").value("protocoloId"))
                .andExpect(jsonPath("$.violations[0].message").value("protocolo_id é obrigatório"));
    }

    @Test
    void shouldReturn400WhenAvaliadorIdIsBlank() throws Exception {
        var request = buildValidRequest();
        request.setAvaliadorId("");

        mockMvc.perform(post("/avaliacoes/vo2max")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("proinsight://problems/validation-error"))
                .andExpect(jsonPath("$.violations[0].field").value("avaliadorId"))
                .andExpect(jsonPath("$.violations[0].message").value("avaliador_id é obrigatório"));
    }

    @Test
    void shouldReturn400WhenResultadoIsNegative() throws Exception {
        var request = buildValidRequest();
        request.setResultado(-1.0);

        mockMvc.perform(post("/avaliacoes/vo2max")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("proinsight://problems/validation-error"))
                .andExpect(jsonPath("$.violations[0].field").value("resultado"))
                .andExpect(jsonPath("$.violations[0].message").value("resultado deve ser maior ou igual a zero"));
    }

    @Test
    void shouldReturn400WithMultipleViolations() throws Exception {
        var request = buildValidRequest();
        request.setClienteId("");
        request.setProtocoloId("");
        request.setAvaliadorId("");
        request.setResultado(-5.0);

        mockMvc.perform(post("/avaliacoes/vo2max")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("proinsight://problems/validation-error"))
                .andExpect(jsonPath("$.violations.length()").value(4));
    }

    private AvaliacaoVo2MaxRequest buildValidRequest() {
        var request = new AvaliacaoVo2MaxRequest();
        request.setClienteId("cliente-1");
        request.setProtocoloId("protocolo-1");
        request.setAvaliadorId("avaliador-1");
        request.setResultado(42.0);
        return request;
    }
}
