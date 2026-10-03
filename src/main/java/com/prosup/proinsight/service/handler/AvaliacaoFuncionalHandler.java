package com.prosup.proinsight.service.handler;

import com.prosup.proinsight.api.dto.request.AvaliacaoFuncionalRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoFuncionalResponse;
import com.prosup.proinsight.api.handler.AvaliacaoFuncionalException;
import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import com.prosup.proinsight.domain.model.MedicaoFuncional;
import com.prosup.proinsight.domain.model.TabelaClassificacao;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.PercentilFuncional;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.AlcancarCostas;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.FlexaoCotovelo;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.LevantarCaminhar2m5;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.MarchaEstacionaria;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.SentarAlcancarPes;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.SentarLevantar;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.TesteAptdaoFisicaIdosos;
import com.prosup.proinsight.domain.strategy.AvaliacaoFuncionalContext;
import com.prosup.proinsight.domain.strategy.AvaliacaoFuncionalContextBuilder;
import com.prosup.proinsight.domain.strategy.AvaliacaoStrategy;
import com.prosup.proinsight.domain.strategy.StrategyRegistry;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoFisicaMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoResponseMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.TabelaClassificacaoMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import com.prosup.proinsight.service.AvaliacaoService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
public class AvaliacaoFuncionalHandler {

    private final ProtocoloAvaliacaoRepository protocoloRepository;
    private final TabelaClassificacaoRepository tabelaClassificacaoRepository;
    private final TabelaClassificacaoMapper tabelaClassificacaoMapper;
    private final AvaliacaoFisicaMapper avaliacaoMapper;
    private final AvaliacaoResponseMapper responseMapper;
    private final StrategyRegistry strategyRegistry;
    private final AvaliacaoService avaliacaoService;
    private final ClienteRepository clienteRepository;

    public AvaliacaoFuncionalHandler(
        ProtocoloAvaliacaoRepository protocoloRepository,
        TabelaClassificacaoRepository tabelaClassificacaoRepository,
        TabelaClassificacaoMapper tabelaClassificacaoMapper,
        AvaliacaoFisicaMapper avaliacaoMapper,
        AvaliacaoResponseMapper responseMapper,
        StrategyRegistry strategyRegistry,
        AvaliacaoService avaliacaoService,
        ClienteRepository clienteRepository
    ) {
        this.protocoloRepository = protocoloRepository;
        this.tabelaClassificacaoRepository = tabelaClassificacaoRepository;
        this.tabelaClassificacaoMapper = tabelaClassificacaoMapper;
        this.avaliacaoMapper = avaliacaoMapper;
        this.responseMapper = responseMapper;
        this.strategyRegistry = strategyRegistry;
        this.avaliacaoService = avaliacaoService;
        this.clienteRepository = clienteRepository;
    }

