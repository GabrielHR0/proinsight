package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.request.AvaliacaoImcRequest;
import com.prosup.proinsight.api.dto.request.ClienteComImcRequest;
import com.prosup.proinsight.api.dto.request.ClienteRequest;
import com.prosup.proinsight.api.dto.response.AvaliacaoImcResponse;
import com.prosup.proinsight.api.dto.response.ClienteComImcResponse;
import com.prosup.proinsight.api.dto.response.ClienteResponse;
import com.prosup.proinsight.config.TenantCheck;
import com.prosup.proinsight.config.TenantContext;
import com.prosup.proinsight.domain.model.Endereco;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.ClienteMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import com.prosup.proinsight.service.handler.AvaliacaoImcHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final AcademiaRepository academiaRepository;
    private final UserRepository userRepository;
    private final AvaliacaoImcHandler avaliacaoImcHandler;

    public ClienteService(ClienteRepository clienteRepository,
                          ClienteMapper clienteMapper,
                          AcademiaRepository academiaRepository,
                          UserRepository userRepository,
                          AvaliacaoImcHandler avaliacaoImcHandler)
    {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.academiaRepository = academiaRepository;
        this.userRepository = userRepository;
        this.avaliacaoImcHandler = avaliacaoImcHandler;
    }

    public ClienteResponse create(ClienteRequest request) {
        if (request.getAcademiaId() != null) {
            if (!academiaRepository.existsById(request.getAcademiaId())) {
                throw new NoSuchElementException("Academia não encontrada: " + request.getAcademiaId());
            }
        }
        if (request.getAvaliadorId() != null) {
            var avaliadorDoc = userRepository.findById(request.getAvaliadorId())
                .orElseThrow(() -> new NoSuchElementException(
                    "Avaliador não encontrado: " + request.getAvaliadorId()));
            if (avaliadorDoc.getCref() == null || avaliadorDoc.getCref().isBlank()) {
                throw new NoSuchElementException(
                    "Avaliador não encontrado: " + request.getAvaliadorId()
                        + ". O usuário não possui CREF cadastrado.");
            }
        }

        var doc = clienteMapper.toDocument(request);
        var saved = clienteRepository.save(doc);
        return clienteMapper.toResponse(saved);
    }

    @Transactional
    public ClienteComImcResponse criarComImc(ClienteComImcRequest request) {
        var doc = toClienteDocument(request);
        var saved = clienteRepository.save(doc);
        var clienteResponse = clienteMapper.toResponse(saved);

        AvaliacaoImcResponse imcResponse = null;

        if (request.temImc()) {
            if (request.getAvaliadorId() == null || request.getAvaliadorId().isBlank()) {
                throw new IllegalArgumentException("É necessário estar logado para cadastrar um aluno com avaliação");
            }

            var protocoloId = request.getProtocoloId() != null
                ? request.getProtocoloId()
                : "protocolo_imc_oms";

            var imcRequest = new AvaliacaoImcRequest(
                saved.getId(),
                protocoloId,
                request.getAvaliadorId(),
                request.getPesoGramas(),
                request.getAlturaCm()
            );

            imcResponse = avaliacaoImcHandler.processar(imcRequest);
        }

        return new ClienteComImcResponse(clienteResponse, imcResponse);
    }

    @TenantCheck
    public List<ClienteResponse> listAll() {
        return clienteRepository.findByAcademiaId(TenantContext.getAcademiaId()).stream()
            .map(clienteMapper::toResponse)
            .toList();
    }

    @TenantCheck
    public ClienteResponse findById(String id) {
        var doc = fetchScoped(id);
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

    @TenantCheck
    public ClienteResponse update(String id, ClienteRequest request) {
        var doc = fetchScoped(id);

        if (request.getAcademiaId() != null
                && !request.getAcademiaId().equals(doc.getAcademiaId())) {
            throw new AccessDeniedException(
                    "Não é permitido mover cliente para outra academia");
        }

        doc.setFullName(request.getFullName());
        doc.setEmail(request.getEmail());
        doc.setPhone(request.getPhone());
        doc.setCpf(request.getCpf());
        doc.setDataNascimento(request.getDataNascimento());
        doc.setSexo(request.getSexo());
        doc.setEndereco(request.toEndereco());
        doc.setAcademiaId(request.getAcademiaId() != null
                ? request.getAcademiaId() : doc.getAcademiaId());
        doc.setAvaliadorId(request.getAvaliadorId());
        doc.setActive(request.getActive() != null ? request.getActive() : doc.isActive());

        var saved = clienteRepository.save(doc);
        return clienteMapper.toResponse(saved);
    }

    private ClienteDocument fetchScoped(String id) {
        String tenantId = TenantContext.getAcademiaId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new AccessDeniedException(
                    "Acesso a dados de academia exige X-Academia-Id");
        }
        var doc = clienteRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Cliente não encontrado: " + id));
        if (!tenantId.equals(doc.getAcademiaId())) {
            throw new AccessDeniedException(
                    "Acesso ao cliente " + id + " não permitido");
        }
        return doc;
    }

    private ClienteDocument toClienteDocument(ClienteComImcRequest req) {
        Endereco endereco = req.toEndereco();
        return new ClienteDocument(
            null,
            req.getFullName(),
            req.getEmail(),
            req.getPhone(),
            req.getCpf(),
            req.getDataNascimento(),
            req.getSexo(),
            endereco,
            req.getAcademiaId(),
            req.getAvaliadorId(),
            req.getActive() != null ? req.getActive() : true
        );
    }
}
