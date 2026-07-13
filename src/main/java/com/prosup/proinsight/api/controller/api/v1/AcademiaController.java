package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.api.dto.request.AcademiaRequest;
import com.prosup.proinsight.api.dto.response.AcademiaResponse;
import com.prosup.proinsight.service.AcademiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/academias")
public class AcademiaController {

    private final AcademiaService service;

    public AcademiaController(AcademiaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AcademiaResponse> create(@RequestBody AcademiaRequest request) {
        AcademiaResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/academias/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademiaResponse> getById(@PathVariable String id) {
        var found = service.findById(id);
        return ResponseEntity.ok(found);
    }

    @GetMapping("/by-owner/{ownerId}")
    public ResponseEntity<List<AcademiaResponse>> getByOwner(@PathVariable String ownerId) {
        var academias = service.findByOwnerId(ownerId);
        return ResponseEntity.ok(academias);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AcademiaResponse> update(@PathVariable String id,
                                                   @RequestBody AcademiaRequest request) {
        var response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
