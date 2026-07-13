package com.prosup.proinsight.api.dto.response;

/**
 * DTO simplificado para listagem de protocolos no hub.
 * Contém apenas dados essenciais para exibição.
 */
public record ProtocoloResumoResponse(
        String id,
        String nome,
        String categoria,
        Boolean padrao,
        String descricao,
        String unidadeMedida
) {}
