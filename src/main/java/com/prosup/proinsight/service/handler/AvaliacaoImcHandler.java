package com.prosup.proinsight.service.handler;

import com.prosup.proinsight.api.dto.request.AvaliacaoImcRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoImcResponse;
import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.MedicaoImc;
import com.prosup.proinsight.domain.model.TabelaClassificacao;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.TesteImc;
import com.prosup.proinsight.domain.strategy.AvaliacaoImcContext;
import com.prosup.proinsight.domain.strategy.AvaliacaoImcContextBuilder;
import com.prosup.proinsight.domain.strategy.AvaliacaoStrategy;
import com.prosup.proinsight.domain.strategy.StrategyRegistry;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoFisicaMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoResponseMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.TabelaClassificacaoMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliacaoFisicaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import com.prosup.proinsight.service.AvaliacaoService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AvaliacaoImcHandler {

    private final ProtocoloAvaliacaoRepository protocoloRepository;
    private final TabelaClassificacaoRepository tabelaClassificacaoRepository;
    private final TabelaClassificacaoMapper tabelaClassificacaoMapper;
    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    private final AvaliacaoFisicaMapper avaliacaoMapper;
    private final AvaliacaoResponseMapper responseMapper;
    private final StrategyRegistry strategyRegistry;
    private final AvaliacaoService avaliacaoService;

    public AvaliacaoImcHandler(
        ProtocoloAvaliacaoRepository protocoloRepository,
        TabelaClassificacaoRepository tabelaClassificacaoRepository,
        TabelaClassificacaoMapper tabelaClassificacaoMapper,
        AvaliacaoFisicaRepository avaliacaoFisicaRepository,
        AvaliacaoFisicaMapper avaliacaoMapper,
        AvaliacaoResponseMapper responseMapper,
        StrategyRegistry strategyRegistry,
        AvaliacaoService avaliacaoService
    ) {
        this.protocoloRepository = protocoloRepository;
        this.tabelaClassificacaoRepository = tabelaClassificacaoRepository;
        this.tabelaClassificacaoMapper = tabelaClassificacaoMapper;
        this.avaliacaoFisicaRepository = avaliacaoFisicaRepository;
        this.avaliacaoMapper = avaliacaoMapper;
        this.responseMapper = responseMapper;
        this.strategyRegistry = strategyRegistry;
        this.avaliacaoService = avaliacaoService;
    }

    @SuppressWarnings("unchecked")
    public AvaliacaoImcResponse processar(AvaliacaoImcRequest request) {
        var protocolo = protocoloRepository.findById(request.protocoloId())
            .orElseThrow(() -> new NoSuchElementException("Protocolo não encontrado: " + request.protocoloId()));

        var teste = new TesteImc(request.pesoGramas(), request.alturaCm());
        var medicao = new MedicaoImc(
            MedicaoTipo.IMC,
            Instant.now(),
            Instant.now(),
            Instant.now(),
            null,
            List.of(teste)
        );

        TabelaClassificacao tabela = tabelaClassificacaoRepository.findById(protocolo.getTabelaClassificacaoId())
            .map(tabelaClassificacaoMapper::toDomain)
            .orElseThrow(() -> new NoSuchElementException(
                "Tabela de classificação não encontrada: " + protocolo.getTabelaClassificacaoId()
            ));

        var dadosAvaliacao = new DadosAvaliacao()
            .adicionar("peso_gramas", request.pesoGramas())
            .adicionar("altura_cm", request.alturaCm());

        AvaliacaoImcContext context = new AvaliacaoImcContextBuilder()
            .comCliente(request.clienteId())
            .comAvaliador(request.avaliadorId())
            .comTabelaClassificacaoId(protocolo.getTabelaClassificacaoId())
            .comMedicao(medicao)
            .comDadosAvaliacao(dadosAvaliacao)
            .comTabelaClassificacao(tabela.getRaiz())
            .build();

        AvaliacaoStrategy<AvaliacaoImcContext> strategy = strategyRegistry.resolve(protocolo.getStrategyKey());
        Leaf resultado = strategy.avaliar(context);

        double imcValor = calcularImc(request.pesoGramas(), request.alturaCm());

        var avaliacaoDoc = avaliacaoMapper.toImcDocument(
            request.clienteId(),
            request.avaliadorId(),
            request.protocoloId(),
            medicao,
            (int) Math.round(imcValor),
            responseMapper.obterNomeClassificacao(resultado)
        );

        var saved = avaliacaoService.save(avaliacaoDoc);

        return responseMapper.toImcResponse(
            resultado,
            protocolo.getNome(),
            request.protocoloId(),
            request.avaliadorId(),
            request.clienteId(),
            saved.getId(),
            imcValor,
            request.pesoGramas(),
            request.alturaCm()
        );
    }

    private double calcularImc(int pesoGramas, int alturaCm) {
        double pesoKg = pesoGramas / 1000.0;
        double alturaM = alturaCm / 100.0;
        return pesoKg / (alturaM * alturaM);
    }
}
