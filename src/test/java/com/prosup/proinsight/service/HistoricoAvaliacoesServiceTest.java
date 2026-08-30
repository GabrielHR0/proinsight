package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.response.AvaliacaoHistoricoResponse;
import com.prosup.proinsight.api.dto.response.NivelReferenciaResponse;
import com.prosup.proinsight.config.TenantContext;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.domain.model.TabelaClassificacao;
import com.prosup.proinsight.domain.model.composite.classes.NivelImc;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaClassificacaoGenerica;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaIdade;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaSexo;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.TabelaClassificacaoMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoAvaliacoesServiceTest {

    @Mock private MongoTemplate mongoTemplate;
    @Mock private ProtocoloAvaliacaoRepository protocoloRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private TabelaClassificacaoRepository tabelaClassificacaoRepository;
    @Mock private TabelaClassificacaoMapper tabelaClassificacaoMapper;
    private final ReferenciaClassificacaoService referenciaService = new ReferenciaClassificacaoService();

    private HistoricoAvaliacoesService service;

    @BeforeEach
    void setUp() {
        service = new HistoricoAvaliacoesService(mongoTemplate, protocoloRepository,
            clienteRepository, tabelaClassificacaoRepository, tabelaClassificacaoMapper, referenciaService);
        TenantContext.setAcademiaId("acad-1");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void deveMontarQueryOtimizadaComProjectionESort() {
        when(mongoTemplate.find(any(), eq(Document.class), eq("avaliacoesFisicas"))).thenReturn(List.of());

        service.listarPorCliente("cliente-1");

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Document.class), eq("avaliacoesFisicas"));
        Query query = captor.getValue();

        assertThat(query.getQueryObject().getString("clienteId")).isEqualTo("cliente-1");
        assertThat(query.getSortObject()).isEqualTo(new Document("createdAt", -1));
        assertThat(query.getFieldsObject()).isEqualTo(new Document()
            .append("clienteId", 1)
            .append("protocoloId", 1)
            .append("createdAt", 1)
            .append("medicoes.tipo", 1)
            .append("medicoes.protocolo", 1)
            .append("medicoes.medidoEm", 1)
            .append("medicoes.observacoes", 1)
            .append("medicoes.vo2MaxCalculado", 1)
            .append("medicoes.classificacaoVo2", 1)
            .append("medicoes.velocidadeKmh", 1)
            .append("medicoes.inclinacaoPercent", 1)
            .append("medicoes.distanciaMetros", 1)
            .append("medicoes.tempoSegundos", 1)
            .append("medicoes.frequenciaCardiacaBpm", 1)
            .append("medicoes.frequenciasCardiacas", 1)
            .append("medicoes.pesoKg", 1)
            .append("medicoes.imcCalculado", 1)
            .append("medicoes.classificacaoImc", 1)
            .append("medicoes.massaCorporalGramas", 1)
            .append("medicoes.alturaCm", 1)
            .append("medicoes.percentualGordura", 1)
            .append("medicoes.massaMagraKg", 1)
            .append("medicoes.massaGordaKg", 1)
            .append("medicoes.aguaCorporalPercentual", 1)
            .append("medicoes.gorduraVisceral", 1)
            .append("medicoes.tmbKcal", 1)
            .append("medicoes.idadeMetabolica", 1));
    }

    @Test
    void deveParsearVo2MaxComNomeDeProtocoloEClassificacaoLegivel() {
        var protocolo = new ProtocoloAvaliacaoDocument();
        protocolo.setId("protocolo_vo2max_esteira_incremental");
        protocolo.setNome("Esteira Incremental");
        when(protocoloRepository.findAll()).thenReturn(List.of(protocolo));

        when(mongoTemplate.find(any(), eq(Document.class), eq("avaliacoesFisicas")))
            .thenReturn(List.of(docVo2Max()));

        List<AvaliacaoHistoricoResponse> respostas = service.listarPorCliente("cliente-1");

        assertThat(respostas).hasSize(1);
        AvaliacaoHistoricoResponse r = respostas.get(0);
        assertThat(r.id()).isEqualTo("6a82074d3fb51575e080081c");
        assertThat(r.clienteId()).isEqualTo("cliente-1");
        assertThat(r.protocoloId()).isEqualTo("protocolo_vo2max_esteira_incremental");
        assertThat(r.protocoloNome()).isEqualTo("Esteira Incremental");
        assertThat(r.tipo()).isEqualTo("VO2_MAX");
        assertThat(r.valor()).isEqualTo(24.0);
        assertThat(r.classificacao()).isEqualTo("MUITO_RUIM");
        assertThat(r.classificacaoLegivel()).isEqualTo("Muito ruim");
        assertThat(r.detalhes()).containsEntry("velocidadeKmh", 6.0)
            .containsEntry("inclinacaoPercent", 1.0)
            .containsEntry("observacoes", "Wizard");
    }

    @Test
    void deveParsearImc() {
        when(protocoloRepository.findAll()).thenReturn(List.of());
        when(mongoTemplate.find(any(), eq(Document.class), eq("avaliacoesFisicas")))
            .thenReturn(List.of(docImc()));

        List<AvaliacaoHistoricoResponse> respostas = service.listarPorCliente("cliente-1");

        assertThat(respostas).hasSize(1);
        AvaliacaoHistoricoResponse r = respostas.get(0);
        assertThat(r.tipo()).isEqualTo("IMC");
        assertThat(r.valor()).isEqualTo(22.86);
        assertThat(r.classificacao()).isEqualTo("NORMAL");
        assertThat(r.classificacaoLegivel()).isEqualTo("Normal");
        assertThat(r.detalhes()).containsEntry("massaCorporalGramas", 70000)
            .containsEntry("alturaCm", 175);
    }

    @Test
    void deveUsarCacheNaSegundaChamada() {
        when(protocoloRepository.findAll()).thenReturn(List.of());
        when(mongoTemplate.find(any(), eq(Document.class), eq("avaliacoesFisicas")))
            .thenReturn(List.of(docVo2Max()));

        service.listarPorCliente("cliente-1");
        service.listarPorCliente("cliente-1");

        verify(mongoTemplate, org.mockito.Mockito.times(1))
            .find(any(), eq(Document.class), eq("avaliacoesFisicas"));
    }

    @Test
    void deveIsolarCachePorTenant() {
        when(protocoloRepository.findAll()).thenReturn(List.of());
        when(mongoTemplate.find(any(), eq(Document.class), eq("avaliacoesFisicas")))
            .thenReturn(List.of(docVo2Max()));

        service.listarPorCliente("cliente-1");
        TenantContext.setAcademiaId("acad-2");
        service.listarPorCliente("cliente-1");

        verify(mongoTemplate, org.mockito.Mockito.times(2))
            .find(any(), eq(Document.class), eq("avaliacoesFisicas"));
    }

    @Test
    void invalidarDeveForcarNovaConsulta() {
        when(protocoloRepository.findAll()).thenReturn(List.of());
        when(mongoTemplate.find(any(), eq(Document.class), eq("avaliacoesFisicas")))
            .thenReturn(List.of(docVo2Max()));

        service.listarPorCliente("cliente-1");
        service.invalidar("cliente-1");
        service.listarPorCliente("cliente-1");

        verify(mongoTemplate, org.mockito.Mockito.times(2))
            .find(any(), eq(Document.class), eq("avaliacoesFisicas"));
    }

    @Test
    void semAvaliacoesNaoConsultaProtocolos() {
        when(mongoTemplate.find(any(), eq(Document.class), eq("avaliacoesFisicas")))
            .thenReturn(List.of());

        List<AvaliacaoHistoricoResponse> respostas = service.listarPorCliente("cliente-1");

        assertThat(respostas).isEmpty();
        verify(protocoloRepository, never()).findAll();
    }

    @Test
    void deveMontarReferenciasDeVo2MaxComFaixaEtariaDoCliente() {
        when(protocoloRepository.findAll()).thenReturn(List.of(protocoloComTabela()));
        when(protocoloRepository.findById("protocolo_vo2max_esteira_incremental"))
            .thenReturn(Optional.of(protocoloComTabela()));
        when(mongoTemplate.find(any(), eq(Document.class), eq("avaliacoesFisicas")))
            .thenReturn(List.of(docVo2Max()));
        when(clienteRepository.findById("cliente-1"))
            .thenReturn(Optional.of(clienteCom(Sexo.MASCULINO, LocalDate.now().minusYears(22))));
        when(tabelaClassificacaoRepository.findById("classificacao_esteira_incremental"))
            .thenReturn(Optional.of(new TabelaClassificacaoDocument()));
        when(tabelaClassificacaoMapper.toDomain(any(TabelaClassificacaoDocument.class)))
            .thenReturn(tabelaVo2Max());

        AvaliacaoHistoricoResponse r = service.listarPorCliente("cliente-1").get(0);

        assertThat(r.referencias()).isNotNull();
        assertThat(r.referencias().sexo()).isEqualTo("MASCULINO");
        assertThat(r.referencias().idadeMin()).isEqualTo(20);
        assertThat(r.referencias().idadeMax()).isEqualTo(29);
        assertThat(r.referencias().niveis()).hasSize(5);
        NivelReferenciaResponse primeiro = r.referencias().niveis().get(0);
        assertThat(primeiro.classificacao()).isEqualTo("MUITO_RUIM");
        assertThat(primeiro.classificacaoLegivel()).isEqualTo("Muito ruim");
        assertThat(primeiro.min()).isNull();
        assertThat(primeiro.max()).isEqualTo(35.0);
        assertThat(primeiro.tipoMax()).isEqualTo("EXCLUSIVO");
        NivelReferenciaResponse ultimo = r.referencias().niveis().get(4);
        assertThat(ultimo.classificacao()).isEqualTo("EXCELENTE");
        assertThat(ultimo.min()).isEqualTo(55.0);
        assertThat(ultimo.max()).isNull();
        assertThat(ultimo.tipoMin()).isEqualTo("INCLUSIVO");
        assertThat(r.referencias().niveis())
            .extracting(NivelReferenciaResponse::classificacao)
            .containsExactly("MUITO_RUIM", "RUIM", "MEDIO", "BOM", "EXCELENTE");
    }

    @Test
    void deveMontarReferenciasDeImcDiretamenteNaRaiz() {
        when(protocoloRepository.findAll()).thenReturn(List.of(protocoloImc()));
        when(protocoloRepository.findById("protocolo_imc_oms"))
            .thenReturn(Optional.of(protocoloImc()));
        when(mongoTemplate.find(any(), eq(Document.class), eq("avaliacoesFisicas")))
            .thenReturn(List.of(docImc()));
        when(clienteRepository.findById("cliente-1"))
            .thenReturn(Optional.of(clienteCom(null, null)));
        when(tabelaClassificacaoRepository.findById("classificacao_imc_oms"))
            .thenReturn(Optional.of(new TabelaClassificacaoDocument()));
        when(tabelaClassificacaoMapper.toDomain(any(TabelaClassificacaoDocument.class)))
            .thenReturn(tabelaImc());

        AvaliacaoHistoricoResponse r = service.listarPorCliente("cliente-1").get(0);

        assertThat(r.referencias()).isNotNull();
        assertThat(r.referencias().sexo()).isNull();
        assertThat(r.referencias().idadeMin()).isNull();
        assertThat(r.referencias().idadeMax()).isNull();
        assertThat(r.referencias().niveis()).hasSize(6);
        assertThat(r.referencias().niveis().get(0).classificacao()).isEqualTo("ABAIXO_DO_PESO");
        assertThat(r.referencias().niveis().get(0).classificacaoLegivel()).isEqualTo("Abaixo do peso");
        assertThat(r.referencias().niveis().get(5).classificacao()).isEqualTo("OBESIDADE_III");
        assertThat(r.referencias().niveis().get(5).classificacaoLegivel()).isEqualTo("Obesidade III");
    }

    @Test
    void semSexoNaoGeraReferenciasDeVo2Max() {
        when(protocoloRepository.findAll()).thenReturn(List.of(protocoloComTabela()));
        when(protocoloRepository.findById("protocolo_vo2max_esteira_incremental"))
            .thenReturn(Optional.of(protocoloComTabela()));
        when(mongoTemplate.find(any(), eq(Document.class), eq("avaliacoesFisicas")))
            .thenReturn(List.of(docVo2Max()));
        when(clienteRepository.findById("cliente-1"))
            .thenReturn(Optional.of(clienteCom(null, LocalDate.now().minusYears(22))));
        when(tabelaClassificacaoRepository.findById("classificacao_esteira_incremental"))
            .thenReturn(Optional.of(new TabelaClassificacaoDocument()));
        when(tabelaClassificacaoMapper.toDomain(any(TabelaClassificacaoDocument.class)))
            .thenReturn(tabelaVo2Max());

        AvaliacaoHistoricoResponse r = service.listarPorCliente("cliente-1").get(0);

        assertThat(r.referencias()).isNull();
    }

    @Test
    void semClienteNaoGeraReferenciasNemConsultaTabelas() {
        when(protocoloRepository.findAll()).thenReturn(List.of(protocoloComTabela()));
        when(mongoTemplate.find(any(), eq(Document.class), eq("avaliacoesFisicas")))
            .thenReturn(List.of(docVo2Max()));
        when(clienteRepository.findById("cliente-1")).thenReturn(Optional.empty());

        AvaliacaoHistoricoResponse r = service.listarPorCliente("cliente-1").get(0);

        assertThat(r.referencias()).isNull();
        verify(tabelaClassificacaoRepository, never()).findById(any());
    }

    private ProtocoloAvaliacaoDocument protocoloComTabela() {
        var protocolo = new ProtocoloAvaliacaoDocument();
        protocolo.setId("protocolo_vo2max_esteira_incremental");
        protocolo.setNome("Esteira Incremental");
        protocolo.setTabelaClassificacaoId("classificacao_esteira_incremental");
        return protocolo;
    }

    private ProtocoloAvaliacaoDocument protocoloImc() {
        var protocolo = new ProtocoloAvaliacaoDocument();
        protocolo.setId("protocolo_imc_oms");
        protocolo.setNome("IMC OMS");
        protocolo.setTabelaClassificacaoId("classificacao_imc_oms");
        return protocolo;
    }

    private ClienteDocument clienteCom(Sexo sexo, LocalDate dataNascimento) {
        var cliente = new ClienteDocument();
        cliente.setId("cliente-1");
        cliente.setSexo(sexo);
        cliente.setDataNascimento(dataNascimento);
        return cliente;
    }

    private TabelaClassificacao tabelaVo2Max() {
        var faixa = new TabelaIdade(20, 29);
        faixa.add(new NivelVo2Max("EXCELENTE", 55.0, null, TipoLimite.INCLUSIVO, TipoLimite.INCLUSIVO));
        faixa.add(new NivelVo2Max("MUITO_RUIM", null, 35.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        faixa.add(new NivelVo2Max("RUIM", 35.0, 44.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        faixa.add(new NivelVo2Max("MEDIO", 44.0, 49.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        faixa.add(new NivelVo2Max("BOM", 49.0, 55.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));

        var tabelaSexo = new TabelaSexo(Sexo.MASCULINO);
        tabelaSexo.add(faixa);

        var raiz = new TabelaClassificacaoGenerica();
        raiz.add(tabelaSexo);
        return new TabelaClassificacao("classificacao_esteira_incremental",
            "VO₂ Máx Esteira Incremental (ACSM/AHA)", raiz);
    }

    private TabelaClassificacao tabelaImc() {
        var raiz = new TabelaClassificacaoGenerica();
        raiz.add(new NivelImc("ABAIXO_DO_PESO", null, 18.5, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        raiz.add(new NivelImc("NORMAL", 18.5, 25.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        raiz.add(new NivelImc("SOBREPESO", 25.0, 30.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        raiz.add(new NivelImc("OBESIDADE_I", 30.0, 35.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        raiz.add(new NivelImc("OBESIDADE_II", 35.0, 40.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        raiz.add(new NivelImc("OBESIDADE_III", 40.0, null, TipoLimite.INCLUSIVO, TipoLimite.INCLUSIVO));
        return new TabelaClassificacao("classificacao_imc_oms", "IMC OMS", raiz);
    }

    private Document docVo2Max() {
        var medicao = new Document()
            .append("tipo", "VO2_MAX")
            .append("protocolo", "ESTEIRA_INCREMENTAL")
            .append("medidoEm", Date.from(Instant.parse("2026-08-16T18:54:05.157Z")))
            .append("observacoes", "Wizard")
            .append("vo2MaxCalculado", 24)
            .append("classificacaoVo2", "MUITO_RUIM")
            .append("velocidadeKmh", 6.0)
            .append("inclinacaoPercent", 1.0);
        return new Document("_id", new ObjectId("6a82074d3fb51575e080081c"))
            .append("clienteId", "cliente-1")
            .append("protocoloId", "protocolo_vo2max_esteira_incremental")
            .append("createdAt", Date.from(Instant.parse("2026-08-16T18:54:05.188Z")))
            .append("medicoes", List.of(medicao));
    }

    private Document docImc() {
        var medicao = new Document()
            .append("tipo", "IMC")
            .append("medidoEm", Date.from(Instant.parse("2026-08-11T22:12:11.915Z")))
            .append("imcCalculado", 22.86)
            .append("classificacaoImc", "NORMAL")
            .append("massaCorporalGramas", 70000)
            .append("alturaCm", 175);
        return new Document("_id", new ObjectId("6a82074d3fb51575e080081d"))
            .append("clienteId", "cliente-1")
            .append("protocoloId", "protocolo_imc_oms")
            .append("createdAt", Date.from(Instant.parse("2026-08-11T22:12:11.915Z")))
            .append("medicoes", List.of(medicao));
    }
}