package com.prosup.proinsight.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.infrastructure.persistence.document.RoleDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Carrega as permissões do usuário a partir do banco (roles atuais), com
 * cache curto de 60s. Usado pelo JwtAuthenticationFilter para que mudanças
 * de role/permissão reflitam em até 60s — não 24h como nas claims do JWT.
 */
@Service
public class UserPermissionService {

    private static final Logger log = LoggerFactory.getLogger(UserPermissionService.class);
    private static final Duration CACHE_TTL = Duration.ofSeconds(60);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final Cache<String, Map<String, Set<Permissao>>> cache = Caffeine.newBuilder()
            .expireAfterWrite(CACHE_TTL)
            .maximumSize(10_000)
            .build();

    public UserPermissionService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public Map<String, Set<Permissao>> loadAcademiaPermissoes(String userId) {
        return cache.get(userId, this::fetch);
    }

    public void evict(String userId) {
        cache.invalidate(userId);
    }

    private Map<String, Set<Permissao>> fetch(String userId) {
        UserDocument doc = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        if (!doc.isActive()) {
            log.warn("[PERM] Usuário inativo: userId={}", userId);
            throw new UsernameNotFoundException("User not found: " + userId);
        }
        Map<String, Set<Permissao>> result = buildAcademiaPermissoes(doc);
        log.debug("[PERM] Permissões carregadas do banco: userId={}, academiaIds={}, roles={}, permissoes={}",
                userId,
                doc.getAcademiaRoles() != null ? doc.getAcademiaRoles().keySet() : "null",
                doc.getAcademiaRoles() != null ? doc.getAcademiaRoles().values() : "null",
                result.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream().map(Enum::name).toList())));
        return result;
    }

    private Map<String, Set<Permissao>> buildAcademiaPermissoes(UserDocument doc) {
        Map<String, Set<Permissao>> academiaPermissoes = new HashMap<>();
        if (doc.getAcademiaRoles() == null || doc.getAcademiaRoles().isEmpty()) {
            log.warn("[PERM] Usuário sem academiaRoles: userId={}", doc.getId());
            return academiaPermissoes;
        }

        Set<String> allRoleIds = doc.getAcademiaRoles().values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        Map<String, RoleDocument> rolesById = roleRepository.findAllById(allRoleIds)
                .stream()
                .collect(Collectors.toMap(RoleDocument::getId, Function.identity()));

        Set<String> missingRoles = new HashSet<>(allRoleIds);
        missingRoles.removeAll(rolesById.keySet());
        if (!missingRoles.isEmpty()) {
            log.error("[PERM] Roles NÃO encontradas no banco: {} (userId={})", missingRoles, doc.getId());
        }

        for (var entry : doc.getAcademiaRoles().entrySet()) {
            Set<Permissao> permissoes = new HashSet<>();
            for (String roleId : entry.getValue()) {
                RoleDocument role = rolesById.get(roleId);
                if (role != null && role.getPermissoes() != null) {
                    permissoes.addAll(role.getPermissoes());
                }
            }
            academiaPermissoes.put(entry.getKey(), permissoes);
        }
        return academiaPermissoes;
    }
}
