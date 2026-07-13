package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.domain.model.Role;
import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class UserMapper {

    public User toDomain(UserDocument d) {
        if (d == null) return null;

        Set<Role> domainRoles = Collections.emptySet();
        if (d.getRoles() != null && !d.getRoles().isEmpty()) {
            domainRoles = new HashSet<>();
            for (String rid : d.getRoles()) {
                domainRoles.add(new Role(rid, null, null, Collections.emptySet()));
            }
        }

        return new User(
                d.getId(),
                d.getEmail(),
                d.getPassword(),
                domainRoles,
                d.isActive(),
                d.getAcademiaIds() != null ? new ArrayList<>(d.getAcademiaIds()) : new ArrayList<>(),
                d.getAvaliadorId(),
                d.getCreatedAt(),
                d.getUpdatedAt());
    }

    public UserDocument toDocument(String email, String passwordHash, Set<String> roleIds) {
        UserDocument doc = new UserDocument();
        doc.setEmail(email);
        doc.setPassword(passwordHash);
        doc.setActive(true);
        if (roleIds != null) {
            doc.setRoles(new HashSet<>(roleIds));
        }
        return doc;
    }
}
