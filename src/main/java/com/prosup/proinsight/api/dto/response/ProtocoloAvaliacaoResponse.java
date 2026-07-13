package com.prosup.proinsight.api.dto.response;

import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;

public record ProtocoloAvaliacaoResponse(
    String id,
    String nome,
    ProtocoloVo2Max protocoloVo2Max,
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
