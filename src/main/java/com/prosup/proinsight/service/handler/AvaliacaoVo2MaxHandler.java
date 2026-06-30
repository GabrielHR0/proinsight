package com.prosup.proinsight.service.handler;

import com.prosup.proinsight.adapter.out.persistence.MongoAvaliacaoFisicaDataRepository;
import com.prosup.proinsight.domain.avalicao_strategy.AvaliacaoStrategy;
import com.prosup.proinsight.dto.request.AvaliacaoVo2MaxRequest;
import com.prosup.proinsight.dto.response.AvaliacaoVo2MaxResponse;
import com.prosup.proinsight.dto.response.ClassificacaoVO2Max;
import com.prosup.proinsight.domain.avalicao_strategy.AvaliacaoVo2MaxContext;
import com.prosup.proinsight.domain.avalicao_strategy.AvaliacaoVo2MaxContextBuilder;
import com.prosup.proinsight.domain.avalicao_strategy.AvaliacaoVo2Max;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.exception.AvaliacaoException;
import com.prosup.proinsight.exception.RecursoNaoEncontradoException;
import com.prosup.proinsight.mapper.AvaliacaoVo2MaxDtoMapper;
import com.prosup.proinsight.validator.AvaliacaoVo2MaxValidator;
import com.prosup.proinsight.validator.RequestValidator;
import org.springframework.stereotype.Service;


@Service
public class AvaliacaoVo2MaxHandler {
    
    private final RequestValidator requestValidator;
    private final AvaliacaoVo2MaxValidator avaliacaoValidator;

    private final AvaliacaoVo2MaxDtoMapper vo2Mapper;

    private final MongoAvaliacaoFisicaDataRepository avaliacaoFisicaRepository;
    

    public AvaliacaoVo2MaxHandler(
        RequestValidator requestValidator,
        AvaliacaoVo2MaxValidator avaliacaoValidator,
        AvaliacaoVo2Max estrategiaAvaliacaoVo2Max,
        AvaliacaoVo2MaxDtoMapper vo2Mapper,
        MongoAvaliacaoFisicaDataRepository avaliacaoFisicaRepository
    ) {
        this.requestValidator = requestValidator;
        this.avaliacaoValidator = avaliacaoValidator;
        this.vo2Mapper = vo2Mapper;
        this.avaliacaoFisicaRepository = avaliacaoFisicaRepository;
    }

    public AvaliacaoVo2MaxResponse processar(AvaliacaoVo2MaxRequest request) {
        try {
            avaliacaoValidator.validarRequisicao(request);

            AvaliacaoVo2MaxContext context = new AvaliacaoVo2MaxContextBuilder()
                .comCliente(request.getClienteId())
                .comAvaliador(request.getAvaliadorId())
                .comMedicao(
                        vo2Mapper.toMedicaoDomain(request.getMedicaoVo2Dto())
                )
                .build();


            
            Leaf resultado = avaliar(context, request);
            
            if (resultado == null) {
                throw new AvaliacaoException("Estratégia de avaliação retornou resultado nulo");
            }
            
            return converterParaResponse(resultado, context);
            
        } catch (AvaliacaoException | RecursoNaoEncontradoException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new com.prosup.proinsight.exception.RegraNeggocioException(
                "Erro ao construir contexto: " + e.getMessage()
            );
        } catch (Exception e) {
            throw new AvaliacaoException(
                "Erro inesperado ao processar avaliação VO2Max: " + e.getMessage(), 
                e
            );
        }
    }

    private Leaf avaliar(AvaliacaoVo2MaxContext context, AvaliacaoVo2MaxRequest request) {
        var avaliacao = avaliacaoFisicaRepository.findById(request.getAvaliacaoFisicaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Avaliação física não encontrada com ID: " + request.getAvaliacaoFisicaId()
                ));

        var strategy = avaliacao.getStrategy();

        return strategy.avaliar(context);
    }

    private AvaliacaoVo2MaxResponse converterParaResponse(
        Leaf resultado, 
        AvaliacaoVo2MaxContext context
    ) {
        String nome = "CLASSIFICACAO_" + resultado.getClass().getSimpleName();
        String descricao = "Resultado da avaliação VO2Max";
        
        ClassificacaoVO2Max classificacao = new ClassificacaoVO2Max(
            nome,
            descricao,
            0.0
        );
        
        return new AvaliacaoVo2MaxResponse(
            context.getClienteId(),
            context.getAvaliadorId(),
            classificacao
        );
    }
}
