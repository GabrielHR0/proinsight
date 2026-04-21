package com.prosup.proinsight.mapper;

import com.prosup.proinsight.adapter.out.persistence.AcademiaDocument;
import com.prosup.proinsight.adapter.out.persistence.PermissionDocument;
import com.prosup.proinsight.adapter.out.persistence.RoleDocument;
import com.prosup.proinsight.adapter.out.persistence.UserDocument;
import com.prosup.proinsight.domain.model.Permission;
import com.prosup.proinsight.domain.model.Role;
import com.prosup.proinsight.domain.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    public static User toDomain(
            UserDocument userDocument,
            Set<RoleDocument> rolesDoc,
            Set<PermissionDocument> permissionsDoc
    ){
        Map<String, Permission> permMap = permissionsDoc.stream()
                .collect(Collectors.toMap(
                        PermissionDocument::getId,
                        p -> new Permission(
                                p.getId(),
                                p.getResource(),
                                p.getAction(),
                                p.getDescription()
                        )
                ));

        Map<String, Role> roleMap = rolesDoc.stream()
                .collect(Collectors.toMap(
                        RoleDocument::getId,
                        r -> new Role()
                ))
    }
}
