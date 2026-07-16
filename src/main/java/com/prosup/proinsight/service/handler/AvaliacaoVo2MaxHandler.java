package com.prosup.proinsight.service.handler;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.MedicaoVo2Max;
import com.prosup.proinsight.domain.model.TabelaClassificacao;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.strategy.AvaliacaoVo2MaxContext;
import com.prosup.proinsight.domain.strategy.AvaliacaoVo2MaxContextBuilder;
import com.prosup.proinsight.domain.strategy.StrategyRegistry;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoFisicaMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoResponseMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.TabelaClassificacaoMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.TesteVo2MaxMapperRegistry;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliacaoFisicaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import com.prosup.proinsight.api.dto.request.AvaliacaoVo2MaxRequest;
import com.prosup.proinsight.api.dto.request.TesteVo2MaxDto;
import com.prosup.proinsight.api.dto.response.AvaliacaoVo2MaxResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AvaliacaoVo2MaxHandler {

    private final TesteVo2MaxMapperRegistry testeRegistry;
    private final ProtocoloAvaliacaoRepository protocoloRepository;
    private final TabelaClassificacaoRepository tabelaClassificacaoRepository;
    private final TabelaClassificacaoMapper tabelaClassificacaoMapper;
    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    private final AvaliacaoFisicaMapper avaliacaoMapper;
    private final AvaliacaoResponseMapper responseMapper;
    private final StrategyRegistry registry;

    public AvaliacaoVo2MaxHandler(
        TesteVo2MaxMapperRegistry testeRegistry,
        ProtocoloAvaliacaoRepository protocoloRepository,
        TabelaClassificacaoRepository tabelaClassificacaoRepository,
        TabelaClassificacaoMapper tabelaClassificacaoMapper,
        AvaliacaoFisicaRepository avaliacaoFisicaRepository,
        AvaliacaoFisicaMapper avaliacaoMapper,
        AvaliacaoResponseMapper responseMapper,
        StrategyRegistry registry
    ) {
        this.testeRegistry = testeRegistry;
        this.protocoloRepository = protocoloRepository;
        this.tabelaClassificacaoRepository = tabelaClassificacaoRepository;
        this.tabelaClassificacaoMapper = tabelaClassificacaoMapper;
        this.avaliacaoFisicaRepository = avaliacaoFisicaRepository;
        this.avaliacaoMapper = avaliacaoMapper;
        this.responseMapper = responseMapper;
        this.registry = registry;
    }

    public AvaliacaoVo2MaxResponse processar(AvaliacaoVo2MaxRequest request) {
        var protocolo = protocoloRepository.findById(request.getProtocoloId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Protocolo não encontrado: " + request.getProtocoloId()
                ));

        DadosAvaliacao dados = new DadosAvaliacao();
        if (request.getIdade() != null) dados.adicionar("idade", request.getIdade());
        if (request.getSexo() != null) dados.adicionar("sexo", request.getSexo());

        String tabelaClassificacaoId = protocolo.getTabelaClassificacaoId();

        var testeDto = new TesteVo2MaxDto();
        testeDto.setProtocolo(protocolo.getProtocoloVo2Max());
        testeDto.setResultado(request.getResultado());
        testeDto.setInclinacaoPercent(request.getInclinacaoPercent());
        testeDto.setFrequenciaCardiaca(request.getFrequenciaCardiaca());
        testeDto.setPesoKg(request.getPesoKg());

        var testeDomain = testeRegistry.toDomain(testeDto);
        var medicao = new MedicaoVo2Max(
                MedicaoTipo.VO2_MAX,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                request.getObservacoes(),
                List.of(testeDomain)
        );

        TabelaClassificacao tabela = tabelaClassificacaoRepository.findById(tabelaClassificacaoId)
                .map(tabelaClassificacaoMapper::toDomain)
                .orElseThrow(() -> new NoSuchElementException(
                        "Tabela de classificação " + tabelaClassificacaoId + " não encontrada"
                ));

        AvaliacaoVo2MaxContext context = new AvaliacaoVo2MaxContextBuilder()
            .comCliente(request.getClienteId())
            .comAvaliador(request.getAvaliadorId())
            .comTabelaClassificacaoId(tabelaClassificacaoId)
            .comMedicao(medicao)
            .comDadosAvaliacao(dados)
            .comTabelaClassificacao(tabela.getRaiz())
            .build();

        var strategy = registry.resolve(protocolo.getStrategyKey());

        Leaf resultado = strategy.avaliar(context);

        if (resultado == null) {
            throw new IllegalStateException(
                "Nenhum nível de classificação encontrado para o protocolo '" + protocolo.getStrategyKey() +
                "' com idade=" + request.getIdade() + ", sexo=" + request.getSexo() +
                ". Verifique se a tabela '" + protocolo.getTabelaClassificacaoId() +
                "' possui faixas compatíveis com os dados fornecidos."
            );
        }

        Double vo2Calculado = testeDomain.calcularVo2Max(dados);
        medicao.setResultado(vo2Calculado != null ? vo2Calculado.intValue() : null);

        String classificacao = responseMapper.obterNomeClassificacao(resultado);

        var avaliacaoDoc = avaliacaoMapper.toVo2MaxDocument(
            request.getClienteId(),
            request.getAvaliadorId(),
            request.getProtocoloId(),
            medicao,
            classificacao
        );

        var saved = avaliacaoFisicaRepository.save(avaliacaoDoc);

        return responseMapper.toVo2MaxResponse(resultado, context, saved.getId());
    }
}
