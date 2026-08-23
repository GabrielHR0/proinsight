package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.api.annotation.Audited;
import com.prosup.proinsight.api.dto.request.UserRequest;
import com.prosup.proinsight.api.dto.response.UserResponse;
import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @Audited
    @PreAuthorize("hasAuthority('USUARIOS_CRIAR') and @auth.hasAnyAcademiaAccess(#request.academiaRoles?.keySet())")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        var academiaRoles = request.getAcademiaRoles() != null
                ? request.getAcademiaRoles()
                : new HashMap<String, java.util.Set<String>>();

        User user = service.register(
                request.getUserName(),
                request.getEmail(),
                request.getPassword(),
                academiaRoles,
                request.getCref(),
                request.getCpf());

        UserResponse response = new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getAcademiaRoles() != null ? user.getAcademiaRoles() : new HashMap<>(),
                user.isActive(),
                user.getAcademiaIds() != null ? user.getAcademiaIds() : new HashSet<>(),
                user.getCref(),
                user.getCpf(),
                user.getCreatedAt(),
                user.getUpdatedAt());

        return ResponseEntity.created(URI.create("/users/" + user.getId())).body(response);
    }
}
