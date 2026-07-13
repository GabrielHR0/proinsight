package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.request.ClienteRequest;
import com.prosup.proinsight.api.dto.response.ClienteResponse;
import com.prosup.proinsight.infrastructure.persistence.mapper.ClienteMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliadorRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final AcademiaRepository academiaRepository;
    private final AvaliadorRepository avaliadorRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          ClienteMapper clienteMapper,
                          AcademiaRepository academiaRepository,
                          AvaliadorRepository avaliadorRepository)
    {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.academiaRepository = academiaRepository;
        this.avaliadorRepository = avaliadorRepository;
    }

    public ClienteResponse create(ClienteRequest request) {
        if (request.getAcademiaId() != null) {
            if (!academiaRepository.existsById(request.getAcademiaId())) {
                throw new NoSuchElementException("Academia não encontrada: " + request.getAcademiaId());
            }
        }
        if (request.getAvaliadorId() != null) {
            if (!avaliadorRepository.existsById(request.getAvaliadorId())) {
                throw new NoSuchElementException("Avaliador não encontrado: " + request.getAvaliadorId());
            }
        }

        var doc = clienteMapper.toDocument(request);
        var saved = clienteRepository.save(doc);
        return clienteMapper.toResponse(saved);
    }

    public List<ClienteResponse> listAll() {
        return clienteRepository.findAll().stream()
            .map(clienteMapper::toResponse)
            .toList();
    }

    public ClienteResponse findById(String id) {
        var doc = clienteRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Cliente não encontrado: " + id));
        return clienteMapper.toResponse(doc);
    }

    public List<ClienteResponse> findByAcademiaId(String academiaId) {
        return clienteRepository.findByAcademiaId(academiaId).stream()
            .map(clienteMapper::toResponse)
            .toList();
    }

    public List<ClienteResponse> findByAvaliadorId(String avaliadorId) {
        return clienteRepository.findByAvaliadorId(avaliadorId).stream()
            .map(clienteMapper::toResponse)
            .toList();
    }
}
