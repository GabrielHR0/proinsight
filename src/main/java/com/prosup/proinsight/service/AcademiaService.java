package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.request.AcademiaRequest;
import com.prosup.proinsight.api.dto.response.AcademiaResponse;
import com.prosup.proinsight.infrastructure.persistence.mapper.AcademiaMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AcademiaService {

    private final AcademiaRepository repo;
    private final UserRepository userRepo;
    private final AcademiaMapper mapper;

    public AcademiaService(AcademiaRepository repo, UserRepository userRepo, AcademiaMapper mapper) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.mapper = mapper;
    }

    public AcademiaResponse create(AcademiaRequest request) {
        if (!userRepo.existsById(request.getOwnerId())) {
            throw new NoSuchElementException("User not found: " + request.getOwnerId());
        }

        var doc = mapper.toDocument(request);
        var saved = repo.save(doc);
        return mapper.toResponse(saved);
    }

    public List<AcademiaResponse> findByOwnerId(String ownerId) {
        return repo.findByOwnerId(ownerId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<AcademiaResponse> findByOwnerIdIn(List<String> ownerIds) {
        return repo.findByOwnerIdIn(ownerIds).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public AcademiaResponse findById(String id) {
        return repo.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Academia não encontrada"));
    }

    public AcademiaResponse update(String id, AcademiaRequest request) {
        var document = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Academia não encontrada"));

        var newDocument = mapper.toDocument(request);
        newDocument.setId(document.getId());

        return mapper.toResponse(
                repo.save(newDocument)
        );
    }

    public void delete(String id) {
        repo.deleteById(id);
    }
}
