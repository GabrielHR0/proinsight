package com.prosup.proinsight.api.dto.response;

import java.time.Instant;

public record ProtocoloDetalheResponse(
        String id,
        String nome,
        String categoria,
        Boolean padrao,
        String strategyKey,
        String tabelaClassificacaoId,
        String descricao,
        String comoRealizar,
        String calculadora,
        String referenciaBibliografica,
        String unidadeMedida,
        Integer tempoMinimoSegundos,
        Integer tempoMaximoSegundos,
        String equipamentoNecessario,
        String criteriosExclusao,
        String observacoes,
        Instant createdAt
) {}
