package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.response.DadosPreAvaliacaoResponse;
import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PreAvaliacaoService {

    private final ClienteRepository clienteRepository;
    private final ProtocoloAvaliacaoRepository protocoloRepository;
    private final MongoTemplate mongoTemplate;

    public PreAvaliacaoService(ClienteRepository clienteRepository,
                               ProtocoloAvaliacaoRepository protocoloRepository,
                               MongoTemplate mongoTemplate) {
        this.clienteRepository = clienteRepository;
        this.protocoloRepository = protocoloRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @SuppressWarnings("unchecked")
    public DadosPreAvaliacaoResponse buscarDados(String clienteId, String protocoloId) {
        var cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new NoSuchElementException("Cliente não encontrado: " + clienteId));

        String protocoloImcId = protocoloRepository.findFirstByCategoria("IMC")
                .map(ProtocoloAvaliacaoDocument::getId)
                .orElse(null);

        Integer idade = null;
        if (cliente.getDataNascimento() != null) {
            idade = Period.between(cliente.getDataNascimento(), LocalDate.now()).getYears();
        }

        Double pesoKg = null;
        Integer alturaCm = null;
        Instant dataUltimoImc = null;

        if (protocoloImcId != null) {
            var query = new Query(Criteria.where("clienteId").is(clienteId)
                    .and("protocoloId").is(protocoloImcId))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .limit(1);
            query.fields().include("createdAt").include("medicoes");

            var rawDoc = mongoTemplate.findOne(query, Document.class, "avaliacoesFisicas");
            if (rawDoc != null) {
                dataUltimoImc = rawDoc.getDate("createdAt") != null
                    ? rawDoc.getDate("createdAt").toInstant()
                    : null;

                var medicoes = (List<Document>) rawDoc.get("medicoes");
                if (medicoes != null) {
                    for (var m : medicoes) {
                        var tipo = m.getString("tipo");
                        if ("IMC".equals(tipo)) {
                            var massaGramas = m.getInteger("massaCorporalGramas");
                            if (massaGramas != null) {
                                pesoKg = massaGramas / 1000.0;
                            }
                            alturaCm = m.getInteger("alturaCm");
                            break;
                        }
                    }
                }
            }
        }

        return new DadosPreAvaliacaoResponse(
            protocoloId,
            protocoloImcId,
            cliente.getSexo(),
            idade,
            pesoKg,
            alturaCm,
            dataUltimoImc
        );
    }
}
