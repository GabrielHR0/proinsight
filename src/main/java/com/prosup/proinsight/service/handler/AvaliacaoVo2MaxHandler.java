package com.prosup.proinsight.service.handler;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.strategy.StrategyRegistry;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliacaoFisicaRepository;
import com.prosup.proinsight.api.dto.request.AvaliacaoVo2MaxRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoVo2MaxResponse;
import com.prosup.proinsight.api.dto.response.ClassificacaoVo2Max;
import com.prosup.proinsight.domain.strategy.AvaliacaoVo2MaxContext;
import com.prosup.proinsight.domain.strategy.AvaliacaoVo2MaxContextBuilder;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.api.mapper.AvaliacaoVo2MaxDtoMapper;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;


@Service
public class AvaliacaoVo2MaxHandler {

    private final AvaliacaoVo2MaxDtoMapper vo2Mapper;

    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;

    private final StrategyRegistry registry;

    public AvaliacaoVo2MaxHandler(
        AvaliacaoVo2MaxDtoMapper vo2Mapper,
        AvaliacaoFisicaRepository avaliacaoFisicaRepository,
        StrategyRegistry registry
    ) {
        this.vo2Mapper = vo2Mapper;
        this.avaliacaoFisicaRepository = avaliacaoFisicaRepository;
        this.registry = registry;
    }

    public AvaliacaoVo2MaxResponse processar(AvaliacaoVo2MaxRequest request) {
        DadosAvaliacao dados = new DadosAvaliacao();
        if (request.getIdade() != null) dados.adicionar("idade", request.getIdade());
        if (request.getSexo() != null) dados.adicionar("sexo", request.getSexo());

        AvaliacaoVo2MaxContext context = new AvaliacaoVo2MaxContextBuilder()
            .comCliente(request.getClienteId())
            .comAvaliador(request.getAvaliadorId())
            .comMedicao(
                    vo2Mapper.toMedicaoDomain(request.getMedicaoVo2MaxDto())
            )
            .comDadosAvaliacao(dados)
            .build();

        Leaf resultado = avaliar(context, request);

        return converterParaResponse(resultado, context);
    }

    private Leaf avaliar(AvaliacaoVo2MaxContext context, AvaliacaoVo2MaxRequest request) {
        var avaliacao = avaliacaoFisicaRepository.findById(request.getAvaliacaoFisicaId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Avaliação física não encontrada com ID: " + request.getAvaliacaoFisicaId()
                ));

            var strategy = registry.resolve(
                    avaliacao.getStrategyKey()
            );

        Leaf resultado = strategy.avaliar(context);

        if (resultado == null) {
            throw new IllegalStateException("Estratégia de avaliação retornou resultado nulo");
        }

        return resultado;
    }

    private AvaliacaoVo2MaxResponse converterParaResponse(
        Leaf resultado,
        AvaliacaoVo2MaxContext context
    ) {
        String nome = obterNomeClassificacao(resultado);
        Double valor = extrairValorClassificado(context);

        ClassificacaoVo2Max classificacao = new ClassificacaoVo2Max(
            nome,
            "Resultado da avaliação VO2Max",
            valor
        );

        return new AvaliacaoVo2MaxResponse(
            context.getClienteId(),
            context.getAvaliadorId(),
            classificacao
        );
    }

    private String obterNomeClassificacao(Leaf resultado) {
        if (resultado instanceof NivelVo2Max n) {
            return n.getClassificacao();
        }
        return resultado.getClass().getSimpleName();
    }

    private Double extrairValorClassificado(AvaliacaoVo2MaxContext context) {
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
}
