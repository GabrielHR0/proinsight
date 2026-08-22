package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.api.annotation.Audited;
import com.prosup.proinsight.api.dto.request.ClienteComImcRequest;
import com.prosup.proinsight.api.dto.request.ClienteRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoHistoricoResponse;
import com.prosup.proinsight.api.dto.response.ClienteComImcResponse;
import com.prosup.proinsight.api.dto.response.ClienteResponse;
import com.prosup.proinsight.service.ClienteService;
import com.prosup.proinsight.service.handler.ListagemAvaliacaoHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ListagemAvaliacaoHandler listagemHandler;

    public ClienteController(ClienteService clienteService, ListagemAvaliacaoHandler listagemHandler) {
        this.clienteService = clienteService;
        this.listagemHandler = listagemHandler;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Audited
    @PreAuthorize("hasAuthority('CLIENTES_CRIAR') and @auth.hasAcademiaAccess(#request.academiaId)")
    public ResponseEntity<ClienteResponse> create(@Valid @RequestBody ClienteRequest request) {
        var response = clienteService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/com-imc")
    @ResponseStatus(HttpStatus.CREATED)
    @Audited
    @PreAuthorize("hasAuthority('CLIENTES_CRIAR') and @auth.hasAcademiaAccess(#request.academiaId)")
    public ResponseEntity<ClienteComImcResponse> criarComImc(@Valid @RequestBody ClienteComImcRequest request) {
        var response = clienteService.criarComImc(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.cliente().id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENTES_LER')")
    public List<ClienteResponse> listAll() {
        return clienteService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTES_LER')")
    public ClienteResponse findById(@PathVariable String id) {
        return clienteService.findById(id);
    }

    @PutMapping("/{id}")
    @Audited
    @PreAuthorize("hasAuthority('CLIENTES_ATUALIZAR') and @auth.hasAcademiaAccess(#request.academiaId)")
    public ResponseEntity<ClienteResponse> update(@PathVariable String id,
                                                  @Valid @RequestBody ClienteRequest request) {
        var response = clienteService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/por-academia/{academiaId}")
    @PreAuthorize("hasAuthority('CLIENTES_LER') and @auth.hasAcademiaAccess(#academiaId)")
    public List<ClienteResponse> findByAcademia(@PathVariable String academiaId) {
        return clienteService.findByAcademiaId(academiaId);
    }

    @GetMapping("/por-avaliador/{avaliadorId}")
    @PreAuthorize("hasAuthority('CLIENTES_LER')")
    public List<ClienteResponse> findByAvaliador(@PathVariable String avaliadorId) {
        return clienteService.findByAvaliadorId(avaliadorId);
    }

    @GetMapping("/{clienteId}/avaliacoes")
    @PreAuthorize("hasAuthority('AVALIACOES_LER')")
    public List<AvaliacaoHistoricoResponse> listarAvaliacoes(@PathVariable String clienteId) {
        return listagemHandler.listarPorCliente(clienteId);
    }
}