package com.prosup.proinsight.api.controller.api.v1;


import com.prosup.proinsight.api.dto.response.TabelaClassificacaoResponse;
import com.prosup.proinsight.infrastructure.persistence.mapper.TabelaClassificacaoMapper;
import com.prosup.proinsight.service.TabelasClassificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tabelas_classificacao")
public class TabelasClassificacaoController {

    private final TabelasClassificacaoService tabelasClassificacaoService;
    private final TabelaClassificacaoMapper tabelaClassificacaoMapper;

    public TabelasClassificacaoController(
            TabelasClassificacaoService tabelasClassificacaoService,
            TabelaClassificacaoMapper tabelaClassificacaoMapper) {
        this.tabelasClassificacaoService = tabelasClassificacaoService;
        this.tabelaClassificacaoMapper = tabelaClassificacaoMapper;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROTOCOLOS_LER')")
    public ResponseEntity<TabelaClassificacaoResponse> getById(@PathVariable String id) {
        return tabelasClassificacaoService.find(id)
                .map(doc -> ResponseEntity.ok(tabelaClassificacaoMapper.toResponse(doc)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('PROTOCOLOS_LER')")
    public ResponseEntity<List<TabelaClassificacaoResponse>> index() {
        return ResponseEntity.ok(
                tabelasClassificacaoService.findAll()
        );
    }
}