    public AvaliacaoFuncionalResponse processar(AvaliacaoFuncionalRequest request) {
        var protocolo = protocoloRepository.findById(request.protocoloId())
            .orElseThrow(() -> new NoSuchElementException("Protocolo não encontrado: " + request.protocoloId()));

        Map<String, String> tabelasPorTeste = protocolo.getTabelasPorTeste();
        if (tabelasPorTeste == null || tabelasPorTeste.isEmpty()) {
            throw new IllegalStateException(
                "Protocolo '" + request.protocoloId() + "' não possui tabelas de classificação configuradas por teste");
        }

        List<TesteAptdaoFisicaIdosos> testes = validarTestes(request, tabelasPorTeste.keySet());

        var tabelaPorTipo = new LinkedHashMap<String, String>();
        for (TesteAptdaoFisicaIdosos teste : testes) {
            String tabelaId = tabelasPorTeste.get(teste.getTipo().name());
            if (tabelaId == null || tabelaId.isBlank()) {
                String mensagem = "Nenhuma tabela de classificação configurada para o teste '"
                    + teste.getTipo().name() + "' no protocolo '" + request.protocoloId() + "'";
                throw AvaliacaoFuncionalException.unprocessable(mensagem, List.of(
                    AvaliacaoFuncionalException.violacao("testes[" + teste.getTipo().name() + "]", mensagem)));
            }
            tabelaPorTipo.put(teste.getTipo().name(), tabelaId);
        }

        Integer idade = request.idade();
        var clienteOpt = request.clienteId() != null
            ? clienteRepository.findById(request.clienteId())
            : Optional.<ClienteDocument>empty();

        if (idade == null && clienteOpt.isPresent() && clienteOpt.get().getDataNascimento() != null) {
            idade = Period.between(clienteOpt.get().getDataNascimento(), LocalDate.now()).getYears();
        }
        Sexo sexo = clienteOpt.map(ClienteDocument::getSexo).orElse(null);

        if (idade == null) {
            String mensagem = "Idade do cliente indisponível: informe 'idade' no request ou cadastre a data de nascimento do cliente";
            throw AvaliacaoFuncionalException.unprocessable(mensagem, List.of(
                AvaliacaoFuncionalException.violacao("idade", mensagem)));
        }
        if (idade < 1 || idade > 130) {
            String mensagem = "Idade inválida (" + idade + "): corrija a data de nascimento do cliente";
            throw AvaliacaoFuncionalException.unprocessable(mensagem, List.of(
                AvaliacaoFuncionalException.violacao("idade", mensagem)));
        }
        if (sexo == null) {
            String mensagem = "Sexo do cliente é obrigatório para classificar os testes da avaliação funcional";
            throw AvaliacaoFuncionalException.unprocessable(mensagem, List.of(
                AvaliacaoFuncionalException.violacao("sexo", mensagem)));
        }

        var dados = new DadosAvaliacao()
            .adicionar("idade", idade)
            .adicionar("sexo", sexo);

        Instant agora = Instant.now();
        var medicao = new MedicaoFuncional(
            MedicaoTipo.FUNCIONAL,
            agora, agora, agora,
            request.observacoes(),
            testes
        );

        AvaliacaoStrategy<AvaliacaoFuncionalContext> strategy =
            strategyRegistry.resolve(protocolo.getStrategyKey(), AvaliacaoFuncionalContext.class);

        var classificacoes = new LinkedHashMap<String, String>();
        var percentis = new LinkedHashMap<String, Integer>();
        for (TesteAptdaoFisicaIdosos teste : testes) {
            var context = classificarTeste(teste, tabelaPorTipo, dados, request);
            Leaf resultado = strategy.avaliar(context);
            if (resultado == null) {
                String mensagem = "Nenhum nível de classificação encontrado para o teste '" + teste.getTipo().name()
                    + "' (tabela '" + context.getTabelaClassificacaoId() + "') com idade=" + idade
                    + ", sexo=" + sexo + ", valor=" + teste.getValor()
                    + ". Verifique se a tabela possui faixas compatíveis com os dados fornecidos.";
                throw AvaliacaoFuncionalException.unprocessable(mensagem, List.of(
                    AvaliacaoFuncionalException.violacao("testes[" + teste.getTipo().name() + "]", mensagem)));
            }
            classificacoes.put(teste.getTipo().name(), responseMapper.obterNomeClassificacao(resultado));
            if (resultado instanceof PercentilFuncional p) {
                percentis.put(teste.getTipo().name(), p.getPercentil());
            }
        }
        medicao.setClassificacoes(classificacoes);
        medicao.setPercentis(percentis);

        var avaliacaoDoc = avaliacaoMapper.toFuncionalDocument(
            request.clienteId(),
            request.avaliadorId(),
            request.protocoloId(),
            medicao
        );

        var saved = avaliacaoService.save(avaliacaoDoc);

        return responseMapper.toFuncionalResponse(
            protocolo.getNome(),
            request.protocoloId(),
            request.avaliadorId(),
            request.clienteId(),
            saved.getId(),
            idade,
            sexo.name(),
            medicao
        );
    }

