package com.prosup.proinsight.service.handler;

import com.prosup.proinsight.api.dto.request.AvaliacaoVo2MaxRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoVo2MaxResponse;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.domain.model.TabelaClassificacao;
import com.prosup.proinsight.domain.model.MedicaoVo2Max;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaClassificacaoGenerica;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaIdade;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaSexo;
import com.prosup.proinsight.domain.strategy.AvaliacaoVo2MaxContext;
import com.prosup.proinsight.domain.strategy.AvaliacaoVo2MaxEsteiraIncremental;
import com.prosup.proinsight.domain.strategy.StrategyRegistry;
import com.prosup.proinsight.infrastructure.persistence.document.AvaliacaoFisicaDocument;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoFisicaMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoResponseMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.TabelaClassificacaoMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.TesteVo2MaxMapperRegistry;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import com.prosup.proinsight.service.AvaliacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliacaoVo2MaxHandlerTest {

    private static final String PROTOCOLO_ID = "protocolo_vo2max_esteira_incremental";
    private static final String TABELA_ID = "classificacao_esteira_incremental";

    @Mock private ProtocoloAvaliacaoRepository protocoloRepository;
    @Mock private TabelaClassificacaoRepository tabelaClassificacaoRepository;
    @Mock private TabelaClassificacaoMapper tabelaClassificacaoMapper;
    @Mock private AvaliacaoFisicaMapper avaliacaoMapper;
    @Mock private StrategyRegistry registry;
    @Mock private AvaliacaoService avaliacaoService;
    @Mock private ClienteRepository clienteRepository;

    private final TesteVo2MaxMapperRegistry testeRegistry = new TesteVo2MaxMapperRegistry();
    private final AvaliacaoResponseMapper responseMapper = new AvaliacaoResponseMapper();

    private AvaliacaoVo2MaxHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AvaliacaoVo2MaxHandler(
            testeRegistry,
            protocoloRepository,
            tabelaClassificacaoRepository,
            tabelaClassificacaoMapper,
            avaliacaoMapper,
            responseMapper,
            registry,
            avaliacaoService,
            clienteRepository
        );
    }

    @Test
    void shouldClassifyUsingSexoFromClienteWhenRequestSexoIsNull() {
        var cliente = clienteMasc(2004, 6, 19);
        when(clienteRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));

        stubTabelaAberta();
        stubPersistencia();
        var response = processar(request(6.0, 1.0, null));

        assertThat(response.getClassificacao().getNome()).isEqualTo("MUITO_RUIM");
        assertThat(response.getClassificacao().getNomeLegivel()).isEqualTo("Muito ruim");
        // 6.0 km/h ≤ 6.0 → caminhada: VO₂ = (0.1 * 100.02 m/min) + 3.5 = 13.502
        assertThat(response.getClassificacao().getValorVo2Max()).isEqualTo(13.502, org.assertj.core.data.Offset.offset(0.001));
        verify(clienteRepository).findById("cliente-1");
    }

    @Test
    void shouldCalculateAgeFromClienteDataNascimento() {
        var cliente = clienteMasc(2004, 6, 19); // 22 anos
        when(clienteRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));

        // Faixa 20-29: MUITO_RUIM(<35); faixa 30-39: RUIM(20-30).
        // 6.5 km/h -> VO2 = 0.2*108.3 + 3.5 = 25.17 -> idade 22 -> MUITO_RUIM;
        // se usasse idade 30+ cairia em RUIM.
        stubTabelaAberta();
        stubPersistencia();
        var response = processar(request(6.5, null, null));

        assertThat(response.getClassificacao().getNome()).isEqualTo("MUITO_RUIM");
        assertThat(response.getClassificacao().getNomeLegivel()).isEqualTo("Muito ruim");
    }

    @Test
    void shouldUseRequestIdadeWhenProvided() {
        var cliente = clienteMasc(2004, 6, 19); // dataNascimento existe mas idade vem no request
        when(clienteRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));

        // 6.5 km/h -> VO2 = 25.17; idade 30 -> faixa 30-39 -> RUIM(20-30)
        stubTabelaAberta();
        stubPersistencia();
        var response = processar(request(6.5, null, 30));

        assertThat(response.getClassificacao().getNome()).isEqualTo("RUIM");
        assertThat(response.getClassificacao().getNomeLegivel()).isEqualTo("Ruim");
    }

    @Test
    void shouldClampToNearestLevelWhenValueIsOutsideTable() {
        var cliente = clienteMasc(2004, 6, 19);
        when(clienteRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));

        // Tabela sem extremos abertos: MUITO_RUIM(30-40), RUIM(40-50), ...
        // valor classificado 13.502 < 30 -> clamp -> MUITO_RUIM
        stubTabelaFechada();
        stubPersistencia();
        var response = processar(request(6.0, 1.0, null));

        assertThat(response.getClassificacao().getNome()).isEqualTo("MUITO_RUIM");
        assertThat(response.getClassificacao().getNomeLegivel()).isEqualTo("Muito ruim");
    }

    @Test
    void shouldThrowWhenClienteSexoIsUnavailable() {
        when(clienteRepository.findById("cliente-1")).thenReturn(Optional.empty());

        stubTabelaAberta();
        assertThatThrownBy(() -> processar(request(6.0, 1.0, null)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Nenhum nível de classificação encontrado");
    }

    private AvaliacaoVo2MaxResponse processar(AvaliacaoVo2MaxRequest request) {
        var protocolo = new ProtocoloAvaliacaoDocument(
            PROTOCOLO_ID, "Esteira Incremental", "VO2Max", true,
            Protocolo.ESTEIRA_INCREMENTAL, "VO2_MAX_ESTEIRA_INCREMENTAL", TABELA_ID
        );
        when(protocoloRepository.findById(PROTOCOLO_ID)).thenReturn(Optional.of(protocolo));

        var tabelaDoc = new TabelaClassificacaoDocument(TABELA_ID, "Classificação Esteira Incremental");
        when(tabelaClassificacaoRepository.findById(TABELA_ID)).thenReturn(Optional.of(tabelaDoc));

        when(registry.resolve("VO2_MAX_ESTEIRA_INCREMENTAL", AvaliacaoVo2MaxContext.class))
            .thenReturn(new AvaliacaoVo2MaxEsteiraIncremental());

        return handler.processar(request);
    }

    private void stubPersistencia() {
        when(avaliacaoMapper.toVo2MaxDocument(anyString(), anyString(), anyString(),
            any(MedicaoVo2Max.class), anyString()))
            .thenReturn(new AvaliacaoFisicaDocument());

        var saved = new AvaliacaoFisicaDocument();
        saved.setId("avaliacao-1");
        when(avaliacaoService.save(any(AvaliacaoFisicaDocument.class))).thenReturn(saved);
    }

    /** Tabela com MASCULINO 20-29 (aberta) e 30-39, FEMININO igual (não usado). */
    private void stubTabelaAberta() {
        var raiz = new TabelaClassificacaoGenerica();
        raiz.add(criarSexoAberto(Sexo.MASCULINO));
        raiz.add(criarSexoAberto(Sexo.FEMININO));
        stubTabela(raiz);
    }

    /** Tabela com faixa 20-29 fechada: MUITO_RUIM(30-40), RUIM(40-50), MÉDIO(50-60), BOM(60-70), EXCELENTE(70-80). */
    private void stubTabelaFechada() {
        var raiz = new TabelaClassificacaoGenerica();
        var masc = new TabelaSexo(Sexo.MASCULINO);
        var faixa = new TabelaIdade(20, 29);
        faixa.add(new NivelVo2Max("MUITO_RUIM", 30.0, 40.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        faixa.add(new NivelVo2Max("RUIM", 40.0, 50.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        faixa.add(new NivelVo2Max("MEDIO", 50.0, 60.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        faixa.add(new NivelVo2Max("BOM", 60.0, 70.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        faixa.add(new NivelVo2Max("EXCELENTE", 70.0, 80.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        masc.add(faixa);
        raiz.add(masc);
        stubTabela(raiz);
    }

    private void stubTabela(Component raiz) {
        when(tabelaClassificacaoMapper.toDomain(any(TabelaClassificacaoDocument.class)))
            .thenReturn(new TabelaClassificacao(TABELA_ID, "Classificação Esteira Incremental", raiz));
    }

    private TabelaSexo criarSexoAberto(Sexo sexo) {
        var tabelaSexo = new TabelaSexo(sexo);

        var f20 = new TabelaIdade(20, 29);
        f20.add(new NivelVo2Max("MUITO_RUIM", null, 35.0, null, TipoLimite.EXCLUSIVO));
        f20.add(new NivelVo2Max("RUIM", 35.0, 44.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        f20.add(new NivelVo2Max("MEDIO", 44.0, 49.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        f20.add(new NivelVo2Max("BOM", 49.0, 55.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        f20.add(new NivelVo2Max("EXCELENTE", 55.0, null, TipoLimite.INCLUSIVO, null));
        tabelaSexo.add(f20);

        var f30 = new TabelaIdade(30, 39);
        f30.add(new NivelVo2Max("MUITO_RUIM", null, 20.0, null, TipoLimite.EXCLUSIVO));
        f30.add(new NivelVo2Max("RUIM", 20.0, 30.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        f30.add(new NivelVo2Max("MEDIO", 30.0, 40.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        f30.add(new NivelVo2Max("BOM", 40.0, 50.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        f30.add(new NivelVo2Max("EXCELENTE", 50.0, null, TipoLimite.INCLUSIVO, null));
        tabelaSexo.add(f30);

        return tabelaSexo;
    }

    private ClienteDocument clienteMasc(int ano, int mes, int dia) {
        var cliente = new ClienteDocument();
        cliente.setId("cliente-1");
        cliente.setSexo(Sexo.MASCULINO);
        cliente.setDataNascimento(LocalDate.of(ano, mes, dia));
        return cliente;
    }

    private AvaliacaoVo2MaxRequest request(Double resultado, Double inclinacao, Integer idade) {
        var request = new AvaliacaoVo2MaxRequest();
        request.setClienteId("cliente-1");
        request.setAvaliadorId("avaliador-1");
        request.setProtocoloId(PROTOCOLO_ID);
        request.setResultado(resultado);
        request.setInclinacaoPercent(inclinacao);
        request.setIdade(idade);
        // sexo NUNCA é enviado pelo frontend
        return request;
    }
}
