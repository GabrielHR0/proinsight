package com.prosup.proinsight.service.handler;

import com.prosup.proinsight.api.dto.request.AvaliacaoImcRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoImcResponse;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.domain.model.TabelaClassificacao;
import com.prosup.proinsight.domain.model.composite.classes.NivelImc;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaClassificacaoGenerica;
import com.prosup.proinsight.domain.strategy.AvaliacaoImcContext;
import com.prosup.proinsight.domain.strategy.AvaliacaoStrategy;
import com.prosup.proinsight.domain.strategy.StrategyRegistry;
import com.prosup.proinsight.infrastructure.persistence.document.AvaliacaoFisicaDocument;
import com.prosup.proinsight.infrastructure.persistence.document.MedicaoImcDocument;
import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoFisicaMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoResponseMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.TabelaClassificacaoMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliacaoFisicaRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoImcHandlerTest {

    @Mock
    private ProtocoloAvaliacaoRepository protocoloRepository;
    @Mock
    private TabelaClassificacaoRepository tabelaClassificacaoRepository;
    @Mock
    private TabelaClassificacaoMapper tabelaClassificacaoMapper;
    @Mock
    private AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    @Mock
    private AvaliacaoFisicaMapper avaliacaoMapper;
    @Mock
    private AvaliacaoResponseMapper responseMapper;
    @Mock
    private StrategyRegistry strategyRegistry;
    @Mock
    private AvaliacaoService avaliacaoService;

    @Captor
    private ArgumentCaptor<AvaliacaoFisicaDocument> avaliacaoCaptor;

    private AvaliacaoImcHandler handler;
    private ProtocoloAvaliacaoDocument protocolo;

    @BeforeEach
    void setUp() {
        handler = new AvaliacaoImcHandler(
            protocoloRepository, tabelaClassificacaoRepository,
            tabelaClassificacaoMapper, avaliacaoFisicaRepository,
            avaliacaoMapper, responseMapper, strategyRegistry, avaliacaoService
        );

        protocolo = new ProtocoloAvaliacaoDocument(
            "protocolo_imc_oms", "IMC OMS", "IMC", true, null, "IMC", "classificacao_imc_oms"
        );
    }

    @Test
    void shouldProcessImcAvaliacao() {
        var request = new AvaliacaoImcRequest("cliente-1", "protocolo_imc_oms", "avaliador-1", 70000, 175);

        when(protocoloRepository.findById("protocolo_imc_oms")).thenReturn(Optional.of(protocolo));

        var tabelaDoc = new TabelaClassificacaoDocument();
        when(tabelaClassificacaoRepository.findById("classificacao_imc_oms")).thenReturn(Optional.of(tabelaDoc));

        var raiz = new TabelaClassificacaoGenerica();
        raiz.add(new NivelImc("NORMAL", 18.5, 25.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        var tabelaDomain = new TabelaClassificacao("classificacao_imc_oms", "IMC OMS", raiz);
        when(tabelaClassificacaoMapper.toDomain(any())).thenReturn(tabelaDomain);

        var strategy = mock(AvaliacaoStrategy.class);
        when(strategy.avaliar(any())).thenReturn(new NivelImc("NORMAL", 18.5, 25.0));
        when(strategyRegistry.resolve("IMC", AvaliacaoImcContext.class)).thenReturn(strategy);

        var expectedDoc = new AvaliacaoFisicaDocument();
        expectedDoc.setClienteId("cliente-1");
        expectedDoc.setProtocoloId("protocolo_imc_oms");
        expectedDoc.setMedicoes(List.of(new MedicaoImcDocument()));
        when(avaliacaoMapper.toImcDocument(any(), any(), any(), any(), anyDouble(), any())).thenReturn(expectedDoc);

        var savedDoc = new AvaliacaoFisicaDocument();
        savedDoc.setId("avaliacao-123");
        when(avaliacaoService.save(any())).thenReturn(savedDoc);

        when(responseMapper.obterNomeClassificacao(any())).thenReturn("NORMAL");
        when(responseMapper.toImcResponse(any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyDouble(), anyInt(), anyInt()))
            .thenReturn(new AvaliacaoImcResponse("NORMAL", "Normal", "IMC OMS", "protocolo_imc_oms", "avaliador-1", "cliente-1", "avaliacao-123", "CONCLUIDA", Map.of("imc", 22.9, "peso_gramas", 70000, "altura_cm", 175)));

        AvaliacaoImcResponse response = handler.processar(request);

        assertThat(response).isNotNull();
        assertThat(response.classificacao()).isEqualTo("NORMAL");
        assertThat(response.classificacaoLegivel()).isEqualTo("Normal");
        assertThat(response.clienteId()).isEqualTo("cliente-1");
        assertThat(response.avaliadorId()).isEqualTo("avaliador-1");
        assertThat(response.protocoloId()).isEqualTo("protocolo_imc_oms");
        assertThat(response.avaliacaoId()).isEqualTo("avaliacao-123");
        assertThat(response.extras()).containsKey("imc").containsKey("peso_gramas").containsKey("altura_cm");

        verify(avaliacaoService).save(avaliacaoCaptor.capture());
        var saved = avaliacaoCaptor.getValue();
        assertThat(saved.getClienteId()).isEqualTo("cliente-1");
        assertThat(saved.getProtocoloId()).isEqualTo("protocolo_imc_oms");
        assertThat(saved.getMedicoes()).hasSize(1);
    }

    @Test
    void shouldThrowWhenProtocoloNotFound() {
        var request = new AvaliacaoImcRequest("cliente-1", "protocolo_inexistente", "avaliador-1", 70000, 175);

        when(protocoloRepository.findById("protocolo_inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.processar(request))
            .isInstanceOf(java.util.NoSuchElementException.class)
            .hasMessageContaining("Protocolo não encontrado");
    }

    @Test
    void shouldThrowWhenTabelaNotFound() {
        var request = new AvaliacaoImcRequest("cliente-1", "protocolo_imc_oms", "avaliador-1", 70000, 175);

        when(protocoloRepository.findById("protocolo_imc_oms")).thenReturn(Optional.of(protocolo));
        when(tabelaClassificacaoRepository.findById("classificacao_imc_oms")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.processar(request))
            .isInstanceOf(java.util.NoSuchElementException.class)
            .hasMessageContaining("Tabela de classificação não encontrada");
    }
}
