package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.response.DadosPreAvaliacaoResponse;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreAvaliacaoServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProtocoloAvaliacaoRepository protocoloRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private PreAvaliacaoService service;

    @Test
    void shouldReturnImcProtocolIdFromBackend() {
        var cliente = new ClienteDocument();
        cliente.setId("cliente-1");
        cliente.setSexo(Sexo.MASCULINO);
        when(clienteRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));

        var protocoloImc = new ProtocoloAvaliacaoDocument(
            "protocolo_imc_oms", "IMC - OMS", "IMC", true,
            Protocolo.IMC, "IMC", "classificacao_imc_oms"
        );
        when(protocoloRepository.findFirstByCategoria("IMC")).thenReturn(Optional.of(protocoloImc));

        var medicaoDoc = new Document("tipo", "IMC")
            .append("massaCorporalGramas", 75000)
            .append("alturaCm", 180);

        var avaliacaoDoc = new Document("clienteId", "cliente-1")
            .append("protocoloId", "protocolo_imc_oms")
            .append("createdAt", Date.from(java.time.Instant.parse("2026-01-01T10:00:00Z")))
            .append("medicoes", List.of(medicaoDoc));

        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("avaliacoesFisicas")))
            .thenReturn(avaliacaoDoc);

        DadosPreAvaliacaoResponse response = service.buscarDados("cliente-1", "protocolo_vo2max_esteira_incremental");

        assertThat(response.getProtocoloImcId()).isEqualTo("protocolo_imc_oms");
        assertThat(response.getPesoKg()).isEqualTo(75.0);
        assertThat(response.getAlturaCm()).isEqualTo(180);
        assertThat(response.getProtocoloId()).isEqualTo("protocolo_vo2max_esteira_incremental");
    }

    @Test
    void shouldReturnNullProtocoloImcIdWhenNoImcProtocolRegistered() {
        var cliente = new ClienteDocument();
        cliente.setId("cliente-1");
        when(clienteRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(protocoloRepository.findFirstByCategoria("IMC")).thenReturn(Optional.empty());

        DadosPreAvaliacaoResponse resposta = service.buscarDados("cliente-1", "protocolo_vo2max_esteira_incremental");

        assertThat(resposta.getProtocoloImcId()).isNull();
        assertThat(resposta.getPesoKg()).isNull();
    }
}