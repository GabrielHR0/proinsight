package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.api.dto.response.AvaliacaoImcResponse;
import com.prosup.proinsight.api.dto.response.AvaliacaoVo2MaxResponse;
import com.prosup.proinsight.api.dto.response.ClassificacaoVo2Max;
import com.prosup.proinsight.api.dto.response.ReferenciaClassificacaoResponse;
import com.prosup.proinsight.domain.model.ClassificacaoLegivel;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.NivelImc;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.strategy.AvaliacaoVo2MaxContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AvaliacaoResponseMapper {

    public String obterNomeClassificacao(Leaf resultado) {
        if (resultado == null) return "SEM_CLASSIFICACAO";
        if (resultado instanceof NivelVo2Max n) {
            return n.getClassificacao();
        }
        if (resultado instanceof NivelImc n) {
            return n.getClassificacao();
        }
        return resultado.getClass().getSimpleName();
    }

    public String obterNomeClassificacaoLegivel(Leaf resultado) {
        return ClassificacaoLegivel.humanizar(obterNomeClassificacao(resultado));
    }

    public Double extrairValorClassificado(AvaliacaoVo2MaxContext context) {
        return context.getTestes().stream()
            .findFirst()
            .map(t -> t.getValorClassificacao(context.getDadosAvaliacao()))
            .filter(v -> v != null && !v.isBlank())
            .map(v -> {
                try {
                    return Double.parseDouble(v);
                } catch (NumberFormatException e) {
                    return null;
                }
            })
            .orElse(null);
    }

    public AvaliacaoVo2MaxResponse toVo2MaxResponse(
        Leaf resultado,
        AvaliacaoVo2MaxContext context,
        String avaliacaoId,
        Double metsCalculado,
        ReferenciaClassificacaoResponse referencias
    ) {
        String nome = obterNomeClassificacao(resultado);
        Double valor = extrairValorClassificado(context);

        ClassificacaoVo2Max classificacao = new ClassificacaoVo2Max(
            nome,
            "Classificação obtida para o teste VO2Max",
            valor,
            metsCalculado
        );
        classificacao.setNomeLegivel(ClassificacaoLegivel.humanizar(nome));

        AvaliacaoVo2MaxResponse response = new AvaliacaoVo2MaxResponse(
            context.getClienteId(),
            context.getAvaliadorId(),
            classificacao,
            avaliacaoId
        );
        response.setReferencias(referencias);
        return response;
    }

    public AvaliacaoImcResponse toImcResponse(
        Leaf resultado,
        String protocoloNome,
        String protocoloId,
        String avaliadorId,
        String clienteId,
        String avaliacaoId,
        double imcValor,
        int pesoGramas,
        int alturaCm
    ) {
        String nomeClassificacao = obterNomeClassificacao(resultado);

        return new AvaliacaoImcResponse(
            nomeClassificacao,
            ClassificacaoLegivel.humanizar(nomeClassificacao),
            protocoloNome,
            protocoloId,
            avaliadorId,
            clienteId,
            avaliacaoId,
            "CONCLUIDA",
            Map.of(
                "imc", Math.round(imcValor * 100.0) / 100.0,
                "peso_gramas", pesoGramas,
                "altura_cm", alturaCm
            )
        );
    }
}
