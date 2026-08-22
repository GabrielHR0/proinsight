package com.prosup.proinsight.service;

import com.prosup.proinsight.config.TenantContext;
import com.prosup.proinsight.infrastructure.persistence.document.AvaliacaoFisicaDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliacaoFisicaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;


@Service
public class AvaliacaoService {

    private final ClienteRepository clienteRepository;
    private final UserRepository userRepository;
    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    private final HistoricoAvaliacoesService historicoAvaliacoesService;

    public AvaliacaoService(ClienteRepository clienteRepository,
                            UserRepository userRepository,
                            AvaliacaoFisicaRepository avaliacaoFisicaRepository,
                            HistoricoAvaliacoesService historicoAvaliacoesService)
    {
        this.clienteRepository = clienteRepository;
        this.userRepository = userRepository;
        this.avaliacaoFisicaRepository = avaliacaoFisicaRepository;
        this.historicoAvaliacoesService = historicoAvaliacoesService;
    }

    @Transactional
    public AvaliacaoFisicaDocument save(AvaliacaoFisicaDocument avaliacaoDoc) {
        var cliente = clienteRepository.findById(avaliacaoDoc.getClienteId())
            .orElseThrow(() -> new NoSuchElementException(
                "Cliente não encontrado: " + avaliacaoDoc.getClienteId()));

        String tenantId = TenantContext.getAcademiaId();
        if (tenantId != null && !tenantId.isBlank()
                && cliente.getAcademiaId() != null
                && !tenantId.equals(cliente.getAcademiaId())) {
            throw new AccessDeniedException("Cliente não pertence à academia do contexto");
        }

        userRepository.findById(avaliacaoDoc.getAvaliadorId())
            .filter(doc -> doc.getCref() != null && !doc.getCref().isBlank())
            .orElseThrow(() -> new NoSuchElementException(
                "Avaliador não encontrado: " + avaliacaoDoc.getAvaliadorId()
                    + ". Verifique se o usuário possui CREF cadastrado."));

        avaliacaoDoc.setAcademiaId(cliente.getAcademiaId());

        var saved = avaliacaoFisicaRepository.save(avaliacaoDoc);

        historicoAvaliacoesService.invalidar(avaliacaoDoc.getClienteId());

        return saved;
    }

}
