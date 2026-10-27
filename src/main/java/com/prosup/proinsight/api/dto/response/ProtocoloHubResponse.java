package com.prosup.proinsight.api.dto.response;

import java.util.List;
import java.util.Map;

/**
 * Resposta tipada do hub de protocolos, mantendo o mesmo contrato JSON
 * anterior (favoritos + porCategoria) sem expor um {@code Map<String, Object>}.
 */
public record ProtocoloHubResponse(
        List<ProtocoloResumoResponse> favoritos,
        Map<String, List<ProtocoloResumoResponse>> porCategoria
) {}
