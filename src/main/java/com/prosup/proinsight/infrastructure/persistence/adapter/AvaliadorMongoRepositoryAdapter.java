package com.prosup.proinsight.infrastructure.persistence.adapter;

import com.prosup.proinsight.infrastructure.persistence.document.AvaliadorDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliadorRepository;
import com.prosup.proinsight.domain.model.Avaliador;
import org.springframework.stereotype.Component;

@Component
public class AvaliadorMongoRepositoryAdapter {

    private final AvaliadorRepository repo;

    public AvaliadorMongoRepositoryAdapter(AvaliadorRepository repo) {
        this.repo = repo;
    }

    private Avaliador toDomain(AvaliadorDocument d) {
        if (d == null) {
            return null;
        }

        return new Avaliador(
                d.getId(),
                d.getUserId(),
                d.getAcademiaId(),
                d.getCref(),
                d.getFirstName(),
                d.getLastName(),
                d.getEmail(),
                d.getTelefone(),
                d.getCpf());
    }

    private AvaliadorDocument toDocument(Avaliador a) {
        if (a == null) {
            return null;
        }

        return new AvaliadorDocument(
                a.getId(),
                a.getUserId(),
                a.getAcademiaId(),
                a.getCref(),
                a.getFirstName(),
                a.getLastName(),
                a.getEmail(),
                a.getTelefone(),
                a.getCpf());
    }

    public Avaliador save(Avaliador avaliador) {
        AvaliadorDocument doc = toDocument(avaliador);
        AvaliadorDocument saved = repo.save(doc);
        return toDomain(saved);
    }

    public java.util.Optional<Avaliador> findByUserId(String userId) {
        return repo.findByUserId(userId).map(this::toDomain);
    }
}
