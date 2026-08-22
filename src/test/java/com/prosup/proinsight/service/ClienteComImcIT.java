package com.prosup.proinsight.service;

import com.prosup.proinsight.AbstractIntegrationTest;
import com.prosup.proinsight.api.dto.request.ClienteComImcRequest;
import com.prosup.proinsight.api.dto.response.ClienteComImcResponse;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedNivelImc;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedTabelaClassificacaoGenerica;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliacaoFisicaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClienteComImcIT extends AbstractIntegrationTest {

    @Autowired
    private ClienteService clienteService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private AcademiaRepository academiaRepository;
    @Autowired
    private AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    @Autowired
    private ProtocoloAvaliacaoRepository protocoloRepository;
    @Autowired
    private TabelaClassificacaoRepository tabelaRepository;

    private String academiaId;
    private String avaliadorId;

    @BeforeAll
    void cleanDatabase() {
        avaliacaoFisicaRepository.deleteAll();
        clienteRepository.deleteAll();
        academiaRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void garantirProtocoloEImc() {
        if (!tabelaRepository.existsById("classificacao_imc_oms")) {
            var tabela = new TabelaClassificacaoDocument("classificacao_imc_oms", "Classificação IMC - OMS");
            var raiz = new PersistedTabelaClassificacaoGenerica();
            raiz.addComponente(new PersistedNivelImc("ABAIXO_DO_PESO", null, 18.5, null, TipoLimite.EXCLUSIVO));
            raiz.addComponente(new PersistedNivelImc("NORMAL", 18.5, 25.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
            raiz.addComponente(new PersistedNivelImc("SOBREPESO", 25.0, 30.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
            raiz.addComponente(new PersistedNivelImc("OBESIDADE_I", 30.0, 35.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
            raiz.addComponente(new PersistedNivelImc("OBESIDADE_II", 35.0, 40.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
            raiz.addComponente(new PersistedNivelImc("OBESIDADE_III", 40.0, null, TipoLimite.INCLUSIVO, null));
            tabela.setRaiz(raiz);
            tabelaRepository.save(tabela);
        }
        if (!protocoloRepository.existsById("protocolo_imc_oms")) {
            var protocolo = new ProtocoloAvaliacaoDocument(
                "protocolo_imc_oms", "IMC - OMS", "IMC", true,
                null, "IMC", "classificacao_imc_oms");
            protocoloRepository.save(protocolo);
        }
    }

    @BeforeEach
    void setUp() {
        var suf = UUID.randomUUID().toString().substring(0, 8);
        var user = new UserDocument();
        user.setUserName("usuario-imc-" + suf);
        user.setEmail("user-imc-" + suf + "@test.com");
        user.setPassword("hash123");
        user.setActive(true);
        user.setAcademiaRoles(Map.of());
        user.setCref("CREF-IMC-" + suf);
        user.setCpf("11122233344" + suf);
        var userId = userRepository.save(user).getId();
        avaliadorId = userId;

        var academia = new AcademiaDocument();
        academia.setOwnerId(userId);
        academia.setCnpj("99.888.777/0001-66");
        academia.setNomeFantasia("Academia IMC Teste");
        academiaId = academiaRepository.save(academia).getId();
    }

    @Test
    void criaClienteComImc() {
        var request = new ClienteComImcRequest();
        request.setFullName("Ana Teste IMC");
        request.setEmail("ana-imc-" + System.nanoTime() + "@test.com");
        request.setPhone("11911112222");
        request.setCpf("52998224725");
        request.setDataNascimento(LocalDate.of(1995, 3, 10));
        request.setRua("Rua do Teste");
        request.setNumero("42");
        request.setCidade("Natal");
        request.setEstado("RN");
        request.setCep("59000-000");
        request.setAcademiaId(academiaId);
        request.setAvaliadorId(avaliadorId);
        request.setPesoGramas(75000);
        request.setAlturaCm(175);

        garantirProtocoloEImc();

        ClienteComImcResponse response = clienteService.criarComImc(request);

        assertThat(response).isNotNull();
        assertThat(response.cliente()).isNotNull();
        assertThat(response.cliente().id()).isNotBlank();
        assertThat(response.cliente().fullName()).isEqualTo("Ana Teste IMC");
        assertThat(response.cliente().dataNascimento()).isEqualTo(LocalDate.of(1995, 3, 10));
        assertThat(response.cliente().endereco()).isNotNull();
        assertThat(response.cliente().endereco().getRua()).isEqualTo("Rua do Teste");

        assertThat(response.avaliacao()).isNotNull();
        assertThat(response.avaliacao().avaliacaoId()).isNotBlank();
        assertThat(response.avaliacao().clienteId()).isEqualTo(response.cliente().id());
        assertThat(response.avaliacao().classificacao()).isNotBlank();
        assertThat(response.avaliacao().extras()).containsKey("imc");
        assertThat(response.avaliacao().extras()).containsEntry("peso_gramas", 75000);
        assertThat(response.avaliacao().extras()).containsEntry("altura_cm", 175);
        assertThat(response.avaliacao().status()).isEqualTo("CONCLUIDA");

        var savedCliente = clienteRepository.findById(response.cliente().id()).orElseThrow();
        assertThat(savedCliente.getFullName()).isEqualTo("Ana Teste IMC");

        var savedAvaliacao = avaliacaoFisicaRepository.findById(response.avaliacao().avaliacaoId()).orElseThrow();
        assertThat(savedAvaliacao.getClienteId()).isEqualTo(response.cliente().id());
        assertThat(savedAvaliacao.getAvaliadorId()).isEqualTo(avaliadorId);
        assertThat(savedAvaliacao.getProtocoloId()).isEqualTo("protocolo_imc_oms");
        assertThat(savedAvaliacao.getMedicoes()).isNotEmpty();
    }

    @Test
    void criaClienteSemImc() {
        var request = new ClienteComImcRequest();
        request.setFullName("Sem IMC Teste");
        request.setEmail("sem-imc-" + System.nanoTime() + "@test.com");
        request.setPhone("11933334444");
        request.setCpf("86295662006");
        request.setDataNascimento(LocalDate.of(2000, 12, 25));
        request.setAcademiaId(academiaId);

        ClienteComImcResponse response = clienteService.criarComImc(request);

        assertThat(response).isNotNull();
        assertThat(response.cliente()).isNotNull();
        assertThat(response.cliente().id()).isNotBlank();
        assertThat(response.avaliacao()).isNull();

        var savedCliente = clienteRepository.findById(response.cliente().id()).orElseThrow();
        assertThat(savedCliente.getFullName()).isEqualTo("Sem IMC Teste");
    }
}
