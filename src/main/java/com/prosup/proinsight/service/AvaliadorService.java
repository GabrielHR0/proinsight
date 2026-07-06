package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.AvaliadorDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliadorRepository;
import com.prosup.proinsight.domain.model.Avaliador;
import com.prosup.proinsight.api.dto.request.AvaliadorRequest;
import com.prosup.proinsight.api.dto.response.AvaliadorResponse;
import org.springframework.stereotype.Service;

/**
 * Service that handles Avaliador profile operations.
 * Controller should remain thin and delegate mapping to this service.
 */
@Service
public class AvaliadorService {

    private final AvaliadorRepository repo;

    public AvaliadorService(AvaliadorRepository repo) {
        this.repo = repo;
    }

    public AvaliadorResponse create(AvaliadorRequest request) {
        return save(request);
    }

    public AvaliadorResponse save(AvaliadorRequest request) {
        AvaliadorDocument doc = toDocument(request);
        AvaliadorDocument saved = repo.save(doc);
        Avaliador domain = toDomain(saved);

        return new AvaliadorResponse(
                domain.getId(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getEmail(),
                domain.getTelefone(),
                domain.getCpf(),
                domain.getCref());
    }

    private AvaliadorDocument toDocument(AvaliadorRequest req) {
        AvaliadorDocument d = new AvaliadorDocument();
        d.setUserId(req.getUserId());
        d.setCref(req.getCref());
        d.setFirstName(req.getFirstName());
        d.setLastName(req.getLastName());
        d.setEmail(req.getEmail());
        d.setTelefone(req.getTelefone());
        // cpf não deve vir no request por design; se vier, validar ou ignorar conforme regra de negócio
        return d;
    }

    private Avaliador toDomain(AvaliadorDocument d) {
        if (d == null) {
            return null;
        }

        return new Avaliador(
                d.getId(),
                d.getUserId(),
                d.getCref(),
                d.getFirstName(),
                d.getLastName(),
                d.getEmail(),
                d.getTelefone(),
                d.getCpf());
    }
}

