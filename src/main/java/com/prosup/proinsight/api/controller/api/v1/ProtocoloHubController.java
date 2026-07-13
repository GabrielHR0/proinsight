package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.api.dto.response.ProtocoloDetalheResponse;
import com.prosup.proinsight.api.dto.response.ProtocoloResumoResponse;
import com.prosup.proinsight.service.ProtocoloHubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/avaliacoes")
public class ProtocoloHubController {

    private final ProtocoloHubService hubService;

    public ProtocoloHubController(ProtocoloHubService hubService) {
        this.hubService = hubService;
    }

    @GetMapping("/hub")
    public ResponseEntity<Map<String, Object>> getHub(@RequestParam String userId) {
        var hub = hubService.getHub(userId);
        return ResponseEntity.ok(hub);
    }

    @GetMapping("/protocolos")
    public ResponseEntity<List<ProtocoloResumoResponse>> listarProtocolos() {
        var protocolos = hubService.listarTodos();
        return ResponseEntity.ok(protocolos);
    }

    @GetMapping("/protocolos/{id}")
    public ResponseEntity<ProtocoloDetalheResponse> getDetalheProtocolo(@PathVariable String id) {
        var detalhe = hubService.getDetalhe(id);
        return ResponseEntity.ok(detalhe);
    }

    @PostMapping("/favoritos")
    public ResponseEntity<Void> favoritar(@RequestParam String userId, @RequestParam String protocoloId) {
        hubService.favoritar(userId, protocoloId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favoritos")
    public ResponseEntity<Void> desfavoritar(@RequestParam String userId, @RequestParam String protocoloId) {
        hubService.desfavoritar(userId, protocoloId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favoritos")
    public ResponseEntity<List<ProtocoloResumoResponse>> listarFavoritos(@RequestParam String userId) {
        var favoritos = hubService.listarFavoritos(userId);
        return ResponseEntity.ok(favoritos);
    }

    @GetMapping("/favoritos/verificar")
    public ResponseEntity<Map<String, Boolean>> verificarFavorito(@RequestParam String userId, @RequestParam String protocoloId) {
        boolean isFavorito = hubService.isFavorito(userId, protocoloId);
        return ResponseEntity.ok(Map.of("isFavorito", isFavorito));
    }
}
