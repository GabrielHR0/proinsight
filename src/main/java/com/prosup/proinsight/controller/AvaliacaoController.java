package com.prosup.proinsight.controller;

import com.prosup.proinsight.dto.request.AvaliacaoVo2MaxRequest;
import com.prosup.proinsight.dto.response.AvaliacaoVo2MaxResponse;
import com.prosup.proinsight.service.handler.AvaliacaoVo2MaxHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para avaliações de VO2Max.
 * 
 * Responsabilidade: Apenas receber HTTP e retornar resposta.
 * Delega toda lógica para AvaliacaoVo2MaxHandler.
 * 
 * Spring: @RestController + @Autowired + @PostMapping
 */
@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {
    
    private final AvaliacaoVo2MaxHandler handler;
    
    @Autowired
    public AvaliacaoController(AvaliacaoVo2MaxHandler handler) {
        this.handler = handler;
    }
    
    /**
     * Realiza uma avaliação VO2Max.
     * 
     * Request:
     * {
     *   "clienteId": "client-123",
     *   "avaliadorId": "avaliador-456",
     *   "medicaoVo2Max": {
     *     "valor": 45.5,
     *     "frequenciaCardiacaRepouso": 60,
     *     "frequenciaCardiacaMaxima": 180,
     *     "tabelaClassificacaoId": "tabela-vo2-123",
     *     "testes": [...]
     *   }
     * }
     * 
     * Response (200 OK):
     * {
     *   "cliente_id": "client-123",
     *   "avaliador_id": "avaliador-456",
     *   "classificacao": {...},
     *   "data_avaliacao": "2026-04-30T11:45:00"
     * }
     * 
     * @param request Dados da avaliação (clienteId, avaliadorId, medicaoVo2Max com testes)
     * @return 200 OK com resultado da classificação
     * @return 400 Bad Request se estrutura inválida (ValidacaoException)
     * @return 422 Unprocessable Entity se pré-requisito não atendido (RegraNeggocioException)
     * @return 404 Not Found se tabela não existe (RecursoNaoEncontradoException)
     * @return 500 Internal Server Error se erro ao processar (AvaliacaoException)
     */
    @PostMapping("/vo2max")
    public ResponseEntity<AvaliacaoVo2MaxResponse> avaliarVo2Max(
        @RequestBody AvaliacaoVo2MaxRequest request
    ) {
        // Handler faz todo o trabalho:
        // 1. Valida estrutura
        // 2. Cria contexto (valida negócio)
        // 3. Executa estratégia
        // 4. Converte para response
        // Exceções são tratadas por GlobalExceptionHandler
        AvaliacaoVo2MaxResponse response = handler.processar(request);
        return ResponseEntity.ok(response);
    }
}

