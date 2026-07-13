package com.prosup.proinsight.infrastructure.persistence;

import com.prosup.proinsight.AbstractIntegrationTest;
import com.prosup.proinsight.api.dto.request.AvaliacaoImcRequest;
import com.prosup.proinsight.api.dto.request.AvaliacaoVo2MaxRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoImcResponse;
import com.prosup.proinsight.api.dto.response.AvaliacaoVo2MaxResponse;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.document.AvaliadorDocument;
import com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliadorRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliacaoFisicaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import com.prosup.proinsight.service.handler.AvaliacaoImcHandler;
import com.prosup.proinsight.service.handler.AvaliacaoVo2MaxHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AvaliacaoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private AvaliadorRepository avaliadorRepository;
    @Autowired
    private AcademiaRepository academiaRepository;
    @Autowired
    private AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    @Autowired
    private ProtocoloAvaliacaoRepository protocoloRepository;
    @Autowired
    private TabelaClassificacaoRepository tabelaClassificacaoRepository;

    @Autowired
    private AvaliacaoVo2MaxHandler avaliacaoVo2MaxHandler;
    @Autowired
    private AvaliacaoImcHandler avaliacaoImcHandler;

    private String userId;
    private String clienteId;
    private String avaliadorId;
    private String academiaId;

    @BeforeAll
    void setUpAll() {
        avaliacaoFisicaRepository.deleteAll();
        avaliadorRepository.deleteAll();
        clienteRepository.deleteAll();
        academiaRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        var user = new UserDocument();
        user.setEmail("test-" + System.nanoTime() + "@test.com");
        user.setPassword("hash123");
        user.setActive(true);
        user.setRoles(Set.of());
        userId = userRepository.save(user).getId();

        var academia = new AcademiaDocument();
        academia.setOwnerId(userId);
        academia.setCnpj("12.345.678/0001-90");
        academia.setNomeFantasia("Academia Teste");
        academiaId = academiaRepository.save(academia).getId();

        var cliente = new ClienteDocument();
        cliente.setFullName("João Silva");
        cliente.setEmail("joao-" + System.nanoTime() + "@test.com");
        cliente.setCpf("12345678901");
        cliente.setAcademiaId(academiaId);
        clienteId = clienteRepository.save(cliente).getId();

        var avaliador = new AvaliadorDocument();
        avaliador.setUserId(userId);
        avaliador.setAcademiaId(academiaId);
        avaliador.setCref("CREF-" + System.nanoTime());
        avaliador.setFirstName("Maria");
        avaliador.setLastName("Santos");
        avaliador.setEmail("maria-" + System.nanoTime() + "@test.com");
        avaliador.setTelefone("11999999999");
        avaliador.setCpf("98765432100");
        avaliadorId = avaliadorRepository.save(avaliador).getId();
    }

    @Test
    void avaliacaoCooper_fluxoCompleto() {
        var request = new AvaliacaoVo2MaxRequest();
        request.setClienteId(clienteId);
        request.setProtocoloId("protocolo_vo2max_cooper");
        request.setAvaliadorId(avaliadorId);
        request.setResultado(2400);
        request.setIdade(25);
        request.setSexo(Sexo.MASCULINO);
        request.setObservacoes("Teste Cooper 12 min");

        AvaliacaoVo2MaxResponse response = avaliacaoVo2MaxHandler.processar(request);

        assertThat(response).isNotNull();
        assertThat(response.getAvaliacaoId()).isNotNull();
        assertThat(response.getClassificacao()).isNotNull();
        assertThat(response.getClassificacao().getNome()).isIn("MUITO_RUIM", "RUIM", "MÉDIO", "BOM", "EXCELENTE");
        assertThat(response.getClassificacao().getValorVo2Max()).isPositive();

        var saved = avaliacaoFisicaRepository.findById(response.getAvaliacaoId()).orElseThrow();
        assertThat(saved.getClienteId()).isEqualTo(clienteId);
        assertThat(saved.getAvaliadorId()).isEqualTo(avaliadorId);
        assertThat(saved.getProtocoloId()).isEqualTo("protocolo_vo2max_cooper");
        assertThat(saved.getMedicoes()).hasSize(1);
    }

    @Test
    void avaliacaoRockport_fluxoCompleto() {
        var request = new AvaliacaoVo2MaxRequest();
        request.setClienteId(clienteId);
        request.setProtocoloId("protocolo_vo2max_rockport");
        request.setAvaliadorId(avaliadorId);
        request.setResultado(15.5);
        request.setIdade(30);
        request.setSexo(Sexo.FEMININO);
        request.setFrequenciaCardiaca(140);
        request.setPesoKg(65.0);
        request.setObservacoes("Teste Rockport 1 mile");

        AvaliacaoVo2MaxResponse response = avaliacaoVo2MaxHandler.processar(request);

        assertThat(response).isNotNull();
        assertThat(response.getAvaliacaoId()).isNotNull();
        assertThat(response.getClassificacao()).isNotNull();
        assertThat(response.getClassificacao().getNome()).isIn("MUITO_RUIM", "RUIM", "MÉDIO", "BOM", "EXCELENTE");
        assertThat(response.getClassificacao().getValorVo2Max()).isPositive();

        var saved = avaliacaoFisicaRepository.findById(response.getAvaliacaoId()).orElseThrow();
        assertThat(saved.getMedicoes()).hasSize(1);
    }

    @Test
    void avaliacaoEsteiraIncremental_fluxoCompleto() {
        var request = new AvaliacaoVo2MaxRequest();
        request.setClienteId(clienteId);
        request.setProtocoloId("protocolo_vo2max_esteira_incremental");
        request.setAvaliadorId(avaliadorId);
        request.setResultado(14.3);
        request.setIdade(35);
        request.setSexo(Sexo.MASCULINO);
        request.setObservacoes("Teste esteira incremental");

        AvaliacaoVo2MaxResponse response = avaliacaoVo2MaxHandler.processar(request);

        assertThat(response).isNotNull();
        assertThat(response.getAvaliacaoId()).isNotNull();
        assertThat(response.getClassificacao()).isNotNull();
        assertThat(response.getClassificacao().getNome()).isIn("MUITO_RUIM", "RUIM", "MÉDIO", "BOM", "EXCELENTE");
        assertThat(response.getClassificacao().getValorVo2Max()).isPositive();

        var saved = avaliacaoFisicaRepository.findById(response.getAvaliacaoId()).orElseThrow();
        assertThat(saved.getMedicoes()).hasSize(1);
    }

    @Test
    void avaliacaoImc_fluxoCompleto() {
        var request = new AvaliacaoImcRequest(
            clienteId,
            "protocolo_imc_oms",
            avaliadorId,
            70000,
            175
        );

        AvaliacaoImcResponse response = avaliacaoImcHandler.processar(request);

        assertThat(response).isNotNull();
        assertThat(response.avaliacaoId()).isNotNull();
        assertThat(response.classificacao()).isIn("ABAIXO_DO_PESO", "NORMAL", "SOBREPESO", "OBESIDADE_I", "OBESIDADE_II", "OBESIDADE_III");
        assertThat(response.extras()).containsKey("imc").containsKey("peso_gramas").containsKey("altura_cm");

        var saved = avaliacaoFisicaRepository.findById(response.avaliacaoId()).orElseThrow();
        assertThat(saved.getClienteId()).isEqualTo(clienteId);
        assertThat(saved.getAvaliadorId()).isEqualTo(avaliadorId);
        assertThat(saved.getProtocoloId()).isEqualTo("protocolo_imc_oms");
        assertThat(saved.getMedicoes()).hasSize(1);
    }

    @Test
    void avaliacaoImc_obesidade() {
        var request = new AvaliacaoImcRequest(
            clienteId,
            "protocolo_imc_oms",
            avaliadorId,
            110000,
            170
        );

        AvaliacaoImcResponse response = avaliacaoImcHandler.processar(request);

        assertThat(response).isNotNull();
        assertThat(response.classificacao()).isEqualTo("OBESIDADE_II");
    }

    @Test
    void avaliacaoImc_abaixoDoPeso() {
        var request = new AvaliacaoImcRequest(
            clienteId,
            "protocolo_imc_oms",
            avaliadorId,
            45000,
            170
        );

        AvaliacaoImcResponse response = avaliacaoImcHandler.processar(request);

        assertThat(response).isNotNull();
        assertThat(response.classificacao()).isEqualTo("ABAIXO_DO_PESO");
    }

    @Test
    void avaliacaoCooper_excelencia() {
        var request = new AvaliacaoVo2MaxRequest();
        request.setClienteId(clienteId);
        request.setProtocoloId("protocolo_vo2max_cooper");
        request.setAvaliadorId(avaliadorId);
        request.setResultado(3000);
        request.setIdade(25);
        request.setSexo(Sexo.MASCULINO);

        AvaliacaoVo2MaxResponse response = avaliacaoVo2MaxHandler.processar(request);

        assertThat(response).isNotNull();
        assertThat(response.getAvaliacaoId()).isNotNull();
        assertThat(response.getClassificacao()).isNotNull();
        assertThat(response.getClassificacao().getValorVo2Max()).isPositive();
    }

    @Test
    void avaliacaoRockport_muitoRuim() {
        var request = new AvaliacaoVo2MaxRequest();
        request.setClienteId(clienteId);
        request.setProtocoloId("protocolo_vo2max_rockport");
        request.setAvaliadorId(avaliadorId);
        request.setResultado(20.0);
        request.setIdade(25);
        request.setSexo(Sexo.MASCULINO);
        request.setFrequenciaCardiaca(160);
        request.setPesoKg(90.0);

        AvaliacaoVo2MaxResponse response = avaliacaoVo2MaxHandler.processar(request);

        assertThat(response).isNotNull();
        assertThat(response.getClassificacao().getNome()).isEqualTo("MUITO_RUIM");
    }
}
