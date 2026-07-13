package com.prosup.proinsight.api.dto.response;

import com.prosup.proinsight.domain.model.Endereco;

public record ClienteResponse(
        String id,
        String fullName,
        String email,
        String phone,
        String cpf,
        Endereco endereco,
        String academiaId,
        String avaliadorId
) {
}
