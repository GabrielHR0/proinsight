package com.prosup.proinsight.service;

import com.prosup.proinsight.config.TenantContext;
import com.prosup.proinsight.infrastructure.persistence.document.AvaliacaoFisicaDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliacaoFisicaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import com.prosup.proinsight.infrastructure.transaction.AfterCommitExecutor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;


@Service
public class AvaliacaoService {

    private final ClienteRepository clienteRepository;
    private final UserRepository userRepository;
    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    private final HistoricoAvaliacoesService historicoAvaliacoesService;
    private final AfterCommitExecutor afterCommitExecutor;

    public AvaliacaoService(ClienteRepository clienteRepository,
                            UserRepository userRepository,
                            AvaliacaoFisicaRepository avaliacaoFisicaRepository,
                            HistoricoAvaliacoesService historicoAvaliacoesService,
                            AfterCommitExecutor afterCommitExecutor)
    {
        this.clienteRepository = clienteRepository;
        this.userRepository = userRepository;
        this.avaliacaoFisicaRepository = avaliacaoFisicaRepository;
        this.historicoAvaliacoesService = historicoAvaliacoesService;
        this.afterCommitExecutor = afterCommitExecutor;
    }

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

        afterCommitExecutor.execute(
                () -> historicoAvaliacoesService.invalidar(avaliacaoDoc.getClienteId()));

        return saved;
    }

}
