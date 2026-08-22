package com.prosup.proinsight.api.dto.response;

import com.prosup.proinsight.domain.enums.Permissao;

import java.util.Set;

public record RoleResponse(
        String id,
        String name,
        String description,
        Set<Permissao> permissoes) {
}
