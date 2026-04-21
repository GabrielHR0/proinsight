package com.prosup.proinsight.service;

import com.prosup.proinsight.adapter.out.persistence.MongoPermissionDataRepository;
import com.prosup.proinsight.domain.model.Role;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.prosup.proinsight.adapter.out.persistence.MongoRoleDataRepository;
import com.prosup.proinsight.adapter.out.persistence.MongoUserDataRepository;
import com.prosup.proinsight.adapter.out.persistence.UserDocument;
import com.prosup.proinsight.domain.model.User;
import java.time.Instant;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for user-related operations.
 *
 * Note: this class intentionally preserves existing behavior — only formatting and readability
 * improvements were applied. Password hashing remains a placeholder and should be replaced
 * with a secure algorithm (BCrypt) when enabling authentication.
 */
@Service
public class UserService {

    private final MongoUserDataRepository userRepo;
    private final MongoRoleDataRepository roleRepo;
    private final MongoPermissionDataRepository permissionRepo;

    private final PasswordEncoder passwordEncoder;

    public UserService(
            MongoUserDataRepository userRepo,
            MongoRoleDataRepository roleRepo,
            MongoPermissionDataRepository permissionRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.permissionRepo = permissionRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user. Accepts role ids and permission ids and validates their existence.
     *
     * Behavior preserved: uses a placeholder password value (UUID) instead of a real hash.
     */
    public User register(String email, String plainPassword, String[] roleIds, String[] permissionIds) {
        String pwHash = passwordEncoder.encode(plainPassword == null ? "" : plainPassword);

        validateRoles(roleIds);
        validatePermissions(permissionIds);

        UserDocument doc = new UserDocument();
        doc.setEmail(email);
        doc.setPassword(pwHash);
        doc.setActive(true);

        // convert incoming role id array to Set<String> to fit UserDocument.roles
        if (roleIds != null) {
            Set<String> roleIdSet = new HashSet<>(Arrays.asList(roleIds));
            doc.setRoles(roleIdSet);
        }

        doc.setCreatedAt(Instant.now());
        doc.setUpdatedAt(Instant.now());

        UserDocument saved = userRepo.save(doc);
        return toDomain(saved);
    }

    private void validatePermissions(String[] permissionIds) {
        if (permissionIds != null) {
            for (String pid : permissionIds) {
                if (!permissionRepo.existsById(pid)) {
                    throw new IllegalArgumentException("Permission id not found: " + pid);
                }
            }
        }
    }

    private void validateRoles(String[] roleIds) {
        if (roleIds != null) {
            for (String rid : roleIds) {
                if (!roleRepo.existsById(rid)) {
                    throw new IllegalArgumentException("Role id not found: " + rid);
                }
            }
        }
    }

    public Optional<User> findById(String id) {
        return userRepo.findById(id).map(this::toDomain);
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findAll()
                .stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst()
                .map(this::toDomain);
    }

    private User toDomain(UserDocument d) {
        if (d == null) {
            return null;
        }

        // map stored role ids (Set<String>) into domain Role objects with minimal information
        Set<Role> domainRoles = Collections.emptySet();
        if (d.getRoles() != null && !d.getRoles().isEmpty()) {
            domainRoles = new HashSet<>();
            for (String rid : d.getRoles()) {
                // Role details (nome, descricao, permissions) can be populated later; keep minimal mapping now
                domainRoles.add(new Role(rid, null, null, Collections.emptySet()));
            }
        }

        return new User(
                d.getId(),
                d.getEmail(),
                d.getPassword(),
                domainRoles,
                d.isActive(),
                d.getCreatedAt(),
                d.getUpdatedAt());
    }
}

