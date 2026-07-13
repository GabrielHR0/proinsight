package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.api.dto.request.ClienteRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoListaResponse;
import com.prosup.proinsight.api.dto.response.ClienteResponse;
import com.prosup.proinsight.service.ClienteService;
import com.prosup.proinsight.service.handler.ListagemAvaliacaoHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ListagemAvaliacaoHandler listagemHandler;

    public ClienteController(ClienteService clienteService, ListagemAvaliacaoHandler listagemHandler) {
        this.clienteService = clienteService;
        this.listagemHandler = listagemHandler;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ClienteResponse> create(@Valid @RequestBody ClienteRequest request) {
        var response = clienteService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<ClienteResponse> listAll() {
        return clienteService.listAll();
    }

    @GetMapping("/{id}")
    public ClienteResponse findById(@PathVariable String id) {
        return clienteService.findById(id);
    }

    @GetMapping("/por-academia/{academiaId}")
    public List<ClienteResponse> findByAcademia(@PathVariable String academiaId) {
        return clienteService.findByAcademiaId(academiaId);
    }

    @GetMapping("/por-avaliador/{avaliadorId}")
    public List<ClienteResponse> findByAvaliador(@PathVariable String avaliadorId) {
        return clienteService.findByAvaliadorId(avaliadorId);
    }

    @GetMapping("/{clienteId}/avaliacoes")
    public List<AvaliacaoListaResponse> listarAvaliacoes(@PathVariable String clienteId) {
        return listagemHandler.listarPorCliente(clienteId);
    }
}
