package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserMapper {

    public static User toDomain(UserDocument d) {
        if (d == null) return null;

        Map<String, Set<String>> academiaRoles = d.getAcademiaRoles() != null
                ? new HashMap<>(d.getAcademiaRoles())
                : new HashMap<>();

        return new User(
                d.getId(),
                d.getUserName(),
                d.getEmail(),
                d.getPassword(),
                academiaRoles,
                d.isActive(),
                d.getFailedLoginAttempts() != null ? d.getFailedLoginAttempts() : 0,
                d.getLockedUntil(),
                d.getAcademiaIds() != null ? new HashSet<>(d.getAcademiaIds()) : new HashSet<>(),
                d.getCref(),
                d.getCpf(),
                d.getCreatedAt(),
                d.getUpdatedAt());
    }

    public static UserDocument toDocument(String userName, String email, String passwordHash,
                                          Map<String, Set<String>> academiaRoles) {
        UserDocument doc = new UserDocument();
        doc.setUserName(userName);
        doc.setEmail(email);
        doc.setPassword(passwordHash);
        doc.setActive(true);
        if (academiaRoles != null) {
            doc.setAcademiaRoles(new HashMap<>(academiaRoles));
        }
        return doc;
    }
}
