package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Controller responsible for user-related endpoints.
 * Keep controllers thin — mapping and business rules belong to services.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public static class RegisterRequest {
        public String email;
        public String password;
        // accept arrays of role ids and permission ids
        public String[] roleIds;
        public String[] permissionIds;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest req) {
        User created = userService.register(req.email, req.password, req.roleIds, req.permissionIds);
        return ResponseEntity.created(URI.create("/users/" + created.getId())).body(created);
    }
}
