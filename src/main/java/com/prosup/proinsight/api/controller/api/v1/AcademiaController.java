package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.domain.model.Academia;
import com.prosup.proinsight.service.AcademiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/academias")
public class AcademiaController {

    private final AcademiaService service;

    public AcademiaController(AcademiaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Academia> create(@RequestParam String userId, @RequestBody Academia academia) {
        Academia created = service.createProfile(userId, academia);
        return ResponseEntity.created(URI.create("/academias/" + created.getId())).body(created);
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Academia> getByUser(@PathVariable String userId) {
        return service.findByUserId(userId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
