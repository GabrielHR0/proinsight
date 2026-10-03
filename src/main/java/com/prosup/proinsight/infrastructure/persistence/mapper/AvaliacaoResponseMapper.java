package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.api.dto.response.AvaliacaoFuncionalResponse;
import com.prosup.proinsight.api.dto.response.AvaliacaoImcResponse;
import com.prosup.proinsight.api.dto.response.AvaliacaoVo2MaxResponse;
import com.prosup.proinsight.api.dto.response.ClassificacaoVo2Max;
import com.prosup.proinsight.api.dto.response.ReferenciaClassificacaoResponse;
import com.prosup.proinsight.api.dto.response.ResultadoFuncionalResponse;
import com.prosup.proinsight.domain.model.ClassificacaoLegivel;
import com.prosup.proinsight.domain.model.MedicaoFuncional;
import com.prosup.proinsight.domain.model.PercentilFaixa;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.NivelImc;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.composite.classes.PercentilFuncional;
import com.prosup.proinsight.domain.strategy.AvaliacaoVo2MaxContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
        if (resultado instanceof PercentilFuncional p) {
            return p.getPercentil() != null
                ? PercentilFaixa.classificar(p.getPercentil())
                : PercentilFaixa.BAIXO;
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

    public AvaliacaoFuncionalResponse toFuncionalResponse(
        String protocoloNome,
        String protocoloId,
        String avaliadorId,
        String clienteId,
        String avaliacaoId,
        Integer idade,
        String sexo,
        MedicaoFuncional medicao
    ) {
        var classificacoes = medicao.getClassificacoes() != null ? medicao.getClassificacoes() : Map.<String, String>of();
        var percentis = medicao.getPercentis() != null ? medicao.getPercentis() : Map.<String, Integer>of();
        var resultados = new ArrayList<ResultadoFuncionalResponse>();

        if (medicao.getTestes() != null) {
            for (var teste : medicao.getTestes()) {
                String nome = classificacoes.getOrDefault(teste.getTipo().name(), "SEM_CLASSIFICACAO");
                Integer percentil = percentis.get(teste.getTipo().name());
                resultados.add(new ResultadoFuncionalResponse(
                    teste.getTipo().name(),
                    teste.getTipo().getNome(),
                    teste.getTipo().getUnidade(),
                    teste.getValor(),
                    percentil,
                    nome,
                    ClassificacaoLegivel.humanizar(nome)
                ));
            }
        }

        return new AvaliacaoFuncionalResponse(
            protocoloNome,
            protocoloId,
            avaliadorId,
            clienteId,
            avaliacaoId,
            "CONCLUIDA",
            idade,
            sexo,
            resultados,
            Map.of("total_testes", resultados.size())
        );
    }
}
