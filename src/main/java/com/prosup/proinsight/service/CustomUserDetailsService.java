package com.prosup.proinsight.service;

import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.infrastructure.persistence.document.RoleDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.UserMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.RoleRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByUserName(username))
                .map(UserMapper::toDomain)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public CustomUserDetails toUserDetails(User user) {
        Map<String, Set<Permissao>> academiaPermissoes = new HashMap<>();

        if (user.getAcademiaRoles() == null || user.getAcademiaRoles().isEmpty()) {
            return new CustomUserDetails(user, List.of(), academiaPermissoes);
        }

        Set<String> allRoleIds = user.getAcademiaRoles().values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        Map<String, RoleDocument> rolesById = roleRepository.findAllById(allRoleIds)
                .stream()
                .collect(Collectors.toMap(RoleDocument::getId, Function.identity()));

        Set<GrantedAuthority> allAuthorities = new HashSet<>();

        for (var entry : user.getAcademiaRoles().entrySet()) {
            Set<Permissao> permissoes = new HashSet<>();
            for (String roleId : entry.getValue()) {
                RoleDocument role = rolesById.get(roleId);
                if (role != null && role.getPermissoes() != null) {
                    permissoes.addAll(role.getPermissoes());
                }
            }
            academiaPermissoes.put(entry.getKey(), permissoes);
            permissoes.forEach(p -> allAuthorities.add(new SimpleGrantedAuthority(p.name())));
        }

        return new CustomUserDetails(user, List.copyOf(allAuthorities), academiaPermissoes);
    }
}
