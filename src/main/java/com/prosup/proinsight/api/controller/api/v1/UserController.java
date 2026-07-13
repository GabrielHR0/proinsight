package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.api.dto.request.UserRequest;
import com.prosup.proinsight.api.dto.response.UserResponse;
import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest request) {
        String[] roleIds = request.getRoleIds() != null
                ? request.getRoleIds().toArray(new String[0])
                : new String[0];
        String[] permissionIds = request.getPermissionIds() != null
                ? request.getPermissionIds().toArray(new String[0])
                : new String[0];

        User user = service.register(request.getEmail(), request.getPassword(), roleIds, permissionIds);

        Set<String> roleIdSet = user.getRoles() != null
                ? user.getRoles().stream().map(r -> r.getId()).collect(java.util.stream.Collectors.toSet())
                : Collections.emptySet();

        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                roleIdSet,
                Set.of(permissionIds),
                user.isActive(),
                user.getAcademiaIds(),
                user.getAvaliadorId(),
                user.getCreatedAt(),
                user.getUpdatedAt());

        return ResponseEntity.created(URI.create("/users/" + user.getId())).body(response);
    }
}
