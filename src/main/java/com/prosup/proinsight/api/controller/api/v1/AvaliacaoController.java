package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.api.dto.request.AvaliacaoVo2MaxRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoVo2MaxResponse;
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
@RequestMapping("/avaliacoes")
public class AvaliacaoController {
    
    private final AvaliacaoVo2MaxHandler handler;
    
    @Autowired
    public AvaliacaoController(AvaliacaoVo2MaxHandler handler) {
        this.handler = handler;
    }
    
    /**
     * Realiza uma avaliação VO2Max.
     * 
     * @param request Dados da avaliação
     * @return 200 OK com resultado da classificação
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
