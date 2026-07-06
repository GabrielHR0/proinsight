package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.api.dto.request.AvaliadorRequest;
import com.prosup.proinsight.api.dto.response.AvaliadorResponse;
import com.prosup.proinsight.service.AvaliadorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/avaliadores")
public class AvaliadorController {

    private final AvaliadorService service;

    public AvaliadorController(AvaliadorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AvaliadorResponse> create(@Valid @RequestBody AvaliadorRequest request) {
        var created = service.create(request);
        return ResponseEntity.created(URI.create("/avaliadores/" + created.id())).body(created);
    }
}