    private List<TesteAptdaoFisicaIdosos> validarTestes(AvaliacaoFuncionalRequest request, Set<String> testesConfigurados) {
        var violations = new ArrayList<Map<String, String>>();
        var testes = new ArrayList<TesteAptdaoFisicaIdosos>();
        Set<TesteFuncionalTipo> vistos = EnumSet.noneOf(TesteFuncionalTipo.class);

        if (request.testes() != null) {
            for (var resultado : request.testes()) {
                TesteFuncionalTipo tipo;
                try {
                    tipo = TesteFuncionalTipo.deChave(resultado.teste());
                } catch (IllegalArgumentException e) {
                    String chave = resultado.teste() == null || resultado.teste().isBlank()
                        ? "testes"
                        : "testes[" + resultado.teste().trim() + "]";
                    violations.add(AvaliacaoFuncionalException.violacao(chave, e.getMessage()));
                    continue;
                }
                if (!vistos.add(tipo)) {
                    violations.add(AvaliacaoFuncionalException.violacao(
                        "testes[" + tipo.name() + "]", "Teste duplicado: " + tipo.name()));
                    continue;
                }
                if (resultado.valor() == null || resultado.valor().isNaN() || resultado.valor().isInfinite()) {
                    violations.add(AvaliacaoFuncionalException.violacao(
                        "testes[" + tipo.name() + "]", "Valor do teste deve ser um número válido"));
                    continue;
                }
                if (resultado.valor() < tipo.getValorMin() || resultado.valor() > tipo.getValorMax()) {
                    String mensagem = "Valor fora do intervalo aceito (" + formatarFaixa(tipo.getValorMin())
                        + " a " + formatarFaixa(tipo.getValorMax()) + " " + tipo.getUnidade()
                        + "): " + resultado.valor();
                    violations.add(AvaliacaoFuncionalException.violacao(
                        "testes[" + tipo.name() + "]", mensagem));
                    continue;
                }
                testes.add(criarTeste(tipo, resultado.valor()));
            }
        }

        for (TesteFuncionalTipo tipo : vistos) {
            if (!testesConfigurados.contains(tipo.name())) {
                String mensagem = "Nenhuma tabela de classificação configurada para o teste '"
                    + tipo.name() + "' no protocolo '" + request.protocoloId() + "'";
                violations.add(AvaliacaoFuncionalException.violacao("testes[" + tipo.name() + "]", mensagem));
            }
        }

        for (TesteFuncionalTipo tipo : TesteFuncionalTipo.values()) {
            if (testesConfigurados.contains(tipo.name()) && !vistos.contains(tipo)) {
                violations.add(AvaliacaoFuncionalException.violacao(
                    "testes[" + tipo.name() + "]", "Teste obrigatório não informado: " + tipo.getNome()));
            }
        }

        if (!violations.isEmpty()) {
            throw AvaliacaoFuncionalException.badRequest(
                "Um ou mais testes estão inválidos ou incompletos. Veja 'violations' para detalhes.", violations);
        }
        return testes;
    }

    private String formatarFaixa(double valor) {
        return valor == Math.floor(valor) ? String.valueOf((long) valor) : String.valueOf(valor);
    }

    private TesteAptdaoFisicaIdosos criarTeste(TesteFuncionalTipo tipo, Double valor) {
        return switch (tipo) {
            case SENTAR_LEVANTAR_30S -> new SentarLevantar(tipo, valor, valor.intValue());
            case FLEXAO_COTOVELO_30S -> new FlexaoCotovelo(tipo, valor, valor.intValue());
            case MARCHA_ESTACIONARIA_2MIN -> new MarchaEstacionaria(tipo, valor, valor.intValue());
            case SENTAR_ALCANCAR_PES -> new SentarAlcancarPes(tipo, valor, valor);
            case ALCANCAR_COSTAS -> new AlcancarCostas(tipo, valor, valor);
            case LEVANTAR_CAMINHAR_2M5 -> new LevantarCaminhar2m5(tipo, valor, valor);
        };
    }

    private AvaliacaoFuncionalContext classificarTeste(
        TesteAptdaoFisicaIdosos teste,
        Map<String, String> tabelaPorTipo,
        DadosAvaliacao dados,
        AvaliacaoFuncionalRequest request
    ) {
        String tabelaId = tabelaPorTipo.get(teste.getTipo().name());

        TabelaClassificacao tabela = tabelaClassificacaoRepository.findById(tabelaId)
            .map(tabelaClassificacaoMapper::toDomain)
            .orElseThrow(() -> {
                String mensagem = "Tabela de classificação não encontrada: " + tabelaId
                    + " (teste " + teste.getTipo().name() + ")";
                return AvaliacaoFuncionalException.unprocessable(mensagem, List.of(
                    AvaliacaoFuncionalException.violacao("testes[" + teste.getTipo().name() + "]", mensagem)));
            });

        var medicaoTeste = new MedicaoFuncional(
            MedicaoTipo.FUNCIONAL,
            Instant.now(), Instant.now(), Instant.now(),
            request.observacoes(),
            List.of(teste)
        );

        return new AvaliacaoFuncionalContextBuilder()
            .comCliente(request.clienteId())
            .comAvaliador(request.avaliadorId())
            .comTabelaClassificacaoId(tabelaId)
            .comMedicao(medicaoTeste)
            .comDadosAvaliacao(dados)
            .comTabelaClassificacao(tabela.getRaiz())
            .build();
    }
}
