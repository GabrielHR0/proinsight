package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.api.annotation.Audited;
import com.prosup.proinsight.api.dto.request.AcademiaRequest;
import com.prosup.proinsight.api.dto.response.AcademiaResponse;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.service.AcademiaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Audited
    @PreAuthorize("hasAuthority('ACADEMIAS_CRIAR')")
    public ResponseEntity<AcademiaResponse> create(@Valid @RequestBody AcademiaRequest request) {
        request.setOwnerId(currentUserId());
        AcademiaResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/academias/" + created.id())).body(created);
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser().getId();
        }
        throw new IllegalStateException("Usuário autenticado não encontrado");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIAS_LER') and @auth.hasAcademiaAccess(#id)")
    public ResponseEntity<AcademiaResponse> getById(@PathVariable String id) {
        var found = service.findById(id);
        return ResponseEntity.ok(found);
    }

    @GetMapping("/by-owner/{ownerId}")
    @PreAuthorize("@auth.isCurrentUser(#ownerId) or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<AcademiaResponse>> getByOwner(@PathVariable String ownerId) {
        var academias = service.findByOwnerId(ownerId);
        return ResponseEntity.ok(academias);
    }

    @PutMapping("/{id}")
    @Audited
    @PreAuthorize("hasAuthority('ACADEMIAS_ATUALIZAR') and @auth.hasAcademiaAccess(#id)")
    public ResponseEntity<AcademiaResponse> update(@PathVariable String id,
                                                    @Valid @RequestBody AcademiaRequest request) {
        var response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Audited
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
