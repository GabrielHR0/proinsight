package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.repository.PermissionRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import com.prosup.proinsight.infrastructure.persistence.mapper.UserMapper;
import com.prosup.proinsight.domain.model.User;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PermissionRepository permissionRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepo,
            RoleRepository roleRepo,
            PermissionRepository permissionRepo,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.permissionRepo = permissionRepo;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String email, String plainPassword, String[] roleIds, String[] permissionIds) {
        String pwHash = passwordEncoder.encode(plainPassword == null ? "" : plainPassword);

        validateRoles(roleIds);
        validatePermissions(permissionIds);

        var roleIdsSet = roleIds != null ? new HashSet<>(Arrays.asList(roleIds)) : null;
        var doc = userMapper.toDocument(email, pwHash, roleIdsSet);
        var saved = userRepo.save(doc);
        return userMapper.toDomain(saved);
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
        return userRepo.findById(id).map(userMapper::toDomain);
    }

    public Optional<com.prosup.proinsight.infrastructure.persistence.document.UserDocument> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }
}
