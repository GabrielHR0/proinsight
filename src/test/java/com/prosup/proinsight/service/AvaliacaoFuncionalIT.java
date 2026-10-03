package com.prosup.proinsight.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosup.proinsight.AbstractIntegrationTest;
import com.prosup.proinsight.api.dto.request.AvaliacaoFuncionalRequest;
import com.prosup.proinsight.api.dto.request.TesteFuncionalResultadoRequest;
import com.prosup.proinsight.api.handler.AvaliacaoFuncionalException;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.model.ClassificacaoLegivel;
import com.prosup.proinsight.domain.model.PercentilFaixa;
import com.prosup.proinsight.service.handler.AvaliacaoFuncionalHandler;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliacaoFisicaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.within;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AvaliacaoFuncionalIT extends AbstractIntegrationTest {

    private static final String PROTOCOLO_ID = "protocolo_avaliacao_funcional_idoso";

    private static final List<String> ORDEM_BATERIA = List.of(
        "SENTAR_LEVANTAR_30S",
        "FLEXAO_COTOVELO_30S",
        "MARCHA_ESTACIONARIA_2MIN",
        "SENTAR_ALCANCAR_PES",
        "ALCANCAR_COSTAS",
        "LEVANTAR_CAMINHAR_2M5"
    );

    @Autowired
    private AvaliacaoFuncionalHandler funcionalHandler;
    @Autowired
    private ProtocoloHubService protocoloHubService;
    @Autowired
    private HistoricoAvaliacoesService historicoService;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AvaliacaoFisicaRepository avaliacaoFisicaRepository;

    private String avaliadorId;
    private String clienteId;

    @BeforeAll
    void cleanDatabase() {
        avaliacaoFisicaRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        var suf = UUID.randomUUID().toString().substring(0, 8);

        var user = new UserDocument();
        user.setUserName("avaliador-func-" + suf);
        user.setEmail("avaliador-func-" + suf + "@test.com");
        user.setPassword("hash123");
        user.setActive(true);
        user.setAcademiaRoles(Map.of());
        user.setCref("CREF-FUNC-" + suf);
        user.setCpf("11122233344" + suf);
        avaliadorId = userRepository.save(user).getId();

        var cliente = new ClienteDocument();
        cliente.setId("cliente-func-" + suf);
        cliente.setFullName("Idoso Fullerton " + suf);
        cliente.setSexo(Sexo.MASCULINO);
        cliente.setDataNascimento(LocalDate.now().minusYears(70));
        clienteId = clienteRepository.save(cliente).getId();
    }

    @Test
    void detalheDoProtocoloExpoeOsSeisTestesComTimer() throws Exception {
        var detalhe = protocoloHubService.getDetalhe(PROTOCOLO_ID);

        assertThat(detalhe.testes()).hasSize(6);
        assertThat(detalhe.testes())
            .extracting(t -> t.teste())
            .containsExactlyElementsOf(ORDEM_BATERIA);

        assertThat(detalhe.testes().get(0).timer()).isEqualTo("REGRESSIVA");
        assertThat(detalhe.testes().get(0).segundos()).isEqualTo(30);
        assertThat(detalhe.testes().get(2).segundos()).isEqualTo(120);
        assertThat(detalhe.testes().get(3).timer()).isEqualTo("NENHUM");
        assertThat(detalhe.testes().get(3).segundos()).isNull();
        assertThat(detalhe.testes().get(5).timer()).isEqualTo("CONTAGEM");

        var json = new ObjectMapper().findAndRegisterModules().writeValueAsString(detalhe);
        assertThat(json)
            .contains("\"testes\":[")
            .contains("\"teste\":\"SENTAR_LEVANTAR_30S\"")
            .contains("\"timer\":\"REGRESSIVA\"")
            .contains("\"segundos\":30");
    }

    @Test
    void processaBateriaCompletaEPersisteNoHistorico() {
        var response = funcionalHandler.processar(requestCompleto());

        assertThat(response.status()).isEqualTo("CONCLUIDA");
        assertThat(response.protocoloId()).isEqualTo(PROTOCOLO_ID);
        assertThat(response.idade()).isEqualTo(70);
        assertThat(response.sexo()).isEqualTo("MASCULINO");
        assertThat(response.resultados()).hasSize(6);
        assertThat(response.resultados())
            .extracting(r -> r.teste())
            .containsExactlyElementsOf(ORDEM_BATERIA);
        assertThat(response.resultados())
            .allSatisfy(r -> {
                assertThat(r.percentil()).isNotNull();
                assertThat(r.classificacaoLegivel()).isIn("Baixo", "Normal", "Elevado");
            });

        var saved = avaliacaoFisicaRepository.findById(response.avaliacaoId()).orElseThrow();
        assertThat(saved.getClienteId()).isEqualTo(clienteId);
        assertThat(saved.getProtocoloId()).isEqualTo(PROTOCOLO_ID);
        assertThat(saved.getMedicoes()).hasSize(1);

        var historico = historicoService.listarPorCliente(clienteId);
        assertThat(historico).hasSize(1);
        var item = historico.get(0);
        assertThat(item.tipo()).isEqualTo("FUNCIONAL");
        assertThat(item.protocoloNome()).contains("Funcional");

        @SuppressWarnings("unchecked")
        var percentis = (Map<String, Object>) item.detalhes().get("percentis");
        assertThat(percentis).hasSize(6);
        var media = percentis.values().stream()
            .filter(Number.class::isInstance)
            .mapToDouble(v -> ((Number) v).doubleValue())
            .average()
            .orElseThrow();

        assertThat(item.valor()).isCloseTo(media, within(1e-6));
        String faixa = PercentilFaixa.classificar(media);
        assertThat(item.classificacao()).isEqualTo(faixa);
        assertThat(item.classificacaoLegivel()).isEqualTo(ClassificacaoLegivel.humanizar(faixa));
    }

    @Test
    void rejeitaBateriaIncompletaComViolacoesPorTeste() {
        var request = new AvaliacaoFuncionalRequest(
            clienteId, PROTOCOLO_ID, avaliadorId, 70, "bateria incompleta",
            List.of(
                new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0),
                new TesteFuncionalResultadoRequest("FLEXAO_COTOVELO_30S", 12.0),
                new TesteFuncionalResultadoRequest("MARCHA_ESTACIONARIA_2MIN", 110.0)
            ));

        var thrown = catchThrowable(() -> funcionalHandler.processar(request));

        assertThat(thrown).isInstanceOf(AvaliacaoFuncionalException.class);
        var erro = (AvaliacaoFuncionalException) thrown;
        assertThat(erro.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(erro.getViolations())
            .extracting(v -> v.get("field"))
            .contains(
                "testes[SENTAR_ALCANCAR_PES]",
                "testes[ALCANCAR_COSTAS]",
                "testes[LEVANTAR_CAMINHAR_2M5]");
        assertThat(erro.getViolations())
            .noneSatisfy(v -> assertThat(v.get("field")).isEqualTo("testes[SENTAR_LEVANTAR_30S]"));
    }

    private AvaliacaoFuncionalRequest requestCompleto() {
        return new AvaliacaoFuncionalRequest(
            clienteId, PROTOCOLO_ID, avaliadorId, null, "IT bateria completa",
            List.of(
                new TesteFuncionalResultadoRequest("SENTAR_LEVANTAR_30S", 14.0),
                new TesteFuncionalResultadoRequest("FLEXAO_COTOVELO_30S", 12.0),
                new TesteFuncionalResultadoRequest("MARCHA_ESTACIONARIA_2MIN", 110.0),
                new TesteFuncionalResultadoRequest("SENTAR_ALCANCAR_PES", 25.0),
                new TesteFuncionalResultadoRequest("ALCANCAR_COSTAS", -2.0),
                new TesteFuncionalResultadoRequest("LEVANTAR_CAMINHAR_2M5", 6.5)
            ));
    }
}
