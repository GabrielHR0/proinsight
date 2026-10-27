package com.prosup.proinsight.service;

import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.infrastructure.persistence.mapper.UserMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashSet;
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
                         Map<String, Set<String>> academiaRoles,
                         String cref, String cpf) {
        String pwHash = passwordEncoder.encode(plainPassword == null ? "" : plainPassword);

        validateRoles(academiaRoles);

        var doc = UserMapper.toDocument(userName, email, pwHash, academiaRoles);

        // Sincroniza academiaIds com as chaves de academiaRoles
        if (academiaRoles != null) {
            for (String academiaId : academiaRoles.keySet()) {
                doc.addAcademiaId(academiaId);
            }
        }

        if (cref != null && !cref.isBlank()) {
            doc.setCref(cref.trim());
        }
        if (cpf != null && !cpf.isBlank()) {
            doc.setCpf(cpf.trim());
        }

        var saved = userRepo.save(doc);
        return UserMapper.toDomain(saved);
    }

    private void validateRoles(Map<String, Set<String>> academiaRoles) {
        if (academiaRoles == null) {
            return;
        }

        Set<String> roleIds = new LinkedHashSet<>();
        for (Set<String> rolesDaAcademia : academiaRoles.values()) {
            if (rolesDaAcademia != null) {
                for (String rid : rolesDaAcademia) {
                    if (rid == null) {
                        throw new IllegalArgumentException("Role id not found: null");
                    }
                    roleIds.add(rid);
                }
            }
        }

        if (roleIds.isEmpty()) {
            return;
        }

        // Uma única consulta para todos os ids, em vez de um existsById por role.
        Set<String> encontrados = new HashSet<>();
        for (var role : roleRepo.findAllById(roleIds)) {
            encontrados.add(role.getId());
        }

        for (String rid : roleIds) {
            if (!encontrados.contains(rid)) {
                throw new IllegalArgumentException("Role id not found: " + rid);
            }
        }
    }

    public Optional<User> findById(String id) {
        return userRepo.findById(id).map(UserMapper::toDomain);
    }
}
