package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.api.annotation.Audited;
import com.prosup.proinsight.api.dto.request.AvaliacaoImcRequest;
import com.prosup.proinsight.api.dto.request.AvaliacaoVo2MaxRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoImcResponse;
import com.prosup.proinsight.api.dto.response.AvaliacaoVo2MaxResponse;
import com.prosup.proinsight.api.dto.response.DadosPreAvaliacaoResponse;
import com.prosup.proinsight.service.PreAvaliacaoService;
import com.prosup.proinsight.service.handler.AvaliacaoImcHandler;
import com.prosup.proinsight.service.handler.AvaliacaoVo2MaxHandler;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoVo2MaxHandler vo2MaxHandler;
    private final AvaliacaoImcHandler imcHandler;
    private final PreAvaliacaoService preAvaliacaoService;

    public AvaliacaoController(AvaliacaoVo2MaxHandler vo2MaxHandler,
                               AvaliacaoImcHandler imcHandler,
                               PreAvaliacaoService preAvaliacaoService) {
        this.vo2MaxHandler = vo2MaxHandler;
        this.imcHandler = imcHandler;
        this.preAvaliacaoService = preAvaliacaoService;
    }

    @PostMapping("/vo2max")
    @Audited
    @PreAuthorize("hasAuthority('AVALIACOES_CRIAR')")
    public ResponseEntity<AvaliacaoVo2MaxResponse> avaliarVo2Max(
        @Valid @RequestBody AvaliacaoVo2MaxRequest request
    ) {
        AvaliacaoVo2MaxResponse response = vo2MaxHandler.processar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/imc")
    @Audited
    @PreAuthorize("hasAuthority('AVALIACOES_CRIAR')")
    public ResponseEntity<AvaliacaoImcResponse> criarImc(
        @Valid @RequestBody AvaliacaoImcRequest request
    ) {
        AvaliacaoImcResponse response = imcHandler.processar(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{protocoloId}/dados-pre-avaliacao/{clienteId}")
    @PreAuthorize("hasAuthority('AVALIACOES_LER')")
    public ResponseEntity<DadosPreAvaliacaoResponse> dadosPreAvaliacao(
            @PathVariable String protocoloId,
            @PathVariable String clienteId) {
        var dados = preAvaliacaoService.buscarDados(clienteId, protocoloId);
        return ResponseEntity.ok(dados);
    }
}
