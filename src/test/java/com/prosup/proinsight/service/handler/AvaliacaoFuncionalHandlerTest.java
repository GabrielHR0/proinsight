package com.prosup.proinsight.service.handler;

import com.prosup.proinsight.api.dto.request.AvaliacaoFuncionalRequest;
import com.prosup.proinsight.api.dto.request.TesteFuncionalResultadoRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoFuncionalResponse;
import com.prosup.proinsight.api.dto.response.ResultadoFuncionalResponse;
import com.prosup.proinsight.api.handler.AvaliacaoFuncionalException;
import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.model.MedicaoFuncional;
import com.prosup.proinsight.domain.model.TabelaClassificacao;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaClassificacaoGenerica;
import com.prosup.proinsight.domain.strategy.AvaliacaoFuncionalContext;
import com.prosup.proinsight.domain.strategy.AvaliacaoStrategy;
import com.prosup.proinsight.domain.strategy.StrategyRegistry;
import com.prosup.proinsight.infrastructure.persistence.document.AvaliacaoFisicaDocument;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoFisicaMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoResponseMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.TabelaClassificacaoMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import com.prosup.proinsight.service.AvaliacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliacaoFuncionalHandlerTest {

    private static final String PROTOCOLO_ID = "protocolo_avaliacao_funcional_idoso";
    private static final String TABELA_SENTAR = "classificacao_fullerton_sentar_levantar";

    @Mock
    private ProtocoloAvaliacaoRepository protocoloRepository;
    @Mock
    private TabelaClassificacaoRepository tabelaClassificacaoRepository;
    @Mock
    private TabelaClassificacaoMapper tabelaClassificacaoMapper;
    @Mock
    private AvaliacaoFisicaMapper avaliacaoMapper;
    @Mock
    private AvaliacaoResponseMapper responseMapper;
    @Mock
    private StrategyRegistry strategyRegistry;
    @Mock
    private AvaliacaoService avaliacaoService;
    @Mock
    private ClienteRepository clienteRepository;

    @Captor
    private ArgumentCaptor<MedicaoFuncional> medicaoCaptor;
    @Captor
    private ArgumentCaptor<AvaliacaoFisicaDocument> avaliacaoCaptor;

    private AvaliacaoFuncionalHandler handler;
    private ProtocoloAvaliacaoDocument protocolo;

    @BeforeEach
    void setUp() {
        handler = new AvaliacaoFuncionalHandler(
            protocoloRepository, tabelaClassificacaoRepository,
            tabelaClassificacaoMapper, avaliacaoMapper,
            responseMapper, strategyRegistry,
            avaliacaoService, clienteRepository
        );

        protocolo = new ProtocoloAvaliacaoDocument(
            PROTOCOLO_ID, "Avaliação Funcional do Idoso - Bateria de Fullerton",
            "FUNCIONAL", true, Protocolo.AVALIACAO_FUNCIONAL_IDOSO,
            "AVALIACAO_FUNCIONAL_IDOSO", null
        );
        protocolo.setTabelasPorTeste(Map.of("SENTAR_LEVANTAR_30S", TABELA_SENTAR));
    }

    private AvaliacaoFuncionalRequest request(List<TesteFuncionalResultadoRequest> testes, Integer idade) {
        return new AvaliacaoFuncionalRequest("cliente-1", PROTOCOLO_ID, "avaliador-1", idade, null, testes);
    }

    private ClienteDocument cliente(Sexo sexo, LocalDate dataNascimento) {
        var cliente = new ClienteDocument();
        cliente.setSexo(sexo);
        cliente.setDataNascimento(dataNascimento);
        return cliente;
    }

    private AvaliacaoFuncionalException processarCapturandoErro(AvaliacaoFuncionalRequest request) {
        var thrown = catchThrowable(() -> handler.processar(request));
        assertThat(thrown).isInstanceOf(AvaliacaoFuncionalException.class);
        return (AvaliacaoFuncionalException) thrown;
    }

    @Test
    void shouldProcessFuncionalAvaliacao() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0)), null);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));
        when(clienteRepository.findById("cliente-1"))
            .thenReturn(Optional.of(cliente(Sexo.MASCULINO, LocalDate.of(1955, 6, 10))));

        when(tabelaClassificacaoRepository.findById(TABELA_SENTAR))
            .thenReturn(Optional.of(new TabelaClassificacaoDocument()));
        var raiz = new TabelaClassificacaoGenerica();
        raiz.add(new NivelVo2Max("BOM", 12.0, 18.0));
        var tabelaDomain = new TabelaClassificacao(TABELA_SENTAR, "Fullerton sentar e levantar", raiz);
        when(tabelaClassificacaoMapper.toDomain(any())).thenReturn(tabelaDomain);

        @SuppressWarnings("unchecked")
        var strategy = mock(AvaliacaoStrategy.class);
        when(strategyRegistry.resolve("AVALIACAO_FUNCIONAL_IDOSO", AvaliacaoFuncionalContext.class))
            .thenReturn(strategy);
        when(strategy.avaliar(any())).thenReturn(new NivelVo2Max("BOM", 12.0, 18.0));

        when(responseMapper.obterNomeClassificacao(any())).thenReturn("BOM");

        var expectedDoc = new AvaliacaoFisicaDocument();
        expectedDoc.setClienteId("cliente-1");
        expectedDoc.setProtocoloId(PROTOCOLO_ID);
        when(avaliacaoMapper.toFuncionalDocument(anyString(), anyString(), anyString(), any()))
            .thenReturn(expectedDoc);

        var savedDoc = new AvaliacaoFisicaDocument();
        savedDoc.setId("avaliacao-999");
        when(avaliacaoService.save(any())).thenReturn(savedDoc);

        var responseEsperada = new AvaliacaoFuncionalResponse(
            "Avaliação Funcional do Idoso - Bateria de Fullerton", PROTOCOLO_ID, "avaliador-1",
            "cliente-1", "avaliacao-999", "CONCLUIDA", null, "MASCULINO",
            List.of(new ResultadoFuncionalResponse(
                "SENTAR_LEVANTAR_30S", "Sentar e levantar da cadeira em 30 segundos",
                "repetições", 14.0, "BOM", "Bom")),
            Map.of("total_testes", 1));
        when(responseMapper.toFuncionalResponse(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(responseEsperada);

        AvaliacaoFuncionalResponse response = handler.processar(request);

        assertThat(response).isSameAs(responseEsperada);
        assertThat(response.avaliacaoId()).isEqualTo("avaliacao-999");

        int idadeEsperada = Period.between(LocalDate.of(1955, 6, 10), LocalDate.now()).getYears();
        verify(responseMapper).toFuncionalResponse(
            anyString(), anyString(), anyString(), anyString(), anyString(),
            eq(idadeEsperada), eq("MASCULINO"), any());

        verify(avaliacaoMapper).toFuncionalDocument(
            anyString(), anyString(), anyString(), medicaoCaptor.capture());
        var medicao = medicaoCaptor.getValue();
        assertThat(medicao.getTipo()).isEqualTo(MedicaoTipo.FUNCIONAL);
        assertThat(medicao.getTestes()).hasSize(1);
        assertThat(medicao.getClassificacoes())
            .containsEntry("SENTAR_LEVANTAR_30S", "BOM");

        verify(avaliacaoService).save(avaliacaoCaptor.capture());
        assertThat(avaliacaoCaptor.getValue()).isSameAs(expectedDoc);
    }

    @Test
    void shouldThrowWhenProtocoloNotFound() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0)), 70);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.processar(request))
            .isInstanceOf(java.util.NoSuchElementException.class)
            .hasMessageContaining("Protocolo não encontrado");
    }

    @Test
    void shouldThrowWhenProtocoloHasNoTabelasPorTeste() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0)), 70);
        protocolo.setTabelasPorTeste(null);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));

        assertThatThrownBy(() -> handler.processar(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("não possui tabelas de classificação configuradas");
    }

    @Test
    void shouldThrowWhenTabelaForTesteIsNotConfigured() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("MARCHA_ESTACIONARIA_2MIN", 160.0)), 70);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(erro.getViolations())
            .anySatisfy(v -> {
                assertThat(v.get("field")).isEqualTo("testes[MARCHA_ESTACIONARIA_2MIN]");
                assertThat(v.get("message")).contains("Nenhuma tabela de classificação configurada");
            })
            .anySatisfy(v -> assertThat(v.get("field")).isEqualTo("testes[SENTAR_LEVANTAR_30S]"));
    }

    @Test
    void shouldThrowWhenTabelaNotFound() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0)), 70);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));
        when(clienteRepository.findById("cliente-1"))
            .thenReturn(Optional.of(cliente(Sexo.FEMININO, null)));
        when(tabelaClassificacaoRepository.findById(TABELA_SENTAR)).thenReturn(Optional.empty());

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(erro.getMessage()).contains("Tabela de classificação não encontrada");
        assertThat(erro.getViolations())
            .anySatisfy(v -> assertThat(v.get("field")).isEqualTo("testes[SENTAR_LEVANTAR_30S]"));
    }

    @Test
    void shouldThrowWhenSexoFromClienteIsUnavailable() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0)), 70);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));
        when(clienteRepository.findById("cliente-1"))
            .thenReturn(Optional.of(cliente(null, LocalDate.of(1955, 6, 10))));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(erro.getMessage()).contains("Sexo do cliente é obrigatório");
        assertThat(erro.getViolations())
            .anySatisfy(v -> assertThat(v.get("field")).isEqualTo("sexo"));
    }

    @Test
    void shouldThrowWhenIdadeFromClienteIsUnavailable() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0)), null);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));
        when(clienteRepository.findById("cliente-1"))
            .thenReturn(Optional.of(cliente(Sexo.MASCULINO, null)));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(erro.getMessage()).contains("Idade do cliente indisponível");
        assertThat(erro.getViolations())
            .anySatisfy(v -> assertThat(v.get("field")).isEqualTo("idade"));
    }

    @Test
    void shouldThrowWhenIdadeIsZero() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0)), 0);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));
        when(clienteRepository.findById("cliente-1"))
            .thenReturn(Optional.of(cliente(Sexo.MASCULINO, LocalDate.of(1955, 6, 10))));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(erro.getMessage()).contains("Idade inválida (0)");
        assertThat(erro.getViolations())
            .anySatisfy(v -> assertThat(v.get("field")).isEqualTo("idade"));
    }

    @Test
    void shouldThrowWhenIdadeIsNegative() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0)), -5);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));
        when(clienteRepository.findById("cliente-1"))
            .thenReturn(Optional.of(cliente(Sexo.MASCULINO, LocalDate.of(1955, 6, 10))));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(erro.getMessage()).contains("Idade inválida (-5)");
        assertThat(erro.getViolations())
            .anySatisfy(v -> assertThat(v.get("field")).isEqualTo("idade"));
    }

    @Test
    void shouldThrowWhenIdadeExceeds130() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0)), 131);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));
        when(clienteRepository.findById("cliente-1"))
            .thenReturn(Optional.of(cliente(Sexo.MASCULINO, LocalDate.of(1890, 1, 1))));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(erro.getMessage()).contains("Idade inválida (131)");
        assertThat(erro.getViolations())
            .anySatisfy(v -> assertThat(v.get("field")).isEqualTo("idade"));
    }

    @Test
    void shouldThrowWhenDerivedIdadeIsZero() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0)), null);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));
        when(clienteRepository.findById("cliente-1"))
            .thenReturn(Optional.of(cliente(Sexo.MASCULINO, LocalDate.now().minusMonths(2))));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(erro.getMessage()).contains("Idade inválida (0)");
        assertThat(erro.getViolations())
            .anySatisfy(v -> assertThat(v.get("field")).isEqualTo("idade"));
    }

    @Test
    void shouldThrowWhenValorExceedsMax() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 9999.0)), 70);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(erro.getViolations())
            .anySatisfy(v -> {
                assertThat(v.get("field")).isEqualTo("testes[SENTAR_LEVANTAR_30S]");
                assertThat(v.get("message")).contains("fora do intervalo aceito (0 a 50 repetições)");
            });
    }

    @Test
    void shouldThrowWhenValorBelowMin() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", -1.0)), 70);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(erro.getViolations())
            .anySatisfy(v -> {
                assertThat(v.get("field")).isEqualTo("testes[SENTAR_LEVANTAR_30S]");
                assertThat(v.get("message")).contains("fora do intervalo aceito (0 a 50 repetições)");
            });
    }

    @Test
    void shouldThrowWhenTesteIsDuplicated() {
        var request = request(List.of(
            new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0),
            new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 15.0)
        ), 70);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(erro.getViolations())
            .anySatisfy(v -> {
                assertThat(v.get("field")).isEqualTo("testes[SENTAR_LEVANTAR_30S]");
                assertThat(v.get("message")).isEqualTo("Teste duplicado: SENTAR_LEVANTAR_30S");
            });
    }

    @Test
    void shouldThrowWhenTesteIsUnknown() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("CORRIDA_100M", 12.0)), 70);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(erro.getViolations())
            .anySatisfy(v -> {
                assertThat(v.get("field")).isEqualTo("testes[CORRIDA_100M]");
                assertThat(v.get("message")).contains("Teste funcional não suportado");
            });
    }

    @Test
    void shouldThrowWhenConfiguredTestsAreMissing() {
        protocolo.setTabelasPorTeste(Map.of(
            "SENTAR_LEVANTAR_30S", TABELA_SENTAR,
            "MARCHA_ESTACIONARIA_2MIN", "classificacao_fullerton_marcha_2min"
        ));
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0)), 70);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(erro.getViolations())
            .anySatisfy(v -> {
                assertThat(v.get("field")).isEqualTo("testes[MARCHA_ESTACIONARIA_2MIN]");
                assertThat(v.get("message")).contains("Teste obrigatório não informado");
            });
        assertThat(erro.getViolations())
            .noneSatisfy(v -> assertThat(v.get("field")).isEqualTo("testes[SENTAR_LEVANTAR_30S]"));
    }

    @Test
    void shouldAggregateAllViolationsInSingleResponse() {
        protocolo.setTabelasPorTeste(Map.of(
            "SENTAR_LEVANTAR_30S", TABELA_SENTAR,
            "MARCHA_ESTACIONARIA_2MIN", "classificacao_fullerton_marcha_2min"
        ));
        var request = request(List.of(
            new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0),
            new TesteFuncionalResultadoRequest("CORRIDA_100M", 12.0)
        ), 70);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(erro.getViolations()).hasSize(2);
        assertThat(erro.getViolations())
            .anySatisfy(v -> assertThat(v.get("field")).isEqualTo("testes[CORRIDA_100M]"))
            .anySatisfy(v -> assertThat(v.get("field")).isEqualTo("testes[MARCHA_ESTACIONARIA_2MIN]"));
    }

    @Test
    void shouldThrowWhenNoLevelMatches() {
        var request = request(
            List.of(new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0)), 70);

        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));
        when(clienteRepository.findById("cliente-1"))
            .thenReturn(Optional.of(cliente(Sexo.MASCULINO, LocalDate.of(1955, 6, 10))));

        when(tabelaClassificacaoRepository.findById(TABELA_SENTAR))
            .thenReturn(Optional.of(new TabelaClassificacaoDocument()));
        var raiz = new TabelaClassificacaoGenerica();
        var tabelaDomain = new TabelaClassificacao(TABELA_SENTAR, "Fullerton sentar e levantar", raiz);
        when(tabelaClassificacaoMapper.toDomain(any())).thenReturn(tabelaDomain);

        @SuppressWarnings("unchecked")
        var strategy = mock(AvaliacaoStrategy.class);
        when(strategyRegistry.resolve("AVALIACAO_FUNCIONAL_IDOSO", AvaliacaoFuncionalContext.class))
            .thenReturn(strategy);
        when(strategy.avaliar(any())).thenReturn(null);

        var erro = processarCapturandoErro(request);

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(erro.getMessage())
            .contains("Nenhum nível de classificação encontrado")
            .contains("SENTAR_LEVANTAR_30S");
        assertThat(erro.getViolations())
            .anySatisfy(v -> assertThat(v.get("field")).isEqualTo("testes[SENTAR_LEVANTAR_30S]"));
    }
}
