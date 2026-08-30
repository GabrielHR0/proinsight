package com.prosup.proinsight.service.handler;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.Sexo;
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
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import com.prosup.proinsight.api.dto.request.AvaliacaoVo2MaxRequest;
import com.prosup.proinsight.api.dto.request.TesteVo2MaxDto;
import com.prosup.proinsight.api.dto.response.AvaliacaoVo2MaxResponse;
import com.prosup.proinsight.service.AvaliacaoService;
import com.prosup.proinsight.service.ReferenciaClassificacaoService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AvaliacaoVo2MaxHandler {

    private final TesteVo2MaxMapperRegistry testeRegistry;
    private final ProtocoloAvaliacaoRepository protocoloRepository;
    private final TabelaClassificacaoRepository tabelaClassificacaoRepository;
    private final TabelaClassificacaoMapper tabelaClassificacaoMapper;
    private final AvaliacaoFisicaMapper avaliacaoMapper;
    private final AvaliacaoResponseMapper responseMapper;
    private final StrategyRegistry registry;
    private final AvaliacaoService avaliacaoService;
    private final ClienteRepository clienteRepository;
    private final ReferenciaClassificacaoService referenciaService;

    public AvaliacaoVo2MaxHandler(
        TesteVo2MaxMapperRegistry testeRegistry,
        ProtocoloAvaliacaoRepository protocoloRepository,
        TabelaClassificacaoRepository tabelaClassificacaoRepository,
        TabelaClassificacaoMapper tabelaClassificacaoMapper,
        AvaliacaoFisicaMapper avaliacaoMapper,
        AvaliacaoResponseMapper responseMapper,
        StrategyRegistry registry,
        AvaliacaoService avaliacaoService,
        ClienteRepository clienteRepository,
        ReferenciaClassificacaoService referenciaService
    ) {
        this.testeRegistry = testeRegistry;
        this.protocoloRepository = protocoloRepository;
        this.tabelaClassificacaoRepository = tabelaClassificacaoRepository;
        this.tabelaClassificacaoMapper = tabelaClassificacaoMapper;
        this.avaliacaoMapper = avaliacaoMapper;
        this.responseMapper = responseMapper;
        this.registry = registry;
        this.avaliacaoService = avaliacaoService;
        this.clienteRepository = clienteRepository;
        this.referenciaService = referenciaService;
    }

    public AvaliacaoVo2MaxResponse processar(AvaliacaoVo2MaxRequest request) {
        var protocolo = protocoloRepository.findById(request.getProtocoloId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Protocolo não encontrado: " + request.getProtocoloId()
                ));

        DadosAvaliacao dados = new DadosAvaliacao();

        // Sexo e idade são resolvidos a partir do cliente cadastrado.
        // O frontend não envia sexo (sempre null); a idade pode vir no request
        // ou ser calculada a partir da data de nascimento do cliente.
        var clienteOpt = request.getClienteId() != null
                ? clienteRepository.findById(request.getClienteId())
                : Optional.<ClienteDocument>empty();

        Integer idade = request.getIdade();
        if (idade == null && clienteOpt.isPresent() && clienteOpt.get().getDataNascimento() != null) {
            idade = Period.between(clienteOpt.get().getDataNascimento(), LocalDate.now()).getYears();
        }
        if (idade != null) dados.adicionar("idade", idade);

        Sexo sexo = clienteOpt.map(ClienteDocument::getSexo).orElse(null);
        if (sexo != null) dados.adicionar("sexo", sexo);

        String tabelaClassificacaoId = protocolo.getTabelaClassificacaoId();

        var testeDto = new TesteVo2MaxDto();
        testeDto.setProtocolo(protocolo.getProtocolo());
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

        if (request.getFrequenciasCardiacas() != null && !request.getFrequenciasCardiacas().isEmpty()) {
            Set<Integer> timestamps = new HashSet<>();
            var fcMeasurements = new java.util.ArrayList<MedicaoVo2Max.MedicaoFrequenciaCardiaca>();
            for (var fcDto : request.getFrequenciasCardiacas()) {
                if (!timestamps.add(fcDto.getTempoDecorridoSegundos())) {
                    throw new IllegalArgumentException(
                        "FC duplicada para o tempo " + fcDto.getTempoDecorridoSegundos() + "s");
                }
                fcMeasurements.add(new MedicaoVo2Max.MedicaoFrequenciaCardiaca(
                        fcDto.getTempoDecorridoSegundos(), fcDto.getFcBpm()));
            }
            medicao.setFrequenciasCardiacas(fcMeasurements);
        }

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

        var strategy = registry.resolve(protocolo.getStrategyKey(), AvaliacaoVo2MaxContext.class);

        Leaf resultado = strategy.avaliar(context);

        if (resultado == null) {
            throw new IllegalStateException(
                "Nenhum nível de classificação encontrado para o protocolo '" + protocolo.getStrategyKey() +
                "' com idade=" + idade + ", sexo=" + sexo +
                ". Verifique se a tabela '" + protocolo.getTabelaClassificacaoId() +
                "' possui faixas compatíveis com os dados fornecidos."
            );
        }

        Double vo2Calculado = testeDomain.calcularVo2Max(dados);
        medicao.setResultado(vo2Calculado != null ? vo2Calculado.intValue() : null);

        if (vo2Calculado != null) {
            medicao.setMetsCalculado(Math.round((vo2Calculado / 3.5) * 10.0) / 10.0);
        }

        String classificacao = responseMapper.obterNomeClassificacao(resultado);

        var avaliacaoDoc = avaliacaoMapper.toVo2MaxDocument(
            request.getClienteId(),
            request.getAvaliadorId(),
            request.getProtocoloId(),
            medicao,
            classificacao
        );

        var saved = avaliacaoService.save(avaliacaoDoc);

        var referencias = referenciaService.extrair(tabela.getRaiz(), sexo, idade);

        return responseMapper.toVo2MaxResponse(resultado, context, saved.getId(), medicao.getMetsCalculado(), referencias);
    }
}
