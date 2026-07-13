package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.request.AvaliadorRequest;
import com.prosup.proinsight.api.dto.response.AvaliadorResponse;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliadorMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliadorRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;

@Service
public class AvaliadorService {

    private final AvaliadorRepository repo;
    private final UserRepository userRepo;
    private final AcademiaRepository academiaRepo;
    private final AvaliadorMapper mapper;

    public AvaliadorService(AvaliadorRepository repo, UserRepository userRepo, AcademiaRepository academiaRepo, AvaliadorMapper mapper) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.academiaRepo = academiaRepo;
        this.mapper = mapper;
    }

    public AvaliadorResponse create(AvaliadorRequest request) {
        if (!userRepo.existsById(request.getUserId())) {
            throw new NoSuchElementException("User not found: " + request.getUserId());
        }
        if (request.getAcademiaId() != null && !academiaRepo.existsById(request.getAcademiaId())) {
            throw new NoSuchElementException("Academia not found: " + request.getAcademiaId());
        }

        var doc = mapper.toDocument(request);
        var saved = repo.save(doc);
        var domain = mapper.toDomain(saved);
        return mapper.toResponse(domain);
    }
}
