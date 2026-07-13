package com.prosup.proinsight.api.dto.response;

import java.time.Instant;

/**
 * DTO completo para visualização de todas as informações do protocolo.
 * Usado quando o avaliador seleciona uma avaliação para ver detalhes.
 */
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
