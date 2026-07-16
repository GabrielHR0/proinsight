package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.api.dto.request.AvaliacaoVo2MaxRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoVo2MaxResponse;
import com.prosup.proinsight.service.handler.AvaliacaoVo2MaxHandler;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {
    
    private final AvaliacaoVo2MaxHandler handler;

    public AvaliacaoController(AvaliacaoVo2MaxHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/vo2max")
    public ResponseEntity<AvaliacaoVo2MaxResponse> avaliarVo2Max(
        @Valid @RequestBody AvaliacaoVo2MaxRequest request
    ) {
        AvaliacaoVo2MaxResponse response = handler.processar(request);
        return ResponseEntity.ok(response);
    }
}
