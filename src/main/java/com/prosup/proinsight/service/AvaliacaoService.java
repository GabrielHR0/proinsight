package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.AvaliacaoFisicaDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliacaoFisicaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliadorRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;


@Service
public class AvaliacaoService {

    private final ClienteRepository clienteRepository;
    private final AvaliadorRepository avaliadorRepository;
    private final ProtocoloAvaliacaoRepository protocoloAvaliacaoRepository;
    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;

    public AvaliacaoService(ClienteRepository clienteRepository,
                            AvaliadorRepository avaliadorRepository,
                            ProtocoloAvaliacaoRepository protocoloAvaliacaoRepository,
                            AvaliacaoFisicaRepository avaliacaoFisicaRepository)
    {
        this.clienteRepository = clienteRepository;
        this.avaliadorRepository = avaliadorRepository;
        this.protocoloAvaliacaoRepository = protocoloAvaliacaoRepository;
        this.avaliacaoFisicaRepository = avaliacaoFisicaRepository;
    }

    public AvaliacaoFisicaDocument save(AvaliacaoFisicaDocument avaliacaoDoc) {
        clienteRepository.findById(avaliacaoDoc.getClienteId())
            .orElseThrow(() -> new NoSuchElementException("Cliente não encontrado"));

        avaliadorRepository.findById(avaliacaoDoc.getAvaliadorId())
            .orElseThrow(() -> new NoSuchElementException("Avaliador não encontrado"));

        protocoloAvaliacaoRepository.findById(avaliacaoDoc.getProtocoloId())
                .orElseThrow(() -> new NoSuchElementException("Protocolo não encontrado"));

        return avaliacaoFisicaRepository.save(avaliacaoDoc);
    }

}
