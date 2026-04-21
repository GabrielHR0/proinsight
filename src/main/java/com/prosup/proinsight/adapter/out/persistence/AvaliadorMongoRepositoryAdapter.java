package com.prosup.proinsight.adapter.out.persistence;

import com.prosup.proinsight.domain.model.Avaliador;
import org.springframework.stereotype.Component;

/**
 * Legacy adapter kept for compatibility. Services now use Spring Data repositories directly,
 * but this adapter remains available until the codebase is fully migrated.
 */
@Component
public class AvaliadorMongoRepositoryAdapter {

    private final MongoAvaliadorDataRepository repo;

    public AvaliadorMongoRepositoryAdapter(MongoAvaliadorDataRepository repo) {
        this.repo = repo;
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

    private AvaliadorDocument toDocument(Avaliador a) {
        if (a == null) {
            return null;
        }

        return new AvaliadorDocument(
                a.getId(),
                a.getUserId(),
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

