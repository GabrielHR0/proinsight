package com.prosup.proinsight.service;

import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.infrastructure.persistence.mapper.UserMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepo,
            RoleRepository roleRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String userName, String email, String plainPassword,
                         Map<String, Set<String>> academiaRoles) {
        String pwHash = passwordEncoder.encode(plainPassword == null ? "" : plainPassword);

        validateRoles(academiaRoles);

        var doc = UserMapper.toDocument(userName, email, pwHash, academiaRoles);
        var saved = userRepo.save(doc);
        return UserMapper.toDomain(saved);
    }

    private void validateRoles(Map<String, Set<String>> academiaRoles) {
        if (academiaRoles != null) {
            for (Set<String> roleIds : academiaRoles.values()) {
                if (roleIds != null) {
                    for (String rid : roleIds) {
                        if (!roleRepo.existsById(rid)) {
                            throw new IllegalArgumentException("Role id not found: " + rid);
                        }
                    }
                }
            }
        }
    }

    public Optional<User> findById(String id) {
        return userRepo.findById(id).map(UserMapper::toDomain);
    }
}
