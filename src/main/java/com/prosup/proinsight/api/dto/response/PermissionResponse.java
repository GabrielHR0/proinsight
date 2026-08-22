package com.prosup.proinsight.api.dto.response;

import com.prosup.proinsight.domain.enums.Permissao;

public record PermissionResponse(
        Permissao permissao,
        String descricao) {
}
