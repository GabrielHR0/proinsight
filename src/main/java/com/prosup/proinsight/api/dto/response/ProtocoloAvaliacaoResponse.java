package com.prosup.proinsight.api.dto.response;

import com.prosup.proinsight.domain.enums.Protocolo;

public record ProtocoloAvaliacaoResponse(
    String id,
    String nome,
    Protocolo protocolo,
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
    String observacoes
) {}
